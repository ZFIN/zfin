--liquibase formatted sql
--changeset pm:pmflankseq


update pmflankseq set varseqid = (select vfseq_zdb_id from variant_flanking_sequence  where vfseq_data_zdb_id=featid)
from variant_flanking_sequence
where vfseq_data_zdb_id=featid;

alter table pmflankseq add column varseq text;
update pmflankseq set varseq = (select fgmd_sequence_of_reference||'/'|| fgmd_sequence_of_variation from feature_genomic_mutation_detail  where fgmd_feature_zdb_id=featid)
from variant_flanking_sequence
where vfseq_data_zdb_id=featid;



drop table if exists tmp_seqref;
create table tmp_seqref(
        featzdb text ,
        flankid text,
        seqref1 text,
        seqref2 text);

insert into tmp_seqref(
        featzdb,flankid,seqref1,seqref2)
  select  distinct featid,featid, seq1, seq2 from pmflankseq
   where varseqid like 'ZDB-ALT%';



   delete from variant_flanking_sequence where vfseq_offset_start=50 and vfseq_zdb_id in (select varseqid from pmflankseq);

   insert into variant_flanking_sequence (vfseq_zdb_id,vfseq_data_zdb_id,vfseq_offset_start,vfseq_offset_stop,vfseq_sequence,vfseq_flanking_sequence_type,vfseq_flanking_sequence_origin,vfseq_type,vfseq_variation,
vfseq_five_prime_flanking_sequence,vfseq_three_prime_flanking_sequence)
  select distinct varseqid,featid,500,500,seq1||'['||varseq ||']'
  ||seq2, 'genomic','directly sequenced','Genomic',
   varseq,seq1,seq2 from pmflankseq where varseqid not in (Select vfseq_zdb_id from variant_flanking_sequence);


update tmp_seqref set flankid = get_id('VFSEQ');

insert into zdb_active_data select distinct flankid from tmp_seqref;



insert into variant_flanking_sequence (vfseq_zdb_id,vfseq_data_zdb_id,vfseq_offset_start,vfseq_offset_stop,vfseq_sequence,vfseq_flanking_sequence_type,vfseq_flanking_sequence_origin,vfseq_type,vfseq_variation,
vfseq_five_prime_flanking_sequence,vfseq_three_prime_flanking_sequence)
  select distinct flankid,featzdb,500,500,seqref1||'['||(select distinct fgmd_sequence_of_reference from feature_genomic_mutation_detail where fgmd_feature_zdb_id=featzdb) ||
  '/'|| (select distinct fgmd_sequence_of_variation from feature_genomic_mutation_detail where fgmd_feature_zdb_id=featzdb)||
  ']'||seqref2, 'genomic','directly sequenced','Genomic',
   (select distinct fgmd_sequence_of_reference|| '/' ||fgmd_sequence_of_variation  from feature_genomic_mutation_detail where fgmd_feature_zdb_id=featzdb),seqref1,seqref2 from tmp_seqref where featzdb not in (Select vfseq_data_zdb_id from variant_flanking_sequence);



insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select distinct flankid,'ZDB-PUB-191030-9' from tmp_seqref;
