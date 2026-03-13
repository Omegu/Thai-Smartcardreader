import javax.smartcardio.TerminalFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestReflection {
    public static void main(String[] args) {
        try {
            Object terminals = TerminalFactory.getDefault().terminals();
            System.out.println("Terminals class: " + terminals.getClass().getName());
            
            Class<?> pcscterminals = Class.forName("sun.security.smartcardio.PCSCTerminals");
            Field contextId = pcscterminals.getDeclaredField("contextId");
            contextId.setAccessible(true);
            
            long ctx = contextId.getLong(pcscterminals); // Does this work?
            System.out.println("Context ID using class: " + ctx);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
