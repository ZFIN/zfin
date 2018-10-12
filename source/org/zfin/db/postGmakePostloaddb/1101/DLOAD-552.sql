--liquibase formatted sql
--changeset pm:DLOAD-552



create table  sanger_flanking_sequence_post_proc (
 alleleid text not null,
        variation text not null,
           sequence text not null);


insert into sanger_flanking_sequence_post_proc (alleleid, variation, sequence) select distinct  feature_zdb_id,ref||'/'||alt,flankingseq from sanger_flanking_sequence, feature where feature_abbrev=allele;

insert into variant_flanking_sequence (vfseq_data_zdb_id, vfseq_type, vfseq_offset_start, vfseq_offset_stop, vfseq_sequence, vfseq_five_prime_flanking_sequence, vfseq_three_prime_flanking_sequence, vfseq_flanking_sequence_type, vfseq_flanking_sequence_origin, vfseq_variation )
select distinct alleleid, 'Genomic',50,50,sequence,SUBSTRING(sequence,1,50),SUBSTRING(sequence,56),'genomic','directly sequenced',variation from sanger_flanking_sequence_post_proc;
