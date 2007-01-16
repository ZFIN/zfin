begin work ;


create table obo_file (obofile_name varchar(30) not null 
				constraint obo_file_name_not_null,
			obofile_text_file blob)
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
 PUT obofile_text_file in 
    (smartbs1, smartbs2, smartbs3)(log)
 extent size 64 next size 64 lock mode row;

insert into obo_file (obofile_name)
  values ('gene_ontology.obo');

update obo_file
  set obofile_text_file = filetoblob("/research/zusers/staylor/SPL_SQL/cvs_branch/ZFIN_WWW/lib/DB_schema/FRODO_new/ZFIN_WWW/lib/DB_schema/FRODO/gene_ontology.obo", 'server')
  where obofile_name = 'gene_ontology.obo';

  select lotofile(obofile_text_file,'/research/zusers/staylor/SPL_SQL/cvs_branch/ZFIN_WWW/lib/DB_schema/FRODO_new/ZFIN_WWW/lib/DB_schema/FRODO/gene_ontology.obo','server')
    from obo_file
    where obofile_name = 'gene_ontology.obo' ;

rollback work ;
--commit work ;