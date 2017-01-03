--liquibase formatted sql
--changeset sierra:removeZdbReplacedDataRecord

delete from zdb_replaced_data
 where zrepld_old_Zdb_id = 'ZDB-GENEP-090511-3'
and zrepld_new_zdb_id = 'ZDB-GENE-110921-3';
