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


