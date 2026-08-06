--liquibase formatted sql
--changeset cmpich:0060-ZFIN-10330-moens-hcr-drop-staging

-- ZFIN-10330 / ZFINHELP-5462 : drop the Moens HCR staging tables.
--
-- Split out of migration 0050 so the source rows outlive the changeset that consumes
-- them. Until this runs, moens_hcr_expression_load still holds the 113 cleaned
-- annotations and moens_hcr_image_load_map still holds the image-stem -> figure
-- mapping, so a load that turns out wrong can be deleted and rebuilt from source.
--
-- Dropping the load map is also what ends MoensHcrImageLoad's re-runnability, which is
-- intended: the 102 figures exist and a second run must not create a duplicate set.
--
-- Note migration 0040 is runOnChange:true. If it is ever edited after this changeset has
-- run it will recreate both tables -- staging repopulated, the load map empty. That is
-- inert leftover scratch, not a second load, because 0050 will not re-run.

drop table if exists moens_hcr_expression_load;
drop table if exists moens_hcr_image_load_map;
