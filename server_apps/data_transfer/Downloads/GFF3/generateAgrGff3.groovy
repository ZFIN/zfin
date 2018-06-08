#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.sql.Sql
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.sequence.GenomeFeature


ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")

String gff3Dir = ZfinPropertiesEnum.TARGETROOT.value + "/home/data_transfer/Downloads"

def out = new File("$gff3Dir/zfin_genes.gff3").newWriter()

List<GenomeFeature> genes = loadFile("$gff3Dir/E_zfin_gene_alias.gff3")
Map transcripts = loadFileWithParentMap("$gff3Dir/E_drerio_transcript.gff3")
Map exons = loadFileWithParentMap("$gff3Dir/E_drerio_constant.gff3")

List ensdargMaps = getEnsdargMaps()
Map ensdargToGene = ensdargMaps[0]
Map geneToEnsdarg = ensdargMaps[1]

printHeader("$gff3Dir/ensembl_contig.gff3", out)

genes.each { GenomeFeature gene ->

    String ensdarg = geneToEnsdarg.get(gene.id)

    if (ensdarg) {
        gene.addAttribute(GenomeFeature.ID,ensdarg)
        out.println gene
        transcripts.get(ensdarg).each { GenomeFeature transcript ->
            String ensdart = transcript.id

            //we only want transcripts that have a zdb_id in this file
            if (transcript.getAttributes().get(GenomeFeature.ZDB_ID) != null) {

                //replace the ENSDART id with a ZDB_ID
                transcript.addAttribute(GenomeFeature.ID, transcript.getAttributes().get(GenomeFeature.ZDB_ID))
                transcript.addAttribute(GenomeFeature.PARENT, gene.id)
                out.println transcript
                exons.get(ensdart).each { GenomeFeature exon ->
                    exon.addAttribute(GenomeFeature.ID, exon.id.replace(ensdart,transcript.id))
                    exon.addAttribute(GenomeFeature.PARENT, transcript.id)
                    out.println exon
                }
            }
        }
    }
}




def loadFile(String filename) {
    List features = []
    new File(filename).eachLine() {line ->
        if (!line.startsWith("#") && line.split("\\t").length == 9) {
            GenomeFeature feature = new GenomeFeature(line)
            features.add(feature)
        }

    }
    features
}

Map <String,List<GenomeFeature>> loadFileWithParentMap(String filename) {
    Map <String,List<GenomeFeature>> features = [:]
    new File(filename).eachLine() {line ->
        if (!line.startsWith("#") && line.split("\\t").length == 9) {
            GenomeFeature feature = new GenomeFeature(line)
            if (features.get(feature.parent)) {
                features.get(feature.parent).add(feature)
            } else {
                features.put(feature.parent, [feature])
            }
        }
    }
    features
}


def getEnsdargMaps() {

    Map ensdargToGene = [:]
    Map geneToEnsdarg = [:]

    String jdbc_driver = ZfinPropertiesEnum.JDBC_DRIVER.value()
    String jdbc_url = ZfinPropertiesEnum.JDBC_URL.value()

    def db = Sql.newInstance(jdbc_url, jdbc_driver)

    String sql = """
        select dblink_linked_recid as gene,
               dblink_acc_num as ensdarg
        from db_link
        where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
    """

    db.eachRow(sql) { row ->
        geneToEnsdarg.put(row.gene, row.ensdarg)
        ensdargToGene.put(row.ensdarg, row.gene)
    }

    [ensdargToGene, geneToEnsdarg]
}


def printHeader(String contigs, BufferedWriter out) {
    out.println "##gff-version\t3"

    def chromosomes = (1..25).collect { it.toString() }
    chromosomes.add("MT")

    new File(contigs).eachLine { line ->
        if (!line.startsWith("##gff-version")) {
            def fields = line.split("\\t")
            if (fields.size() == 4 && chromosomes.contains(fields[1])) {
                out.println line
            }
        }
    }

}