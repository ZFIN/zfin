--liquibase formatted sql
--changeset cmpich:ZFIN-9156a.sql

CREATE UNIQUE INDEX ensembl_transcript_renaming_id
    ON ensembl_transcript_renaming (etr_ensdart_id);

insert into ensembl_transcript_renaming
values ('ENSDART00000156333','ENSDART00000156333','si:dkey-112m2.1-202');

delete from db_link where dblink_acc_num ='ENSDART00000114516' and dblink_linked_recid = 'ZDB-TSCRIPT-141209-2561';

update marker set mrkr_abbrev = 'ottdart00000032212', mrkr_name = 'OTTDART00000032212'
              where mrkr_zdb_id = 'ZDB-TSCRIPT-141209-2561';

update transcript set tscript_status_id = 1 where tscript_mrkr_zdb_id = 'ZDB-TSCRIPT-141209-2561';

insert into ensembl_transcript_renaming
values ('ENSDART00000114516','ENSDART00000114516','si:ch211-209f23.6-202');

insert into ensembl_transcript_renaming
values ('ENSDART00000180517','ENSDART00000180517','si:ch211-204c21.1-204');

insert into ensembl_transcript_renaming
values ('ENSDART00000188969','ENSDART00000188969','si:ch211-204c21.1-203');


update ensembl_transcript_renaming set etr_name = 'si:ch211-216l23.1-203'
where etr_ensdart_id = 'ENSDART00000187095';

update ensembl_transcript_renaming set etr_name = 'si:ch211-216l23.1-201'
where etr_ensdart_id = 'ENSDART00000083296';

update ensembl_transcript_renaming set etr_name = 'si:ch211-216l23.1-207'
where etr_ensdart_id = 'ENSDART00000190237';

update ensembl_transcript_renaming set etr_name = 'si:ch211-216l23.1-204'
where etr_ensdart_id = 'ENSDART00000189345';

update ensembl_transcript_renaming set etr_name = 'si:ch211-216l23.1-206'
where etr_ensdart_id = 'ENSDART00000190203';

update ensembl_transcript_renaming set etr_name = 'si:ch211-216l23.1-205'
where etr_ensdart_id = 'ENSDART00000189493';

insert into ensembl_transcript_renaming
values ('ENSDART00000146133','ENSDART00000146133','si:ch211-216l23.1-202');

