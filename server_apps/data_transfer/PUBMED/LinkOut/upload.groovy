#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.sql.Sql
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply

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

def env = System.getenv()
def client = new FTPClient()
def db = [
        url: "jdbc:informix-sqli://${env.SQLHOSTS_HOST}:${env.INFORMIX_PORT}/${env.DBNAME}:INFORMIXSERVER=${env.INFORMIXSERVER}",
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

// connect to NCBI FTP site and get into "holdings" directory
client.connect("ftp-private.ncbi.nlm.nih.gov")
if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
    fail("unable to connect to NCBI FTP server")
}
client.enterLocalPassiveMode()
client.login("zfin", "TeDZUG3E") || fail("unable to login to NCBI FTP server")
client.changeWorkingDirectory("holdings") || fail("unable to change directories")

// do the pub query and upload results
def pubList = client.storeFileStream(PUB_FILE)
Sql.withInstance(db) { sql ->
    sql.eachRow(PUB_QUERY) { row ->
        pubList.write((row.getString("accession_no") + "\n").bytes)
    }
}
pubList.close()
client.completePendingCommand() || fail("unable to upload " + PUB_FILE)

// upload static files
upload(INFO_FILE)
upload(RESOURCES_FILE)

client.logout()
client.disconnect()
