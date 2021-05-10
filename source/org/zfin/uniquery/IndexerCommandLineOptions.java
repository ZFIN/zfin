package org.zfin.uniquery;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Command line options used for indexing ZFIN site.
 */
public interface IndexerCommandLineOptions {

    public static final Option numberOfDetailPagesOption = OptionBuilder.withArgName("numberOfDetailPages").hasArg().
            withDescription("the number of detail pages to be indexed for texting purposed").create("numberOfDetailPages");
    public static final Option log4jFileOption = OptionBuilder.withArgName("log4jFilename").hasArg().withDescription("location of the log4j file").create("log4jFilename");
    public static final Option webrootDirectory = OptionBuilder.withArgName("webrootDirectory").hasArg().withDescription("location of webroot directory").create("webrootDirectory");
    public static final Option dbScriptFileOption = OptionBuilder.withArgName("dbScriptFileNames").hasArg().withDescription("location and file name of db script file").create("dbScriptFileNames");

}
