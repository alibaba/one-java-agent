package java.oneagent;

/**
 * 
 * @author hengyunabc 2020-12-11
 *
 */
public class NopAgent implements Agent {
    public static final Agent INSTANCE = new NopAgent();

    @Override
    public Class<?> loadClass(String name) {
        return null;
    }
}
