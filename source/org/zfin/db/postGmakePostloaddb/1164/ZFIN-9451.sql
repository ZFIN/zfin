--liquibase formatted sql
--changeset rtaylor:ZFIN-9451.sql

-- Remove based on curation review:
delete from db_link where dblink_acc_num like 'C173-A2' and dblink_zdb_id = 'ZDB-DBLINK-120828-1' and dblink_linked_recid = 'ZDB-TGCONSTRCT-111118-4' and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36';

-- Fix note:
update construct set construct_comments='The BAC clone zC173-A2 was used to create this construct.' where construct_zdb_id = 'ZDB-TGCONSTRCT-111118-4';
update marker set mrkr_comments = 'The BAC clone zC173-A2 was used to create this construct.' where mrkr_zdb_id = 'ZDB-TGCONSTRCT-111118-4';

-- These are duplicates of existing dblinks that have the same acc_num but without the version on the end
delete from db_link where dblink_linked_recid = 'ZDB-GENE-110208-1' and dblink_acc_num = 'JQ340773.1' and dblink_info = 'David Fashena 02/28/2012' and dblink_acc_num_display = 'JQ340773.1' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37' AND dblink_zdb_id = 'ZDB-DBLINK-120228-22';
delete from db_link where dblink_linked_recid = 'ZDB-GENE-050208-317' and dblink_acc_num = 'JN106182.1' and dblink_info = 'David Fashena 05/29/2012' and dblink_acc_num_display = 'JN106182.1' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37' AND dblink_zdb_id = 'ZDB-DBLINK-120529-30';
delete from db_link where dblink_linked_recid = 'ZDB-GENE-131101-1' and dblink_acc_num = 'CU633908.1' and dblink_info = 'Amy Singer 11/01/2013' and dblink_acc_num_display = 'CU633908.1' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' AND dblink_zdb_id = 'ZDB-DBLINK-131101-2';
delete from db_link where dblink_linked_recid = 'ZDB-GENE-081113-2' and dblink_acc_num = 'CU929052.1' and dblink_info = 'Amy Singer 11/25/2013' and dblink_acc_num_display = 'CU929052.1' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' AND dblink_zdb_id = 'ZDB-DBLINK-131125-11';
delete from db_link where dblink_linked_recid = 'ZDB-GENE-140820-7' and dblink_acc_num = 'EH440805.1' and dblink_info = 'Amy Singer 08/20/2014' and dblink_acc_num_display = 'EH440805.1' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37' AND dblink_zdb_id = 'ZDB-DBLINK-140820-6';
delete from db_link where dblink_linked_recid = 'ZDB-GENE-200107-1' and dblink_acc_num = 'BX248331.13' and dblink_info IS NULL and dblink_acc_num_display = 'BX248331.13' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37' AND dblink_zdb_id = 'ZDB-DBLINK-200107-14';
delete from db_link where dblink_linked_recid = 'ZDB-GENE-090303-5' and dblink_acc_num = 'BX323596.2' and dblink_info IS NULL and dblink_acc_num_display = 'BX323596.2' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37' AND dblink_zdb_id = 'ZDB-DBLINK-200107-17';
delete from db_link WHERE dblink_zdb_id = 'ZDB-DBLINK-200107-14';

-- These need to be updated to remove the version number from the acc_num

UPDATE db_link SET dblink_acc_num = 'LO017791' WHERE dblink_zdb_id = 'ZDB-DBLINK-220914-7';
update db_link set dblink_acc_num = 'AL928727' where dblink_zdb_id = 'ZDB-DBLINK-210122-21' and dblink_acc_num = 'AL928727.8';
update db_link set dblink_acc_num = 'AL935186' where dblink_zdb_id = 'ZDB-DBLINK-180607-2' and dblink_acc_num = 'AL935186.22';
update db_link set dblink_acc_num = 'AL935186' where dblink_zdb_id = 'ZDB-DBLINK-180607-4' and dblink_acc_num = 'AL935186.22';
update db_link set dblink_acc_num = 'AL935186' where dblink_zdb_id = 'ZDB-DBLINK-180607-5' and dblink_acc_num = 'AL935186.22';
update db_link set dblink_acc_num = 'BC142879' where dblink_zdb_id = 'ZDB-DBLINK-200128-3' and dblink_acc_num = 'BC142879.1';
update db_link set dblink_acc_num = 'BX005088' where dblink_zdb_id = 'ZDB-DBLINK-131108-122' and dblink_acc_num = 'BX005088.4';
update db_link set dblink_acc_num = 'BX323800' where dblink_zdb_id = 'ZDB-DBLINK-131108-119' and dblink_acc_num = 'BX323800.8';
update db_link set dblink_acc_num = 'BX510305' where dblink_zdb_id = 'ZDB-DBLINK-131108-121' and dblink_acc_num = 'BX510305.5';
update db_link set dblink_acc_num = 'BX530026' where dblink_zdb_id = 'ZDB-DBLINK-131108-127' and dblink_acc_num = 'BX530026.5';
update db_link set dblink_acc_num = 'BX649257' where dblink_zdb_id = 'ZDB-DBLINK-131108-120' and dblink_acc_num = 'BX649257.8';
update db_link set dblink_acc_num = 'BX957306' where dblink_zdb_id = 'ZDB-DBLINK-210122-22' and dblink_acc_num = 'BX957306.12';
update db_link set dblink_acc_num = 'BX957306' where dblink_zdb_id = 'ZDB-DBLINK-210122-23' and dblink_acc_num = 'BX957306.12';
update db_link set dblink_acc_num = 'CABZ01079817' where dblink_zdb_id = 'ZDB-DBLINK-160216-1' and dblink_acc_num = 'CABZ01079817.1';
update db_link set dblink_acc_num = 'CABZ01084347' where dblink_zdb_id = 'ZDB-DBLINK-131125-6' and dblink_acc_num = 'CABZ01084347.1';
update db_link set dblink_acc_num = 'CABZ01101980' where dblink_zdb_id = 'ZDB-DBLINK-150910-13' and dblink_acc_num = 'CABZ01101980.1';
update db_link set dblink_acc_num = 'CR354435' where dblink_zdb_id = 'ZDB-DBLINK-180104-1' and dblink_acc_num = 'CR354435.5';
update db_link set dblink_acc_num = 'CR550308' where dblink_zdb_id = 'ZDB-DBLINK-230404-1' and dblink_acc_num = 'CR550308.6';
update db_link set dblink_acc_num = 'CR762428' where dblink_zdb_id = 'ZDB-DBLINK-201001-1' and dblink_acc_num = 'CR762428.7';
update db_link set dblink_acc_num = 'CR762428' where dblink_zdb_id = 'ZDB-DBLINK-201001-3' and dblink_acc_num = 'CR762428.7';
update db_link set dblink_acc_num = 'CR762428' where dblink_zdb_id = 'ZDB-DBLINK-201001-4' and dblink_acc_num = 'CR762428.7';
update db_link set dblink_acc_num = 'CT033790' where dblink_zdb_id = 'ZDB-DBLINK-200107-13' and dblink_acc_num = 'CT033790.16';
update db_link set dblink_acc_num = 'CU457819' where dblink_zdb_id = 'ZDB-DBLINK-180104-2' and dblink_acc_num = 'CU457819.2';
update db_link set dblink_acc_num = 'CU459186' where dblink_zdb_id = 'ZDB-DBLINK-180104-5' and dblink_acc_num = 'CU459186.3';
update db_link set dblink_acc_num = 'CU929237' where dblink_zdb_id = 'ZDB-DBLINK-140822-18' and dblink_acc_num = 'CU929237.12';
update db_link set dblink_acc_num = 'EB956894' where dblink_zdb_id = 'ZDB-DBLINK-181207-1' and dblink_acc_num = 'EB956894.1';
update db_link set dblink_acc_num = 'EG579528' where dblink_zdb_id = 'ZDB-DBLINK-180219-1' and dblink_acc_num = 'EG579528.1';
update db_link set dblink_acc_num = 'FP236812' where dblink_zdb_id = 'ZDB-DBLINK-180104-4' and dblink_acc_num = 'FP236812.2';
update db_link set dblink_acc_num = 'GDQQ01005499' where dblink_zdb_id = 'ZDB-DBLINK-200108-2' and dblink_acc_num = 'GDQQ01005499.1';
update db_link set dblink_acc_num = 'GDQQ01033549' where dblink_zdb_id = 'ZDB-DBLINK-200624-1' and dblink_acc_num = 'GDQQ01033549.1';
update db_link set dblink_acc_num = 'GFIL01021740' where dblink_zdb_id = 'ZDB-DBLINK-230518-4' and dblink_acc_num = 'GFIL01021740.1';

-- These have extra space at the end
UPDATE db_link set dblink_acc_num = 'CU467865' where dblink_linked_recid = 'ZDB-TGCONSTRCT-161221-1' and dblink_acc_num = 'CU467865 ' and dblink_info IS NULL and dblink_acc_num_display = 'CU467865 ' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' and dblink_zdb_id = 'ZDB-DBLINK-161221-1';
UPDATE db_link set dblink_acc_num = 'CU855920' where dblink_linked_recid = 'ZDB-TGCONSTRCT-170621-2' and dblink_acc_num = 'CU855920 ' and dblink_info IS NULL and dblink_acc_num_display = 'CU855920 ' and dblink_length IS NULL and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' and dblink_zdb_id = 'ZDB-DBLINK-170621-7';

-- These accessions need manual fixes
-- C173-A2
-- CD75461
-- CT72701
-- DN90345
-- GDQH0102311
-- MG9579
