package org.openhab.core.scala.internal;

import java.io.File;
import java.io.IOException;

public class FileUtil {
	public static File createTempDir(String prefix, String postfix)
			throws IOException {
		File outputPath = File.createTempFile(prefix, postfix + "d");
		outputPath.delete();
		String path = outputPath.getAbsolutePath();
		outputPath = new File(path.substring(0, path.length() - 2));
		outputPath.mkdir();
		outputPath.deleteOnExit();
		return outputPath;
	}
}
