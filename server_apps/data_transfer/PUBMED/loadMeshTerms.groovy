#!/bin/bash
//private/apps/groovy/bin/groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.apache.commons.net.ftp.FTPClient
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

def download (ftp, filename, closure) {
  println("Downloading $filename")
  input = ftp.retrieveFileStream(filename)
  closure(input)
  input.close()
  if (!ftp.completePendingCommand()) {
    println("Error downloading file!")
    System.exit(1)
  }
}

def withMeshFtpSite (closure) {
  ftp = new FTPClient();
  ftp.connect("nlmpubs.nlm.nih.gov")
  ftp.enterLocalPassiveMode()
  ftp.login("anonymous", "zfinadmn@zfin.org")
  ftp.changeWorkingDirectory("/online/mesh/MESH_FILES/xmlmesh/")
  closure(ftp)
  ftp.disconnect()
}

def parse (InputStream inputStream) {
  parser = new XmlSlurper()
  parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
  parser.parse(inputStream)
}

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

OUTFILE = "mesh.unl"
PRE_FILE = "meshTermsPre.txt"
POST_FILE = "meshTermsPost.txt"

withMeshFtpSite { ftp ->
  new File(OUTFILE).withWriter { out ->
    names = ftp.listNames()
    descFile = names.find { it =~ /desc\d{4}.xml/ }
    if (!descFile) {
      println "Failed to find descYYYY.xml file! Listed files were: ${names.join(", ")}"
      System.exit(1);
    }
    download(ftp, descFile) { stream ->
      xml = parse(stream)
      records = xml.DescriptorRecord.each { record ->
        id = record.DescriptorUI.text()
        name = record.DescriptorName.String.text()
        out.writeLine("$id|$name|DESCRIPTOR")
      }
      println("Parsed ${records.size()} descriptor terms")
    }

    qualFile = names.find { it =~ /qual\d{4}.xml/ }
    if (!descFile) {
      println "Failed to find qualYYYY.xml file! Listed files were: ${names.join(", ")}"
      System.exit(1);
    }
    download(ftp, qualFile) { stream ->
      xml = parse(stream)
      records = xml.QualifierRecord.each { record ->
        id = record.QualifierUI.text()
        name = record.QualifierName.String.text()
        out.writeLine("$id|$name|QUALIFIER")
      }
      println("Parsed ${records.size()} qualifier terms")
    }
  }
}

dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")

dbaccess dbname, """
  UNLOAD TO $PRE_FILE
    SELECT mesht_mesh_id, mesht_term_name, mesht_type
    FROM mesh_term;

  CREATE TEMP TABLE tmp_terms(
    id varchar(10),
    name varchar(255),
    type varchar(30)
  ) with no log;

  LOAD FROM $OUTFILE
    INSERT INTO tmp_terms;

  DELETE FROM mesh_term
    WHERE mesht_mesh_id NOT IN (SELECT id FROM tmp_terms);

  MERGE INTO mesh_term USING tmp_terms ON mesh_term.mesht_mesh_id = tmp_terms.id
    WHEN MATCHED THEN
    UPDATE SET mesh_term.mesht_term_name = tmp_terms.name
    WHEN NOT MATCHED THEN
    INSERT (mesht_mesh_id, mesht_term_name, mesht_type) VALUES (id, name, type);

  UNLOAD TO $POST_FILE
    SELECT mesht_mesh_id, mesht_term_name, mesht_type
    FROM mesh_term;
"""

if (args) {
  // means we're (probably) running from Jenkins, so make a report.
  preLines = new File(PRE_FILE).readLines()
  postLines = new File(POST_FILE).readLines()

  added = postLines - preLines
  removed = preLines - postLines

  new ReportGenerator().with {
    setReportTitle("Report for ${args[0]}")
    includeTimestamp()
    addDataTable("${added.size()} terms added", ["ID", "Term", "Type"], added.collect { it.split("\\|") as List })
    addDataTable("${removed.size()} terms removed", ["ID", "Term", "Type"], removed.collect { it.split("\\|") as List })
    writeFiles(new File("."), "loadMeshTermsReport")
  }
}
