package io.oneagent.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author hengyunabc 2020-07-31
 *
 */
public class PropertiesUtils {

	public static Properties loadNotNull(File path) {

		Properties properties = loadOrNull(path);

		if (properties == null) {
			properties = new Properties();
		}

		return properties;
	}

	public static Properties loadOrNull(File path) {

		InputStream inputStream = null;
		try {
			Properties properties = new Properties();
			inputStream = path.toURI().toURL().openStream();
			properties.load(inputStream);
			return properties;
		} catch (Throwable e) {
			// ignore
		} finally {
			IOUtils.close(inputStream);
		}

		return null;
	}

}
