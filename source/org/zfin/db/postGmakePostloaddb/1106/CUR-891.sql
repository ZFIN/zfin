--liquibase formatted sql
--changeset pm:CUR-891

update feature set feature_abbrev_order=zero_pad(feature_abbrev) where feature_abbrev_order=feature_abbrev;
update feature set feature_abbrev_order=zero_pad(feature_abbrev) where feature_abbrev like 'zf%';
update feature set feature_name_order=zero_pad(feature_name) where feature_abbrev like 'zf%';
