-- ZFIN-10371: features that simultaneously have
--   (a) "Assembly information not known as of"  -> feature.ftr_chr_info_date
--   (b) genome location information             -> sequence_feature_chromosome_location
-- These two states contradict each other: the date says the location is unknown,
-- but a location row exists anyway.

-- 1. How many, and how many carry a populated assembly specifically.
select count(*)                                              as with_location_row,
       count(nullif(trim(coalesce(l.sfcl_assembly, '')), '')) as with_assembly_populated,
       count(*) filter (where l.sfcl_start_position is null
                           or l.sfcl_end_position   is null)  as missing_coordinates
from   feature f
join   sequence_feature_chromosome_location l
       on l.sfcl_feature_zdb_id = f.feature_zdb_id
where  f.ftr_chr_info_date is not null;

-- 2. The listing.
select f.feature_abbrev            as feature,
       f.feature_zdb_id,
       f.feature_type,
       f.ftr_chr_info_date::date   as not_known_as_of,
       l.sfcl_assembly             as assembly,
       l.sfcl_chromosome           as chr,
       l.sfcl_start_position       as start_pos,
       l.sfcl_end_position         as end_pos,
       l.sfcl_zdb_id
from   feature f
join   sequence_feature_chromosome_location l
       on l.sfcl_feature_zdb_id = f.feature_zdb_id
where  f.ftr_chr_info_date is not null
order  by f.ftr_chr_info_date desc, f.feature_abbrev;

-- 3. Narrower reading, if "assembly information filled in" means the assembly
--    column itself rather than merely having a location row.
select f.feature_abbrev, f.feature_zdb_id, f.ftr_chr_info_date::date as not_known_as_of,
       l.sfcl_assembly, l.sfcl_chromosome, l.sfcl_start_position, l.sfcl_end_position
from   feature f
join   sequence_feature_chromosome_location l
       on l.sfcl_feature_zdb_id = f.feature_zdb_id
where  f.ftr_chr_info_date is not null
  and  coalesce(trim(l.sfcl_assembly), '') <> ''
order  by f.ftr_chr_info_date desc, f.feature_abbrev;
