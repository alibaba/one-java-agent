package java.oneagent;

/**
 * 
 * @author hengyunabc 2020-12-11
 *
 */
public class NopAgentSpy implements AgentSpy {
    public static final AgentSpy INSTANCE = new NopAgentSpy();

    @Override
    public Class<?> loadClass(String name) {
        return null;
    }
}
