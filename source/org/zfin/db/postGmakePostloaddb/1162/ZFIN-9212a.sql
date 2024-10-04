--liquibase formatted sql
--changeset cmpich:ZFIN-9212a.sql

insert into ensembl_transcript_renaming
values ('ENSDART00000183975', 'ENSDART00000183975', 'zgc:171740-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000180247', 'ENSDART00000180247', 'si:dkey-237h12.3-204');

insert into ensembl_transcript_renaming
values ('ENSDART00000179540', 'ENSDART00000179540', 'mrasb-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000187440', 'ENSDART00000187440', 'zgc:136929-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000087670', 'ENSDART00000087670', 'rapgef2a-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000184190', 'ENSDART00000184190', 'ftr31-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000132945', 'ENSDART00000132945', 'si:ch211-226o13.2-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000125345', 'ENSDART00000125345', 'si:dkey-237h12.3-206');

insert into ensembl_transcript_renaming
values ('ENSDART00000105073', 'ENSDART00000105073', 'si:dkey-28d5.14-001');

create table ensembl_transcript_add
(
    eta_ensdart_id  varchar not null,
    eta_mrkr_zdb_id varchar not null
);

insert into ensembl_transcript_add
values ('ENSDART00000173587', 'ZDB-TSCRIPT-160623-333');

create table ensembl_transcript_delete
(
    etd_ensdart_id  varchar not null,
    etd_mrkr_zdb_id varchar not null
);

insert into ensembl_transcript_delete
values ('ENSDART00000147574', 'ZDB-TSCRIPT-090929-12239');

insert into ensembl_transcript_delete
values ('ENSDART00000098052', 'ZDB-TSCRIPT-141209-296');

insert into ensembl_transcript_delete
values ('ENSDART00000162509', 'ZDB-TSCRIPT-131113-3276');

insert into ensembl_transcript_delete
values ('ENSDART00000133799', 'ZDB-TSCRIPT-090929-12245');

