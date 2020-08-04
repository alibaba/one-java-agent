package com.alibaba.oneagent.service;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * 
 * @author hengyunabc 2020-07-30
 *
 */
public class OneAgentClassFileTransformer implements ClassFileTransformer {

	private TransformerManager transformerManager;
	private boolean canRetransform;
	
	public OneAgentClassFileTransformer(TransformerManager transformerManager, boolean canRetransform) {
		this.transformerManager = transformerManager;
		this.canRetransform = canRetransform;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		List<ClassFileTransformer> transformers = transformerManager.classFileTransformer(canRetransform);

		for (ClassFileTransformer transformer : transformers) {
			byte[] transformResult = null;
			try {
				transformResult = transformer.transform(loader, className, classBeingRedefined, protectionDomain,
						classfileBuffer);
				
			} catch (Throwable e) {
				// TODO: log exception?
			}
			if (transformResult != null) {
				classfileBuffer = transformResult;
			}
		}

		return classfileBuffer;
	}
}
