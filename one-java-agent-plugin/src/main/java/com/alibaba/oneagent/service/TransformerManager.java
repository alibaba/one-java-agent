package com.alibaba.oneagent.service;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

/**
 * 
 * @author hengyunabc 2020-07-27
 *
 */
public interface TransformerManager {

	public static int DEFAULT_ORDER = 1000;

	public void addTransformer(ClassFileTransformer transformer);

	public void addTransformer(ClassFileTransformer transformer, boolean canRetransform);

	public void addTransformer(ClassFileTransformer transformer, int order);

	public void addTransformer(ClassFileTransformer transformer, boolean canRetransform, int order);

	public void removeTransformer(ClassFileTransformer transformer);

	public List<ClassFileTransformer> classFileTransformer(boolean canRetransform);
	
	public void destory();
}
