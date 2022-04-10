--liquibase formatted sql
--changeset cmpich:ZFIN-7849


-- insert into DB-link
create table pre_gene_temp
(
    pre_gene_zdb_id    varchar(50) not null,
    pre_feature_zdb_id varchar(50) not null
);

insert into pre_gene_temp (pre_gene_zdb_id, pre_feature_zdb_id)
select get_id('GENE'), feature_id
from feature_temp;

insert into zdb_active_data select pre_gene_zdb_id from pre_gene_temp;

insert into marker (mrkr_zdb_id, mrkr_type, mrkr_abbrev, mrkr_name, mrkr_owner)
select pre_gene_zdb_id, 'GENE', 'unm_' || feature_abbrev, 'un-named ' || feature_abbrev, 'ZDB-PERS-060413-1'
from pre_gene_temp,
     feature_temp
where pre_feature_zdb_id = feature_id;

create table pre_fmrel_temp
(
    pref_fmrel_zdb_id    varchar(50) not null,
    pref_feature_zdb_id varchar(50) not null
);

insert into pre_fmrel_temp (pref_fmrel_zdb_id, pref_feature_zdb_id)
select get_id('FMREL'), feature_id
from feature_temp;

insert into zdb_active_data select pref_fmrel_zdb_id from pre_fmrel_temp;

insert into feature_marker_relationship (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)
select pref_fmrel_zdb_id, 'is allele of', pref_feature_zdb_id, pre_gene_zdb_id
from pre_fmrel_temp, pre_gene_temp
where pref_feature_zdb_id = pre_feature_zdb_id;



