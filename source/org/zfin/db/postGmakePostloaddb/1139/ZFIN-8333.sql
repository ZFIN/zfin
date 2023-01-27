--liquibase formatted sql
--changeset cmpich:ZFIN-8333

select mrel_mrkr_2_zdb_id as antibody_zdb_id, mrel_mrkr_1_zdb_id as gene_zdb_id
INTO TEMP TABLE antibody_temp
from marker_relationship
where mrel_mrkr_2_zdb_id ~ 'ATB';

select antibody_zdb_id as antibody_ct_id, count(*) as ct
INTO TEMP TABLE count_temp
from antibody_temp
group by antibody_zdb_id;

delete
from antibody_temp
where antibody_zdb_id in (select antibody_ct_id from count_temp where antibody_ct_id = antibody_zdb_id and ct > 1);

update feature_stats
set fstat_gene_zdb_id = (select gene_zdb_id from antibody_temp where antibody_zdb_id = fstat_feat_zdb_id)
where fstat_type = 'Antibody'
  and fstat_gene_zdb_id is null;
