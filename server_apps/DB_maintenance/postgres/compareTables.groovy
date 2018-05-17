#!/bin/bash
//usr/bin/env groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import groovy.sql.Sql
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

def postgresDb = [
        url: "${ZfinPropertiesEnum.JDBC_URL}",
        driver: "${ZfinPropertiesEnum.JDBC_DRIVER}"
]

def postgresSql = Sql.newInstance(postgresDb.url, postgresDb.driver)

def informixDb = [
        url: "${ZfinPropertiesEnum.JDBC_URL_INFORMIX}",
        driver: "${ZfinPropertiesEnum.JDBC_DRIVER_INFORMIX}"
]

def informixSql = Sql.newInstance(informixDb.url, informixDb.driver)


postgresSql.eachRow ("select * from information_schema.tables where table_name not like 'pg_%' and table_name not like 'sql_%' and is_insertable_into = 'YES' order by table_name") { row ->
        //print ("tableName: " +"$row.table_name" )
        def tableName = "${row.table_name}"
       //print ("table: " + tableName.toUpperCase().padRight(10) + " ")

        postgresSql.eachRow('select count(*) as counter from ' + tableName) { pgCounter ->
                pgTableCount = "$pgCounter.counter"
                //print("pg: " + "$pgCounter.counter" +" ")
        }
        informixSql.eachRow('select count(*) as ifxCounter from ' + tableName) { informixRow ->
                ifxTableCount = "$informixRow.ifxCounter"
                //print("$informixRow.ifxCounter" + " :informix")
        }
        //print "\n"
        if (pgTableCount != ifxTableCount) {
                println("ERROR: Table count mismatch " + "tableName: "+ tableName.toUpperCase()+ " " + "ifx: " + ifxTableCount + " " + pgTableCount + " :pg")
        }

}