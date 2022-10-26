--liquibase formatted sql
--changeset rtaylor:ZFIN-8231-pre

CREATE SCHEMA thisse;
COMMENT ON SCHEMA thisse IS 'This schema was created to facilitate the import of legacy information from Thisse FileMaker Pro databases.';

CREATE TABLE thisse.thisse_plates_march17 (
	count text,
	clone_name text,
	original_name text,
	stars text,
	gene_name text,
	gene_code text,
	sequence text,
	size text,
	sending text,
	pattern text,
	plate1 text,
	plate2 text,
	plates text,
	marker text,
	zfin_name text,
	zfin_page text,
	morpholino text,
	top_blast text,
	test text,
	go_component text,
	go_function text,
	go_process text,
	plate1data text,
	plate2data text,
	plate1filename text,
	plate2filename text
);

