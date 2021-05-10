--liquibase formatted sql
--changeset sierra:restoreGenotypes

create temp table tmp_genofeat (geno_id varchar(50), feature_id varchar(50), zyg_id varchar(50), mom_zygocity_id varchar(50), dad_zygocity_id varchar(50), zdb_id varchar(50));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-2', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-2', (select feature_zdb_id from feature where feature_name ='rw0144Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-5', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-5', (select feature_zdb_id from feature where feature_name ='mi1006Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-5', (select feature_zdb_id from feature where feature_name ='rw0144Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-8', (select feature_zdb_id from feature where feature_name ='mi1000Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-8', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-8', (select feature_zdb_id from feature where feature_name ='mi1005Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-8', (select feature_zdb_id from feature where feature_name ='rw0144Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-2', (select feature_zdb_id from feature where feature_name ='hi4161aTg'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-5', (select feature_zdb_id from feature where feature_name ='y72'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-5', (select feature_zdb_id from feature where feature_name ='s896Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160608-1', (select feature_zdb_id from feature where feature_name ='m631'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-2', (select feature_zdb_id from feature where feature_name ='gi1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'W'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-2', (select feature_zdb_id from feature where feature_name ='mq6'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'W') );

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-5', (select feature_zdb_id from feature where feature_name ='mq6'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-3', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-3', (select feature_zdb_id from feature where feature_name ='mi1005Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-6', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-6', (select feature_zdb_id from feature where feature_name ='mi1005Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-6', (select feature_zdb_id from feature where feature_name ='rw0144Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-9', (select feature_zdb_id from feature where feature_name ='mi1000Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-9', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-9', (select feature_zdb_id from feature where feature_name ='mi1006Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-9', (select feature_zdb_id from feature where feature_name ='rw0144Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-3', (select feature_zdb_id from feature where feature_name ='hi4161aTg'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-3', (select feature_zdb_id from feature where feature_name ='vu234Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160606-1', (select feature_zdb_id from feature where feature_name ='zf608Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-1', (select feature_zdb_id from feature where feature_name ='gi1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-3', (select feature_zdb_id from feature where feature_name ='mi600'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-6', (select feature_zdb_id from feature where feature_name ='gi1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-9', (select feature_zdb_id from feature where feature_name ='gi1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-1', (select feature_zdb_id from feature where feature_name ='gz23Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-', (select feature_zdb_id from feature where feature_name ='gz34Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-', (select feature_zdb_id from feature where feature_name ='gz35Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-4', (select feature_zdb_id from feature where feature_name ='mi1006Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-4', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-7', (select feature_zdb_id from feature where feature_name ='mi1000Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-7', (select feature_zdb_id from feature where feature_name ='mi1004Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160601-7', (select feature_zdb_id from feature where feature_name ='rw0144Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-1', (select feature_zdb_id from feature where feature_name ='ia4Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-1', (select feature_zdb_id from feature where feature_name ='s896Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-1', (select feature_zdb_id from feature where feature_name ='y72'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-4', (select feature_zdb_id from feature where feature_name ='b140'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-4', (select feature_zdb_id from feature where feature_name ='w2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160602-4', (select feature_zdb_id from feature where feature_name ='y1Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));

insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160606-2', (select feature_zdb_id from feature where feature_name ='zf609Tg'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-10', (select feature_zdb_id from feature where feature_name ='mq6'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'), (select zyg_zdb_id from zygocity where zyg_abbrev = 'U'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-4', (select feature_zdb_id from feature where feature_name ='mi601'), (select zyg_zdb_id from zygocity where zyg_abbrev = '2'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));


insert into tmp_genofeat (geno_id, feature_id, zyg_id, mom_zygocity_id, dad_zygocity_id)
 values ('ZDB-GENO-160609-7', (select feature_zdb_id from feature where feature_name ='gi1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'), (select zyg_zdb_id from zygocity where zyg_abbrev = '1'));

update tmp_genofeat
 set zdb_id = get_id('GENOFEAT');

insert into zdb_active_data
 select zdb_id from tmp_genofeat;

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
 select zdb_id,
 	geno_id,
	feature_id,
	dad_zygocity_id,
	mom_zygocity_id,
	zyg_id
  from tmp_genofeat
 where exists (Select 'x' from genotype
       	      	      	 where geno_zdb_id = geno_id);

