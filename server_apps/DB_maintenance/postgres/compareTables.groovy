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

def informixDb = [
        url: "${ZfinPropertiesEnum.JDBC_URL_INFORMIX}",
        driver: "${ZfinPropertiesEnum.JDBC_DRIVER_INFORMIX}"
]

Sql.withInstance(postgresDb) { Sql sql ->
    sql.eachRow("select table_name from information_schema.tables where table_name not like 'pg_%' and table_name not like 'sql_%' and is_insertable_into = 'YES'") { row ->
        print("tableName: " + $row.table_name)

        sql.eachRow("select count(*) as counter from " + $row.table_name) {
            print("postgresCount: " + $row.counter)
        }

        Sql.withInstance(informixDb) { Sql informixSql ->
            sql.eachRow('select count(*) as ifxCounter from ' + $row.table_name) { informixRow ->
                print ("informixCount: " + $informixRow.ifxCounter)
            }
        }
    }
}