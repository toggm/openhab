package org.openhab.core.scala.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scala.collection.JavaConverters;
import scala.collection.convert.Decorators.AsScala;
import scala.collection.mutable.Buffer;
import scala.tools.nsc.Global;
import scala.tools.nsc.Global.Run;
import scala.tools.nsc.Settings;
import scala.tools.nsc.reporters.ConsoleReporter;
import scala.tools.nsc.reporters.Reporter;

public class ScalaUtil {
	/**
	 * Recompile scala files using
	 * 
	 * @param files
	 * @param libs
	 * @param outputDir
	 * @return list of compile resources
	 */
	public static boolean compileScalaFiles(File[] files, File[] libs,
			File outputDir) {

		// set output directory
		Settings currentSettings = new Settings();
		currentSettings.outdir().value_$eq(outputDir.getAbsolutePath());
		currentSettings.processArgumentString("-usejavacp");

		Reporter reporter = new ConsoleReporter(currentSettings);
		Global global = new Global(currentSettings, reporter);
		Run run = global.new Run();

		List<String> filenames = new ArrayList<String>();
		for (File file : files) {
			filenames.add(file.getAbsolutePath());
		}

		AsScala<Buffer<String>> scalaBuffer = JavaConverters
				.asScalaBufferConverter(filenames);
		run.compile(scalaBuffer.asScala().toList());

		return !reporter.hasErrors();
	}
}
