package org.openhab.core.scala.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openhab.core.scala.internal.model.Model;

@RunWith(JUnit4.class)
public class ClassloaderUtilTest {

	@Test
	public void shouldLoadClassesFromURL() throws InstantiationException,
			IllegalAccessException {
		String baseUrl = getClass().getResource("/org").getFile();
		baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
		baseUrl = baseUrl.substring(5);
		String classUrl = "org/openhab/core/scala/internal/model/SimpleClassWithoutDependencies.class";
		String classUrl2 = "org/openhab/core/scala/internal/model/ClassWithDependencies.class";

		Class<? extends Model>[] foundClasses = new ClassloaderUtil()
				.loadImplementationsFromFiles(Model.class, baseUrl,
						new String[] { classUrl, classUrl2 });

		assertNotNull(foundClasses);
		assertEquals(2, foundClasses.length);

		// instanciate the found classes
		for (Class<? extends Model> clazz : foundClasses) {
			Model model = clazz.newInstance();

			// and execute method call
			model.test();
		}
	}

	@Test
	public void shouldFileFindingClass() {
		String baseUrl = getClass().getResource("/org").getFile();
		baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
		baseUrl = baseUrl.substring(5);
		String classUrl = "org/openhab/core/scala/internal/model/ClassNotExists.class";

		Class<? extends Model>[] foundClasses = new ClassloaderUtil()
				.loadImplementationsFromFiles(Model.class, baseUrl,
						new String[] { classUrl });

		assertNotNull(foundClasses);
		assertEquals(0, foundClasses.length);
	}
}
