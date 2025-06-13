--liquibase formatted sql
--changeset rtaylor:ZFIN-9723

-- Fix feature abbreviation order
update feature set feature_abbrev_order = zero_pad(feature_abbrev_order) where feature_abbrev_order <> zero_pad(feature_abbrev_order);

