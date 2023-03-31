package io.oneagent.service;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author hengyunabc 2020-12-10
 *
 */
public class ComponentManagerImpl implements ComponentManager {
    private static final Logger logger = LoggerFactory.getLogger(ComponentManagerImpl.class);
    private List<Component> components = Collections.emptyList();

    private Instrumentation instrumentation;

    public ComponentManagerImpl(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void initComponents(Properties properties) {
        logger.info("Init available components");
        this.components = scanForAvailableComponents();
        try {
            inject();
        } catch (Exception e) {
            throw new IllegalStateException("init oneagent component error", e);
        }
        for (Component component : components) {
            logger.info("Init component {}", component.getName());
            component.init(properties);
        }
    }

    @Override
    public void startComponents() {
        logger.info("Starting available components");
        for (Component component : components) {
            logger.info("Start component {}", component.getName());
            component.start();
        }
    }

    @Override
    public void stopComponents() {
        logger.info("Stopping available components");
        for (Component component : components) {
            logger.info("Stop component {}", component.getName());
            component.stop();
        }
    }

    private List<Component> scanForAvailableComponents() {
        List<Component> result = new ArrayList<Component>();
        ServiceLoader<Component> components = ServiceLoader.load(Component.class, this.getClass().getClassLoader());
        for (Component component : components) {
            result.add(component);
        }

        Collections.sort(result, new Comparator<Component>() {
            @Override
            public int compare(Component c1, Component c2) {
                return c1.order() - c2.order();
            }
        });
        return result;
    }

    /**
     * simple inject
     */
    private void inject() throws IllegalArgumentException, IllegalAccessException {
        for (Component component : components) {
            Field[] fields = component.getClass().getDeclaredFields();
            for (Field field : fields) {
                Class<?> fieldClass = field.getType();
                if (fieldClass.isAssignableFrom(Component.class)) {
                    Component toInject = (Component) this.getComponent(fieldClass);
                    if (toInject != null) {
                        field.setAccessible(true);
                        field.set(component, toInject);
                    }
                } else if (fieldClass.isAssignableFrom(Instrumentation.class)) {
                    field.setAccessible(true);
                    field.set(component, this.instrumentation);
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getComponent(Class<T> clazz) {
        for (Component component : components) {
            if(clazz.isAssignableFrom(component.getClass())) {
                return (T) component;
            }
        }
        return null;
    }
}
