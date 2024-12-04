--liquibase formatted sql
--changeset cmpich:ZFIN-9281.sql

delete from ensembl_transcript_delete;

insert into ensembl_transcript_delete
values ('ENSDART00000056441', 'ZDB-TSCRIPT-090929-14210');

insert into ensembl_transcript_delete
values ('ENSDART00000133042', 'ZDB-TSCRIPT-090929-12887');

insert into ensembl_transcript_delete
values ('ENSDART00000152062', 'ZDB-TSCRIPT-091110-537');

insert into ensembl_transcript_delete
values ('ENSDART00000174326', 'ZDB-TSCRIPT-160919-160');

insert into ensembl_transcript_delete
values ('ENSDART00000157802', 'ZDB-TSCRIPT-090929-16061');

insert into ensembl_transcript_delete
values ('ENSDART00000142512', 'ZDB-TSCRIPT-090929-9061');

insert into ensembl_transcript_delete
values ('ENSDART00000132824', 'ZDB-TSCRIPT-090929-15969');

insert into ensembl_transcript_delete
values ('ENSDART00000038301', 'ZDB-TSCRIPT-091110-789');

insert into ensembl_transcript_delete
values ('ENSDART00000151278', 'ZDB-TSCRIPT-120213-738');

insert into ensembl_transcript_delete
values ('ENSDART00000099074', 'ZDB-TSCRIPT-131113-1301');


delete from ensembl_transcript_renaming;

insert into ensembl_transcript_renaming
values ('ENSDART00000162210', 'ENSDART00000162210', 'kcnq1.2-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000162259', 'ENSDART00000162259', 'zfyve9b-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000186126', 'ENSDART00000186126', 'zfyve9b-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000087670', 'ENSDART00000087670', 'rapgef2a-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000188893', 'ENSDART00000188893', 'rapgef2a-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000168705', 'ENSDART00000168705', 'si:ch73-103b11.2-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000184611', 'ENSDART00000184611', 'si:ch73-103b11.2-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000193494', 'ENSDART00000193494', 'si:ch73-103b11.2-204');

insert into ensembl_transcript_renaming
values ('ENSDART00000190728', 'ENSDART00000190728', 'si:ch73-103b11.2-205');

insert into ensembl_transcript_renaming
values ('ENSDART00000114516', 'ENSDART00000114516', 'si:ch211-209f23.6-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000092983', 'ENSDART00000092983', 'mazb-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000169094', 'ENSDART00000169094', 'si:ch73-329n5.2-204');

insert into ensembl_transcript_renaming
values ('ENSDART00000188162', 'ENSDART00000188162', 'cpne8-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000132945', 'ENSDART00000132945', 'si:ch211-226o13.2-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000060330', 'ENSDART00000060330', 'si:dkey-88n24.9-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000158253', 'ENSDART00000158253', 'chst12b.5-202');


-- ZFIN-9436
insert into ensembl_transcript_renaming
values ('ENSDART00000187088', 'ENSDART00000187088', 'si:ch211-266c8.2-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000122700', 'ENSDART00000122700', 'tenm3-202 ');

insert into ensembl_transcript_renaming
values ('ENSDART00000184727', 'ENSDART00000184727', 'si:dkey-237h12.3-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000188386', 'ENSDART00000188386', 'si:ch1073-440p11.2-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000104648', 'ENSDART00000104648', 'zgc:165582-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000189926', 'ENSDART00000189926', 'si:cabz01076231.1-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000177174', 'ENSDART00000177174', 'cabp4-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000193316', 'ENSDART00000193316', 'si:dkey-5g14.1-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000181928', 'ENSDART00000181928', 'slc46a3-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000089126', 'ENSDART00000089126', 'trhde.1-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000112301', 'ENSDART00000112301', 'trhde.2-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000181489', 'ENSDART00000181489', 'kcnt1a-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000189898', 'ENSDART00000189898', 'kcnt1b-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000042002', 'ENSDART00000042002', 'slc4a5a-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000160976', 'ENSDART00000160976', 'slc4a5b-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000123648', 'ENSDART00000123648', 'pcdh1a6-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000078403', 'ENSDART00000078403', 'brd8a-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000187440', 'ENSDART00000187440', 'zgc:136929-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000190841', 'ENSDART00000190841', 'si:ch73-256j6.4-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000180517','ENSDART00000180517','si:ch211-204c21.1-204');

insert into ensembl_transcript_renaming
values ('ENSDART00000188969','ENSDART00000188969','si:ch211-204c21.1-203');


delete from ensembl_transcript_delete;

insert into ensembl_transcript_delete
values ('ENSDART00000114516', 'ZDB-TSCRIPT-141209-2561');

insert into ensembl_transcript_delete
values ('ENSDART00000169094', 'ZDB-TSCRIPT-141209-1128');

insert into ensembl_transcript_delete
values ('ENSDART00000174150', 'ZDB-TSCRIPT-090929-17774');

insert into ensembl_transcript_delete
values ('ENSDART00000141094', 'ZDB-TSCRIPT-090929-17777');

insert into ensembl_transcript_delete
values ('ENSDART00000174015', 'ZDB-TSCRIPT-160919-376');

insert into ensembl_transcript_delete
values ('ENSDART00000147574', 'ZDB-TSCRIPT-090929-12239');

insert into ensembl_transcript_delete
values ('ENSDART00000134229', 'ZDB-TSCRIPT-090929-12239');

insert into ensembl_transcript_delete
values ('ENSDART00000134229', 'ZDB-TSCRIPT-090929-12237');

insert into ensembl_transcript_delete
values ('ENSDART00000098052', 'ZDB-TSCRIPT-141209-296');

insert into ensembl_transcript_delete
values ('ENSDART00000133799', 'ZDB-TSCRIPT-090929-12245');

insert into ensembl_transcript_delete
values ('ENSDART00000148454', 'ZDB-TSCRIPT-110325-1216');

insert into ensembl_transcript_delete
values ('ENSDART00000099074', 'ZDB-TSCRIPT-131113-1301');

insert into ensembl_transcript_delete
values ('ENSDART00000174367', 'ZDB-TSCRIPT-160919-420');

insert into ensembl_transcript_delete
values ('ENSDART00000056441', 'ZDB-TSCRIPT-090929-14210');

insert into ensembl_transcript_delete
values ('ENSDART00000133042', 'ZDB-TSCRIPT-090929-12887');

insert into ensembl_transcript_delete
values ('ENSDART00000152062', 'ZDB-TSCRIPT-091110-537');

insert into ensembl_transcript_delete
values ('ENSDART00000174326', 'ZDB-TSCRIPT-160919-160');

insert into ensembl_transcript_delete
values ('ENSDART00000157802', 'ZDB-TSCRIPT-090929-16061');

insert into ensembl_transcript_delete
values ('ENSDART00000142512', 'ZDB-TSCRIPT-090929-9061');

insert into ensembl_transcript_delete
values ('ENSDART00000132824', 'ZDB-TSCRIPT-090929-15969');

insert into ensembl_transcript_delete
values ('ENSDART00000038301', 'ZDB-TSCRIPT-091110-789');

insert into ensembl_transcript_delete
values ('ENSDART00000174367', 'ZDB-TSCRIPT-160919-420');

delete from ensembl_transcript_add;

insert into ensembl_transcript_add

values ('ENSDART00000142854', 'ZDB-TSCRIPT-141209-296');

update transcript set tscript_status_id = 1 where tscript_mrkr_zdb_id in ('ZDB-TSCRIPT-110325-1216', 'ZDB-TSCRIPT-090929-12239','ZDB-TSCRIPT-090929-12237', 'ZDB-TSCRIPT-090929-17774', 'ZDB-TSCRIPT-090929-17777');

update marker set mrkr_abbrev = 'ottdart00000029880', mrkr_name = 'ottdart00000029880' where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-12239';

update marker set mrkr_abbrev = 'ottdart00000029878', mrkr_name = 'ottdart00000029878' where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-12237 ';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-TSCRIPT-241112-182';
