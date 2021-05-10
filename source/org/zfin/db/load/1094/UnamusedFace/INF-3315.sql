--liquibase formatted sql
--changeset staylor:INF-3315

delete from publication_file
where not exists (select 'x' from publication
      	  	 	 where zdb_id = pf_pub_zdb_id);

alter table publication_file
 drop constraint publication_file_alternate_key;

create index publication_file_pub_fk_index
 on publication_file (pf_pub_zdb_id)
 using btree in idxdbs1;

alter table publication_file 
  add constraint (foreign key (pf_pub_zdb_id) references publication on 
  delete cascade constraint pf_pub_zdb_id_fk_odc);

alter table publication_file add constraint unique(pf_pub_zdb_id,
    pf_file_name,pf_file_type_id) constraint publication_file_alternate_key 
     ;
