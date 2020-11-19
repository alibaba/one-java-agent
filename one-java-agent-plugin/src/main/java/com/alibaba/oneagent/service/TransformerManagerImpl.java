package com.alibaba.oneagent.service;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author hengyunabc 2020-07-30
 *
 */
public class TransformerManagerImpl implements TransformerManager {
	private Instrumentation instrumentation;
	private ClassFileTransformer classFileTransformer;
	private ClassFileTransformer reClassFileTransformer;

	private volatile List<ClassFileTransformerWraper> transformers = new ArrayList<ClassFileTransformerWraper>();
	private volatile List<ClassFileTransformerWraper> reTransformers = new ArrayList<ClassFileTransformerWraper>();

	private static Comparator<ClassFileTransformerWraper> comparator = new Comparator<ClassFileTransformerWraper>() {

		@Override
		public int compare(ClassFileTransformerWraper o1, ClassFileTransformerWraper o2) {
			return o1.order - o2.order;
		}

	};

	public TransformerManagerImpl(Instrumentation instrumentation) {
		this.instrumentation = instrumentation;
		classFileTransformer = new OneAgentClassFileTransformer(this, false);
		reClassFileTransformer = new OneAgentClassFileTransformer(this, true);
		instrumentation.addTransformer(classFileTransformer);
		instrumentation.addTransformer(reClassFileTransformer);
	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer) {
		addTransformer(transformer, TransformerManager.DEFAULT_ORDER);
	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer, int order) {
		transformers.add(new ClassFileTransformerWraper(transformer, order));
		Collections.sort(transformers, comparator);

	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
		addTransformer(transformer, canRetransform, TransformerManager.DEFAULT_ORDER);
	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer, boolean canRetransform, int order) {
		if (canRetransform) {
			reTransformers.add(new ClassFileTransformerWraper(transformer, order));
			Collections.sort(reTransformers, comparator);
		} else {
			this.addTransformer(transformer, order);
		}
	}

	@Override
	synchronized public void removeTransformer(ClassFileTransformer transformer) {
		for (ClassFileTransformerWraper wraper : this.transformers) {
			if (wraper.classFileTransformer.equals(transformer)) {
				this.transformers.remove(wraper);
				break;
			}
		}

		for (ClassFileTransformerWraper wraper : this.reTransformers) {
			if (wraper.classFileTransformer.equals(transformer)) {
				this.reTransformers.remove(wraper);
				break;
			}
		}
	}

	@Override
	synchronized public List<ClassFileTransformer> classFileTransformer(boolean canRetransform) {
		ArrayList<ClassFileTransformer> result = new ArrayList<ClassFileTransformer>();

		if (canRetransform) {
			for (ClassFileTransformerWraper wraper : this.reTransformers) {
				result.add(wraper.classFileTransformer);
			}
		} else {
			for (ClassFileTransformerWraper wraper : this.transformers) {
				result.add(wraper.classFileTransformer);
			}
		}

		return result;
	}

	public void destory() {
		instrumentation.removeTransformer(classFileTransformer);
		instrumentation.removeTransformer(reClassFileTransformer);
	}

	static class ClassFileTransformerWraper {
		ClassFileTransformer classFileTransformer;
		int order = TransformerManager.DEFAULT_ORDER;

		public ClassFileTransformerWraper(ClassFileTransformer classFileTransformer) {
			this(classFileTransformer, TransformerManager.DEFAULT_ORDER);
		}

		public ClassFileTransformerWraper(ClassFileTransformer classFileTransformer, int order) {
			this.classFileTransformer = classFileTransformer;
			this.order = order;
		}
	}

}
