package java.oneagent;

/**
 * 
 * @author hengyunabc 2020-12-11
 *
 */
public class AgentSpyBridge {

    private static volatile AgentSpy spy = NopAgentSpy.INSTANCE;

    public static AgentSpy spy() {
        return spy;
    }

    // TODO private ?
    public static void spy(AgentSpy agentSpy) {
        AgentSpyBridge.spy = agentSpy;
    }
}
