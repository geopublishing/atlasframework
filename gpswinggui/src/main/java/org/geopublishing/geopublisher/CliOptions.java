package org.geopublishing.geopublisher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.geopublisher.export.JarExportUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.versionnumber.ReleaseUtil;
import de.schmitzm.versionnumber.ReleaseUtil.License;

/**
 * This class manages all the command line options of Geopublisher.
 */
public class CliOptions extends Options {
    public static final String VERBOSE = "v";
    public static final String HELP = "h";
    public static final String EXPORT = "e";
    public static final String AWCFOLDER = "a";
    public static final String FORCE = "f";
    public static final String DISK = "d";
    public static final String JWS = "j";
    // public static final String GS_URL = "gs";
    // public static final String GS_USER = "gsu";
    // public static final String GS_PASSWORD = "gsp";
    static final String ZIPDISK = "z";
    private static final String LICENSE = "l";
    private static final String KEEPTEMP = "t";
    private static final String JWSURL = "u";
    private static final String SAVEANDEXIT = "s";

    private static final Logger log = Logger.getLogger(CliOptions.class);

    public static enum Errors {

        PARSEEXCEPTION(1), AWCPARAM_MISSING(2), AWCPARAM_ILLEGAL(3), EXPORTDIR_MISSING(
                4), EXPORTDIR_ILLEGAL(5), EXPORTDIR_NOTEMPTYNOFORCE(6), EXPORT_FAILED(
                7), NOHEAD(8), SAVEERROR(9), GS_EXPORTERROR(10);

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
        addOption(new Option(HELP, "help", false, "print this message."));

        addOption(new Option(VERBOSE, "verbose", false,
                "Print verbose information while running."));

        addOption(new Option(LICENSE, "license", false,
                "Print license information."));

        Option optAwc = new Option(AWCFOLDER, "atlas", true,
                "Folder to load the atlas from (atlas.gpa). The path may not contain spaces!");
        optAwc.setArgName("srcDir");
        addOption(optAwc);

        addOption(new Option(
                SAVEANDEXIT,
                "saveandexit",
                false,
                "Save the atlas after loading and exit. This will update atlas.xml to the lastest format."));

        Option optExport = new Option(
                EXPORT,
                "export",
                true,
                "exports an atlas as a stand-alone application to a given directory, combine this option with -f / -d and/or -j. The path may not contain spaces!");
        optExport.setArgName("dstDir");
        addOption(optExport);

        Option diskOption = new Option(DISK, "disk", false,
                "Create DISK version of atlas when exporting.");
        addOption(diskOption);

        // Option gsOption = new Option(GS_URL, "gsurl", true,
        // "URL of Geoserver to configure during export.");
        // addOption(gsOption);
        //
        // Option gsUserOption = new Option(GS_USER, "gsuser", true,
        // "Geoserver username");
        // addOption(gsUserOption);
        //
        // Option gsPwdOption = new Option(GS_PASSWORD, "gspassword", true,
        // "Geoserver password");
        // addOption(gsPwdOption);

        addOption(new Option(ZIPDISK, "zipdisk", false,
                "Zip the DISK folder after export."));

        addOption(new Option(JWS, "jws", false,
                "Create JavaWebStart version of atlas when exporting."));

        Option jwsUrlOp = new Option(
                JWSURL,
                "jwsurl",
                true,
                "Set the JNLP export URL specifically, overriding the URL stored in the atlas.xml. Must end with a /.");
        jwsUrlOp.setArgName("jnlpUrl");
        addOption(jwsUrlOp);

        addOption(new Option(FORCE, "force", false,
                "Overwrite any existing files during export."));

        addOption(new Option(KEEPTEMP, "keeptemp", false,
                "Do not clean temp files, needed if exporting in parallel"));

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
            if (!commandLine.hasOption(CliOptions.AWCFOLDER)
                    && commandLine.hasOption(CliOptions.EXPORT)) {
                System.out.println("Parameter " + CliOptions.AWCFOLDER
                        + " is needed.");
                exitAfterInterpret = true;
                errorDuringInterpret = Errors.AWCPARAM_MISSING;
            } else if (commandLine.hasOption(CliOptions.AWCFOLDER)) {
                awcFile = new File(commandLine.getOptionValue(
                        CliOptions.AWCFOLDER).trim());
                if (AtlasConfig.isAtlasDir(awcFile)) {
                } else if (AtlasConfig.isAtlasDir(awcFile.getParentFile())) {
                    awcFile = awcFile.getParentFile();
                } else {
                    // first argument when invoking gp under Linux now is the
                    // directory from where it is called
                    File awcFile2 = new File(args[0], commandLine
                            .getOptionValue(CliOptions.AWCFOLDER).trim());
                    if (AtlasConfig.isAtlasDir(awcFile2)) {
                        awcFile = awcFile2;
                    } else if (AtlasConfig.isAtlasDir(awcFile2.getParentFile())) {
                        awcFile = awcFile2.getParentFile();
                    } else {
                        System.out
                                .println("'"
                                        + awcFile
                                        + "' is no valid atlas directory. It should contain an atlas.gpa.");
                        exitAfterInterpret = true;
                        errorDuringInterpret = Errors.AWCPARAM_ILLEGAL;
                    }
                }
            }

            // export?
            if (!commandLine.hasOption(CliOptions.EXPORT)
                    && !commandLine.hasOption(CliOptions.SAVEANDEXIT)
            // && !commandLine.hasOption(CliOptions.GS_URL)
            ) {
                // Use the GUI
                startGui = true;
            } else if (commandLine.hasOption(CliOptions.EXPORT)) {

                /**
                 * Read the export Directory
                 */
                exportFile = new File(commandLine.getOptionValue(
                        CliOptions.EXPORT).trim());

                if (exportFile != null) {
                    if (!exportFile.isDirectory()) {
                        exportFile = new File(args[0], commandLine
                                .getOptionValue(CliOptions.EXPORT).trim());
                        if (!exportFile.isDirectory()) {
                            System.out.println("Not a valid export directory: "
                                    + exportFile);
                            exitAfterInterpret = true;
                            errorDuringInterpret = Errors.EXPORTDIR_ILLEGAL;
                        }
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

            }

            if (startGui && GraphicsEnvironment.isHeadless()
                    && !exitAfterInterpret && errorDuringInterpret == null) {
                exitAfterInterpret = true;
                errorDuringInterpret = Errors.NOHEAD;
                System.err
                        .println("Can't open Geopublisher GUI because your environment doesn't provide a window system. You may only use pure CLI commands:");
                cliOptions.printHelp();
            }

            if (exitAfterInterpret || errorDuringInterpret != null) {
                if (errorDuringInterpret != null) {
                    System.out.println("Error "
                            + errorDuringInterpret.getErrCode());
                    // System.exit();
                    return errorDuringInterpret.getErrCode();
                } else
                    return 0;
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
                        if (commandLine.hasOption(CliOptions.VERBOSE)) {
                            // exitAfterInterpret = true;
                            // Logger.getLogger("root").setLevel(Level.ALL);
                            Logger.getRootLogger().setLevel(
                                    org.apache.log4j.Level.DEBUG);
                        } else {
                            // Logger.getRootLogger().setLevel(
                            // org.apache.log4j.Level.WARN);
                            // Logger.getLogger("root").setLevel(Level.WARNING);
                        }

                        if (awcFileToLoad != null)
                            instance.loadAtlasFromDir(awcFileToLoad);

                    }
                });
                return -1;
            } else {

                // Not starting GUI, initialize logging. If other appends
                // already exist, don't to anything
                initLoggingForConsole();

                if (commandLine.hasOption(CliOptions.VERBOSE))
                    Logger.getRootLogger().setLevel(
                            org.apache.log4j.Level.DEBUG);

                if (awcFile != null) {
                    final AtlasConfigEditable ace = new AMLImportEd()
                            .parseAtlasConfig(null, awcFile);
                    log.info("Successfully loaded atlas: '" + ace.getTitle()
                            + "'");

                    if (commandLine.hasOption(SAVEANDEXIT)) {
                        // Save and exit..
                        AMLExporter amlExporter = new AMLExporter(ace);
                        try {
                            amlExporter.saveAtlasConfigEditable();
                            return 0;
                        } catch (Exception e) {
                            errorDuringInterpret = Errors.SAVEERROR;
                            log.error("Error while saving the atlas", e);
                            ace.dispose();
                            return Errors.SAVEERROR.errCode;
                        }

                    } else {

                        // Export
                        if (exportFile != null) {

                            try {
                                boolean toDisk = commandLine.hasOption(DISK);
                                boolean toJws = commandLine.hasOption(JWS);

                                // If nothing is selected, all export modes are
                                // selected
                                if (!toDisk && !toJws)
                                    toDisk = toJws = true;

                                JarExportUtil jeu = new JarExportUtil(ace,
                                        null, exportFile, toDisk, toJws, false);
                                jeu.setZipDiskAfterExport(commandLine
                                        .hasOption(ZIPDISK));

                                // Is an extra JNLP base url specified?
                                if (commandLine.hasOption(JWSURL)) {
                                    URL jwsUrl = new URL(
                                            commandLine.getOptionValue(JWSURL));
                                    jeu.setOverwriteJnlpBaseUrl(jwsUrl);
                                    if (!toJws)
                                        log.error("Paraeter -"
                                                + JWSURL
                                                + " ignored because not exporting to JWS.");
                                }

                                jeu.setKeepTempFiles(commandLine
                                        .hasOption(KEEPTEMP));
                                jeu.export();
                            } catch (Exception e) {
                                errorDuringInterpret = Errors.EXPORT_FAILED;
                                log.error(Errors.EXPORT_FAILED.toString(), e);
                                return Errors.EXPORT_FAILED.errCode;
                            }
                        } // Export to disk or jws

                        // if (commandLine.hasOption(GS_URL)) {
                        // // Export to Geoserver
                        // String gsUrl = commandLine.getOptionValue(GS_URL);
                        // try {
                        // GsServerSettings gsServer = new GsServerSettings();
                        // gsServer.setUrl(gsUrl);
                        // gsServer.setUsername(commandLine
                        // .getOptionValue(GS_USER));
                        // gsServer.setPassword(commandLine
                        // .getOptionValue(GS_PASSWORD));
                        // AtlasGeoserverExporter gse = new
                        // AtlasGeoserverExporter(
                        // ace, gsServer);
                        // ResultProgressHandle progress = null;
                        // gse.export(progress);
                        // } catch (Exception e) {
                        // log.error("Problem with geoserver at url "
                        // + gsUrl);
                        // return Errors.GS_EXPORTERROR.errCode;
                        // }
                        // }
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

    /**
     * Does nothing if there already is an {@link ConsoleAppender} registered to
     * the root logger. Otherwise adds a console Logger.
     */
    private static void initLoggingForConsole() {
        {
            Enumeration<Appender> allAppenders = Logger.getRootLogger()
                    .getAllAppenders();
            while (allAppenders.hasMoreElements()) {
                Appender app = allAppenders.nextElement();
                if (app instanceof ConsoleAppender)
                    return;
            }

            ConsoleAppender cp = new ConsoleAppender(new PatternLayout(
                    PatternLayout.TTCC_CONVERSION_PATTERN));
            cp.setName("CLI output");
            cp.setTarget("System.out");
            Logger.getRootLogger().addAppender(cp);
        }
    }

}
