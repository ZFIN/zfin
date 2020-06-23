//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?


import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.zfin.infrastructure.ant.RunSQLFiles
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.sequence.GenomeFeature

import java.util.stream.Collectors

Logger log = Logger.getLogger(getClass())

ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")
def db = Sql.newInstance(ZfinPropertiesEnum.JDBC_URL.value(), ZfinPropertiesEnum.JDBC_DRIVER.value())
String targetRoot = ZfinPropertiesEnum.TARGETROOT.value
def downloadDir = "$targetRoot/home/data_transfer/Downloads/"

println 'Start generating GFF3 download files...'

def propertiesFile = "$targetRoot/home/WEB-INF/zfin.properties"
RunSQLFiles runScriptFiles = new RunSQLFiles("Generate-GFF3", propertiesFile, ".")
runScriptFiles.initializeLogger("./log4j.xml")
runScriptFiles.initDatabase()
runScriptFiles.setQueryFiles(
        "E_zfin_ensembl_gene.sql",
        "E_expression_gff3.sql",
        "E_phenotype_gff3.sql",
        "E_antibody_gff3.sql",
        "unload_mutants.sql"
)
runScriptFiles.execute()


def contigFile = new File('/research/zprodmore/gff3/zfin_genes_header.gff3')
def destination = new File(downloadDir + "zfin_genes_header.gff3")
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

    Map<String,String> zfinToVegaIDMap = [:]
    Map<String,String> ensemblToZfinIDMap = [:]
    Map<String,String> zfinToEnsemblIDMap = [:]

    db.eachRow("""
    select tscript_ensdart_id, tscript_load_id, tscript_mrkr_zdb_id
    from transcript where tscript_ensdart_id is not null """) { row ->
        zfinToVegaIDMap[row.tscript_mrkr_zdb_id] = row.tscript_load_id
        ensemblToZfinIDMap[row.tscript_ensdart_id] = row.tscript_mrkr_zdb_id
        zfinToEnsemblIDMap[row.tscript_mrkr_zdb_id] = row.tscript_ensdart_id
    }


    db.eachRow("""
    select dblink_acc_num, dblink_linked_recid from db_link where dblink_acc_num like 'ENSDARG%' """) { row ->
        ensemblToZfinIDMap[row.dblink_acc_num] = row.dblink_linked_recid
    }

    Map<String,String> ensemblToZfinNameMap = [:]
    db.eachRow("""
    select tscript_ensdart_id, mrkr_abbrev
    from transcript join marker on tscript_mrkr_zdb_id = mrkr_zdb_id """) { row ->
        ensemblToZfinNameMap[row.tscript_ensdart_id] = row.mrkr_abbrev
    }

    Map<String,List<String>> aliasMap = [:]
    db.eachRow("""select dalias_data_zdb_id, dalias_alias from data_alias join db_link on dblink_linked_recid = dalias_data_zdb_id where dblink_acc_num like 'ENSDARG%'""") { row ->
        String id = row.dalias_data_zdb_id
        String alias = row.dalias_alias

        if (aliasMap[id]) {
            aliasMap[id].add(alias)
        } else {
            aliasMap[id] = [alias]
        }
    }

    db.eachRow(""" select zeg_gene_zdb_id, dblink_acc_num
                   from zfin_ensembl_gene
                        join db_link on dblink_linked_recid = zeg_gene_zdb_id
                   where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39','ZDB-FDBCONT-040412-14','ZDB-FDBCONT-131021-1','ZDB-FDBCONT-061018-1'); """) { row ->
        String id = row.zeg_gene_zdb_id
        String alias = row.dblink_acc_num

        if (aliasMap[id]) {
            aliasMap[id].add(alias)
        } else {
            aliasMap[id] = [alias]
        }
    }

    def proteinIds = [:]
    db.eachRow("""
        select dblink_linked_recid, dblink_acc_num from db_link where dblink_acc_num like 'ENSDARP%';
    """) { row ->

        if (!proteinIds[row.dblink_linked_recid]) {
            proteinIds[row.dblink_linked_recid] = ["ENSEMBL:" + row.dblink_acc_num]
        } else {
            proteinIds[row.dblink_linked_recid].add("ENSEMBL:" + row.dblink_acc_num)
        }
    }

    def secondaryIds = [:]
    db.eachRow("""
        select zrepld_new_zdb_id, zrepld_old_zdb_id 
        from zdb_replaced_data 
    """) { row ->
        if (!secondaryIds[row.zrepld_new_zdb_id]) {
            secondaryIds[row.zrepld_new_zdb_id] = [row.zrepld_old_zdb_id]
        } else {
            secondaryIds[row.zrepld_new_zdb_id].add(row.zrepld_old_zdb_id)
        }

    }
    int cdsCounter = 0
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
        //explicitly set the parent to back to ensembl ID for transcript components
        if (['CDS', 'exon', 'three_prime_UTR', 'five_prime_UTR'].contains(row.gff_feature)) {
            parent = row.gff_parent
        }
        String id = row.gff_id
        if (row.gff_feature == "CDS") {
            cdsCounter++
            id += ":$cdsCounter"
        }
        String zdbId = ensemblToZfinIDMap[row.gff_id]
        //only switch to ZFIN IDs for genes
        if (id.startsWith("ENSDARG") && zdbId) {
            id = zdbId
        }

        String name = ensemblToZfinNameMap[row.gff_id] ?: row.gff_name

        gff += "ID=$id;Name=$name;Parent=$parent"

        if (zdbId) {
            gff += ";zdb_id=$zdbId"
        }

        if (id.startsWith("ENSDAR")) {
            gff += ";curie=ENSEMBL:" + id
        } else if (id.startsWith("ZDB")) {
            gff += ";curie=ZFIN:" + id
        }

        def xrefs = []
        if (zdbId) {
            xrefs.add("ZFIN:" + zdbId)
        }
        if (zfinToVegaIDMap[zdbId]) {
            xrefs.add("VEGA:" + zfinToVegaIDMap[zdbId])
        }
        if (row.gff_id.startsWith("ENS")) {
            xrefs.add("ENSEMBL:" + row.gff_id)
        }

        gff += ";dbxref=" + xrefs.join(",")

        if (row.gff_id.startsWith("ENSDART")) {
            gff += ";transcript_id=ENSDART:" + row.gff_id
        }

        if (secondaryIds[zdbId] && !secondaryIds[zdbId].isEmpty() ) {
            gff += ";secondaryIds=" + secondaryIds[zdbId].join(",")
        }

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

    printHeader("$gff3Dir/zfin_genes_header.gff3", zfinGenesWriter)

    genes.each { GenomeFeature gene ->

        if (secondaryIds[gene.id]) {
            gene.addAttribute("secondaryIds",secondaryIds[gene.id].join(','))
        }

        if (proteinIds[gene.id]) {
            gene.addAttribute("protein_id", proteinIds[gene.id].join(','))
        }

        if (!gene.getAttribute("curie") && gene.getId().startsWith("ZDB")) {
            gene.addAttribute("curie", "ZFIN:" + gene.getId())
        }

        if (!gene.getAttribute("gene_id") && gene.getId().startsWith("ZDB")) {
            gene.addAttribute("gene_id", "ZFIN:" + gene.getId())
        }

        //Alliance requirements are that we use gene rather than protein_coding_gene in gff3 files
        if (gene.getType() == "protein_coding_gene") {
            gene.setType("gene")
        }

        def zfinTranscripts = []
        def otherTranscripts = []
        ensemblFeatureMap[gene.getId()]?.each { transcript ->
            String zdbId = transcript.getAttribute("zdb_id")
            if (StringUtils.isNotEmpty(zdbId)) {
                zfinTranscripts.add(transcript)
            } else {
                otherTranscripts.add(transcript)
            }
        }

        if (aliasMap[gene.getId()]) {
            gene.addAttribute("Alias",aliasMap[gene.getId()].join(","))
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
    new File(contigs).eachLine { line ->
        out.println line
    }
    out.println "#!date-produced " + new Date().format( 'yyyy-MM-dd' )
    out.println "#!data-source ZFIN"
    out.println "#!assembly GRCz11"


}

