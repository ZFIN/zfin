--liquibase formatted sql
--changeset ryan:ZFIN-7891

-- fix names
update fish set fish_name = 'ihb19Tg' where fish_zdb_id = 'ZDB-FISH-150901-13474';
update fish set fish_name = 'nhsa<sup>fh299</sup>' where fish_zdb_id = 'ZDB-FISH-150901-705';
update fish set fish_name = 'sart3<sup>sm471/sm471</sup>' where fish_zdb_id = 'ZDB-FISH-150901-28190';
update fish set fish_name = 'rbm15<sup>cq96/cq96</sup>' where fish_zdb_id = 'ZDB-FISH-211230-1';

update fish set fish_name = 'slc24a5<sup>b1/+</sup>' where fish_zdb_id = 'ZDB-FISH-210104-17';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup>' where fish_zdb_id = 'ZDB-FISH-150901-19447';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup>' where fish_zdb_id = 'ZDB-FISH-150901-3352';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup>' where fish_zdb_id = 'ZDB-FISH-210415-9';
update fish set fish_name = 'slc24a5<sup>b1</sup> (AB)' where fish_zdb_id = 'ZDB-FISH-150901-15263';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> (AB)' where fish_zdb_id = 'ZDB-FISH-150901-16706';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> (AB)' where fish_zdb_id = 'ZDB-FISH-150901-29362';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> (AB)' where fish_zdb_id = 'ZDB-FISH-150901-19912';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + MO1-myo9aa + MO1-myo9ab + MO4-tp53' where fish_zdb_id = 'ZDB-FISH-161013-4';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + MO1-slc25a1b + MO2-slc25a1a + MO4-tp53' where fish_zdb_id = 'ZDB-FISH-210104-16';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + MO1-slc25a1b + MO2-slc25a1a' where fish_zdb_id = 'ZDB-FISH-210104-18';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + MO2-dok7b' where fish_zdb_id = 'ZDB-FISH-180911-2';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + MO2-musk' where fish_zdb_id = 'ZDB-FISH-180911-3';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + MO5-inpp5ka + MO5-inpp5kb' where fish_zdb_id = 'ZDB-FISH-170510-3';
update fish set fish_name = 'slc24a5<sup>b1/+</sup> + TALEN1-slc24a5' where fish_zdb_id = 'ZDB-FISH-150901-19410';
update fish set fish_name = 'slc24a5<sup>b1/+</sup>; gin2<sup>zf57/zf57</sup>; slc45a2<sup>zf67/+</sup>' where fish_zdb_id = 'ZDB-FISH-150901-7565';
update fish set fish_name = 'slc24a5<sup>b1/+</sup>; gin5<sup>zf60/zf60</sup>; pig5<sup>zf68/+</sup>' where fish_zdb_id = 'ZDB-FISH-150901-13040';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO1-ahi1' where fish_zdb_id = 'ZDB-FISH-150901-21290';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO1-map9 + MO2-map9' where fish_zdb_id = 'ZDB-FISH-161229-27';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO1-map9' where fish_zdb_id = 'ZDB-FISH-161229-26';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO1-trpm7' where fish_zdb_id = 'ZDB-FISH-150901-24277';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO1-upf1' where fish_zdb_id = 'ZDB-FISH-150901-11298';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO1-upf1' where fish_zdb_id = 'ZDB-FISH-150901-6039';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO11-tp53 + MO2-map9' where fish_zdb_id = 'ZDB-FISH-161229-28';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO2-dhx34' where fish_zdb_id = 'ZDB-FISH-150901-6711';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO2-map9' where fish_zdb_id = 'ZDB-FISH-161229-25';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO2-nbas' where fish_zdb_id = 'ZDB-FISH-150901-4935';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO2-sil1' where fish_zdb_id = 'ZDB-FISH-190925-7';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO2-spi1b + MO3-spi1b' where fish_zdb_id = 'ZDB-FISH-161010-1';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO2-upf1' where fish_zdb_id = 'ZDB-FISH-150901-4294';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup> + MO7-cftr' where fish_zdb_id = 'ZDB-FISH-191219-11';
update fish set fish_name = 'slc24a5<sup>b1/b1</sup>; dguok<sup>zf3123/zf3123</sup>' where fish_zdb_id = 'ZDB-FISH-210415-8';


-- add constraint
update fish set fish_name = '_' where fish_name = '';
alter table fish add CONSTRAINT no_blank_fish_names check (fish_name <> '');
