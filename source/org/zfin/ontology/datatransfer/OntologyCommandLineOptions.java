package org.zfin.ontology.datatransfer;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Command line options used for downloading and importing ontologies.
 */
public interface OntologyCommandLineOptions {

    public static final Option oboFileNameOption = OptionBuilder.withArgName("localOboFileName").hasArg().withDescription("the obo file name").create("localOboFileName");
    public static final Option termStageUpdateFileOption = OptionBuilder.withArgName("termStageUpdateFile").hasArg().withDescription("the term-stage update file name").create("termStageUpdateFile");
    public static final Option log4jFileOption = OptionBuilder.withArgName("log4jFilename").hasArg().withDescription("location of the log4j file").create("log4jFilename");
    public static final Option workingDirectoryOption = OptionBuilder.withArgName("workingDirectory").hasArg().withDescription("root directory of the indexer").create("workingDirectory");
    public static final Option dbScriptFileOption = OptionBuilder.withArgName("dbScriptFileNames").hasArg().withDescription("location and file name of db script file").create("dbScriptFileNames");
    public static final Option oboFileURL = OptionBuilder.withArgName("oboFileURL").hasArg().withDescription("The URL from which to " +
            "download the most current OBO file name").create("oboFileURL");
    public static final Option webrootDirectory = OptionBuilder.withArgName("webrootDirectory").hasArg().withDescription("location of webroot directory").create("webrootDirectory");
    public static final Option loadDir = OptionBuilder.withArgName("loadDir").hasArg().withDescription("location of the load directory").create("loadDir");
    public static final Option productionModeOption = OptionBuilder.withArgName("productionMode").hasArg().withDescription("production mode or dev mode").create("productionMode");
    public static final Option debugModeOption = OptionBuilder.withArgName("debugMode").hasArg().withDescription("include debug rows or not").create("debugMode");
    public static final Option forceLoadOption = OptionBuilder.withArgName("forceLoad").hasArg().withDescription("force load").create("forceLoad");

}
