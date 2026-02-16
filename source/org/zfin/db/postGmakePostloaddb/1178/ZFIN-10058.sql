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

-- set feature_zdb_id when allele_name is found (check both uppercase and lowercase versions)
update gene_allele_mutation_detail
set feature_zdb_id = (select feature_zdb_id
                      from feature
                      where feature_abbrev = lower(substring(allele_name, 1, 3)) || substring(allele_name, 4)
                         OR feature_abbrev = allele_name)
where exists(select * from feature
             where feature_abbrev = lower(substring(allele_name, 1, 3)) || substring(allele_name, 4)
                OR feature_abbrev = allele_name)
;

-- insert into temp table then create
create temp table temp_feature as
    (select get_id_and_insert_active_data('ALT') as id,
            allele_name as name
     from gene_allele_mutation_detail
     where allele_name is not null
       and feature_zdb_id is null
       and zdb_id is not null )
;

insert into feature (feature_zdb_id, feature_abbrev, feature_name, feature_type)
    (select id,
            lower(substring(allele_name, 1, 3)) || substring(allele_name, 4),  -- Convert ZKO to zko
            lower(substring(allele_name, 1, 3)) || substring(allele_name, 4),  -- Convert ZKO to zko
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

insert into feature_assay (featassay_feature_zdb_id,featassay_mutagee, featassay_mutagen)
(
select id, 'embryos', g.type from temp_feature, gene_allele_mutation_detail g where temp_feature.name = g.allele_name
)
;

update gene_allele_mutation_detail set feature_zdb_id = (
    select id from temp_feature
    where name = allele_name
)
where feature_zdb_id is null
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
-- fix case where mutation is '+1bp,-1bp' resulting in '1,-1bp'
update gene_allele_mutation_detail
set inserted_basePair = REGEXP_REPLACE(inserted_basePair, '^(\d+),.*', '\1')
where inserted_basePair like '%,%';

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select feature_zdb_id, 'ZDB-PUB-250106-1', 'standard'
from gene_allele_mutation_detail
where feature_zdb_id is not null
ON CONFLICT DO NOTHING
;

-- set feature type:
-- insertion
-- deletion
-- indel
update feature as f set feature_type = (
    select case
               when inserted_basePair is not null and deleted_basePair is null then 'INSERTION'
               when inserted_basePair is null and deleted_basePair is not null then 'DELETION'
               when inserted_basePair is not null and deleted_basePair is not null then 'INDEL'
               else 'complex'
               end
    from gene_allele_mutation_detail as ga
    where ga.feature_zdb_id = f.feature_zdb_id
      and ga.feature_zdb_id is not null
)
where exists (select * from gene_allele_mutation_detail as ga
              where ga.feature_zdb_id = f.feature_zdb_id
                and ga.feature_zdb_id is not null
                and (inserted_basePair is not null or deleted_basePair is not null)
)
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

update gene_allele_mutation_detail set exons = null where exons like '%&%';

-- remove complex entries: if '&' is used in the intron or exon field
update gene_allele_mutation_detail
set exons = null
where exons like '%&%';
-- update gene_allele_mutation_detail
-- set deleted_basepair = null
-- where deleted_basepair like '%&%';
update gene_allele_mutation_detail
set exons = null
where exons like '%-%';

update gene_allele_mutation_detail
set inserted_basepair = null
where inserted_basepair !~ '^[0-9]+$';

delete from feature_dna_mutation_detail_id;

insert into feature_dna_mutation_detail_id
select feature_zdb_id, get_id('FDMD')
from gene_allele_mutation_detail
where feature_zdb_id is not null;

insert into zdb_active_data
select get_id
from feature_dna_mutation_detail_id;

insert into feature_dna_mutation_detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_number_additional_dna_base_pairs, fdmd_number_removed_dna_base_pairs, fdmd_exon_number)
select get_id, ff.feature_zdb_id, cast(gg.inserted_basePair as integer), cast(gg.deleted_basePair as integer), cast(gg.exons as integer)
from feature_dna_mutation_detail_id as ff,
     gene_allele_mutation_detail as gg
where ff.feature_zdb_id = gg.feature_zdb_id
  and not exists(select *
                 from feature_dna_mutation_detail
                 where fdmd_feature_zdb_id = gg.feature_zdb_id
)
;

-- populate fdmd_gene_localization_term_zdb_id for 5' UTR introns
update feature_dna_mutation_detail
set fdmd_gene_localization_term_zdb_id = mdcv.mdcv_term_zdb_id
from gene_allele_mutation_detail g, mutation_detail_controlled_vocabulary mdcv
where feature_dna_mutation_detail.fdmd_feature_zdb_id = g.feature_zdb_id
  and mdcv.mdcv_term_display_name = '5'' UTR'
  and g.introns LIKE '5%UTR'
  and feature_dna_mutation_detail.fdmd_gene_localization_term_zdb_id is null
;

-- populate fdmd_gene_localization_term_zdb_id = 'exon' for records with exon numbers
update feature_dna_mutation_detail
set fdmd_gene_localization_term_zdb_id = 'ZDB-TERM-130401-150'
from gene_allele_mutation_detail g
where feature_dna_mutation_detail.fdmd_feature_zdb_id = g.feature_zdb_id
  and feature_dna_mutation_detail.fdmd_exon_number is not null
  and feature_dna_mutation_detail.fdmd_gene_localization_term_zdb_id is null
;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select get_id, 'ZDB-PUB-250106-1', 'standard'
from feature_dna_mutation_detail_id
ON CONFLICT DO NOTHING
;

-- add data supplier for each new feature
insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num)
select feature_zdb_id, 'ZDB-LAB-130226-1', alias
from gene_allele_mutation_detail
where feature_zdb_id is not null
ON CONFLICT DO NOTHING
;

-- fix typo in marker abbreviation and name: CRISRP -> CRISPR
update marker
set mrkr_abbrev = replace(mrkr_abbrev, 'CRISRP', 'CRISPR'),
    mrkr_name = replace(mrkr_name, 'CRISRP', 'CRISPR')
where mrkr_abbrev like 'CRISRP%';


drop table if exists temp_crispr;

create table temp_crispr as
-- sequence1 for CRISPR type
select zko_id, zdb_id, sequence1, regexp_replace(sequence1, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail where type = 'CRISPR' and sequence1 is not null and trim(sequence1) != '' and sequence1 != '-'
union
-- sequence2 for CRISPR type (when both sequences are provided)
select zko_id, zdb_id, sequence2, regexp_replace(sequence2, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail where type = 'CRISPR' and sequence2 is not null and trim(sequence2) != '' and sequence2 != '-'
union
-- sequence2 for CRISPRi type
select zko_id, zdb_id, sequence2, regexp_replace(sequence2, '(,*)[a-z]{3}', '\1') as sequence2
from crispr_detail where type = 'CRISPRi' and sequence2 is not null and trim(sequence2) != '' and sequence2 != '-'
;

delete
from temp_crispr
where temp_crispr.sequence1 = '';

delete
from temp_crispr
where trim(temp_crispr.zdb_id) = '';


-- associate existing CRISPRs to features before removing them from temp_crispr
-- create temp table with feature/crispr pairs that need relationships
drop table if exists temp_existing_crispr_fmrel;
create table temp_existing_crispr_fmrel as
select distinct g.feature_zdb_id, ms.seq_mrkr_zdb_id as crispr_zdb_id, get_id('FMREL') as fmrel_id
from temp_crispr tc
join gene_allele_mutation_detail g on g.zko_id = tc.zko_id  -- Join by zko_id for correct feature-CRISPR association
join marker_sequence ms on (tc.sequence1 = ms.seq_sequence OR tc.sequence2 = ms.seq_sequence)
where g.feature_zdb_id is not null
  and get_obj_type(ms.seq_mrkr_zdb_id) = 'CRISPR'
  and not exists (
    select 1 from feature_marker_relationship
    where fmrel_ftr_zdb_id = g.feature_zdb_id
      and fmrel_mrkr_zdb_id = ms.seq_mrkr_zdb_id
      and fmrel_type = 'created by'
  )
;

-- insert the new fmrel IDs into zdb_active_data
insert into zdb_active_data (zactvd_zdb_id)
select fmrel_id from temp_existing_crispr_fmrel
on conflict do nothing
;

-- create feature_marker_relationship for existing CRISPRs
insert into feature_marker_relationship (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)
select fmrel_id, 'created by', feature_zdb_id, crispr_zdb_id
from temp_existing_crispr_fmrel
ON CONFLICT DO NOTHING
;

-- associate existing CRISPRs when gene ID was replaced (e.g., trappc9 case)
-- This handles cases where crispr_detail has old gene ID but CRISPR targets the replaced gene
drop table if exists temp_existing_crispr_replaced_gene;
create table temp_existing_crispr_replaced_gene as
select distinct g.feature_zdb_id, mr.mrel_mrkr_1_zdb_id as crispr_zdb_id, get_id('FMREL') as fmrel_id
from crispr_detail cd
join zdb_replaced_data zrd on zrd.zrepld_old_zdb_id = cd.zdb_id
join gene_allele_mutation_detail g on g.zko_id = cd.zko_id  -- Join by zko_id for correct feature-CRISPR association
join marker_sequence ms on (
    regexp_replace(cd.sequence1, '[a-z]{3}$', '') = ms.seq_sequence
    OR regexp_replace(cd.sequence2, '[a-z]{3}$', '') = ms.seq_sequence
)
join marker_relationship mr on mr.mrel_mrkr_1_zdb_id = ms.seq_mrkr_zdb_id
    and mr.mrel_mrkr_2_zdb_id = zrd.zrepld_new_zdb_id
    and mr.mrel_type = 'knockdown reagent targets gene'
where g.feature_zdb_id is not null
  and cd.type IN ('CRISPR', 'CRISPRi')
  and get_obj_type(ms.seq_mrkr_zdb_id) = 'CRISPR'
  and not exists (
    select 1 from feature_marker_relationship
    where fmrel_ftr_zdb_id = g.feature_zdb_id
      and fmrel_mrkr_zdb_id = mr.mrel_mrkr_1_zdb_id
      and fmrel_type = 'created by'
  )
;

insert into zdb_active_data (zactvd_zdb_id)
select fmrel_id from temp_existing_crispr_replaced_gene
on conflict do nothing
;

insert into feature_marker_relationship (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)
select fmrel_id, 'created by', feature_zdb_id, crispr_zdb_id
from temp_existing_crispr_replaced_gene
ON CONFLICT DO NOTHING
;

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



-- use replaced data when gene is not found
create temp table temp_replaced_marker_cr as
    (select distinct zdb_Id from temp_crispr
     where not exists (
         select * from marker where mrkr_zdb_id = zdb_id
     )
       and zdb_id is not null and zdb_id != ''
    )
;

select mrkr_zdb_id from marker, temp_replaced_marker_cr, zdb_replaced_data
where mrkr_zdb_id = zrepld_new_zdb_id
  and zrepld_old_zdb_id = zdb_id;

update temp_crispr as g
set zdb_id = (
    select mrkr_zdb_id from marker, temp_replaced_marker_cr as t, zdb_replaced_data
    where mrkr_zdb_id = zrepld_new_zdb_id
      and zrepld_old_zdb_id = t.zdb_id
      and t.zdb_id = g.zdb_id
)
where zdb_id in (select zdb_id from temp_replaced_marker_cr)
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

drop table crispr_name_index;

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

drop table crispr_name_index_max;

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

drop table crispr_id;

create table crispr_id as
select zko_id, zdb_id, sequence2, get_id('CRISPR')
from temp_crispr
;

insert into zdb_active_data
select get_id
from crispr_id;

drop table str_index ;

create table str_index as
select gene_zdb_id, max_index from crispr_name_index_max
;

insert into str_index (gene_zdb_id, max_index)
select distinct (zdb_id), 0 from temp_crispr where not exists (select * from str_index where str_index.gene_zdb_id = temp_crispr.zdb_id);
-- have to wait until this function is created / deployed into the database
select create_str_marker('CRISPR', 'ZDB-PERS-981201-7') from crispr_name_index_max;

-- TALEN records
drop table if exists temp_talen;

create table temp_talen as
select zko_id, zdb_id, trim(sequence1)as sequence1, trim(sequence2) as sequence2
from crispr_detail where type = 'TALEN';

delete from temp_talen where temp_talen.sequence1 = '';
delete from temp_talen where trim(temp_talen.zdb_id) = '';

-- associate existing TALENs to features before removing them from temp_talen
-- create temp table with feature/TALEN pairs that need relationships
drop table if exists temp_existing_talen_fmrel;
create table temp_existing_talen_fmrel as
select distinct g.feature_zdb_id, ms.seq_mrkr_zdb_id as talen_zdb_id, get_id('FMREL') as fmrel_id
from temp_talen tt
join gene_allele_mutation_detail g on g.zko_id = tt.zko_id  -- Join by zko_id for correct feature-TALEN association
join marker_sequence ms on (tt.sequence1 = ms.seq_sequence OR tt.sequence2 = ms.seq_sequence)
where g.feature_zdb_id is not null
  and get_obj_type(ms.seq_mrkr_zdb_id) = 'TALEN'
  and not exists (
    select 1 from feature_marker_relationship
    where fmrel_ftr_zdb_id = g.feature_zdb_id
      and fmrel_mrkr_zdb_id = ms.seq_mrkr_zdb_id
      and fmrel_type = 'created by'
  )
;

-- insert the new fmrel IDs into zdb_active_data
insert into zdb_active_data (zactvd_zdb_id)
select fmrel_id from temp_existing_talen_fmrel
on conflict do nothing
;

-- create feature_marker_relationship for existing TALENs
insert into feature_marker_relationship (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)
select fmrel_id, 'created by', feature_zdb_id, talen_zdb_id
from temp_existing_talen_fmrel
ON CONFLICT DO NOTHING
;

-- remove TALEN with an already existing sequence in marker_sequence table
delete from temp_talen
where exists(
    select *
    from marker_sequence
    where temp_talen.sequence1 = marker_sequence.seq_sequence
       OR temp_talen.sequence2 = marker_sequence.seq_sequence
);

-- use replaced data when gene is not found
drop table if exists temp_replaced_marker_talen;
create temp table temp_replaced_marker_talen as
    (select distinct zdb_id from temp_talen
     where not exists (
         select * from marker where mrkr_zdb_id = zdb_id
     )
       and zdb_id is not null and zdb_id != ''
    );

update temp_talen as g
set zdb_id = (
    select mrkr_zdb_id from marker, temp_replaced_marker_talen as t, zdb_replaced_data
    where mrkr_zdb_id = zrepld_new_zdb_id
      and zrepld_old_zdb_id = t.zdb_id
      and t.zdb_id = g.zdb_id
)
where zdb_id in (select zdb_id from temp_replaced_marker_talen);

drop table if exists talen_name_index;

create table talen_name_index as
select gene.mrkr_zdb_id as gene_zdb_id,
       gene.mrkr_abbrev as gene_abbrev,
       str.mrkr_zdb_id as talen_zdb_id,
       str.mrkr_abbrev, regexp_replace(str.mrkr_abbrev, '^TALEN([0-9]+)-(.*)', '\1') as talen_Index_ID,
       regexp_replace(str.mrkr_abbrev, '^TALEN([0-9]+)-(.*)', '\2') as talen_target_name
from marker_relationship, marker as gene, marker as str, temp_talen
where mrel_mrkr_2_zdb_id = gene.mrkr_zdb_id
  and temp_talen.zdb_id = mrel_mrkr_2_zdb_id
  and mrel_type = 'knockdown reagent targets gene'
  and get_obj_type(mrel_mrkr_1_zdb_id) = 'TALEN'
  and str.mrkr_zdb_id = mrel_mrkr_1_zdb_id;

drop table if exists talen_name_index_max;

create table talen_name_index_max as
select gene_zdb_id, talen_target_name, max(NULLIF(talen_Index_ID, '')::int) as max_index
from talen_name_index
where talen_Index_ID ~ '^[0-9]+$'
group by gene_zdb_id, talen_target_name
order by 1;

delete from str_index;

-- add TALEN genes to str_index
insert into str_index (gene_zdb_id, max_index)
select gene_zdb_id, max_index from talen_name_index_max
where not exists (select * from str_index where str_index.gene_zdb_id = talen_name_index_max.gene_zdb_id);

insert into str_index (gene_zdb_id, max_index)
select distinct (zdb_id), 0 from temp_talen where not exists (select * from str_index where str_index.gene_zdb_id = temp_talen.zdb_id);

-- Create talen_id table (one row per gene with both sequences)
drop table if exists talen_id;
create table talen_id as
select zko_id, zdb_id, sequence1 as sequence2, get_id('TALEN') as get_id
from temp_talen;

-- Insert TALEN data into crispr_id so create_str_marker can find it
insert into crispr_id (zko_id, zdb_id, sequence2, get_id)
select zko_id, zdb_id, sequence2, get_id from talen_id;

-- Insert TALEN IDs into zdb_active_data
insert into zdb_active_data (zactvd_zdb_id)
select get_id from talen_id
on conflict do nothing;

-- have to wait until this function is created
select create_str_marker('TALEN', 'ZDB-PERS-981201-7') from talen_name_index_max;

--drop table crispr_name_index;
--drop table crispr_name_index_max;

--drop table temp_crispr;

-- Populate chromosome location for new features by deriving from associated gene
-- This enables the chromosome facet in the search results
insert into sequence_feature_chromosome_location_generated (
    sfclg_data_zdb_id,
    sfclg_chromosome,
    sfclg_location_source,
    sfclg_assembly
)
select distinct
    f.feature_zdb_id,
    ul.ul_chromosome,
    'General Load',
    'GRCz11'
from feature f
join feature_marker_relationship fmr on fmr.fmrel_ftr_zdb_id = f.feature_zdb_id
    and fmr.fmrel_type = 'is allele of'
join unique_location ul on ul.ul_data_zdb_id = fmr.fmrel_mrkr_zdb_id
where f.feature_zdb_id like 'ZDB-ALT-260201%'
  and ul.ul_chromosome is not null
  and not exists (
    select 1 from sequence_feature_chromosome_location_generated
    where sfclg_data_zdb_id = f.feature_zdb_id
  )
;

