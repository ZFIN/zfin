#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "https://raw.githubusercontent.com/evidenceontology/evidenceontology/master/gaf-eco-mapping.txt"
final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/eco_go_mapping")


def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

File inputFile = new File("gaf-eco-mapping.txt")
OUTFILE = "gafeco.txt"


new File(OUTFILE).withWriter { outFile ->
    inputFile.withReader {
        reader ->
            while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        evidence_code = line.split()[0]
                        eco_term = line.split()[2]
                        println(evidence_code + " " + eco_term)
                        outFile.writeLine("$evidence_code,$eco_term")
                    }
            }
    }
}

givePubsPermissions = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/insert_eco_go_map.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
givePubsPermissions.waitFor()
