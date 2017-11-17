//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

import org.apache.log4j.Logger
import org.zfin.infrastructure.ant.RunSQLFiles

Logger log = Logger.getLogger(getClass());

def env = System.getenv()

println 'Start generating GFF3 download files...'

def propertiesFile = "${env['TARGETROOT']}/home/WEB-INF/zfin.properties"
RunSQLFiles runScriptFiles = new RunSQLFiles("Generate-GFF3", propertiesFile, ".")
runScriptFiles.initializeLogger("./log4j.xml")
runScriptFiles.initDatabaseWithoutSysmaster()
runScriptFiles.setQueryFiles(
        "zfin_tginsertion_gff3.sql",
        "zfin_zmp_gff3.sql",
        "E_drerio_transcript_gff3.sql",
        "E_zfin_ensembl_gene_PG.sql",
        "E_zfin_gene_alias_scattered_gff3.sql",
        "E_expression_gff3.sql",
        "E_phenotype_gff3.sql",
        "E_antibody_gff3.sql"
)
runScriptFiles.execute()

def downloadDir = "${env['TARGETROOT']}/home/data_transfer/Downloads/"

def backboneFileName = "E_drerio_backbone.gff3"
def constantFile = new File('/research/zprodmore/gff3/E_drerio_constant.gff3')
def backboneFile = new File(downloadDir + backboneFileName)
backboneFile.write(constantFile.text)

def transcriptFile = new File("E_drerio_transcript.gff3")
backboneFile << transcriptFile.text

def contigFile = new File('ensembl_contig.gff3')
def destination = new File(downloadDir + "ensembl_contig.gff3")
destination.write(contigFile.text)

def knockdownFile = new File('E_zfin_knockdown_reagents.gff3')
destination = new File(downloadDir + "E_zfin_knockdown_reagents.gff3")
destination.write(knockdownFile.text)

def aliasFileName = "E_zfin_gene_alias.gff3"
def proc = "../gather_alias.awk E_zfin_gene_alias_scattered.tmp".execute()
def buffer = new StringBuffer()
proc.consumeProcessErrorStream(buffer)

File alias = new File(aliasFileName)
alias << proc.text

destination = new File(downloadDir + aliasFileName)
destination.write(alias.text)

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
