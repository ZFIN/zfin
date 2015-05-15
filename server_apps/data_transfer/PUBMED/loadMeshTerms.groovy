#!/usr/bin/env groovy

import java.util.zip.GZIPInputStream

def download (url, closure) {
  println("Downloading $url")
  url.toURL().withInputStream { closure(it) }
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

new File(OUTFILE).withWriter { out ->

  download("ftp://nlmpubs.nlm.nih.gov/online/mesh/.xmlmesh/desc2015.gz") { stream ->
    xml = parse(new GZIPInputStream(stream))
    records = xml.DescriptorRecord.each { record ->
      id = record.DescriptorUI.text()
      name = record.DescriptorName.String.text()
      out.writeLine("$id|$name|DESCRIPTOR")
    }
    println("Parsed ${records.size()} descriptor terms")
  }

  download("ftp://nlmpubs.nlm.nih.gov/online/mesh/.xmlmesh/qual2015.xml") { stream ->
    xml = parse(stream)
    records = xml.QualifierRecord.each { record ->
      id = record.QualifierUI.text()
      name = record.QualifierName.String.text()
      out.writeLine("$id|$name|QUALIFIER")
    }
    println("Parsed ${records.size()} qualifier terms")
  }

}

dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")

dbaccess dbname, """

  CREATE TEMP TABLE tmp_terms(
    id varchar(10),
    name varchar(255),
    type varchar(30)
  ) with no log;

  LOAD FROM $OUTFILE
    INSERT INTO tmp_terms;

  INSERT INTO mesh_term (mesht_mesh_id, mesht_term_name, mesht_type)
    SELECT id, name, type
    FROM tmp_terms
    WHERE NOT EXISTS (SELECT 'x' FROM mesh_term WHERE mesht_mesh_id = id);

"""
