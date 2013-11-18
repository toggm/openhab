package org.openhab.core.scala.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ScalaCompilerTest {

	@Test
	public void shouldRecompileScalaFiles() throws IOException {

		File scalaSourceFile = new File(getClass().getResource(
				"/ScalaModel.scala").getFile());
		File library = new File(getClass().getResource(
				"/lib/hammurabi_2.11.0-M3-0.1.jar").getFile());

		File outputDir = FileUtil.createTempDir("outputDir", "test");

		boolean success = new ScalaCompiler().compileScalaFiles(
				new File[] { scalaSourceFile }, new File[] { library },
				outputDir);

		File[] listFiles = outputDir.listFiles();

		String[] expectedFilenames = new String[] {
				"ScalaModelWithDependencies$class.class",
				"ScalaModelWithDependencies.class",
				"SimpleScalaModel$class.class", "SimpleScalaModel.class" };

		assertTrue(success);
		assertEquals(4, listFiles.length);

		for (File file : listFiles) {
			assertTrue(Arrays.binarySearch(expectedFilenames, file.getName()) >= 0);
		}

		outputDir.delete();
	}
}
