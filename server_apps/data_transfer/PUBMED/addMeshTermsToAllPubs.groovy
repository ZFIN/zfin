#!/usr/bin/env groovy

import groovy.util.slurpersupport.GPathResult

def dbaccess (String dbname, String sql) {
    proc = "dbaccess -a $dbname".execute()
    proc.getOutputStream().with {
        write(sql.bytes)
        close()
    }
    proc.waitFor()
    proc.getErrorStream().eachLine { println(it) }
    if (proc.exitValue()) {
        throw new RuntimeException("dbaccess call failed")
    }
    proc
}

GPathResult getFromPubmed(List ids) {
    // pubmed doc says "if more than about 200 UIDs are to be provided, the request should be
    // made using the HTTP POST method" ... okay pubmed, you're such a good guy, we'll play
    // by your rules
    def url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
    def query = "db=pubmed&id=${ids.join(",")}&retmode=xml"
    def connection = new URL(url).openConnection()
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    def writer = new OutputStreamWriter(connection.outputStream)
    writer.write(query)
    writer.flush()
    writer.close()
    connection.connect()
    new XmlSlurper().parse(connection.inputStream)
}

DBNAME = System.getenv("DBNAME")
PUBMED_IDS_TO_CHECK = "pubmedIdList.txt"
MESH_TO_LOAD = "meshHeadings.txt"

dbaccess DBNAME, """
  UNLOAD TO $PUBMED_IDS_TO_CHECK
  SELECT accession_no
  FROM publication
  WHERE accession_no IS NOT NULL
  AND zdb_id NOT IN (
    SELECT DISTINCT mh_pub_zdb_id
    FROM mesh_heading
  );
"""

batchSize = 2000
count = 0
println("Fetching pubs from PubMed")
new File(MESH_TO_LOAD).withWriter { output ->
    new File(PUBMED_IDS_TO_CHECK).withReader { reader ->
        def lines = reader.iterator()
        while (lines.hasNext()) {
            ids = lines.take(batchSize).collect { it.split("\\|")[0] }
            articleSet = getFromPubmed(ids)
            count += articleSet.PubmedArticle.size()
            articleSet.PubmedArticle.each { article ->
                id = article.MedlineCitation.PMID
                article.MedlineCitation.MeshHeadingList.MeshHeading.each { heading ->
                    descriptor = heading.DescriptorName
                    output.writeLine([id, descriptor.@UI, "", descriptor.@MajorTopicYN == "Y" ? "t" : "f"].join("|"))
                    heading.QualifierName.each { qualifier ->
                        output.writeLine([id, descriptor.@UI, qualifier.@UI, qualifier.@MajorTopicYN == "Y" ? "t" : "f"].join("|"))
                    }
                }
            }
            println("Fetched $count pubs")
        }
    }
}

dbaccess DBNAME, """
  BEGIN WORK;

  CREATE TEMP TABLE tmp_mesh (
    pmid VARCHAR(30),
    descriptor_id VARCHAR(10),
    qualifier_id VARCHAR(10),
    is_major BOOLEAN
  ) WITH NO LOG;

  LOAD FROM $MESH_TO_LOAD
    INSERT INTO tmp_mesh;

  INSERT INTO mesh_heading (mh_pub_zdb_id, mh_mesht_mesh_descriptor_id, mh_descriptor_is_major_topic)
    SELECT DISTINCT publication.zdb_id, tmp_mesh.descriptor_id, tmp_mesh.is_major
    FROM publication
    INNER JOIN tmp_mesh ON publication.accession_no = tmp_mesh.pmid
    WHERE tmp_mesh.qualifier_id IS NULL;

  INSERT INTO mesh_heading_qualifier (mhq_mesh_heading_id, mhq_mesht_mesh_qualifier_id, mhq_is_major_topic)
    SELECT mesh_heading.mh_pk_id, tmp_mesh.qualifier_id, tmp_mesh.is_major
    FROM tmp_mesh
    INNER JOIN mesh_heading ON tmp_mesh.descriptor_id = mesh_heading.mh_mesht_mesh_descriptor_id
    INNER JOIN publication ON tmp_mesh.pmid = publication.accession_no AND mesh_heading.mh_pub_zdb_id = publication.zdb_id
    WHERE tmp_mesh.qualifier_id IS NOT NULL;

  COMMIT WORK;
"""

new File(PUBMED_IDS_TO_CHECK).delete()
new File(MESH_TO_LOAD).delete()
