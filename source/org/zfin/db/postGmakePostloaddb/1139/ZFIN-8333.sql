--liquibase formatted sql
--changeset cmpich:ZFIN-8333

create temp table antibody_temp
(
    antibody_zdb_id varchar(50),
    gene_zdb_id     varchar(50)
);

insert into antibody_temp
select mrel_mrkr_2_zdb_id, mrel_mrkr_1_zdb_id
from marker_relationship
where mrel_mrkr_2_zdb_id ~'ATB';

create temp table count_temp
(
    antibody_ct_id varchar(50),
    ct             integer
);

insert into count_temp
select antibody_zdb_id, count(*) as ct
from antibody_temp
group by antibody_zdb_id;

delete from antibody_temp
    where antibody_zdb_id in (select antibody_ct_id from count_temp where antibody_ct_id = antibody_zdb_id and ct > 1);

update feature_stats
set fstat_gene_zdb_id = (select gene_zdb_id from antibody_temp where antibody_zdb_id = fstat_feat_zdb_id)
where fstat_type = 'Antibody'
  and fstat_gene_zdb_id is null;
