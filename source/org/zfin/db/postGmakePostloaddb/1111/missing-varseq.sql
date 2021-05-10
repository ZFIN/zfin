--liquibase formatted sql
--changeset pm:missing-varseq

update feature_genomic_mutation_detail set fgmd_sequence_of_reference = (select seqref from missing_varseq  where fgmd_zdb_id=fgmdid)
from missing_varseq
where fgmdid=fgmd_zdb_id and  length(fgmd_sequence_of_reference)=0;


drop table if exists tmp_seqref;
create table tmp_seqref(
        featzdbid text ,
        seqrefzdbid text,
        seqreftext text);

insert into tmp_seqref(
        featzdbid,seqrefzdbid,seqreftext)

  select distinct featurezdb, fgmdid, seqref
    from missing_varseq
   where fgmdid like 'ZDB-ALT%';




update tmp_seqref set seqrefzdbid = get_id('FGMD');


insert into zdb_active_data select seqrefzdbid from tmp_seqref;

insert into feature_genomic_mutation_detail (fgmd_zdb_id,fgmd_feature_zdb_id,fgmd_sequence_of_reference)
  select seqrefzdbid,featzdbid,seqreftext from tmp_seqref;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select seqrefzdbid,'ZDB-PUB-191030-9' from tmp_seqref;


