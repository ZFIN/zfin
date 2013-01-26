
-- loadOMIM.sql
-- input file: pre_load_input_omim.txt, which is the parsing result of OMIM.pl
-- records loaded to OMIM_Phenotype table are dumped to whatHaveBeenInsertedIntoOmimPhenotypeTable.txt for checking
-- no duplication of omimp_gene_zdb_id and omimp_name

begin work;

{
insert into the table to store the phenotype description is required. 
Not all phenotypes have an OMIM id so the db_link table will not be able store this data. 
The table architecture for links to other databases can be used. (foreign_db)

Table Name: OMIM_Phenotype

Column: omimp_pk_id		primary key
Column: omimp_name		description provided by the OMIM download file
Column: omimp_omim_id		(optional) omim phenotype id
Column: omimp_gene_zdb_id	(foreign key to marker) gene zdb id
}

create temp table omimPhenotypesAndGenes (
    gene_zdb_id    varchar(50) not null,
    gene_omim_num  varchar(10) not null,
    phenotype      varchar(200) not null,  
    phenotype_omim_id  varchar(10)    
) with no log;

--!echo 'Load from pre_load_input_omim.txt'
load from pre_load_input_omim.txt insert into omimPhenotypesAndGenes;

--!echo 'check what have been loaded into the omim_phenotype table'
unload to whatHaveBeenInsertedIntoOmimPhenotypeTable.txt
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenes
  where not exists (select "x" from omim_phenotype
                     where gene_zdb_id = omimp_gene_zdb_id
                       and phenotype = omimp_name);

-- !echo 'do the actual loading into the omim_phenotype table'
insert into omim_phenotype (omimp_gene_zdb_id, omimp_name, omimp_omim_id)
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenes
  where not exists (select "x" from omim_phenotype
                     where gene_zdb_id = omimp_gene_zdb_id
                       and phenotype = omimp_name);


--!echo 'check what genes with human ortholog not having disorder records'
unload to genesWithMIMnotFoundOnOMIMPtable.txt delimiter "	"
 select distinct c_gene_id gene, dblink_acc_num mim
   from orthologue, db_link 
  where zdb_id = dblink_linked_recid 
    and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-25" 
    and organism = "Human"
    and not exists (select "x" from omim_phenotype
                     where omimp_gene_zdb_id == c_gene_id);


--rollback work;

commit work;