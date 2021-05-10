--liquibase formatted sql
--changeset sierra:geoLink

create table pub_db_xref (pdx_pk_id serial8 not null constraint pdx_pk_id_not_null,
       pdx_pub_zdb_id varchar(50) not null constraint pdx_pub_zdb_id_not_null,
       pdx_accession_number varchar(70) not null constraint pdx_accession_number_not_null,
       pdx_fdbcont_zdb_id varchar(50) not null constraint pdx_fdbcont_Zdb_id_not_null)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 2048 next size 2048
lock mode row;

create unique index pub_db_xref_primary_key_index
 on pub_db_xref (pdx_pk_id)
 using btree in idxdbs2;

create unique index pub_db_xref_alternate_key_index
 on pub_db_xref (pdx_pub_zdb_id, pdx_accession_number, pdx_fdbcont_zdb_id)
 using btree in idxdbs3;

create index pub_db_xref_fdbcont_foreign_key_index
 on pub_db_xref (pdx_fdbcont_Zdb_id)
 using btree in idxdbs1;

create index pub_db_xref_pub_foreign_key_index
 on pub_db_xref				     (pdx_pub_Zdb_id)
 using btree in				     idxdbs1;

alter table pub_db_xref
 add constraint primary key (pdx_pk_id)
constraint pub_db_Xref_primary_key;

alter table pub_db_xref
 add constraint (Foreign key (pdx_pub_zdb_id)
 references publication constraint pub_db_xref_pub_fk);

alter table pub_db_xref
 add constraint (foreign key (pdx_fdbcont_Zdb_id)
 references foreign_db_contains constraint pdx_db_xref_fdbcont_fk);
