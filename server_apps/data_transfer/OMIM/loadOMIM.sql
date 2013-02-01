
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

create table omimPhenotypesAndGenes (
    gene_zdb_id    varchar(50) not null,
    gene_omim_num  varchar(10) not null,
    phenotype      varchar(200) not null,  
    phenotype_omim_id  varchar(10)    
);

create index omimPhenotypesAndGenes_gene_zdb_id_idx on omimPhenotypesAndGenes(gene_zdb_id) in idxdbs3;
create index omimPhenotypesAndGenes_gene_omim_num_idx on omimPhenotypesAndGenes(gene_omim_num) in idxdbs3;
create index omimPhenotypesAndGenes_phenotype_idx on omimPhenotypesAndGenes(phenotype) in idxdbs3;

--!echo 'Load from pre_load_input_omim.txt'
load from pre_load_input_omim.txt insert into omimPhenotypesAndGenes;

--!echo 'check what will have been deleted from the omim_phenotype table'
unload to whatHaveBeenDeletedFromOmimPhenotypeTable.txt
 select *
   from omim_phenotype
 where not exists (select "x" from omimPhenotypesAndGenes
                    where omimp_name = phenotype
                      and omimp_gene_zdb_id = gene_zdb_id)
   order by omimp_gene_zdb_id;
                      
--!echo 'delete records in omim_phenotype table that are not in the load'
delete from omim_phenotype
 where not exists (select "x" from omimPhenotypesAndGenes
                    where omimp_name = phenotype
                      and omimp_gene_zdb_id = gene_zdb_id);
                   

--!echo 'check what will have been updated for the omimp_omim_id in omim_phenotype table'
unload to whatPhenoOMIMnumInOmimPhenotypeTableHaveBeenUpdated.txt
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenes
 where exists (select "x" from omim_phenotype
                where omimp_name = phenotype
                  and omimp_gene_zdb_id = gene_zdb_id
                  and (omimp_omim_id <> phenotype_omim_id
                    or omimp_omim_id is null and phenotype_omim_id is not null
                    or phenotype_omim_id is null and omimp_omim_id is not null)
              )
   order by gene_zdb_id;

--!echo 'update the null omimp_omim_id in omim_phenotype table'
update omim_phenotype set omimp_omim_id = (
 select phenotype_omim_id
   from omimPhenotypesAndGenes
  where omimp_name = phenotype
    and omimp_gene_zdb_id = gene_zdb_id
    and phenotype_omim_id is not null
) where omimp_omim_id is null
    and exists (select "x" from omimPhenotypesAndGenes
                  where omimp_name = phenotype
                    and omimp_gene_zdb_id = gene_zdb_id 
                    and phenotype_omim_id is not null);       
    

--!echo 'update the omimp_omim_id in omim_phenotype table to null where omimp_omim_id used to be not null but now OMIM has change it to null; should be rare'
update omim_phenotype set omimp_omim_id = null
 where omimp_omim_id is not null
    and exists (select "x" from omimPhenotypesAndGenes
                  where omimp_name = phenotype
                    and omimp_gene_zdb_id = gene_zdb_id 
                    and phenotype_omim_id is null);    

--!echo 'update the omimp_omim_id in omim_phenotype table where omimp_omim_id is different from phenotype_omim_id in table omimPhenotypesAndGenes'
update omim_phenotype set omimp_omim_id = (
 select phenotype_omim_id
   from omimPhenotypesAndGenes
  where omimp_name = phenotype
    and omimp_gene_zdb_id = gene_zdb_id
 ) where exists (select "x" from omimPhenotypesAndGenes
                  where omimp_name = phenotype
                    and omimp_gene_zdb_id = gene_zdb_id 
                    and omimp_omim_id <> phenotype_omim_id);    


--!echo 'check what new records will have been added into the omim_phenotype table'
unload to whatHaveBeenInsertedIntoOmimPhenotypeTable.txt
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenes
  where not exists (select "x" from omim_phenotype
                     where gene_zdb_id = omimp_gene_zdb_id
                       and phenotype = omimp_name)
  order by gene_zdb_id;

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


drop table omimPhenotypesAndGenes;

--rollback work;

commit work;