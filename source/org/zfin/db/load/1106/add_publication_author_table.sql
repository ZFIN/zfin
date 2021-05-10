--liquibase formatted sql
--changeset sierra:add_publication_author_table.sql

create table pubmed_publication_author (ppa_pk_id serial8 not null primary key,
                                        ppa_pubmed_id text not null,
                                        ppa_publication_zdb_id text,
                                        ppa_author_first_name text, 
                                        ppa_author_middle_name text,
                                        ppa_author_last_name text not null)
;

create unique index pubmed_publication_author_index on pubmed_publication_author (ppa_pubmed_id,
                                                     ppa_author_first_name,
                                                     ppa_author_middle_name,
                                                     ppa_author_last_name)
;

alter table pubmed_publication_author
  add constraint ppa_publication_fk
  foreign key (ppa_publication_zdb_id)
  references publication on delete cascade;
