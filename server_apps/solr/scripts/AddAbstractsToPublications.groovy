#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient
import org.apache.solr.common.SolrInputDocument
import groovy.sql.Sql
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.properties.ZfinProperties

ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")

String jdbc_driver = ZfinPropertiesEnum.JDBC_DRIVER.value()
String jdbc_url = ZfinPropertiesEnum.JDBC_URL.value()

def sql = Sql.newInstance(jdbc_url, jdbc_driver)

def solrPort = System.env.get("SOLR_PORT")
SolrClient client = new ConcurrentUpdateSolrClient("http://localhost:$solrPort/solr/prototype", 100, 20)

Map<String, String> abstractMap = new HashMap<>()

pubQuery = """
    select zdb_id as id,
           pub_abstract
    from publication
"""

println "building documents"

sql.withStatement { statement ->
    statement.fetchSize = 1
}

sql.query(pubQuery) { rs ->
    while (rs.next()) {
        String id = rs.getString("id")
        String abstractText = rs.getString("pub_abstract")

        SolrInputDocument doc = new SolrInputDocument();
        Map<String, String> partialUpdate = new HashMap<String, String>();
        partialUpdate.put("set", abstractText);
        doc.addField("id", id);
        doc.addField("abstract", partialUpdate);
        client.add(doc)
    }
}

client.commit()
