#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.sql.Sql
import org.apache.commons.net.ftp.FTPClient

def env = System.getenv()

def db = [
        url: "jdbc:informix-sqli://${env.SQLHOSTS_HOST}:${env.INFORMIX_PORT}/${env.DBNAME}:INFORMIXSERVER=${env.INFORMIXSERVER}",
        driver: 'com.informix.jdbc.IfxDriver'
]

Sql.withInstance(db) { sql ->
  new File("publication.uid").withWriter { out ->
    sql.eachRow("SELECT accession_no FROM publication WHERE accession_no IS NOT NULL") { row ->
      out.writeLine(row.accession_no as String)
    }
  }


client = new FTPClient()
client.connect("ftp-private.ncbi.nlm.nih.gov")
client.enterLocalPassiveMode()
client.login("zfin", "TeDZUG3E")
println(client.listFiles())