package org.zfin.util.commandline;

import org.zfin.framework.ToolBootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Central registry for all ZFIN command-line utilities.
 * Add new utilities by calling register() in the static initializer block.
 */
public class CommandLineUtilityRegistry {

    private static final Map<String, UtilityInfo> UTILITIES = new LinkedHashMap<>();

    static {
        // Register available utilities here.
        //
        // Use the 4-arg register(...) for utilities that do NOT need the database (pure file/IO tools);
        // their main() is invoked directly. Use the 5-arg register(..., requiresDatabase=true) for
        // data tools (reports, audits, loads): they run inside ToolBootstrap.run(...), which loads
        // ZfinProperties, brings up Hibernate, and wraps the run in a single managed transaction
        // (commit on success, rollback on failure). Such tools should let main() return normally
        // rather than calling System.exit(), so the transaction commits and the session closes.
        register("csv2xlsx",
                "org.zfin.datatransfer.util.CSVToXLSXConverter",
                "Convert CSV file to XLSX format",
                "csv2xlsx <output.xlsx> <input.csv>");

        register("csv-diff",
                "org.zfin.datatransfer.util.CSVDiff",
                "Diff two CSV files by key column(s); writes <prefix>_{deletes,adds,updated_1,updated_2}.csv",
                "csv-diff <outputPrefix> <file1.csv> <file2.csv> <keyCol1,keyCol2,...> [ignoreCol1,ignoreCol2,...]");

        //org.zfin.infrastructure.TokenStorage
        register("token-storage",
                "org.zfin.infrastructure.TokenStorage",
                "Manage token storage operations",
                "token-storage <operation> [options] \n eg. token-storage read NCBI_API_TOKEN\n     token-storage write NCBI_API_TOKEN 'your_token_value'");

        // Merge one marker into another (port of cgi-bin/merge_markers.pl). Database-backed, so it
        // runs inside ToolBootstrap.run's managed transaction.
        register("merge-markers",
                "org.zfin.marker.MergeMarkersCommandLine",
                "Merge one marker into another (reassign references, then delete the old record)",
                "merge-markers <zdbIdToDelete> <zdbIdToMergeInto> [--dry-run] [--skip-regen]",
                true);

        // Add more utilities as they are discovered/created.
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
        register(name, mainClass, description, usage, false);
    }

    /**
     * Register a utility, specifying whether it needs the database.
     *
     * @param requiresDatabase if true, the utility runs inside {@link ToolBootstrap#run}, which loads
     *                         properties, brings up Hibernate, and manages a single transaction; if
     *                         false, its main() is invoked directly with no environment setup.
     */
    private static void register(String name, String mainClass, String description, String usage, boolean requiresDatabase) {
        UTILITIES.put(name.toLowerCase(), new UtilityInfo(name, mainClass, description, usage, requiresDatabase));
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

        if (util.requiresDatabase) {
            // Run inside the ToolBootstrap environment: properties + Hibernate + a managed transaction.
            ToolBootstrap.run(util.name, args, () -> invokeMain(mainMethod, args));
        } else {
            invokeMain(mainMethod, args);
        }
    }

    /**
     * Invoke a utility's static main(String[]), unwrapping the reflection wrapper so the utility's
     * own exception propagates (and, for database-backed tools, reaches ToolBootstrap's rollback).
     */
    private static void invokeMain(Method mainMethod, String[] args) throws Exception {
        try {
            mainMethod.invoke(null, (Object) args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            if (cause instanceof Error err) {
                throw err;
            }
            throw e;
        }
    }

    /**
     * Internal class to hold utility metadata.
     */
    private static class UtilityInfo {
        final String name;
        final String mainClass;
        final String description;
        final String usage;
        final boolean requiresDatabase;

        UtilityInfo(String name, String mainClass, String description, String usage, boolean requiresDatabase) {
            this.name = name;
            this.mainClass = mainClass;
            this.description = description;
            this.usage = usage;
            this.requiresDatabase = requiresDatabase;
        }
    }
}
