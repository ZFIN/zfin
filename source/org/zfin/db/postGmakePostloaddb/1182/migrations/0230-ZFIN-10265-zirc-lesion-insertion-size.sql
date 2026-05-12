--liquibase formatted sql

-- Add zirc.lesion.l_insertion_size_bp. Indel (delins) lesions have both a
-- deletion side and an insertion side; the existing l_lesion_size_bp stores
-- the deletion size for indel rows, and this new column captures the
-- insertion size. Other lesion types continue using l_lesion_size_bp alone.
--
--changeset rtaylor:zirc-lesion-insertion-size
ALTER TABLE zirc.lesion ADD COLUMN IF NOT EXISTS l_insertion_size_bp INTEGER;
