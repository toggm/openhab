package org.openhab.core.scala.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ClassloaderUtil {
	/**
	 * load class implementations from an array of classes based and attach
	 * provided libs to classpath. The implementation extends provided base
	 * class. Resolving will be made using own URL classloader
	 * 
	 * @param clazz
	 * @param classes
	 * @param libs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <C> Class<? extends C>[] loadImplementationsFromFiles(
			Class<C> clazz, String basePath, String[] relativePathOfClass,
			URL... libs) {

		URL[] urlClasses = new URL[relativePathOfClass.length];
		for (int i = 0; i < urlClasses.length; ++i) {
			try {
				urlClasses[i] = new File(basePath, relativePathOfClass[i])
						.toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		// create classloader for ruleset factory
		URLClassLoader urlClassLoader = new URLClassLoader(urlClasses,
				new URLClassLoader(libs));

		List<Class<? extends C>> foundClasses = new ArrayList<Class<? extends C>>();
		for (String filePath : relativePathOfClass) {
			Class<?> loadedClass;
			try {
				filePath = filePath.replace(".class", "");
				filePath = filePath.replace("/", ".");
				loadedClass = urlClassLoader.loadClass(filePath);
				if (clazz.isAssignableFrom(loadedClass)) {
					foundClasses.add((Class<? extends C>) loadedClass);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return foundClasses.toArray(new Class[foundClasses.size()]);
	}
}
