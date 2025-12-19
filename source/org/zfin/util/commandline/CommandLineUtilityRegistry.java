package org.zfin.util.commandline;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Central registry for all ZFIN command-line utilities.
 * Add new utilities by calling register() in the static initializer block.
 */
public class CommandLineUtilityRegistry {

    private static final Map<String, UtilityInfo> UTILITIES = new LinkedHashMap<>();

    static {
        // Register available utilities here
        register("csv2xlsx",
                "org.zfin.datatransfer.util.CSVToXLSXConverter",
                "Convert CSV file to XLSX format",
                "csv2xlsx <output.xlsx> <input.csv>");

        //org.zfin.infrastructure.TokenStorage
        register("token-storage",
                "org.zfin.infrastructure.TokenStorage",
                "Manage token storage operations",
                "token-storage <operation> [options] \n eg. token-storage read NCBI_API_TOKEN\n     token-storage write NCBI_API_TOKEN 'your_token_value'");

        // Add more utilities as they are discovered/created
    }

    /**
     * Register a utility in the system.
     *
     * @param name        Short name for the utility (used on command line)
     * @param mainClass   Fully qualified class name with main() method
     * @param description Brief description of what the utility does
     * @param usage       Usage example (without the 'zfin-util' prefix)
     */
    private static void register(String name, String mainClass, String description, String usage) {
        UTILITIES.put(name.toLowerCase(), new UtilityInfo(name, mainClass, description, usage));
    }

    /**
     * List all available utilities to stdout.
     */
    public static void listUtilities() {
        System.out.println("Available utilities:");
        System.out.println("===================");
        System.out.println();

        UTILITIES.values().forEach(util -> {
            System.out.printf("  %-20s %s\n", util.name, util.description);
        });

        System.out.println();
        System.out.println("Run 'zfin-util help <utility>' for detailed usage information");
    }

    /**
     * Show detailed help for a specific utility.
     *
     * @param utilityName Name of the utility
     */
    public static void showHelp(String utilityName) {
        UtilityInfo util = UTILITIES.get(utilityName.toLowerCase());
        if (util == null) {
            System.err.println("Unknown utility: " + utilityName);
            System.err.println("Run 'zfin-util list' to see available utilities");
            System.exit(1);
        }

        System.out.println("Utility: " + util.name);
        System.out.println("Description: " + util.description);
        System.out.println();
        System.out.println("Usage: zfin-util " + util.usage);
        System.out.println();
        System.out.println("Main class: " + util.mainClass);
    }

    /**
     * Run a utility with the given arguments.
     *
     * @param utilityName Name of the utility to run
     * @param args        Arguments to pass to the utility
     * @throws Exception If the utility fails or cannot be found
     */
    public static void run(String utilityName, String[] args) throws Exception {
        UtilityInfo util = UTILITIES.get(utilityName.toLowerCase());
        if (util == null) {
            System.err.println("Unknown utility: " + utilityName);
            System.err.println("Run 'zfin-util list' to see available utilities");
            System.exit(1);
        }

        // Load the main class and invoke its main method
        Class<?> mainClass = Class.forName(util.mainClass);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    /**
     * Internal class to hold utility metadata.
     */
    private static class UtilityInfo {
        final String name;
        final String mainClass;
        final String description;
        final String usage;

        UtilityInfo(String name, String mainClass, String description, String usage) {
            this.name = name;
            this.mainClass = mainClass;
            this.description = description;
            this.usage = usage;
        }
    }
}
