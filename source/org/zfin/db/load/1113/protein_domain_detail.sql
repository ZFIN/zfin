--liquibase formatted sql
--changeset pm:protein_domain_detail.sql

create table protein_to_pdb (ptp_pk_id serial8 not null primary key,
                                  ptp_uniprot_id text not null,
                                  ptp_pdb_id text not null);
alter table protein_to_pdb
 add constraint protein_to_pdb_ak unique (ptp_uniprot_id, ptp_pdb_id);

alter table protein_to_pdb
 add constraint protein_to_pdb_uniprot_id foreign key (ptp_uniprot_id)
 references protein on delete cascade;



insert into foreign_db (fdb_db_name,    
                        fdb_db_query,
                        fdb_url_suffix,
                        fdb_db_display_name,
                        fdb_db_significance)
values ('PDB',
       'http://www.rcsb.org/pdb/protein/',
       '',
       'PDB',
       '2');

create temp table tmp_id (id text);
insert into tmp_id 
  select get_id('FDBCONT') from single;

insert into foreign_db_contains (fdbcont_zdb_id, 
                                 fdbcont_organism_common_name,
                                 fdbcont_fdbdt_id, 
                                 fdbcont_fdb_db_id
       )
select id, 'Zebrafish',8,(select fdb_db_pk_id from foreign_db where fdb_db_name = 'PDB')
  from tmp_id;

