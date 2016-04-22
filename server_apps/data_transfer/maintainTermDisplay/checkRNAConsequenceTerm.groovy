#!/bin/bash
//usr/bin/env groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import groovy.sql.Sql
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.util.ReportGenerator

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

def db = [
        url: "jdbc:informix-sqli://${ZfinPropertiesEnum.SQLHOSTS_HOST}:${ZfinPropertiesEnum.INFORMIX_PORT}/${ZfinPropertiesEnum.DBNAME}:INFORMIXSERVER=${ZfinPropertiesEnum.INFORMIXSERVER}",
        driver: 'com.informix.jdbc.IfxDriver'
]

def termsObsoletedRows = []


Sql.withInstance(db) { Sql sql ->

    def termsObsoleted = sql.rows("select mdcv_term_zdb_id, term_ont_id, term_name, mdcv_term_display_name from mutation_detail_controlled_vocabulary, term where term_zdb_id = mdcv_term_zdb_id and (term_is_obsolete = 't' or term_is_secondary = 't')")

    termsObsoleted.each {row ->
	
	termsObsoletedRows.add([row.rct_term_zdb_id, row.term_ont_id, row.term_name, row.rct_term_display_name])
    }
    println("Fetching ${termsObsoleted.size()} obsolete rna_consequence terms.")
    
}

def report = new ReportGenerator()
if (args) {
    report.setReportTitle("Report for ${args[0]}")
}
report.includeTimestamp()
report.addDataTable("Mutation Detail Consequence Terms obsoleted or secondary ${termsObsoletedRows.size()} terms",
       ["Term Zdb ID ", "Term Ontology ID", "Term Name", "Curator Defined Term Display Name"], termsObsoletedRows)
report.writeFiles(new File("."), "termsObsoletedReport")
