#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

def targetroot = System.getenv('TARGETROOT')
ZfinProperties.init("$targetroot/home/WEB-INF/zfin.properties")

def ignoreList = [
        ['ZDB-MRPHLNO-041110-3'],
        ['ZDB-MRPHLNO-041208-2'],
        ['ZDB-MRPHLNO-050208-7'],
        ['ZDB-MRPHLNO-050209-8'],
        ['ZDB-MRPHLNO-050308-8'],
        ['ZDB-MRPHLNO-050310-1'],
        ['ZDB-MRPHLNO-050420-3'],
        ['ZDB-MRPHLNO-050531-5'],
        ['ZDB-MRPHLNO-050715-2'],
        ['ZDB-MRPHLNO-050722-3'],
        ['ZDB-MRPHLNO-050725-1'],
        ['ZDB-MRPHLNO-050809-7'],
        ['ZDB-MRPHLNO-050902-1'],
        ['ZDB-MRPHLNO-051004-1'],
        ['ZDB-MRPHLNO-051024-1'],
        ['ZDB-MRPHLNO-060111-1'],
        ['ZDB-MRPHLNO-060130-10'],
        ['ZDB-MRPHLNO-060206-1'],
        ['ZDB-MRPHLNO-060301-5'],
        ['ZDB-MRPHLNO-060505-1'],
        ['ZDB-MRPHLNO-060519-1'],
        ['ZDB-MRPHLNO-060524-8'],
        ['ZDB-MRPHLNO-060601-6'],
        ['ZDB-MRPHLNO-060817-3'],
        ['ZDB-MRPHLNO-060918-4'],
        ['ZDB-MRPHLNO-060919-2'],
        ['ZDB-MRPHLNO-061122-2'],
        ['ZDB-MRPHLNO-061122-5'],
        ['ZDB-MRPHLNO-061206-1'],
        ['ZDB-MRPHLNO-061208-4'],
        ['ZDB-MRPHLNO-061218-1']
]

// nothing really to run here. just need to pick up the result from the latest GFF run and report it.
def logfile = "$targetroot/server_apps/data_transfer/Downloads/GFF3/mo_seq_E_miss.fa"
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
    addDataTable("${ids.size()} MOs which bowtie couldn't align", ["MO ZDB ID"], ids)
    writeFiles(new File("."), "moBowtieReport")
}
