--liquibase formatted sql
--changeset pm:ZFIN-6869



alter table tmp_ftrnote add fgmdid varchar(50);
update tmp_ftrnote set fgmdid=(select fgmd_zdb_id from feature_genomic_mutation_detail where fgmd_feature_zdb_id=featureid);
update external_note set extnote_tag='variant' from tmp_ftrnote where varianttag='variant' and alleletag ='' and fgmdid is null and extnote_data_zdb_id=featureid;
update external_note set extnote_tag='variant with ID '||fgmdid from tmp_ftrnote where varianttag='variant' and alleletag ='' and fgmdid is not null and extnote_data_zdb_id=featureid;
update external_note set extnote_tag='feature' from tmp_ftrnote where alleletag='allele' and extnote_data_zdb_id=featureid;
delete from tmp_ftrnote where alleletag='' and varianttag='variant';
delete from tmp_ftrnote where alleletag='allele' and varianttag='';

drop table if exists fgmdnote;
create table fgmdnote (

       fgmdzdb text not null,
       fgmdnote text not null,
        fgmdref text not null,
        fgmdtype text,
        fgmdtag text

);

insert into fgmdnote (fgmdzdb,fgmdnote,fgmdref,fgmdtype) select featureid,ftrnote,extnote_source_zdb_id,'variant with ID'||fgmdid from tmp_ftrnote,external_note where extnote_data_zdb_id=featureid and fgmdid is not null;
insert into fgmdnote (fgmdzdb,fgmdnote,fgmdref,fgmdtype) select featureid,extnote_note,extnote_source_zdb_id,'variant' from tmp_ftrnote,external_note where extnote_data_zdb_id=featureid  and fgmdid is null;
alter table fgmdnote add fgmdnote_zdb_id varchar(50);
update fgmdnote set fgmdnote_zdb_id=get_id('EXTNOTE');
insert into zdb_active_data select fgmdnote_zdb_id from fgmdnote;
insert into external_note (extnote_zdb_id, extnote_data_zdb_id,extnote_note,extnote_source_zdb_id,extnote_note_type,extnote_tag) select fgmdnote_zdb_id,fgmdzdb,fgmdnote,fgmdref,'feature',fgmdtype from fgmdnote;

--updating one record which does not have a tag yet(note is empty)
update external_note set extnote_tag='feature' where extnote_zdb_id='ZDB-EXTNOTE-170202-1';




