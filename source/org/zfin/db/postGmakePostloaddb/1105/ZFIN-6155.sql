--liquibase formatted sql
--changeset pm:ZFIN-6155

create temp table sfclg (zdb text,ftr text,st integer,endloc integer,assembly text,chr integer);

insert into sfclg values ('ZDB-ALT-180131-2','ZDB-ALT-180131-2',29349668,29349674,'GRCz11',19);
update sfclg set zdb=get_id('SFCL');
insert into zdb_active_Data select zdb from sfclg;
insert into sequence_feature_chromosome_location (sfcl_zdb_id, sfcl_feature_zdb_id,sfcl_start_position,sfcl_end_position,sfcl_assembly,sfcl_chromosome,sfcl_evidence_code) select zdb,ftr,st,endloc,assembly,chr,'ZDB-TERM-170419-250' from sfclg;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select zdb,'ZDB-PUB-180131-10' from sfclg;


