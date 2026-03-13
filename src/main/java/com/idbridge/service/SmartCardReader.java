package com.idbridge.service;

import com.idbridge.util.ThaiIdApdu;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import org.springframework.stereotype.Service;

import javax.smartcardio.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SmartCardReader ที่ใช้ JNA Reset WinSCard Context ก่อนทุกครั้งที่อ่าน
 * แก้ปัญหา: ถอดเครื่องอ่านแล้วเสียบใหม่ -> list() failed ใน javax.smartcardio
 */
@Service
public class SmartCardReader {

    private static final long RECOVERY_MIN_INTERVAL_MS = 2000L;
    private final AtomicLong lastRecoveryAtMs = new AtomicLong(0L);

    // JNA Interface สำหรับ WinSCard.dll ของ Windows
    private interface WinSCardLib extends Library {
        int SCardEstablishContext(int dwScope, Pointer pvReserved1, Pointer pvReserved2, LongByReference phContext);
        int SCardReleaseContext(long hContext);
    }

    /**
     * Reset WinSCard Context ผ่าน JNA เรียก WinSCard.dll โดยตรง
     * บังคับให้ Windows รีเซ็ตสถานะ PC/SC ภายใน ก่อนที่ javax.smartcardio จะเข้าไปอ่าน
     */
    private void resetSCardContext() {
        try {
            WinSCardLib winSCard = Native.load("WinSCard", WinSCardLib.class);
            LongByReference hContext = new LongByReference(0L);
            // ขอ Context ใหม่ (SCARD_SCOPE_USER = 0)
            int ret = winSCard.SCardEstablishContext(2, null, null, hContext);
            if (ret == 0 && hContext.getValue() != 0) {
                // คืน Context กลับทันที -> บังคับให้ Windows Flush สถานะ PC/SC
                winSCard.SCardReleaseContext(hContext.getValue());
            }
        } catch (Exception e) {
            // ถ้า JNA ใช้ไม่ได้ (non-Windows) ก็ข้ามไปเงียบๆ
            System.out.println("WinSCard reset skipped: " + e.getMessage());
        }
    }

    private TerminalFactory createTerminalFactory() {
        try {
            // Avoid relying solely on the cached default factory.
            return TerminalFactory.getInstance("PC/SC", null);
        } catch (Exception ignored) {
            return TerminalFactory.getDefault();
        }
    }

    private boolean isRecoveryCandidate(Exception e) {
        if (e instanceof CardException) return true;

        String msg = e.getMessage();
        if (msg == null) return false;
        String m = msg.toLowerCase();

        // Don't do heavy recovery when the user simply hasn't inserted a card.
        if (m.contains("not inserted") || m.contains("not inserted in the reader") || m.contains("card is not inserted")) {
            return false;
        }

        return m.contains("list() failed")
                || m.contains("no smart card reader")
                || m.contains("scard")
                || m.contains("pc/sc")
                || m.contains("pcsc");
    }

    private void recoverPcsc(boolean force) {
        long now = System.currentTimeMillis();
        long last = lastRecoveryAtMs.get();
        if (!force && (now - last) < RECOVERY_MIN_INTERVAL_MS) return;
        if (!force && !lastRecoveryAtMs.compareAndSet(last, now)) return;
        if (force) lastRecoveryAtMs.set(now);

        try {
            resetSCardContext();
            resetJdkSmartcardioCache();
            try {
                // Trigger native layer to re-enumerate readers if it was stuck.
                createTerminalFactory().terminals().list();
            } catch (Exception ignored) {
            }

            // Some PC/SC implementations only fully release cached contexts when GC runs.
            System.gc();
            Thread.sleep(1000);
            resetSCardContext();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void resetJdkSmartcardioCache() {
        // On some Windows setups, javax.smartcardio caches a broken PC/SC context after USB unplug/replug.
        // Best-effort: clear internal JDK caches via reflection. This requires JVM args:
        //   --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED
        // If not permitted, we simply skip.
        try {
            clearStaticLong("sun.security.smartcardio.PCSCTerminals", "contextId");
            clearStaticLong("sun.security.smartcardio.PCSC", "contextId");
            clearStaticCollection("sun.security.smartcardio.PCSCTerminals", "terminals");
        } catch (Throwable ignored) {
            // Best-effort only.
        }
    }

    private void clearStaticLong(String className, String fieldName) throws Exception {
        Class<?> clazz = Class.forName(className);
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        if (field.getType() == long.class) {
            field.setLong(null, 0L);
        }
    }

    private void clearStaticCollection(String className, String fieldName) throws Exception {
        Class<?> clazz = Class.forName(className);
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(null);
        if (value instanceof java.util.Map<?, ?> m) {
            m.clear();
        } else if (value instanceof java.util.List<?> l) {
            l.clear();
        }
    }

    private CardTerminal findTerminal() throws Exception {
        // Reset WinSCard Context ก่อนทุกครั้ง เพื่อล้าง Cache ของ javax.smartcardio
        resetSCardContext();

        TerminalFactory factory = createTerminalFactory();
        List<CardTerminal> terminals;
        try {
            factory = createTerminalFactory();
            terminals = factory.terminals().list();
        } catch (CardException e) {
            // ถ้ายัง list() failed ให้ลองอีกครั้ง
            resetSCardContext();
            try {
                factory = createTerminalFactory();
                terminals = factory.terminals().list();
            } catch (CardException e2) {
                recoverPcsc(true);
                factory = createTerminalFactory();
                terminals = factory.terminals().list();
            }
        }

        if (terminals == null || terminals.isEmpty()) {
            recoverPcsc(false);
            throw new Exception("No smart card reader found. Please check USB connection.");
        }

        for (CardTerminal t : terminals) {
            try {
                if (t.isCardPresent()) return t;
            } catch (CardException ignored) {}
        }
        return terminals.get(0);
    }

    // เรียกจาก System Tray เพื่อตรวจสอบและ Reset เครื่องอ่าน
    public void fixPcscContext() {
        try {
            resetSCardContext();
            TerminalFactory factory = createTerminalFactory();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Hardware check: found " + (terminals == null ? 0 : terminals.size()) + " reader(s).");
        } catch (Exception e) {
            System.err.println("Hardware check failed: " + e.getMessage());
        }
    }

    public void refreshHardwareConnection() {
        recoverPcsc(true);
    }

    public Card connect() throws Exception {
        Exception first = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return connectOnce();
            } catch (Exception e) {
                if (first == null) first = e;
                if (attempt == 0 && isRecoveryCandidate(e)) {
                    recoverPcsc(false);
                    continue;
                }
                throw first;
            }
        }
        throw first == null ? new Exception("Failed to connect to smart card reader.") : first;
    }

    private Card connectOnce() throws Exception {
        CardTerminal terminal = findTerminal();
        if (!terminal.isCardPresent()) {
            throw new Exception("Smart card is not inserted in the reader.");
        }

        Card card = terminal.connect("*");
        CardChannel channel = card.getBasicChannel();

        CommandAPDU selectCmd = new CommandAPDU(ThaiIdApdu.SELECT_APPLET);
        ResponseAPDU response = channel.transmit(selectCmd);

        if (response.getSW() != 0x9000) {
            throw new Exception("Failed to select Thai ID Applet. SW: " + Integer.toHexString(response.getSW()));
        }

        return card;
    }

    public byte[] sendCommand(Card card, byte[] command) throws Exception {
        CardChannel channel = card.getBasicChannel();
        CommandAPDU cmdApdu = new CommandAPDU(command);
        ResponseAPDU response = channel.transmit(cmdApdu);

        if (response.getSW() == 0x9000) {
            return response.getData();
        } else {
            throw new Exception("Card command failed. SW: " + Integer.toHexString(response.getSW()));
        }
    }
}
