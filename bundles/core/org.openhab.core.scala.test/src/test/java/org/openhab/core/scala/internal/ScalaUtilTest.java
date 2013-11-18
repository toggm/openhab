package org.openhab.core.scala.internal;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ScalaUtilTest {

	@Test
	public void shouldRecompileScalaFiles() throws IOException {

		File scalaSourceFile = new File(getClass().getResource(
				"/ScalaModel.scala").getFile());
		File library = new File(getClass().getResource(
				"/lib/hammurabi_2.11.0-M3-0.1.jar").getFile());

		File outputDir = FileUtil.createTempDir("outputDir", "test");

		boolean success = ScalaUtil.compileScalaFiles(
				new File[] { scalaSourceFile }, new File[] { library },
				outputDir);

		File[] listFiles = outputDir.listFiles();

		Assert.assertTrue(success);
		Assert.assertEquals(2, listFiles.length);
		Assert.assertEquals("SimpleScalaModel.class", listFiles[0]);
		Assert.assertEquals("ScalaModelWithDependencies.class", listFiles[1]);

		outputDir.delete();
	}
}
