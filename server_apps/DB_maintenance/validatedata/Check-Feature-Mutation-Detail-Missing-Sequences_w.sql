-- Features whose feature_genomic_mutation_detail row is missing a sequence that the feature's type
-- requires: POINT_MUTATION / INDEL / MNV need both a reference and a variant sequence, DELETION
-- needs a reference, INSERTION needs a variant. Null and empty string are both treated as missing,
-- because the column currently holds both.
--
-- A missing sequence reaches the Alliance variant submission (org.zfin.marker.agr.BasicVariantInfo)
-- as a null, and a POINT_MUTATION with a reference base but no variant base -- ZDB-ALT-220927-6 --
-- crashed the whole Generate-Alliance-Files_m job outright. Only features carrying GRCz12tu
-- coordinates plus a reference accession are actually submitted, so submitted_to_alliance ranks
-- which rows matter now versus which are merely incomplete.
--
-- TODO: enforce this in the database so the rows cannot be written in the first place; this report
-- only notices them after the fact. It cannot be a plain CHECK constraint -- the rule depends on
-- feature.feature_type, which lives in another table -- so it needs a BEFORE INSERT OR UPDATE
-- trigger on feature_genomic_mutation_detail (that table already carries one for
-- fgmd_modified_at). Two things have to happen first, or the trigger will reject edits to rows
-- that are already broken: clear the backlog this report lists, and normalize the empty-string
-- sequences to null so there is a single representation of "missing". Curation-side validation in
-- FeatureValidationService.getMissingMutationDetailSequence already blocks the new-row case.
select feature_zdb_id,
       feature_abbrev,
       feature_type,
       case feature_type
         when 'DELETION'  then 'reference'
         when 'INSERTION' then 'variant'
         else
           case when coalesce(fgmd_sequence_of_reference, '') = ''
                     and coalesce(fgmd_sequence_of_variation, '') = '' then 'reference and variant'
                when coalesce(fgmd_sequence_of_reference, '') = '' then 'reference'
                else 'variant'
           end
       end as missing_sequence,
       case when exists (select 'x'
                           from sequence_feature_chromosome_location
                          where sfcl_feature_zdb_id = feature_zdb_id
                            and sfcl_assembly = 'GRCz12tu'
                            and sfcl_start_position is not null
                            and sfcl_end_position is not null
                            and coalesce(sfcl_chromosome_reference_accession_number, '') != '')
            then 'yes'
            else 'no'
       end as submitted_to_alliance
  from feature, feature_genomic_mutation_detail
 where fgmd_feature_zdb_id = feature_zdb_id
   and ((feature_type in ('POINT_MUTATION', 'INDEL', 'MNV')
         and (coalesce(fgmd_sequence_of_reference, '') = ''
              or coalesce(fgmd_sequence_of_variation, '') = ''))
        or (feature_type = 'DELETION' and coalesce(fgmd_sequence_of_reference, '') = '')
        or (feature_type = 'INSERTION' and coalesce(fgmd_sequence_of_variation, '') = ''))
 order by feature_type, feature_zdb_id;
