#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

def targetroot = System.getenv('TARGETROOT')
ZfinProperties.init("$targetroot/home/WEB-INF/zfin.properties")

// nothing really to run here. just need to pick up the result from the latest GFF run and report it.
def logfile = "$targetroot/server_apps/data_transfer/Downloads/GFF3/talen_seq_E_miss.fa"
def ids = new File(logfile)
        .collect { it =~ /zdb_id=([^;]+);/ }
        .findAll { it }
        .collect { [it.group(1)] }
        .unique()

if (!ids?.empty) {
    println("Validation Errors found")
}

new ReportGenerator().with {
    setReportTitle("Report for ${args[0]}")
    includeTimestamp()
    addDataTable("${ids.size()} TALENS which bowtie couldn't align", ["TALEN ZDB ID"], ids)
    writeFiles(new File("."), "talenBowtieReport")
}