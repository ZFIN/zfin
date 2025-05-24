--liquibase formatted sql
--changeset cmpich:ZFIN-9514

-- use replaced data when gene is not found
create temp table temp_replaced_marker as
    (select distinct zdb_Id from gene_allele_mutation_detail
                   where not exists (
                       select * from marker where mrkr_zdb_id = zdb_id
                       )
                     and zdb_id is not null and zdb_id != ''
    )
;

select mrkr_zdb_id from marker, temp_replaced_marker, zdb_replaced_data
    where mrkr_zdb_id = zrepld_new_zdb_id
  and zrepld_old_zdb_id = zdb_id;

update gene_allele_mutation_detail as g
    set zdb_id = (
        select mrkr_zdb_id from marker, temp_replaced_marker as t, zdb_replaced_data
        where mrkr_zdb_id = zrepld_new_zdb_id
          and zrepld_old_zdb_id = t.zdb_id
                                      and t.zdb_id = g.zdb_id
                    )
where zdb_id in (select zdb_id from temp_replaced_marker)
;

-- set feature_zdb_id when allele_name is found
update gene_allele_mutation_detail
set feature_zdb_id = (select feature_zdb_id
                      from feature
                      where feature_abbrev = allele_name)
where exists(select * from feature where feature_abbrev = allele_name)
;

-- insert into temp table then create
create temp table temp_feature as
    (select get_id_and_insert_active_data('ALT') as id,
            allele_name as name
     from gene_allele_mutation_detail
     where allele_name is not null
       and feature_zdb_id is null
       and zdb_id is not null
       and zdb_id is not null )
;

insert into feature (feature_zdb_id, feature_abbrev, feature_name, feature_type)
    (select id,
            allele_name,
            allele_name,
            'INDEL'
     from gene_allele_mutation_detail, temp_feature
     where allele_name is not null
       and allele_name = name
       and feature_zdb_id is null
       and zdb_id is not null )
;
-- generate feature_marker_relationship records
insert into feature_marker_relationship (fmrel_zdb_id, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id, fmrel_type)
(select get_id_and_insert_active_data('FMREL'),
        id,
            zdb_id,
            'is allele of'
     from gene_allele_mutation_detail, temp_feature
     where allele_name = name
       and trim(zdb_id) != '' )
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
update gene_allele_mutation_detail
set deleted_basePair=(select REGEXP_REPLACE(mutation, '^[-](\d+)[bp](.*)', '\1'));
update gene_allele_mutation_detail
set deleted_basePair=null
where deleted_basePair like '+%';
-- set number of deletions
update gene_allele_mutation_detail
set inserted_basePair=(select REGEXP_REPLACE(mutation, '(.*)[+](\d*)bp', '\2'));
update gene_allele_mutation_detail
set inserted_basePair=null
where inserted_basePair like '-%';
--select cast(zko_id as decimal) as id, mutation, inserted_basePair, deleted_basePair from gene_allele_mutation_detail order by id asc;
update gene_allele_mutation_detail
set inserted_basePair=null
where trim(inserted_basePair) = '';
update gene_allele_mutation_detail
set deleted_basePair=null
where trim(deleted_basePair) = '';

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select feature_zdb_id, 'ZDB-PUB-191214-4', 'standard'
from gene_allele_mutation_detail
where feature_zdb_id is not null
ON CONFLICT DO NOTHING
;

-- insert feature Mutation detail records
--feature_dna_mutation_detail

select *
from feature_dna_mutation_detail,
     gene_allele_mutation_detail
where feature_dna_mutation_detail.fdmd_feature_zdb_id = gene_allele_mutation_detail.feature_zdb_id
;

select distinct feature_type
from feature,
     gene_allele_mutation_detail
where feature.feature_zdb_id = gene_allele_mutation_detail.feature_zdb_id
;

-- alleles that are not associated to the gene as in the allele file
select zko_id, symbol, zdb_id, allele_name, feature_zdb_id from gene_allele_mutation_detail where not exists (
    select * from feature_marker_relationship where fmrel_ftr_zdb_id = feature_zdb_id
    AND fmrel_mrkr_zdb_id = zdb_id
    )
and feature_zdb_id is not null
;

/*delete from gene_allele_mutation_detail where not exists (
    select * from feature_marker_relationship where fmrel_ftr_zdb_id = feature_zdb_id
    AND fmrel_mrkr_zdb_id = zdb_id
    )
and feature_zdb_id is not null
;
*/

create table feature_dna_mutation_detail_id as
select feature_zdb_id, get_id('FDMD')
from gene_allele_mutation_detail
where feature_zdb_id is not null;

insert into zdb_active_data
select get_id
from feature_dna_mutation_detail_id;

insert into feature_dna_mutation_detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_number_additional_dna_base_pairs, fdmd_number_removed_dna_base_pairs)
select get_id, ff.feature_zdb_id, cast(gg.inserted_basePair as integer), cast(gg.deleted_basePair as integer)
from feature_dna_mutation_detail_id as ff,
     gene_allele_mutation_detail as gg
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



create table temp_crispr as
select zdb_id, sequence1, regexp_replace(sequence1, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence2, regexp_replace(sequence2, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence3, regexp_replace(sequence3, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence4, regexp_replace(sequence4, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence5, regexp_replace(sequence5, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence6, regexp_replace(sequence6, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence7, regexp_replace(sequence7, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence8, regexp_replace(sequence8, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence9, regexp_replace(sequence9, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence10, regexp_replace(sequence10, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence11, regexp_replace(sequence11, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence12, regexp_replace(sequence12, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence13, regexp_replace(sequence13, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence14, regexp_replace(sequence14, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence15, regexp_replace(sequence15, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence16, regexp_replace(sequence16, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence17, regexp_replace(sequence17, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence18, regexp_replace(sequence18, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence19, regexp_replace(sequence19, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence20, regexp_replace(sequence20, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
union
select zdb_id, sequence21, regexp_replace(sequence21, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail
;

delete
from temp_crispr
where temp_crispr.sequence1 = '';

delete
from temp_crispr
where trim(temp_crispr.zdb_id) = '';



-- remove crispr with an already existing sequence in marker_sequence table
delete from
temp_crispr
where exists(
              select *
              from marker_sequence
              where temp_crispr.sequence1 = marker_sequence.seq_sequence
                 OR temp_crispr.sequence2 = marker_sequence.seq_sequence
          )
;

select *
from marker_relationship
where exists(
              select *
              from temp_crispr
              where temp_crispr.zdb_id = mrel_mrkr_2_zdb_id
                and mrel_type = 'knockdown reagent targets gene'
          )
;


select * from temp_crispr where exists (
    select * from gene_allele_mutation_detail
    where feature_zdb_id != ''
              AND temp_crispr.zdb_id = gene_allele_mutation_detail.zdb_id
    )
;


create table crispr_name_index as
select gene.mrkr_zdb_id as gene_zdb_id,
       gene.mrkr_abbrev as gene_abbrev,
       str.mrkr_zdb_id as crispr_zdb_id,
       str.mrkr_abbrev, regexp_replace(str.mrkr_abbrev, '^CRISPR([0-9]+)[-](.*)', '\1') as crispr_Index_ID,
       regexp_replace(str.mrkr_abbrev, '^CRISPR([0-9]+)[-](.*)', '\2') as crispr_target_name
from marker_relationship, marker as gene, marker as str, temp_crispr
where mrel_mrkr_2_zdb_id = gene.mrkr_zdb_id
                                                  and temp_crispr.zdb_id = mrel_mrkr_2_zdb_id
                                                  and mrel_type = 'knockdown reagent targets gene'
                                    and get_obj_type( mrel_mrkr_1_zdb_id)= 'CRISPR'
and str.mrkr_zdb_id = mrel_mrkr_1_zdb_id
;

create table crispr_name_index_max as
select gene_zdb_id, crispr_target_name, max(NULLIF(crispr_Index_ID, '')::int) as max_index
from crispr_name_index
group by gene_zdb_id, crispr_target_name
order by 1
    ;

delete from crispr_name_index_max where exists (
    select * from marker where mrkr_zdb_id = gene_zdb_id
    and crispr_target_name != marker.mrkr_abbrev )
;


create table crispr_id as
select zdb_id, sequence2, get_id('CRISPR')
from temp_crispr
    ;

insert into zdb_active_data
select get_id
from crispr_id;

create table str_index as
    select gene_zdb_id, max_index from crispr_name_index_max
;

--select create_str_marker('CRISPR', gene_zdb_id, crispr_target_name , 'ZDB-PERS-100329-1') from crispr_name_index_max


--drop table crispr_name_index;
--drop table crispr_name_index_max;

--drop table temp_crispr;
