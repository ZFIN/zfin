#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")
DOWNLOAD_URL = "https://raw.githubusercontent.com/evidenceontology/evidenceontology/master/gaf-eco-mapping.txt"
final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/eco_go_mapping")
WORKING_DIR.mkdirs()

// both files have to land in WORKING_DIR: ant runs this script with its working directory in
// SOURCEROOT, but insert_eco_go_map.sql \copy's gafeco.txt out of TARGETROOT
File inputFile = new File(WORKING_DIR, DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(new FileOutputStream(inputFile))
out << new URL(DOWNLOAD_URL).openStream()
out.close()

File outputFile = new File(WORKING_DIR, "gafeco.txt")

mappingCount = 0
outputFile.withWriter { outFile ->
    inputFile.withReader {
        reader ->
            while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        evidence_code = line.split()[0]
                        eco_term = line.split()[2]
                        println(evidence_code + " " + eco_term)
                        outFile.writeLine("$evidence_code,$eco_term")
                        mappingCount++
                    }
            }
    }
}

// bail out rather than \copy an empty file into a load that would then report success:
// a truncated or error-page download has to be a failure, not a no-op
if (mappingCount == 0) {
    System.err.println("No mappings parsed out of ${inputFile.absolutePath} -- refusing to run the load")
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
