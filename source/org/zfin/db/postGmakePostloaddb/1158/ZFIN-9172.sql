--liquibase formatted sql
--changeset cmpich:ZFIN-9172.sql

update marker set mrkr_abbrev = 'zgc:165582-201', mrkr_name = 'zgc:165582-201'
              where mrkr_zdb_id = 'ZDB-TSCRIPT-131113-2029';

update marker set mrkr_abbrev = 'slc4a5a-201', mrkr_name = 'slc4a5a-201'
              where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-4580';

insert into ensembl_transcript_renaming
values ('ENSDART00000124868','ENSDART00000124868','lpla-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000162943','ENSDART00000162943','lplb-201');

--clashes with ENSDART00000184727 -> si:dkey-237h12.3-204
--insert into ensembl_transcript_renaming
--values ('ENSDART00000184727','ENSDART00000184727','si:dkey-237h12.3-202');

-- clashes with ENSDART00000193316 -> si:dkey-5g14.1-203
--insert into ensembl_transcript_renaming
--values ('ENSDART00000193316','ENSDART00000193316','si:dkey-5g14.1-201');

-- clashes with ENSDART00000189898 -> kcnt1b-204
--insert into ensembl_transcript_renaming
--values ('ENSDART00000189898','ENSDART00000189898','kcnt1b-203');

-- clashes with ENSDART00000042002 -> slc4a5a-201
--insert into ensembl_transcript_renaming
--values ('ENSDART00000042002','ENSDART00000042002','slc4a5a-202');

--clashes with ENSDART00000160976 -> slc4a5b-204
--insert into ensembl_transcript_renaming
--values ('ENSDART00000160976','ENSDART00000160976','slc4a5b-202');

insert into ensembl_transcript_renaming
values ('ENSDARG00000095482','ENSDARG00000095482','chst12b.2-202');

