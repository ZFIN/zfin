--liquibase formatted sql
--changeset cmpich:ZFIN-ensembl-load.sql

update marker set mrkr_abbrev = 'cdk18-201', mrkr_name = 'cdk18-201' where mrkr_zdb_id = 'ZDB-TSCRIPT-141209-2211';
