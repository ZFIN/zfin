--liquibase formatted sql
--changeset cmpich:ZFIN-9505.sql

update blast_database set blastdb_description = 'Vega nucleotide sequences given to ZFIN through collaboration with Vega curators. Also includes WashUZ RNA sequences.'
where blastdb_abbrev = 'unreleasedRNA';
