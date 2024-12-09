--liquibase formatted sql
--changeset cmpich:ZFIN-9456.sql

delete from ensembl_transcript_renaming;

insert into ensembl_transcript_renaming
values ('ENSDART00000098668', 'ENSDART00000098668', 'abcc8b-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000182215', 'ENSDART00000182215', 'abcc8b-202');

delete from ensembl_transcript_delete;

insert into ensembl_transcript_delete
values ('ENSDART00000150783', 'ZDB-TSCRIPT-110912-248');

insert into ensembl_transcript_delete
values ('ENSDART00000099074', 'ZDB-TSCRIPT-131113-1301');

insert into ensembl_transcript_delete
values ('ENSDART00000158631', 'ZDB-TSCRIPT-141209-1765');

insert into ensembl_transcript_delete
values ('ENSDART00000193724', 'ZDB-TSCRIPT-141209-1765');

