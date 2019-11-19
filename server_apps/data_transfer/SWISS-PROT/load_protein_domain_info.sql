-- load_protein_domain_info.sql

begin work;

create temp table pre_interpro(id text, name text, type text);

create index pre_interpro_id_idx on pre_interpro(id);

copy pre_interpro from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/domain.txt' (delimiter '|');

insert into zdb_active_data(zactvd_zdb_id)
select id from pre_interpro;

insert into interpro_protein(ip_interpro_id, ip_name, ip_type)
 select id, name, type
   from pre_interpro;

create temp table pre_unipro(id text, fdbcont text, len integer);

create index pre_unipro_id_idx on pre_unipro(id);

copy pre_unipro from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/protein.txt' (delimiter '|');

insert into zdb_active_data(zactvd_zdb_id)
select id from pre_unipro;

insert into protein(up_uniprot_id, up_fdbcont_zdb_id, up_length)
 select id, fdbcont, len
   from pre_unipro;

create temp table pre_mrkr_unipro(geneid text, uniprot text);

create index pre_mrkr_unipro_geneid_idx on pre_mrkr_unipro(geneid);
create index pre_mrkr_unipro_uniproid_idx on pre_mrkr_unipro(uniprot);

copy pre_mrkr_unipro from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/zfinprotein.txt' (delimiter '|');

insert into marker_to_protein(mtp_mrkr_zdb_id, mtp_uniprot_id)
 select geneid, uniprot
   from pre_mrkr_unipro;

delete from marker_to_protein
  where not exists(select 'x' from protein
                    where up_uniprot_id = mtp_uniprot_id)
     or not exists(select 'x' from marker
                    where mrkr_zdb_id = mtp_mrkr_zdb_id);

create temp table pre_unipro_interpro(unip text, interpro text);

create index pre_uniprointerpro_unip_idx on pre_unipro_interpro(unip);
create index pre_uniprointerpro_iprid_idx on pre_unipro_interpro(interpro);

copy pre_unipro_interpro from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/unipro2interpro.txt' (delimiter '|');

insert into protein_to_interpro(pti_uniprot_id, pti_interpro_id)
 select unip, interpro
   from pre_unipro_interpro;

delete from protein_to_interpro
  where not exists(select 'x' from protein
                    where up_uniprot_id = pti_uniprot_id)
     or not exists(select 'x' from interpro_protein
                    where ip_interpro_id = pti_interpro_id);

--rollback work;

commit work;  


