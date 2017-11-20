#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

def targetroot = System.getenv('TARGETROOT')
ZfinProperties.init("$targetroot/home/WEB-INF/zfin.properties")

def ignoreList = [
         ['ZDB-CRISPR-150924-1'],
         ['ZDB-CRISPR-150925-1'],
         ['ZDB-CRISPR-150924-3'],
         ['ZDB-CRISPR-160128-304'],
         ['ZDB-CRISPR-160728-1'],
         ['ZDB-CRISPR-160128-200'],
         ['ZDB-CRISPR-160128-241'],
         ['ZDB-CRISPR-140710-4'],
         ['ZDB-CRISPR-140811-12'],
         ['ZDB-CRISPR-160128-272'],
         ['ZDB-CRISPR-160128-270']
]

// nothing really to run here. just need to pick up the result from the latest GFF run and report it.
def logfile = "$targetroot/server_apps/data_transfer/Downloads/GFF3/crispr_seq_E_miss.fa"
def ids = new File(logfile)
        .collect { it =~ /zdb_id=([^;]+);/ }
        .findAll { it }
        .collect { [it.group(1)] }
        .unique()
ids.removeAll(ignoreList)

if (!ids?.empty) {
    println("Validation Errors found")
}

new ReportGenerator().with {
    setReportTitle("Report for ${args[0]}")
    includeTimestamp()
    addDataTable("${ids.size()} CRISPRs which bowtie couldn't align", ["CRISPR ZDB ID"], ids)
    writeFiles(new File("."), "crisprBowtieReport")
}
