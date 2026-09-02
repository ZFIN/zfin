--liquibase formatted sql

-- ZFIN-10419: the RFLP / dCAPS restriction enzyme catalog box was one field
-- doing two jobs -- its placeholder literally read "vendor + cat #". Yvonne's
-- mockup splits it into two labelled boxes, Vendor and Catalog #, on the same
-- form row. This adds the column the Vendor box writes to.
--
-- Second time this field has been divided: 1182/0050-ZFIN-10162 notes that
-- "ga_restriction_enzyme is split into name + catalog", and now the catalog
-- half splits again.
--
-- Existing values are left alone. Anything already recorded stays in
-- ga_restriction_enzyme_catalog, including values that combine a vendor and a
-- catalog number, because there is no reliable way to split "NEB R0580S" from
-- "R0580S, New England Biolabs" automatically and no realistic sample data to
-- calibrate a parse against. Curators can separate them by editing the assay.
--
-- No constraint or validation: the ticket says "no validation needed on entry
-- boxes", and the sibling columns are plain TEXT for the same reason.

--changeset cmpich:ZFIN-10419-restriction-enzyme-vendor
ALTER TABLE zirc.genotyping_assay
    ADD COLUMN IF NOT EXISTS ga_restriction_enzyme_vendor TEXT;
--rollback ALTER TABLE zirc.genotyping_assay DROP COLUMN IF EXISTS ga_restriction_enzyme_vendor;
