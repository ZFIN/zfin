--liquibase formatted sql
--changeset cmpich:ZFIN-10078-update

delete from tmp_flank_seq where difftype is null or difftype = '';

update variant_flanking_sequence
set vfseq_variation = tmp.calculated_sequence
from tmp_flank_seq tmp
where vfseq_data_zdb_id = tmp.feature_id;

