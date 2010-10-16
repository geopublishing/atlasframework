package org.geopublishing.geopublisher;

import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.geopublisher.export.JarExportUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import skrueger.versionnumber.ReleaseUtil;
import skrueger.versionnumber.ReleaseUtil.License;

/**
 * This class manages all the command line options of Geopublisher.
 */
public class CliOptions extends Options {
	public static final String VERSION = "v";
	public static final String HELP = "h";
	public static final String EXPORT = "e";
	public static final String AWCFOLDER = "a";
	public static final String FORCE = "f";
	public static final String DISK = "d";
	public static final String JWS = "j";
	static final String ZIPDISK = "z";
	private static final String LICENSE = "l";

	private static final Logger log = Logger.getLogger(CliOptions.class);

	public static enum Errors {

		PARSEEXCEPTION(1), AWCPARAM_MISSING(2), AWCPARAM_ILLEGAL(3), EXPORTDIR_MISSING(
				4), EXPORTDIR_ILLEGAL(5), EXPORTDIR_NOTEMPTYNOFORCE(6), EXPORT_FAILED(
				7);

		private final int errCode;

		Errors(int errCode) {
			this.errCode = errCode;
		};

		public boolean equals(int errCode) {
			return getErrCode() == errCode;
		}

		public int getErrCode() {
			return errCode;
		}
	}

	public CliOptions() {
		addOption(new Option(HELP, "help", false, "print this message"));

		addOption(new Option(VERSION, "verbose", false,
				"print verbose information while running"));

		addOption(new Option(LICENSE, "license", false,
				"print license information"));

		Option optAwc = new Option(AWCFOLDER, "atlas", true,
				"folder to load the atlas from (atlas.gpa)");
		optAwc.setArgName("srcDir");
		addOption(optAwc);

		Option optExport = new Option(
				EXPORT,
				"export",
				true,
				"exports an atlas to a given directory, combine this option with -f / -d and/or -j.");
		optExport.setArgName("dstDir");
		addOption(optExport);

		Option diskOption = new Option(DISK, "disk", false,
				"create DISK version of atlas when exporting");
		addOption(diskOption);

		addOption(new Option(JWS, "jws", false,
				"create JavaWebStart version of atlas when exporting"));

		addOption(new Option(ZIPDISK, "zipdisk", false,
				"zip the DISK folder after export"));

		addOption(new Option(FORCE, "force", false,
				"overwrite any existing files during export"));

	}

	public CommandLine parse(String[] args) throws ParseException {
		CommandLineParser parser = new PosixParser();
		return parser.parse(this, args);
	}

	public void printHelp() {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("geopublisher", this);

	}

	/**
	 * @return <code>-1</code> means the application GUI is open and the program
	 *         should not exit. <code>0</code> the program exits normally after
	 *         successful execution.
	 */
	public static int performArgs(final String[] args) {
		/** Any number >= 0 will result in exit **/
		boolean exitAfterInterpret = false;
		CliOptions.Errors errorDuringInterpret = null;
		boolean startGui = false;

		/** Any atlas to load **/
		File awcFile = null;
		/** Export folder to use **/
		File exportFile = null;

		CliOptions cliOptions = new CliOptions();

		try {
			final CommandLine commandLine = cliOptions.parse(args);

			// Help
			if (commandLine.hasOption(CliOptions.HELP)) {
				cliOptions.printHelp();
				exitAfterInterpret = true;
			}

			// Show license
			if (commandLine.hasOption(CliOptions.LICENSE)) {
				/** Output information about the GPL license **/
				System.out.println(ReleaseUtil.getLicense(License.GPL3,
						"Geopublisher"));
			}

			// AWC folder
			if (!commandLine.hasOption(CliOptions.AWCFOLDER)) {
				System.out.println("Paramter " + CliOptions.AWCFOLDER
						+ " is needed.");
				exitAfterInterpret = true;
				errorDuringInterpret = Errors.AWCPARAM_MISSING;
			} else if (commandLine.getOptionValue(CliOptions.AWCFOLDER) == null) {
				System.out.println("Paramter " + CliOptions.AWCFOLDER
						+ " needs a paramter pointing to an atlas.gpa");
				exitAfterInterpret = false;
				errorDuringInterpret = Errors.AWCPARAM_ILLEGAL;
			} else {
				awcFile = new File(commandLine.getOptionValue(
						CliOptions.AWCFOLDER).trim());
				if (AtlasConfig.isAtlasDir(awcFile)) {
				} else if (AtlasConfig.isAtlasDir(awcFile.getParentFile())) {
					awcFile = awcFile.getParentFile();
				} else {
					System.out
							.println("'"
									+ awcFile
									+ "' is no valid atlas directory. It should contain an atlas.gpa.");
					exitAfterInterpret = true;
					errorDuringInterpret = Errors.AWCPARAM_ILLEGAL;
				}
			}

			// export?
			if (!commandLine.hasOption(CliOptions.EXPORT)) {
				// Use the GUI
				startGui = true;
			} else {
				exportFile = new File(commandLine.getOptionValue(
						CliOptions.EXPORT).trim());

				if (!exportFile.isDirectory()) {
					System.out.println("Not a valid export directory: "
							+ exportFile);
					exitAfterInterpret = true;
					errorDuringInterpret = Errors.EXPORTDIR_ILLEGAL;
				} else {

					// Check export dir
					if (exportFile.list().length > 0
							&& !commandLine.hasOption(CliOptions.FORCE)) {
						System.out
								.println("Export directory is not empty. Use --force to delete any older atlases.");
						exitAfterInterpret = true;
						errorDuringInterpret = Errors.EXPORTDIR_NOTEMPTYNOFORCE;
					}
				}
			}

			if (exitAfterInterpret || errorDuringInterpret != null) {
				System.out
						.println("Error " + errorDuringInterpret.getErrCode());
				// System.exit();
				return errorDuringInterpret.getErrCode();
			}

			/***
			 * Start Running the commands
			 */

			if (startGui) {
				final File awcFileToLoad = awcFile;
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// Load an atlas with GUI
						GeopublisherGUI instance = GeopublisherGUI
								.getInstance(false);
						if (commandLine.hasOption(CliOptions.VERSION)) {
							// exitAfterInterpret = true;
							// Logger.getLogger("root").setLevel(Level.ALL);
							Logger.getRootLogger().setLevel(
									org.apache.log4j.Level.DEBUG);
						} else {
							Logger.getRootLogger().setLevel(
									org.apache.log4j.Level.WARN);
							// Logger.getLogger("root").setLevel(Level.WARNING);
						}
						instance.loadAtlasFromDir(awcFileToLoad);

					}
				});
				return -1;
			} else {
				// Not starting GUI, initialize logging
				ConsoleAppender cp = new ConsoleAppender(new PatternLayout(
						PatternLayout.TTCC_CONVERSION_PATTERN));
				cp.setName("CLI output");
				cp.setTarget("System.out");
				Logger.getRootLogger().addAppender(cp);
				if (commandLine.hasOption(CliOptions.VERSION))
					Logger.getRootLogger().setLevel(
							org.apache.log4j.Level.DEBUG);
				else
					Logger.getRootLogger()
							.setLevel(org.apache.log4j.Level.WARN);

				if (awcFile != null) {
					final AtlasConfigEditable ace = new AMLImportEd()
							.parseAtlasConfig(null, awcFile);
					System.out
							.println("Loaded atlas: '" + ace.getTitle() + "'");

					try {

						boolean toDisk = commandLine.hasOption(DISK);
						boolean toJws = commandLine.hasOption(JWS);

						// If nothing is selected, all export modes are selected
						if (!toDisk && !toJws)
							toDisk = toJws = true;

						JarExportUtil jeu = new JarExportUtil(ace, exportFile,
								toDisk, toJws, false);
						jeu.setZipDiskAfterExport(commandLine
								.hasOption(ZIPDISK));
						jeu.export(null);
					} catch (Exception e) {
						errorDuringInterpret = Errors.EXPORT_FAILED;
						return Errors.EXPORT_FAILED.errCode;
					}

				}
			}

			// Do not System.exit()
			return 0;

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed. Reason: " + exp.getMessage());

			cliOptions.printHelp();

			return CliOptions.Errors.PARSEEXCEPTION.getErrCode();
		}
	}
}
