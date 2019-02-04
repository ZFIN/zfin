--liquibase formatted sql
--changeset sierra:fcc_audit.sql

create table feature_community_contribution_audit(fcc_feature_zdb_id text,
	fcc_functional_consequence text,
	fcc_adult_viable boolean,
	fcc_maternal_zygosity_examined boolean,
	fcc_currently_available boolean,
	fcc_other_line_information text,
	fcc_date_added timestamp);

