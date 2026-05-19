//usr/bin/env groovy "$0" "$@"; exit $?
//
// Inline a JSON data file into the report template by replacing the section
// between //PLACEHOLDER_START and //PLACEHOLDER_END markers.
//
// Usage:
//   ./build.groovy report-sample.json
//   ./build.groovy report-sample.json -o report-sample.html
//   ./build.groovy report-sample.json --template my-template.html
//
// No external dependencies — uses only the Groovy stdlib (JsonSlurper / JsonOutput).

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

final String START = '//PLACEHOLDER_START'
final String END   = '//PLACEHOLDER_END'

def usage = '''Usage: build.groovy DATA.json [--template TEMPLATE.html] [-o OUTPUT.html]

Inline a JSON data file into the report template. Output is written to the
given file with -o/--output, otherwise to stdout.
'''

def opts = [data: null, template: null, output: null, help: false]
def positional = []
for (int i = 0; i < args.length; i++) {
    switch (args[i]) {
        case '-o':
        case '--output':   opts.output   = args[++i]; break
        case '--template': opts.template = args[++i]; break
        case '-h':
        case '--help':     opts.help = true; break
        default:           positional << args[i]
    }
}
if (positional) opts.data = positional[0]
if (opts.help)            { println usage; System.exit(0) }
if (!opts.data)           { System.err.println usage; System.exit(1) }

def scriptDir = new File(getClass().protectionDomain.codeSource.location.toURI()).parentFile
// The authoritative template lives next to its Java consumers under source/, so
// the same file backs both the Java ReportWriter and this standalone viewer.
def defaultTemplate = new File(scriptDir, '../../../source/org/zfin/report/report-template.html').canonicalFile
def templateFile = new File(opts.template ?: defaultTemplate.path)
def dataFile     = new File(opts.data)

if (!dataFile.exists())     { System.err.println "Data file not found: ${dataFile}"; System.exit(2) }
if (!templateFile.exists()) { System.err.println "Template not found: ${templateFile}"; System.exit(2) }

def data     = new JsonSlurper().parse(dataFile)
def template = templateFile.text

int startIdx = template.indexOf(START)
int endIdx   = template.indexOf(END)
if (startIdx < 0 || endIdx < 0) {
    System.err.println "Could not find ${START} / ${END} markers in ${templateFile}"
    System.exit(3)
}

// Escape "</" → "<\/" so any string in the data can't break out of the
// surrounding <script> block (e.g. an error blob containing "</script>")
// and can't reproduce our PLACEHOLDER_END sentinel. "<\/" is JSON-legal.
def json    = JsonOutput.prettyPrint(JsonOutput.toJson(data)).replace('</', '<\\/')
def payload = "${START}\n        window.REPORT_DATA = ${json};\n        "
def out     = template.substring(0, startIdx) + payload + template.substring(endIdx)

if (opts.output) {
    new File(opts.output).text = out
} else {
    print out
}
