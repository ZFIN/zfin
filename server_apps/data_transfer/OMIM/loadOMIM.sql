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

insert into omim_phenotype (omimp_gene_zdb_id, omimp_name, omimp_omim_id) 
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenes;

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