package java.oneagent;

/**
 * 
 * @author hengyunabc 2020-12-11
 *
 */
public class AgentBridge {

    private static volatile Agent agent = NopAgent.INSTANCE;

    public static Agent agent() {
        return agent;
    }
}
