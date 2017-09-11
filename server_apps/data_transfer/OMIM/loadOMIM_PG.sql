
-- loadOMIM.sql
-- input file: pre_load_input_omim.txt, which is the parsing result of OMIM.pl
-- records loaded to OMIM_Phenotype table are dumped to whatHaveBeenInsertedIntoOmimPhenotypeTable.txt for checking
-- no duplication of omimp_gene_zdb_id and omimp_name

begin work;

create temp table omimPhenotypesAndGenes (
    gene_zdb_id    varchar(50) not null,
    gene_omim_num  varchar(10) not null,
    phenotype      varchar(200) not null,  
    phenotype_omim_id  varchar(10)    
) ;

create temp table omimPhenotypesAndGenesOrtho (
    gene_zdb_id    varchar(50) not null,
    gene_omim_num  varchar(10) not null,
    phenotype      varchar(200) not null,  
    phenotype_omim_id  varchar(10),
    ortho_id	       varchar(50) not null    
) ;

create index omimPhenotypesAndGenes_gene_zdb_id_idx on omimPhenotypesAndGenes(gene_zdb_id);
create index omimPhenotypesAndGenes_gene_omim_num_idx on omimPhenotypesAndGenes(gene_omim_num);
create index omimPhenotypesAndGenes_phenotype_idx on omimPhenotypesAndGenes(phenotype);

copy omimPhenotypesAndGenes from '<!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/pre_load_input_omim.txt' (delimiter '|');

insert into omimPhenotypesAndGenesOrtho 
  select gene_Zdb_id, gene_omim_num, phenotype, phenotype_omim_id, ortho_zdb_id
    from ortholog, ortholog_external_reference, omimPhenotypesAndGenes
    where ortho_zebrafish_gene_zdb_id = gene_Zdb_id
    and gene_omim_num = oef_accession_number;

select count(*) from omimPhenotypesAndGenesOrtho ;

--check what will have been deleted from the omim_phenotype table
create view whatHaveBeenDeletedFromOmimPhenotypeTable as
 select *                                                             
   from omim_phenotype                                                
  where not exists (select 1 from omimPhenotypesAndGenesOrtho          
                     where omimp_name = phenotype                      
		       and omimp_ortho_zdb_id = ortho_id)              
   order by omimp_ortho_zdb_id;
\copy (select * from whatHaveBeenDeletedFromOmimPhenotypeTable) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/whatHaveBeenDeletedFromOmimPhenotypeTable.txt';
drop view whatHaveBeenDeletedFromOmimPhenotypeTable;

select count(*) from omim_phenotype;

--delete records in omim_phenotype table that are not in the load
delete from omim_phenotype
 where not exists (select 1 from omimPhenotypesAndGenesOrtho
                    where omimp_name = phenotype
                      and omimp_ortho_zdb_id = ortho_id);
                   

select count(*) from omim_phenotype;

--check what will have been updated for the omimp_omim_id in omim_phenotype table
create view whatPhenoOMIMnumInOmimPhenotypeTableHaveBeenUpdated as
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenesOrtho
 where exists (select 1 from omim_phenotype
                where omimp_name = phenotype
                  and omimp_ortho_zdb_id = ortho_id
                  and (omimp_omim_id <> phenotype_omim_id
                    or (omimp_omim_id is null and phenotype_omim_id is not null)
                    or (phenotype_omim_id is null and omimp_omim_id is not null))
              )
   order by gene_zdb_id;
\copy (select * from whatPhenoOMIMnumInOmimPhenotypeTableHaveBeenUpdated) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/whatPhenoOMIMnumInOmimPhenotypeTableHaveBeenUpdated.txt';
drop view whatPhenoOMIMnumInOmimPhenotypeTableHaveBeenUpdated;

--update the null omimp_omim_id in omim_phenotype table
update omim_phenotype set omimp_omim_id = (
 select distinct phenotype_omim_id
   from omimPhenotypesAndGenesOrtho
  where omimp_name = phenotype
    and omimp_ortho_zdb_id = ortho_id
    and phenotype_omim_id is not null
) where omimp_omim_id is null
    and exists (select 1 from omimPhenotypesAndGenesOrtho
                  where omimp_name = phenotype
                    and omimp_ortho_zdb_id = ortho_id 
                    and phenotype_omim_id is not null);     


--update the omimp_omim_id in omim_phenotype table to null where omimp_omim_id used to be not null but now OMIM has change it to null; should be rare
update omim_phenotype set omimp_omim_id = null
 where omimp_omim_id is not null
    and exists (select 1 from omimPhenotypesAndGenesOrtho
                  where omimp_name = phenotype
                    and omimp_ortho_zdb_id = ortho_id 
                    and phenotype_omim_id is null);    

create temp table toUpdate (
    pheno    varchar(200) not null,
    ortho    varchar(50),
    pk       int8,
    old_id   varchar(50) not null,
    new_id  varchar(50) not null
) ;

insert into toUpdate
select omimp_name as pheno, omimp_ortho_zdb_id, omimp_pk_id, omimp_omim_id as old_id, omim.phenotype_omim_id as new_id
  from omimPhenotypesAndGenesOrtho omim, omim_phenotype
 where omimp_name = omim.phenotype
   and omimp_ortho_zdb_id = omim.ortho_id
   and omimp_omim_id is not null
   and omim.phenotype_omim_id is not null
   and omim.phenotype_omim_id != omimp_omim_id
 group by omimp_name, omimp_ortho_zdb_id, omimp_omim_id, omimp_pk_id, omim.phenotype_omim_id;

--!echo 'update the omimp_omim_id in omim_phenotype table where omimp_omim_id is different from phenotype_omim_id in table omimPhenotypesAndGenes'
update omim_phenotype set omimp_omim_id = (
 select new_id 
   from toUpdate
  where omimp_pk_id = pk 
 ) where omimp_omim_id is not null
     and exists (select 1 from toUpdate
                  where pk = omimp_pk_id);


--check what new records will have been added into the omim_phenotype table
create view whatHaveBeenInsertedIntoOmimPhenotypeTable as
 select gene_zdb_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenesOrtho
  where not exists (select 1 from omim_phenotype
                     where ortho_id = omimp_ortho_zdb_id
                       and phenotype = omimp_name)
  order by gene_zdb_id;
\copy (select * from whatHaveBeenInsertedIntoOmimPhenotypeTable) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/whatHaveBeenInsertedIntoOmimPhenotypeTable.txt';
drop view whatHaveBeenInsertedIntoOmimPhenotypeTable;

-- do the actual loading into ZFIN omim_phenotype table
insert into omim_phenotype (omimp_ortho_zdb_id, omimp_name, omimp_omim_id)
 select ortho_id,phenotype,phenotype_omim_id
   from omimPhenotypesAndGenesOrtho
  where not exists (select 1 from omim_phenotype
                     where ortho_id = omimp_ortho_zdb_id
                       and phenotype = omimp_name);


--check what genes with human ortholog not having disorder records
create view genesWithMIMnotFoundOnOMIMPtable as
 select distinct ortho_zebrafish_gene_zdb_id, oef_accession_number
   from ortholog, ortholog_external_reference
  where oef_ortho_zdb_id = ortho_zdb_id
    and oef_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-25'
    and not exists (select 1 from omim_phenotype
                     where omimp_ortho_zdb_id = ortho_zdb_id);
\copy (select * from genesWithMIMnotFoundOnOMIMPtable) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/genesWithMIMnotFoundOnOMIMPtable.txt';
drop view genesWithMIMnotFoundOnOMIMPtable;

--rollback work;

commit work;
