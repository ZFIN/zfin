--liquibase formatted sql
--changeset cmpich:ZFIN-9514

-- set feature_zdb_id when allele_name is found
update gene_allele_mutation_detail
set feature_zdb_id = (select feature_zdb_id
                      from feature
                      where feature_abbrev = allele_name)
where exists(select * from feature where feature_abbrev = allele_name)
;

-- set is all
update gene_allele_mutation_detail
set has_gene_allele = true
where exists(
              select *
              from feature_marker_relationship
              where fmrel_mrkr_zdb_id = zdb_id
                and fmrel_ftr_zdb_id = feature_zdb_id
                and fmrel_type = 'is allele of'
          )
;

-- set number of insertions
update gene_allele_mutation_detail set deleted_basePair=(select REGEXP_REPLACE(mutation, '^[-](\d+)[bp](.*)', '\1'));
update gene_allele_mutation_detail set deleted_basePair=null where deleted_basePair like '+%';
-- set number of deletions
update gene_allele_mutation_detail set inserted_basePair=(select REGEXP_REPLACE(mutation, '(.*)[+](\d*)bp', '\2'));
update gene_allele_mutation_detail set inserted_basePair=null where inserted_basePair like '-%';
--select cast(zko_id as decimal) as id, mutation, inserted_basePair, deleted_basePair from gene_allele_mutation_detail order by id asc;
update gene_allele_mutation_detail set inserted_basePair=null where trim(inserted_basePair) = '';
update gene_allele_mutation_detail set deleted_basePair=null where trim(deleted_basePair) = '';

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select feature_zdb_id, 'ZDB-PUB-191214-4', 'standard'
from gene_allele_mutation_detail
where feature_zdb_id is not null
ON CONFLICT DO NOTHING
;

-- insert feature Mutation detail records
--feature_dna_mutation_detail

select * from  feature_dna_mutation_detail, gene_allele_mutation_detail
where feature_dna_mutation_detail.fdmd_feature_zdb_id = gene_allele_mutation_detail.feature_zdb_id
;

select distinct feature_type from feature, gene_allele_mutation_detail
where feature.feature_zdb_id = gene_allele_mutation_detail.feature_zdb_id
;


create temp table feature_dna_mutation_detail_id as
select feature_zdb_id, get_id('FDMD') from gene_allele_mutation_detail
where feature_zdb_id is not null;

insert into zdb_active_data  select get_id from feature_dna_mutation_detail_id;

insert into feature_dna_mutation_detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_number_additional_dna_base_pairs, fdmd_number_removed_dna_base_pairs)
select get_id, ff.feature_zdb_id, cast(gg.inserted_basePair as integer), cast(gg.deleted_basePair as integer)
from feature_dna_mutation_detail_id as ff, gene_allele_mutation_detail as gg
where ff.feature_zdb_id = gg.feature_zdb_id
    and not exists(select *
                 from feature_dna_mutation_detail
                 where fdmd_feature_zdb_id = gg.feature_zdb_id
    )
;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select get_id, 'ZDB-PUB-191214-4', 'standard'
from feature_dna_mutation_detail_id
ON CONFLICT DO NOTHING
;
