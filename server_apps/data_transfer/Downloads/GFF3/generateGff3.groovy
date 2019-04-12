//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?


import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.zfin.infrastructure.ant.RunSQLFiles
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.sequence.GenomeFeature

import java.util.stream.Collectors

Logger log = Logger.getLogger(getClass());

ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")
def db = Sql.newInstance(ZfinPropertiesEnum.JDBC_URL.value(), ZfinPropertiesEnum.JDBC_DRIVER.value())
String targetRoot = ZfinPropertiesEnum.TARGETROOT.value
def downloadDir = "$targetRoot/home/data_transfer/Downloads/"

println 'Start generating GFF3 download files...'

def propertiesFile = "$targetRoot/home/WEB-INF/zfin.properties"
RunSQLFiles runScriptFiles = new RunSQLFiles("Generate-GFF3", propertiesFile, ".")
runScriptFiles.initializeLogger("./log4j.xml")
runScriptFiles.initDatabaseWithoutSysmaster()
runScriptFiles.setQueryFiles(
        "zfin_tginsertion_gff3.sql",
        "zfin_zmp_gff3.sql",
        "E_zfin_ensembl_gene.sql",
        "E_expression_gff3.sql",
        "E_phenotype_gff3.sql",
        "E_antibody_gff3.sql"
)
runScriptFiles.execute()


def contigFile = new File('/research/zprodmore/gff3/ensembl_contig.gff3')
def destination = new File(downloadDir + "ensembl_contig.gff3")
//.text method writes the entire content of the file, contigFile, to the new destination.
destination.write(contigFile.text)

def knockdownFile = new File("$targetRoot/server_apps/data_transfer/Downloads/GFF3/knockdown_reagents/E_zfin_knockdown_reagents.gff3")
destination = new File(downloadDir + "E_zfin_knockdown_reagents.gff3")
//.text method writes the entire content of the file, knockdownFile, to the new destination.
destination.write(knockdownFile.text)

generateGenesAndTranscripts()

// copy files into download directory
File dir = new File(".")
println dir.getAbsolutePath()
File[] files = dir.listFiles(new FileFilter() {
    @Override
    boolean accept(File pathname) {
        return pathname.getName().endsWith(".gff3")
    }
})
files.each {file ->
    destination = new File(downloadDir + file.getName())
    destination.write(file.text)
    file.delete()
}



println 'Finished generating GFF3 download files'
System.exit(0)


def generateGenesAndTranscripts() {
    String gff3Dir = ZfinPropertiesEnum.TARGETROOT.value + "/home/data_transfer/Downloads"
    def db = Sql.newInstance(ZfinPropertiesEnum.JDBC_URL.value(), ZfinPropertiesEnum.JDBC_DRIVER.value())

    List<GenomeFeature> genes = []
    db.eachRow("""
    select zeg_seqname, zeg_source, zeg_feature, zeg_start, zeg_end,
    zeg_score, zeg_strand, zeg_frame, 
    zeg_id_name, zeg_gene_zdb_id
    from zfin_ensembl_gene
    order by zeg_seqname asc, zeg_start asc """) { row ->
        String gff = row.zeg_seqname + "\t" +
                row.zeg_source + "\t" +
                row.zeg_feature + "\t" +
                row.zeg_start + "\t" +
                row.zeg_end + "\t" +
                row.zeg_score + "\t" +
                row.zeg_strand + "\t" +
                row.zeg_frame + "\t"
        gff += "ID=" + row.zeg_gene_zdb_id + ";" + row.zeg_id_name

        genes.add(new GenomeFeature(gff))
    }


    Map<String,String> ensemblToZfinIDMap = [:]
    Map<String,String> zfinToEnsemblIDMap = [:]

    db.eachRow(""" 
    select tscript_ensdart_id, tscript_mrkr_zdb_id
    from transcript where tscript_ensdart_id is not null """) { row ->
        ensemblToZfinIDMap[row.tscript_ensdart_id] = row.tscript_mrkr_zdb_id
        zfinToEnsemblIDMap[row.tscript_mrkr_zdb_id] = row.tscript_ensdart_id
    }

    db.eachRow("""
    select dblink_acc_num, dblink_linked_recid from db_link where dblink_acc_num like 'ENSDARG%' """) { row ->
        ensemblToZfinIDMap[row.dblink_acc_num] = row.dblink_linked_recid;
    }

    Map<String,String> ensemblToZfinNameMap = [:]
    db.eachRow("""
    select tscript_ensdart_id, mrkr_abbrev
    from transcript join marker on tscript_mrkr_zdb_id = mrkr_zdb_id """) { row ->
        ensemblToZfinNameMap[row.tscript_ensdart_id, row.mrkr_abbrev]
    }

    Map <String,List<GenomeFeature>> ensemblFeatureMap = [:]
    db.eachRow("""
        select 
        gff_seqname, gff_source, gff_feature, gff_start, gff_end, gff_score,
        gff_strand, gff_frame, gff_id, gff_name, gff_parent
        from gff3
        where gff_source like 'Ensembl_%' """) { row ->
        String gff = row.gff_seqname + "\t" +
                row.gff_source + "\t" +
                row.gff_feature + "\t" +
                row.gff_start + "\t" +
                row.gff_end + "\t" +
                row.gff_score + "\t" +
                row.gff_strand + "\t" +
                row.gff_frame + "\t"

        //need zdb_id for self & parent on transcripts
        String parent = ensemblToZfinIDMap[row.gff_parent] ?: row.gff_parent
        String id = ensemblToZfinIDMap[row.gff_id] ?: row.gff_id
        String name = ensemblToZfinNameMap[row.gff_id] ?: row.gff_name

        gff += "ID=$id;Name=$name;Parent=$parent"

        if (ensemblFeatureMap[parent]) {
            ensemblFeatureMap[parent].add(new GenomeFeature(gff))
        } else {
            ensemblFeatureMap[parent] = [new GenomeFeature(gff)]
        }

    }

    File genesWithoutTranscriptsFile = new File("$gff3Dir/genes_without_transcripts.gff3")
    genesWithoutTranscriptsFile.createNewFile()
    def genesWithoutTranscriptsWriter = genesWithoutTranscriptsFile.newWriter()

    File zfinGenesFile = new File("$gff3Dir/zfin_genes.gff3")
    zfinGenesFile.createNewFile()
    def zfinGenesWriter = zfinGenesFile.newWriter()

    printHeader("$gff3Dir/ensembl_contig.gff3", zfinGenesWriter)

    genes.each { GenomeFeature gene ->

        def zfinTranscripts = []
        def otherTranscripts = []
        ensemblFeatureMap[gene.getId()]?.each { transcript ->
            if (StringUtils.startsWith(transcript.getId(), "ZDB")) {
                zfinTranscripts.add(transcript)
            } else {
                otherTranscripts.add(transcript)
            }
        }

        if (zfinTranscripts.size() > 0) {
            zfinGenesWriter.println gene.toString()
            zfinTranscripts.each { transcript ->
                transcript.setSource("ZFIN")
                zfinGenesWriter.println(transcript.toString())
                ensemblFeatureMap[transcript.getId()].each { GenomeFeature region ->
                    region.setSource("ZFIN")
                    zfinGenesWriter.println(region)
                }
            }
        } else {
            //capture the ZFIN genes which have no ZFIN transcripts
            gene.setSource("ZFIN_genes_without_transcripts")
            genesWithoutTranscriptsWriter.println(gene.toString())
        }

        //todo: what do I do with transcrips of a gene with a ZDB_ID that only have an ENSDART ?
    }

    zfinGenesWriter.flush()
    zfinGenesWriter.close()

    genesWithoutTranscriptsWriter.flush()
    genesWithoutTranscriptsWriter.close()

    File ensemblTranscriptsFile = new File("$gff3Dir/additional_transcripts.gff3")
    ensemblTranscriptsFile.createNewFile()
    def ensemblTranscriptsWriter = ensemblTranscriptsFile.newWriter()

    ensemblFeatureMap.keySet().each { String parent ->
        ensemblFeatureMap[parent].each { GenomeFeature transcript ->
            if (transcript.getId()?.startsWith("ENSDART")) { //loop over just the ENSDARTS
                transcript.getAttributes().remove(GenomeFeature.PARENT) //leave parents off of these transcripts
                transcript.setSource("Ensembl")
                ensemblTranscriptsWriter.println transcript
                ensemblFeatureMap[transcript.getId()].each { GenomeFeature region -> //then print each child of ENSDART
                    region.setSource("Ensembl")
                    ensemblTranscriptsWriter.println region
                }
            }

        }
    }

    ensemblTranscriptsWriter.flush()
    ensemblTranscriptsWriter.close()


}

def printHeader(String contigs, BufferedWriter out) {
    out.println("# Genome build: GRCz11")
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

