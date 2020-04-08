#!/bin/bash
import groovy.json.JsonSlurper
import org.zfin.properties.ZfinProperties

//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.zfin.util.ReportGenerator

cli = new CliBuilder(usage: 'LoadAddgene')
cli.jobName(args: 1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load a local ncbiEnsembl file instead of downloading')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}

/*addgeneDb = RepositoryFactory.sequenceRepository.getReferenceDatabase(
        ForeignDB.AvailableName.ADDGENE,
        ForeignDBDataType.DataType.OTHER,
        ForeignDBDataType.SuperType.SUMMARY_PAGE,
        Species.Type.ZEBRAFISH)
entrezGeneDb = session.get(ReferenceDatabase.class, 'ZDB-FDBCONT-040412-1')*/



ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "https://www.addgene.org/download/2cae1f5eb19075da8ba8de3ac954e4d5/plasmids/"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

def proc1 = "rm -rf addgeneDesc.csv".execute()
proc1
print "Loading local JSON file ... "


json = new JsonSlurper().parse(new FileReader("plasmids"))

def geneids = new ArrayList<String>()
def outCSV = new File('addgeneDesc.csv')
json.plasmids.each {
    aGene ->

        def gene = new String(aGene.id + '@' + aGene.name.replaceAll("(?:\\n|\\r|@)", "") + '@' + aGene.inserts.entrez_gene.id + '\n')
        geneids.add(gene)


}
geneids.each
        { aGene -> outCSV.append aGene }
println "done"

static Process dbaccess(String dbname, String sql) {
    sql = sql.replace("\n", "")
    sql = sql.replace("\\copy", "\n  \\copy")
    println sql

    def proc
    proc = "psql -d $dbname -a".execute()
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

static Process psql(String dbname, String sql) {
    return dbaccess(dbname, sql)
}


println "done"


dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")
PRE_FILE = "preaddgene.unl"
POST_FILE = "postaddgene.unl"

psql dbname, """

Insert into foreign_db_contains values ('Mouse', get_id('FDBCONT'),11,null,64);
Insert into foreign_db_contains values ('Human', get_id('FDBCONT'),11,null,64);


/*
\\\\COPY (SELECT dblink_linked_recid,dblink_acc_num
    FROM db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-141007-1') TO $PRE_FILE;
*/

DROP TABLE if exists tmp_ncbi_ensembl;

CREATE TABLE tmp_ncbi_ensembl (
    taxon_id text,
    gene_id text,
    gene_symbol text,
    ncbi_id text,
    ensembl_id text
);

\copy tmp_ncbi_ensembl FROM 'ncbi_ensembl.tsv' WITH delimiter E'\t';

-- remove prefix of taxon IDs
update tmp_ncbi_ensembl set taxon_id = (replace(taxon_id,'NCBITaxon:',''));

select * from tmp_ncbi_ensembl limit 5;

-- remove import entries that already exist by Ensembl ID
delete from tmp_ncbi_ensembl where 
exists (
select * from ortholog_external_reference, ortholog where
 oef_ortho_zdb_id = ortho_zdb_id AND
 ortho_other_species_taxid = taxon_id::integer AND
 oef_accession_number = ensembl_id
);


select count(*) from tmp_ncbi_ensembl where 
not exists (
select * from ortholog_external_reference, ortholog where
 oef_ortho_zdb_id = ortho_zdb_id AND
 ortho_other_species_taxid = taxon_id::integer AND
 oef_accession_number = ncbi_id
);

-- remove import entries that do not have a ncbi ID
delete from tmp_ncbi_ensembl where 
not exists (
select 'x' from ortholog_external_reference, ortholog where
 oef_ortho_zdb_id = ortho_zdb_id AND
 ortho_other_species_taxid = taxon_id::integer AND
 oef_accession_number = ncbi_id
);

drop table tmp_ortholog_external_reference;

create table tmp_ortholog_external_reference (
ortho_id text,
accession text,
fdbcont_id text
);

insert into tmp_ortholog_external_reference
select oef_ortho_zdb_id, ensembl_id, 
case 
when taxon_id = '10090' then (select fdbcont_zdb_id from foreign_db_contains where fdbcont_organism_common_name = 'Mouse' and fdbcont_fdb_db_id = 64)
when taxon_id = '9606' then (select fdbcont_zdb_id from foreign_db_contains where fdbcont_organism_common_name = 'Human' and fdbcont_fdb_db_id = 64)
END
 from ortholog_external_reference, tmp_ncbi_ensembl, ortholog
where ncbi_id = oef_accession_number
AND ortho_zdb_id = oef_ortho_zdb_id
AND ortho_other_species_taxid = taxon_id::integer
;

select count(*) from tmp_ortholog_external_reference;

select * from tmp_ortholog_external_reference limit 5;

insert into ortholog_external_reference
select * from tmp_ortholog_external_reference;


-- load data into tmp table
\\\\copy tmp_ncbi_ensembl FROM 'ncbi_ensembl.tsv' WITH delimiter E'\t';


select ortho_zdb_id, count(ortho_zdb_id) as ct from ortholog, tmp_ncbi_ensembl where ncbi_id = ortho_other_species_ncbi_gene_id 
and taxon_id::integer = ortho_other_species_taxid 
group by ortho_zdb_id order by ct desc;

-- remove prefix of taxon IDs
update tmp_ncbi_ensembl set taxon_id = (replace(taxon_id,'NCBITaxon:',''));

delete from tmp_ncbi_ensembl where 
exists (
select * from ortholog_external_reference, ortholog where
 oef_ortho_zdb_id = ortho_zdb_id AND
 ortho_other_species_taxid = taxon_id::integer AND
 oef_accession_number = ncbi_id
);


    
"""
println("done with script")


if (args) {
    // means we're (probably) running from Jenkins, so make a report.
    preLines = new File(PRE_FILE).readLines()
    postLines = new File(POST_FILE).readLines()

    added = postLines - preLines
    removed = preLines - postLines

    new ReportGenerator().with {
        setReportTitle("Report for ${args[0]}")
        includeTimestamp()
        addDataTable("${added.size()} terms added", ["ID", "Term"], added.collect { it.split("\\|") as List })
        addDataTable("${removed.size()} terms removed", ["ID", "Term"], removed.collect { it.split("\\|") as List })
        writeFiles(new File("."), "loadAddGeneReport")
    }
}
System.exit(0)

