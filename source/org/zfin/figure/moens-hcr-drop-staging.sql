-- ZFIN-10330 / ZFINHELP-5462 : drop the Moens HCR staging tables.
--
-- NOT a liquibase changeset, and deliberately not part of the default load. As a
-- changeset in the 1184 migrations directory it would run in the same liquibase pass
-- as 0040 and drop the staging tables before the image load ever read them.
--
-- Run only once the loaded records have been checked:
--     ./gradlew loadMoensHcrImages -PdropStaging
--
-- Until then moens_hcr_expression_load still holds the 123 cleaned annotations and
-- moens_hcr_image_load_map still holds the image-stem -> figure mapping, so a load that
-- turns out wrong can be deleted and rebuilt from source.
--
-- Dropping the load map is also what ends MoensHcrImageLoad's re-runnability, which is
-- intended: the 105 figures exist and a second run must not create a duplicate set.

drop table if exists moens_hcr_expression_load;
drop table if exists moens_hcr_image_load_map;
