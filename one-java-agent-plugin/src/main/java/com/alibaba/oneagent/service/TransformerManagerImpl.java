package com.alibaba.oneagent.service;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author hengyunabc 2020-07-30
 *
 */
public class TransformerManagerImpl implements TransformerManager, Component {
	private Instrumentation instrumentation;
	private ClassFileTransformer classFileTransformer;
	private ClassFileTransformer reClassFileTransformer;

	private volatile List<ClassFileTransformerWrapper> transformers = new ArrayList<ClassFileTransformerWrapper>();
	private volatile List<ClassFileTransformerWrapper> reTransformers = new ArrayList<ClassFileTransformerWrapper>();

	private static Comparator<ClassFileTransformerWrapper> comparator = new Comparator<ClassFileTransformerWrapper>() {

		@Override
		public int compare(ClassFileTransformerWrapper o1, ClassFileTransformerWrapper o2) {
			return o1.order - o2.order;
		}

	};

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer) {
		addTransformer(transformer, TransformerManager.DEFAULT_ORDER);
	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer, int order) {
		transformers.add(new ClassFileTransformerWrapper(transformer, order));
		Collections.sort(transformers, comparator);

	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
		addTransformer(transformer, canRetransform, TransformerManager.DEFAULT_ORDER);
	}

	@Override
	synchronized public void addTransformer(ClassFileTransformer transformer, boolean canRetransform, int order) {
		if (canRetransform) {
			reTransformers.add(new ClassFileTransformerWrapper(transformer, order));
			Collections.sort(reTransformers, comparator);
		} else {
			this.addTransformer(transformer, order);
		}
	}

	@Override
	synchronized public void removeTransformer(ClassFileTransformer transformer) {
		for (ClassFileTransformerWrapper wrapper : this.transformers) {
			if (wrapper.classFileTransformer.equals(transformer)) {
				this.transformers.remove(wrapper);
				break;
			}
		}

		for (ClassFileTransformerWrapper wrapper : this.reTransformers) {
			if (wrapper.classFileTransformer.equals(transformer)) {
				this.reTransformers.remove(wrapper);
				break;
			}
		}
	}

	@Override
	synchronized public List<ClassFileTransformer> classFileTransformer(boolean canRetransform) {
		ArrayList<ClassFileTransformer> result = new ArrayList<ClassFileTransformer>();

		if (canRetransform) {
			for (ClassFileTransformerWrapper wrapper : this.reTransformers) {
				result.add(wrapper.classFileTransformer);
			}
		} else {
			for (ClassFileTransformerWrapper wraper : this.transformers) {
				result.add(wraper.classFileTransformer);
			}
		}

		return result;
	}

	static class ClassFileTransformerWrapper {
		ClassFileTransformer classFileTransformer;
		int order = TransformerManager.DEFAULT_ORDER;

		public ClassFileTransformerWrapper(ClassFileTransformer classFileTransformer) {
			this(classFileTransformer, TransformerManager.DEFAULT_ORDER);
		}

		public ClassFileTransformerWrapper(ClassFileTransformer classFileTransformer, int order) {
			this.classFileTransformer = classFileTransformer;
			this.order = order;
		}
	}

    @Override
    public void init(Properties properties) {
        classFileTransformer = new OneAgentClassFileTransformer(this, false);
        reClassFileTransformer = new OneAgentClassFileTransformer(this, true);
        instrumentation.addTransformer(classFileTransformer);
        instrumentation.addTransformer(reClassFileTransformer, true);
    }

    @Override
    public void stop() {
        instrumentation.removeTransformer(classFileTransformer);
        instrumentation.removeTransformer(reClassFileTransformer);
    }

}
