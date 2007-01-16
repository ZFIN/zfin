 begin work ;


create table obo_file (obofile_name varchar(30) not null
                constraint obo_file_name_not_null,
            obofile_text blob,
	    obofile_load_date datetime year to second,
	    obofile_load_process varchar(60))
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
 PUT obofile_text in
    (smartbs1, smartbs2, smartbs3)(log)
 extent size 64 next size 64 lock mode row;

insert into obo_file (obofile_name)
  values ('gene_ontology.obo');

insert into obo_file (obofile_name)
  values ('quality.obo');

insert into obo_file (obofile_name)
  values ('zebrafish_anatomy.obo');

update obo_file
  set obofile_text = filetoblob("/research/zcentral/www_homes/coral/server_apps/data_transfer/LoadGO/gene_ontology.obo", 'server')
  where obofile_name = 'gene_ontology.obo';

update obo_file
  set obofile_text = filetoblob("/research/zcentral/www_homes/coral/server_apps/data_transfer/PATO/quality.obo", 'server')
  where obofile_name = 'quality.obo';

update obo_file
  set obofile_text = filetoblob("/research/zcentral/www_homes/coral/server_apps/data_transfer/Anatomy/zebrafish_anatomy.obo", 'server')
  where obofile_name = 'zebrafish_anatomy.obo';


--rollback work ;
commit work ;
