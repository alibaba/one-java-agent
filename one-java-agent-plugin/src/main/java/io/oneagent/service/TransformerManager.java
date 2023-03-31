package io.oneagent.service;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

/**
 * @author hengyunabc 2020-07-27
 */
public interface TransformerManager {

    int DEFAULT_ORDER = 1000;

    /**
     * add class file  transformer canRetransform is false and order is default
     *
     * @param transformer
     */
    void addTransformer(ClassFileTransformer transformer);

    /**
     * add class file  transformer,order is default
     *
     * @param transformer
     * @param canRetransform
     */
    void addTransformer(ClassFileTransformer transformer, boolean canRetransform);

    /**
     * add class file  transformer,canRetransform is false
     *
     * @param transformer
     * @param order
     */
    void addTransformer(ClassFileTransformer transformer, int order);

    /**
     * add class file  transformer
     *
     * @param transformer
     * @param canRetransform
     * @param order
     */
    void addTransformer(ClassFileTransformer transformer, boolean canRetransform, int order);

    /**
     * remove  class file  transformer
     *
     * @param transformer
     */
    void removeTransformer(ClassFileTransformer transformer);

    /**
     * get all ClassFileTransformers
     *
     * @param canRetransform
     * @return
     */
    List<ClassFileTransformer> classFileTransformer(boolean canRetransform);
}
