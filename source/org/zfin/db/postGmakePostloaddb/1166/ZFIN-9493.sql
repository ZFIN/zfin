--liquibase formatted sql
--changeset cmpich:ZFIN-9495.sql

delete from ensembl_transcript_renaming;

insert into ensembl_transcript_renaming
values ('ENSDART00000026000', 'ENSDART00000026000', 'myofl-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000158654', 'ENSDART00000158654', 'si:ch73-221f6.4-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000157687', 'ENSDART00000157687', 'zgc:63569-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000167610', 'ENSDART00000167610', 'zgc:63569-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000162942', 'ENSDART00000162942', 'zgc:152904-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000183039', 'ENSDART00000183039', 'cox5bb-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000190839', 'ENSDART00000190839', 'zgc:86839-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000080850', 'ENSDART00000080850', 'si:ch211-155i14.1-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000170715', 'ENSDART00000170715', 'zgc:171592-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000128918', 'ENSDART00000128918', 'fgfbp1b-201');


insert into ensembl_transcript_renaming
values ('ENSDART00000113926', 'ENSDART00000113926', 'zgc:100918-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000123608', 'ENSDART00000123608', 'zgc:100918-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000182734', 'ENSDART00000182734', 'zgc:100918-201');

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-TSCRIPT-241211-2576';

update marker set mrkr_abbrev = 'tmem216-201', mrkr_name = 'tmem216-201' where mrkr_zdb_id = 'ZDB-TSCRIPT-141209-2285';

