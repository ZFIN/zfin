--liquibase formatted sql
--changeset cmpich:ZFIN-10078-update

delete from tmp_flank_seq where difftype is null or difftype = '';

update variant_flanking_sequence
set vfseq_variation = reverse(translate(upper(left(vfseq_variation, -2)), 'ACGT', 'TGCA')) || '/-'
from tmp_flank_seq tmp
where vfseq_data_zdb_id = tmp.feature_id
  and tmp.difftype = 'reverse complement'
  and tmp.feattype = 'DELETION';

update variant_flanking_sequence
set vfseq_variation = upper(vfseq_variation)
from tmp_flank_seq tmp
where vfseq_data_zdb_id = tmp.feature_id
  and tmp.difftype = 'case-only mismatch';

