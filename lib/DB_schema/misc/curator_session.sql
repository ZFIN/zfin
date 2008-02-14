begin work ;

create table curator_session (cs_person_zdb_id varchar(50)
                              not null constraint
                cs_curator_zdb_id_not_null,
                  cs_data_zdb_id varchar(50),
                  cs_field_name varchar(100)
                    not null constraint
                cs_field_name_not_null,
                  cs_field_name_value lvarchar(1000)
                    not null constraint
                cs_field_name_value_not_null)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 2048 next size 2048 lock mode page ;

create unique index curator_session_primary_key_index
  on curator_session (cs_person_zdb_id, cs_data_zdb_id, cs_field_name)
  using btree in idxdbs3 ;

create index curator_session_person_zdb_id_foreign_key_index
  on curator_session (cs_person_zdb_id)
  using btree in idxdbs3 ;

alter table curator_session
  add constraint (Foreign key (cs_person_zdb_id)
  references person on delete cascade constraint
  curator_session_person_zdb_id_foreign_key_odc );


create procedure p_delete_curator_session (
      vPubZdbId varchar(50), vClosed datetime year to day)
 if vClosed is not null
 then
  delete from curator_Session 
    where cs_data_zdb_id = vPubZdbId ;
 end if;

end procedure ;

drop trigger publication_update_trigger ;

create trigger publication_update_trigger update of
    title,accession_no , pub_doi, pubmed_authors, authors,
    pub_mini_ref, jtype, pub_pages, zdb_id
    on publication referencing new as new_publication
    for each row
        (
        execute function scrub_char(new_publication.title
    ) into publication.title,
        execute function scrub_char(new_publication.accession_no
    ) into publication.accession_no,
        execute function scrub_char(new_publication.pub_doi
    ) into publication.pub_doi,
        execute function scrub_char(new_publication.pubmed_authors
    ) into publication.pubmed_authors,
        execute function lower(new_publication.authors
    ) into publication.pub_authors_lower,
        execute function scrub_char(new_publication.jtype
    ) into publication.jtype,
        execute function get_pub_mini_ref(new_publication.zdb_id
    ) into publication.pub_mini_ref,
        execute function scrub_char(new_publication.pub_mini_ref
    ) into publication.pub_mini_ref,
        execute function scrub_char(new_publication.pub_pages
    ) into publication.pub_pages);


create trigger publication_completion_date_update_trigger
  update of pub_completion_date on publication
  referencing old as old_publication new as new_publication
  for each row (
            execute procedure p_delete_curator_session(new_publication.zdb_id,
      new_publication.pub_completion_date)
);

--rollback work ;
commit work ;