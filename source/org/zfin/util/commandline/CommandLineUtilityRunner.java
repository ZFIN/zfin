package org.zfin.util.commandline;

import java.util.Arrays;

/**
 * Main entry point for ZFIN command-line utilities.
 * This multiplexer dispatches commands to individual utility classes.
 *
 * Usage:
 *   zfin-util list              - List all available utilities
 *   zfin-util help <utility>    - Show help for a specific utility
 *   zfin-util <utility> [args]  - Run a utility with arguments
 */
public class CommandLineUtilityRunner {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];
        String[] utilArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            switch (command.toLowerCase()) {
                case "list":
                    CommandLineUtilityRegistry.listUtilities();
                    break;
                case "help":
                    if (utilArgs.length > 0) {
                        CommandLineUtilityRegistry.showHelp(utilArgs[0]);
                    } else {
                        printUsage();
                    }
                    break;
                default:
                    CommandLineUtilityRegistry.run(command, utilArgs);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("ZFIN Command-Line Utilities");
        System.out.println("===========================");
        System.out.println();
        System.out.println("Usage: zfin-util <command> [args...]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  list              List all available utilities");
        System.out.println("  help <utility>    Show help for a specific utility");
        System.out.println("  <utility> [args]  Run a utility with arguments");
        System.out.println();
        System.out.println("Run 'zfin-util list' to see available utilities");
    }
}
