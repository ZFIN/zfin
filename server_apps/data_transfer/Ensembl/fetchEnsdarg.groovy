//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

String queryXml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE Query>
<Query  virtualSchemaName = "default" formatter = "CSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" >
<Dataset name = "drerio_gene_ensembl" interface = "default" >
<Filter name = "chromosome_name" value = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,MT"/>
<Filter name = "with_zfin_id" excluded = "0"/>
<Attribute name = "ensembl_gene_id" />
<Attribute name = "zfin_id_id" />
</Dataset>
</Query>"""

String encodedQuery = java.net.URLEncoder.encode(queryXml, "UTF-8")
String url = "http://www.ensembl.org/biomart/martservice?query=" + encodedQuery

Map<String,String> ensdargZdbMap = [:]
List<String> zdbList = []

url.toURL().text.readLines().each { line ->
    String ensdarg = line.split(",")[0]
    String zdbId = line.split(",")[1]

    ensdargZdbMap[ensdarg] = zdbId
    zdbList.add(zdbId)
}

ensdargZdbMap.keySet().each { ensdarg ->
    String zdbId = ensdargZdbMap[ensdarg]
    if (zdbList.count {it == zdbId} == 1) {
        println zdbId + "," + ensdarg
    }
}