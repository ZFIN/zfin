begin work;

create temp table pubs_to_update_temp  (
	pub_zdb_id_temp varchar(50),
	pubmed_id_temp varchar(50)
) with no log ;

load from  'listOfUpdatedPubs'  delimiter '	'  insert into  pubs_to_update_temp;

update publication 
   set status = "active" 
 where exists( select "x" from pubs_to_update_temp
                where zdb_id = pub_zdb_id_temp 
                  and accession_no = pubmed_id_temp);

insert into updates (rec_id,field_name,new_value,when) 
select pub_zdb_id_temp, "status", "active", current
  from pubs_to_update_temp 
 where exists( select "x" from publication
                where zdb_id = pub_zdb_id_temp 
                  and accession_no = pubmed_id_temp);

--rollback work;
commit work;

