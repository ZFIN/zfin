--liquibase formatted sql
--changeset pm:ZFIN-6774

insert into external_note_type(extntype_name) values ('variant');
insert into external_note_type(extntype_name) values ('feature and variant');
alter table tmp_ftrnote add fgmdid varchar(50);
update tmp_ftrnote set fgmdid=(select fgmd_zdb_id from feature_genomic_mutation_detail where fgmd_feature_zdb_id=featureid);

alter table tmp_ftrnote add extnoteid varchar(50);
--update tmp_ftrnote set extnoteid = (select distinct extnote_zdb_id from external_note where extnote_data_zdb_id=featureid);

--update external_note set extnote_data_zdb_id=(select fgmdid from tmp_ftrnote where variantnote='variant' and allelenote ='' and fgmdid !='') from tmp_ftrnote where extnote_note_type='feature' and extnote_data_zdb_id=featureid;
update external_note set extnote_note_type='variant' from tmp_ftrnote where variantnote='variant' and allelenote ='' and fgmdid ='' and extnote_data_zdb_id=featureid;
update external_note set extnote_note_type='variant' from tmp_ftrnote where variantnote='variant' and allelenote ='' and fgmdid!='' and extnote_data_zdb_id=featureid;


drop table if exists fgmdnote;
create table fgmdnote (

       fgmdzdb text not null,
       fgmdnote text not null,
        fgmdref text not null,
        fgmdtype text

);

insert into fgmdnote (fgmdzdb,fgmdnote,fgmdref,fgmdtype) select fgmdid,extnote_note,extnote_source_zdb_id,'feature and variant' from tmp_ftrnote,external_note where extnote_data_zdb_id=featureid and variantnote='variant' and allelenote='allele' and fgmdid !='';
insert into fgmdnote (fgmdzdb,fgmdnote,fgmdref,fgmdtype) select fgmdid,extnote_note,extnote_source_zdb_id,'variant' from tmp_ftrnote,external_note where extnote_data_zdb_id=featureid and variantnote='variant' and allelenote='' and fgmdid !='';
alter table fgmdnote add fgmdnote_zdb_id varchar(50);
update fgmdnote set fgmdnote_zdb_id=get_id('EXTNOTE');
insert into zdb_active_data select fgmdnote_zdb_id from fgmdnote;
insert into external_note (extnote_zdb_id, extnote_data_zdb_id,extnote_note,extnote_source_zdb_id,extnote_note_type) select fgmdnote_zdb_id,fgmdzdb,fgmdnote,fgmdref,fgmdtype from fgmdnote;
delete from external_note where extnote_note_type ='variant' and extnote_data_zdb_id like 'ZDB-ALT%' ;

rollback work;

