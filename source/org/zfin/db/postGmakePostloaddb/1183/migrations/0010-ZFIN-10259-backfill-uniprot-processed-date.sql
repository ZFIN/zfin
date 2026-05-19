--liquibase formatted sql
--changeset rtaylor:ZFIN-10259-backfill-uniprot-processed-date

UPDATE uniprot_release
SET upr_processed_date = upr_date + INTERVAL '7 days',
    upr_notes = COALESCE(upr_notes || E'\n\n', '')
                || 'Backfill (ZFIN-10259): upr_processed_date set to upr_date + 7 days; original load did not record a processed_date.'
WHERE upr_processed_date IS NULL
  AND upr_id IN (1, 2, 3, 6, 14, 15);
