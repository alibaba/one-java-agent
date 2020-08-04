package com.alibaba.oneagent.utils;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 
 * @author hengyunabc 2020-07-31
 *
 */
public class JarUtils {

	public static Attributes read(File jarFile) throws IOException {

		JarFile jar = null;
		Attributes attributes = null;
		try {
			jar = new JarFile(jarFile);
			Manifest manifest = jar.getManifest();
			attributes = manifest.getMainAttributes();
		} finally {
			IOUtils.close(jar);
		}

		return attributes;
	}

}
