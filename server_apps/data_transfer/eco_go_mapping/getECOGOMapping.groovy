#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.apache.commons.io.FileUtils
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")
// GO's DERIVED mapping, not the flat one: the flat file carries only the high-level equivalence
// mappings, the derived one propagates a GO evidence code down to child ECO terms and is a strict
// superset of it.
//
// The PURL, not the GitHub raw URL: GO's own header says "Always use this URL".
// It must be https: the PURL redirects to an https target, and Java will not follow a redirect
// that changes protocol, so the http form silently yields the redirect page instead of the file.
DOWNLOAD_URL = "https://purl.obolibrary.org/obo/eco/gaf-eco-mapping-derived.txt"
final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/eco_go_mapping")
WORKING_DIR.mkdirs()

// both files have to land in WORKING_DIR: ant runs this script with its working directory in
// SOURCEROOT, but insert_eco_go_map.sql \copy's gafeco.txt out of TARGETROOT
File downloadedFile = new File(WORKING_DIR, DOWNLOAD_URL.tokenize("/")[-1])
// Timeouts matter more than they look: without them a stalled fetch hangs the load indefinitely.
FileUtils.copyURLToFile(new URL(DOWNLOAD_URL), downloadedFile, 30000, 120000)

File outputFile = new File(WORKING_DIR, "gafeco.txt")

// Columns are ECO <tab> CODE <tab> [Default] -- reversed from the flat file, and the opposite of
// what this file's own header comment claims. Most rows have no third field, and split() drops
// the trailing empty, hence the length check.
mappingCount = 0
defaultCount = 0
outputFile.withWriter { outFile ->
    downloadedFile.withReader {
        reader ->
            while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#") && !line.trim().isEmpty()) {
                        fields = line.split()
                        if (fields.length < 2) {
                            continue
                        }
                        eco_term = fields[0]
                        evidence_code = fields[1]
                        // A stub or error page splits into two fields perfectly well; what it
                        // cannot do is put an ECO CURIE in the first column.
                        if (!eco_term.startsWith("ECO:")) {
                            throw new IOException("${downloadedFile.name}: expected an ECO id in column 1, got [$eco_term] on line: $line")
                        }
                        // "Default" marks an ECO term equivalent to the GO code. Passed through
                        // so the loader can break a tie when a term maps to several codes.
                        is_default = (fields.length > 2 && fields[2] == "Default") ? "Default" : ""
                        if (is_default) {
                            defaultCount++
                        }
                        outFile.writeLine("$evidence_code,$eco_term,$is_default")
                        mappingCount++
                    }
            }
    }
}
println("parsed $mappingCount ECO->GO mappings ($defaultCount marked Default) from ${downloadedFile.name}")

// Bail rather than \copy a short file into a load that would report success: a truncated or
// error-page download has to be a failure, not a no-op. The floor sits well below what GO
// publishes and well above anything a stub or partial transfer produces.
MINIMUM_EXPECTED_MAPPINGS = 500
if (mappingCount < MINIMUM_EXPECTED_MAPPINGS) {
    System.err.println("Only $mappingCount mappings parsed out of ${downloadedFile.absolutePath}, expected at least "
        + "$MINIMUM_EXPECTED_MAPPINGS -- refusing to run the load. If GO has genuinely shrunk the file, lower the floor deliberately.")
    System.exit(1)
}

givePubsPermissions = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql -v ON_ERROR_STOP=1 " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/insert_eco_go_map.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
givePubsPermissions.waitFor()
if (givePubsPermissions.exitValue() != 0) {
    // psql's stderr went to the log file, so surface it -- an unchecked exit value here made
    // a failed insert look like a successful load
    System.err.println("insert_eco_go_map.sql failed:")
    System.err.println(new File(WORKING_DIR, "loadSQLError.log").text)
    System.exit(1)
}
