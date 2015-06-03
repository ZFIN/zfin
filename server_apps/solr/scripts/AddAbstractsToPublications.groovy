#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrResponse
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrInputDocument

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

def dbname = System.getenv('DBNAME')
def sqlhost = System.getenv('SQLHOSTS_HOST')
def port = System.getenv('INFORMIX_PORT')
def informixServer = System.getenv('INFORMIXSERVER')


args = [driver: 'com.informix.jdbc.IfxDriver',
        url: "jdbc:informix-sqli://$sqlhost:$port/$dbname:INFORMIXSERVER=$informixServer",
        user: 'zfinner',
        password: 'Rtwm4ts'
]

Class.forName("com.informix.jdbc.IfxDriver")

Connection conn = null
Properties connectionProps = new Properties()
connectionProps.put("user", args.user)
connectionProps.put("password", args.password)
connectionProps.put("driver", args.driver)

conn = DriverManager.getConnection(args.url, connectionProps)

def solrPort = System.env.get("SOLR_PORT")
//SolrServer server = new HttpSolrServer("http://localhost:$solrPort/solr/prototype")
SolrServer server = new ConcurrentUpdateSolrServer("http://localhost:$solrPort/solr/prototype", 100, 20)


Map<String, String> abstractMap = new HashMap<>()

pubQuery = """
    select zdb_id as id,
           pub_abstract
    from publication
"""

Statement statement = conn.createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY)
statement.setFetchSize(1);
ResultSet rs = statement.executeQuery(pubQuery);
println "building documents"
while (rs.next()) {
        String id = rs.getString("id")
        String abstractText = rs.getString("pub_abstract")

        SolrInputDocument doc = new SolrInputDocument();
        Map<String, String> partialUpdate = new HashMap<String, String>();
        partialUpdate.put("set", abstractText);
        doc.addField("id", id);
        doc.addField("abstract", partialUpdate);
        server.add(doc)

        print "."

}

server.commit()
