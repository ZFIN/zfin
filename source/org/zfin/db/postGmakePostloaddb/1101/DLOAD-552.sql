--liquibase formatted sql
--changeset pm:DLOAD-552



create table  sanger_flanking_sequence_post_proc (
 alleleid text not null,
        variation text not null,
           sequence text not null);


insert into sanger_flanking_sequence_post_proc (alleleid, variation, sequence) select distinct  feature_zdb_id,ref||'/'||alt,flankingseq from sanger_flanking_sequence, feature where feature_abbrev=allele;

insert into variant_sequence (vseq_data_zdb_id,vseq_type,veq_offset_start,vseq_offset_stop,vseq_sequence,vseq_variation)
select distinct alleleid, 'Genomic',50,50,sequence,variation from sanger_flanking_sequence_post_proc;
