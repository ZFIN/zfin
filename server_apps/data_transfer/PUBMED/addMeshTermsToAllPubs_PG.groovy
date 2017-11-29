#!/bin/bash
//private/apps/groovy/bin/groovy -cp "/opt/zfin/www_homes/punkt/home/WEB-INF/lib*:/opt/zfin/source_roots/punkt/ZFIN_WWW/lib/Java/*:/opt/zfin/www_homes/punkt/home/WEB-INF/classes:/private/apps/tomcat/endorsed/*:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pubIdList.txt"
MESH_TO_LOAD = "meshHeadings.txt"
PUB_IDS_AFTER_LOAD = "pubIdListPost.txt"

PubmedUtils.dbaccess DBNAME, """
  \\copy (
  SELECT accession_no, zdb_id
  FROM publication
  WHERE accession_no IS NOT NULL
  AND zdb_id NOT IN (
    SELECT DISTINCT mh_pub_zdb_id
    FROM mesh_heading
  )  ) to '$PUB_IDS_TO_CHECK' delimiter '|';
"""

batchSize = 2000
count = 0
println("Fetching pubs from PubMed")
new File(MESH_TO_LOAD).withWriter { output ->
    new File(PUB_IDS_TO_CHECK).withReader { reader ->
        def lines = reader.iterator()
        while (lines.hasNext()) {
            ids = lines.take(batchSize).collect { it.split("\\|")[0] }
            articleSet = PubmedUtils.getFromPubmed(ids)
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

PubmedUtils.dbaccess DBNAME, """
 BEGIN WORK;

  CREATE TEMP TABLE tmp_mesh (
    pmid integer,
    descriptor_id VARCHAR(10),
    qualifier_id VARCHAR(10),
    is_major BOOLEAN
  );

  \\copy tmp_mesh FROM '$MESH_TO_LOAD' delimiter '|';

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

  \\copy (
    SELECT accession_no, zdb_id
    FROM publication
    WHERE accession_no IS NOT NULL
    AND zdb_id NOT IN (
      SELECT DISTINCT mh_pub_zdb_id
      FROM mesh_heading
    ) ) to '$PUB_IDS_AFTER_LOAD' delimiter '|';

  COMMIT WORK;
"""

if (args) {
    // means we're (probably) running from Jenkins, so make a report.
    preLines = new File(PUB_IDS_TO_CHECK).collect { it.split("\\|") as List }
    postLines = new File(PUB_IDS_AFTER_LOAD).collect { it.split("\\|") as List }
    added = preLines - postLines
    new ReportGenerator().with {
        setReportTitle("Report for ${args[0]}")
        includeTimestamp()
        addDataTable("Added terms to ${added.size()} pubs", ["PubMed ID", "ZDB ID"], added)
        writeFiles(new File("."), "addMeshTermsReport")
    }
}

new File(PUB_IDS_TO_CHECK).delete()
new File(MESH_TO_LOAD).delete()
new File(PUB_IDS_AFTER_LOAD).delete()
