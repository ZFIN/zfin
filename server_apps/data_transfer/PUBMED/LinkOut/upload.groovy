#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.sql.Sql
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

def PUB_FILE = "publications.uid"
def INFO_FILE = "providerinfo.xml"
def RESOURCES_FILE = "resources.xml"

// get pubmed ids for pubs that have some curated data
def PUB_QUERY = """
SELECT accession_no
FROM publication
WHERE accession_no IS NOT NULL
AND (
  EXISTS (
    SELECT 't'
    FROM record_attribution
    WHERE recattrib_source_zdb_id = zdb_id
    AND recattrib_data_zdb_id NOT LIKE 'ZDB-FIG-%'
    AND recattrib_data_zdb_id NOT LIKE 'ZDB-IMAGE-%'
  )
  OR
  EXISTS (
    SELECT 't'
    FROM ortholog_evidence
    WHERE oev_pub_zdb_id = zdb_id
  )
)
ORDER BY accession_no
"""

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

def client = new FTPClient()
def db = [
        url: "jdbc:informix-sqli://${ZfinPropertiesEnum.SQLHOSTS_HOST}:${ZfinPropertiesEnum.INFORMIX_PORT}/${ZfinPropertiesEnum.DBNAME}:INFORMIXSERVER=${ZfinPropertiesEnum.INFORMIXSERVER}",
        driver: 'com.informix.jdbc.IfxDriver'
]

def fail = { String message ->
    if (client.isConnected()) {
        client.logout()
    }
    client.disconnect()
    System.err.println(message)
    System.exit(1)
}

def upload = { String filename ->
    new File(filename).withInputStream { file ->
        client.storeFile(filename, file) || fail("unable to upload " + filename)
    }
}

// do the pub query and write to file
new File(PUB_FILE).withWriter { out ->
    Sql.withInstance(db) { sql ->
        sql.eachRow(PUB_QUERY) { row ->
            out.writeLine(row.accession_no as String)
        }
    }
}

if (!Boolean.valueOf(ZfinPropertiesEnum.NCBI_LINKOUT_UPLOAD.toString())) {
    println("Not uploading to NCBI FTP because NCBI_LINKOUT_UPLOAD not set")
    System.exit(0)
}

// connect to NCBI FTP site and get into "holdings" directory
client.connect("ftp-private.ncbi.nlm.nih.gov")
if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
    fail("unable to connect to NCBI FTP server")
}
client.enterLocalPassiveMode()
client.login("zfin", "TeDZUG3E") || fail("unable to login to NCBI FTP server")
client.changeWorkingDirectory("holdings") || fail("unable to change directories")

// upload files
upload(PUB_FILE)
upload(INFO_FILE)
upload(RESOURCES_FILE)

client.logout()
client.disconnect()
