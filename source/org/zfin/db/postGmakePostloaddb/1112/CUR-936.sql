--liquibase formatted sql
--changeset prita:CUR-936.sql


delete from seqvar where trim(featureid) in (Select trim(fgmd_feature_zdb_id) from feature_genomic_mutation_detail);
create temp table featgenomemd (fgmdfeat text,fmmdid text,fgmdstrand text,fgmdseqref text,fgmdseqvar text,pub text);

insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub)
select distinct featureid,featureid,'+',refseq,varseq,recattrib_source_zdb_id
from seqvar,feature_dna_mutation_detail,record_attribution
where featureid=fdmd_feature_zdb_id and fdmd_zdb_id=recattrib_data_zdb_id;




update featgenomemd set fmmdid=get_id('FGMD');

insert into zdb_active_data (zactvd_zdb_id) select fmmdid from featgenomemd;

insert into feature_genomic_mutation_detail(fgmd_zdb_id,fgmd_feature_zdb_id,fgmd_sequence_of_reference,fgmd_sequence_of_variation,fgmd_variation_strand) select fmmdid,fgmdfeat,fgmdseqref,fgmdseqvar,fgmdstrand from featgenomemd;
insert into record_Attribution(recattrib_Data_Zdb_id,recattrib_source_zdb_id) select fmmdid,pub from featgenomemd;




