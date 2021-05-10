--liquibase formatted sql
--changeset cmpich:ZFIN-6779.sql

delete from term_xref where tx_accession like 'ZDB-STAGE%';

delete from term_xref where tx_accession like 'ZDB-TERM%';


