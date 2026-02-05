#!/usr/bin/env groovy
/**
 * ZFIN Properties Processor
 *
 * Processes all-properties.yml and generates a flat shell-compatible properties file
 * for a specific instance.
 *
 * Usage:
 *   INSTANCE=trunk groovy PropertiesProcessor.groovy [options]
 *
 * Environment Variables:
 *   INSTANCE                  Instance name (required)
 *
 * Options:
 *   -o, --output <file>      Output file path (default: stdout)
 *   -c, --config <file>      Config file path (default: all-properties.yml in same directory)
 *   -d, --debug              Enable debug output
 *   -h, --help               Show this help message
 *
 * Output Format:
 *   Shell-compatible key=value pairs that can be sourced directly:
 *     source zfin.properties
 *
 * Resolution Order:
 *   1. common_properties       - Base properties shared by all instances
 *   2. deprecated_properties   - Deprecated properties (to be phased out)
 *   3. unique_properties       - Properties that vary per instance (with defaults)
 *   4. emails                  - Email addresses for notifications/reports
 *   5. email_overrides         - Replaces ALL email properties for specific instances
 *   6. instance_environment    - Maps instance name → environment
 *   7. defaults_by_environment - Applies dev_defaults or prod_defaults based on step 6
 *   8. instance_overrides      - Instance-specific overrides
 */

@Grab('org.yaml:snakeyaml:2.2')
import org.yaml.snakeyaml.Yaml
import groovy.cli.commons.CliBuilder

class PropertiesProcessor {

    private Map<String, Object> config
    private String instanceName
    private boolean debug = false
    private Set<String> resolving = [] as Set  // For circular reference detection
    private List<Map> overwriteLog = []  // Track all overwrites for debug summary

    // Sections that must have unique keys (no overlap allowed)
    private static final List<String> UNIQUE_KEY_SECTIONS = [
        'common_properties',
        'deprecated_properties',
        'unique_properties',
        'emails'
    ]

    PropertiesProcessor(String configPath, String instanceName, boolean debug = false) {
        this.instanceName = instanceName
        this.debug = debug
        this.config = loadConfig(configPath)
    }

    private Map<String, Object> loadConfig(String configPath) {
        def yaml = new Yaml()
        def file = new File(configPath)
        if (!file.exists()) {
            throw new RuntimeException("Configuration file not found: ${configPath}")
        }
        return yaml.load(file.text)
    }

    /**
     * Validate the configuration file for errors
     */
    List<String> validate() {
        def errors = []

        // Check for duplicate keys across sections that should be unique
        def allKeys = [:] as Map<String, String>  // key -> section name
        UNIQUE_KEY_SECTIONS.each { section ->
            def sectionData = config[section] as Map
            if (sectionData) {
                sectionData.keySet().each { key ->
                    if (allKeys.containsKey(key)) {
                        errors << "Duplicate key '${key}' found in both '${allKeys[key]}' and '${section}'"
                    } else {
                        allKeys[key] = section
                    }
                }
            }
        }

        // Check that referenced environment defaults exist
        def defaultsByEnv = config['defaults_by_environment'] as Map
        if (defaultsByEnv) {
            defaultsByEnv.each { env, defaultsKey ->
                if (!config[defaultsKey]) {
                    errors << "defaults_by_environment references '${defaultsKey}' for environment '${env}', but section not found"
                }
            }
        }

        // Validate instance_environment values
        def validEnvironments = defaultsByEnv?.keySet() ?: ['development', 'testing', 'production']
        def instanceEnvs = config['instance_environment'] as Map
        if (instanceEnvs) {
            instanceEnvs.each { instance, env ->
                if (!validEnvironments.contains(env)) {
                    errors << "instance_environment: '${instance}' has invalid environment '${env}'. Valid values: ${validEnvironments}"
                }
            }
        }

        // Check for potential variable reference issues
        def variables = config['variables'] as Map ?: [:]
        def allValues = []
        UNIQUE_KEY_SECTIONS.each { section ->
            def sectionData = config[section] as Map
            if (sectionData) {
                allValues.addAll(sectionData.values())
            }
        }

        allValues.each { value ->
            if (value instanceof String) {
                def refs = extractVariableReferences(value)
                refs.each { ref ->
                    if (!ref.startsWith('env.') && !variables.containsKey(ref)) {
                        // Check if it might be defined in the properties themselves
                        if (!allKeys.containsKey(ref.toUpperCase())) {
                            errors << "Variable reference '\${${ref}}' not found in variables section"
                        }
                    }
                }
            }
        }

        return errors
    }

    /**
     * Process the configuration and return resolved properties
     */
    Map<String, String> process() {
        def resolved = [:] as LinkedHashMap<String, String>

        debugLog("Processing instance: ${instanceName}")

        // Step 1: common_properties
        debugLog("Step 1: Applying common_properties")
        mergeSection(resolved, 'common_properties')

        // Step 2: deprecated_properties
        debugLog("Step 2: Applying deprecated_properties")
        mergeSection(resolved, 'deprecated_properties')

        // Step 3: unique_properties
        debugLog("Step 3: Applying unique_properties")
        mergeSection(resolved, 'unique_properties')

        // Step 4: emails
        debugLog("Step 4: Applying emails")
        mergeSection(resolved, 'emails')

        // Step 5: email_overrides (if instance is listed)
        debugLog("Step 5: Checking email_overrides")
        applyEmailOverrides(resolved)

        // Step 6: instance_environment lookup
        debugLog("Step 6: Looking up instance_environment")
        def environment = lookupInstanceEnvironment()
        resolved['ENVIRONMENT'] = environment
        debugLog("  ENVIRONMENT = ${environment}")

        // Step 7: defaults_by_environment
        debugLog("Step 7: Applying defaults_by_environment for '${environment}'")
        applyEnvironmentDefaults(resolved, environment)

        // Step 8: instance_overrides
        debugLog("Step 8: Applying instance_overrides for '${instanceName}'")
        applyInstanceOverrides(resolved)

        // Now resolve all variable references
        debugLog("Resolving variable references...")
        resolved = resolveAllVariables(resolved)

        // Print overwrite summary in debug mode
        if (debug && overwriteLog) {
            debugLog("")
            debugLog("=" * 60)
            debugLog("OVERWRITE SUMMARY")
            debugLog("=" * 60)
            overwriteLog.each { entry ->
                debugLog("  ${entry.key}:")
                debugLog("    was: ${entry.oldValue}")
                debugLog("    now: ${entry.newValue}")
                debugLog("    by:  ${entry.source}")
            }
            debugLog("=" * 60)
            debugLog("Total overwrites: ${overwriteLog.size()}")
        }

        return resolved
    }

    private void mergeSection(Map<String, String> target, String sectionName) {
        def section = config[sectionName] as Map
        if (section) {
            section.each { key, value ->
                if (value != null) {
                    def keyStr = key as String
                    def valueStr = value as String
                    if (target.containsKey(keyStr) && target[keyStr] != valueStr) {
                        overwriteLog << [key: keyStr, oldValue: target[keyStr], newValue: valueStr, source: sectionName]
                    }
                    target[keyStr] = valueStr
                    debugLog("  ${key} = ${value}")
                }
            }
        }
    }

    private void applyEmailOverrides(Map<String, String> target) {
        def emailOverrides = config['email_overrides'] as Map
        if (emailOverrides && emailOverrides.containsKey(instanceName)) {
            def overrideEmail = emailOverrides[instanceName] as String
            debugLog("  Overriding all emails with: ${overrideEmail}")

            // Get all keys from the emails section and override them
            def emailsSection = config['emails'] as Map
            if (emailsSection) {
                emailsSection.keySet().each { key ->
                    target[key as String] = overrideEmail
                    debugLog("    ${key} = ${overrideEmail}")
                }
            }
        } else {
            debugLog("  No email override for instance '${instanceName}'")
        }
    }

    private String lookupInstanceEnvironment() {
        def instanceEnvs = config['instance_environment'] as Map
        if (instanceEnvs && instanceEnvs.containsKey(instanceName)) {
            return instanceEnvs[instanceName] as String
        }
        // Default to 'development' if not specified
        return 'development'
    }

    private void applyEnvironmentDefaults(Map<String, String> target, String environment) {
        def defaultsByEnv = config['defaults_by_environment'] as Map
        if (!defaultsByEnv) {
            debugLog("  No defaults_by_environment section found")
            return
        }

        def defaultsKey = defaultsByEnv[environment] as String
        if (!defaultsKey) {
            debugLog("  No defaults mapping for environment '${environment}'")
            return
        }

        def defaults = config[defaultsKey] as Map
        if (!defaults) {
            debugLog("  Defaults section '${defaultsKey}' not found")
            return
        }

        debugLog("  Applying '${defaultsKey}':")
        defaults.each { key, value ->
            def keyStr = key as String
            def valueStr = value as String
            if (target.containsKey(keyStr) && target[keyStr] != valueStr) {
                overwriteLog << [key: keyStr, oldValue: target[keyStr], newValue: valueStr, source: defaultsKey]
            }
            target[keyStr] = valueStr
            debugLog("    ${key} = ${value}")
        }
    }

    private void applyInstanceOverrides(Map<String, String> target) {
        def instanceOverrides = config['instance_overrides'] as Map
        if (!instanceOverrides) {
            debugLog("  No instance_overrides section found")
            return
        }

        def overrides = instanceOverrides[instanceName] as Map
        if (!overrides) {
            debugLog("  No overrides for instance '${instanceName}'")
            return
        }

        overrides.each { key, value ->
            def keyStr = key as String
            def valueStr = value as String
            if (target.containsKey(keyStr) && target[keyStr] != valueStr) {
                overwriteLog << [key: keyStr, oldValue: target[keyStr], newValue: valueStr, source: "instance_overrides.${instanceName}"]
            }
            target[keyStr] = valueStr
            debugLog("    ${key} = ${value}")
        }
    }

    private Map<String, String> resolveAllVariables(Map<String, String> properties) {
        def variables = config['variables'] as Map ?: [:]
        def resolved = [:] as LinkedHashMap<String, String>

        // First, resolve variables section itself (they can reference each other)
        def resolvedVars = [:] as Map<String, String>
        variables.each { key, value ->
            resolvedVars[key as String] = resolveValue(value as String, resolvedVars, properties)
        }

        // Then resolve all properties
        properties.each { key, value ->
            resolved[key] = resolveValue(value, resolvedVars, properties)
        }

        return resolved
    }

    private String resolveValue(String value, Map<String, String> variables, Map<String, String> properties) {
        if (!value || !value.contains('${')) {
            return value
        }

        def result = value
        def pattern = ~/\$\{([^}]+)\}/
        def matcher = pattern.matcher(result)

        while (matcher.find()) {
            def fullMatch = matcher.group(0)
            def varName = matcher.group(1)
            def replacement = null

            // Check for circular reference
            if (resolving.contains(varName)) {
                throw new RuntimeException("Circular variable reference detected: ${varName} (resolution chain: ${resolving})")
            }

            resolving.add(varName)
            try {
                if (varName.startsWith('env.')) {
                    // Environment variable reference
                    def envVarName = varName.substring(4)
                    replacement = System.getenv(envVarName)
                    if (replacement == null) {
                        throw new RuntimeException("Required environment variable '${envVarName}' is not set. Set it before running the processor.")
                    }
                    debugLog("  Resolved env.${envVarName} = ${replacement}")
                } else if (variables.containsKey(varName)) {
                    // Variable from variables section
                    replacement = resolveValue(variables[varName], variables, properties)
                } else if (properties.containsKey(varName)) {
                    // Reference to another property
                    replacement = resolveValue(properties[varName], variables, properties)
                } else if (properties.containsKey(varName.toUpperCase())) {
                    // Try uppercase version
                    replacement = resolveValue(properties[varName.toUpperCase()], variables, properties)
                } else {
                    // Leave unresolved with a warning
                    System.err.println("WARNING: Unresolved variable reference: \${${varName}}")
                    replacement = fullMatch
                }
            } finally {
                resolving.remove(varName)
            }

            if (replacement != null && replacement != fullMatch) {
                result = result.replace(fullMatch, replacement)
                // Reset matcher for the new string
                matcher = pattern.matcher(result)
            }
        }

        return result
    }

    private List<String> extractVariableReferences(String value) {
        def refs = []
        def pattern = ~/\$\{([^}]+)\}/
        def matcher = pattern.matcher(value)
        while (matcher.find()) {
            refs << matcher.group(1)
        }
        return refs
    }

    private void debugLog(String message) {
        if (debug) {
            System.err.println("[DEBUG] ${message}")
        }
    }

    /**
     * Format properties for shell-compatible output
     */
    static String formatProperties(Map<String, String> properties, String instanceName) {
        def sb = new StringBuilder()
        sb.append("#Properties for INSTANCE=${instanceName}\n")

        properties.sort().each { key, value ->
            def escapedValue = escapeForShell(value)
            sb.append("${key}=${escapedValue}\n")
        }

        return sb.toString()
    }

    private static String escapeForShell(String value) {
        if (value == null) return ''
        // Escape backslashes, double quotes, backticks, and dollar signs
        return value
            .replace('\\', '\\\\')
            .replace('"', '\\"')
            .replace('`', '\\`')
            .replace('$', '\\$')
    }

    // =========================================================================
    // Main entry point
    // =========================================================================

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'INSTANCE=<name> groovy PropertiesProcessor.groovy [options]')
        cli.with {
            h(longOpt: 'help', 'Show usage information')
            o(longOpt: 'output', args: 1, argName: 'file', 'Output file path (default: stdout)')
            c(longOpt: 'config', args: 1, argName: 'file', 'Config file path (default: all-properties.yml)')
            d(longOpt: 'debug', 'Enable debug output')
        }

        def options = cli.parse(args)
        if (!options) {
            System.exit(1)
        }

        if (options.h) {
            cli.usage()
            System.exit(0)
        }

        // Instance name must come from environment variable
        def instanceName = System.getenv('INSTANCE')
        if (!instanceName) {
            System.err.println("ERROR: INSTANCE environment variable is required.")
            System.err.println("Usage: INSTANCE=trunk groovy PropertiesProcessor.groovy [options]")
            System.exit(1)
        }

        // Determine config file path
        def scriptDir = new File(PropertiesProcessor.class.protectionDomain.codeSource.location.path).parent
        def configPath = options.c ?: "${scriptDir}/all-properties.yml"

        def debug = options.d as boolean

        try {
            def processor = new PropertiesProcessor(configPath, instanceName, debug)

            // Validate first - exit on errors
            def errors = processor.validate()
            if (errors) {
                System.err.println("Validation errors:")
                errors.each { System.err.println("  - ${it}") }
                System.exit(1)
            }

            // Process and generate output
            def properties = processor.process()
            def output = formatProperties(properties, instanceName)

            if (options.o) {
                new File(options.o as String).text = output
                System.err.println("Properties written to: ${options.o} (INSTANCE=${instanceName})")
            } else {
                print(output)
            }

        } catch (Exception e) {
            System.err.println("ERROR: ${e.message}")
            if (debug) {
                e.printStackTrace()
            }
            System.exit(1)
        }
    }
}
