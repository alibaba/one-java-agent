package java.oneagent;

/**
 * 
 * TODO 是否用抽象类提高性能？
 * @author hengyunabc 2020-12-11
 *
 */
public interface Agent {

    /**
     * 用OneAgent里注册的Handler来加载类
     * 
     * @param name
     * @return
     */
    public Class<?> loadClass(String name);
}
