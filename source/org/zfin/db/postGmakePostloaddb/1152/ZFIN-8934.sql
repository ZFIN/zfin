--liquibase formatted sql
--changeset rtaylor:ZFIN-8934.sql

-- Original Request:
-- Please merge these 2 features:
--  zf2099 (ZDB-ALT-190404-20)(Df(chr4:mir430a-18,mir430b-17)zf2099)
--  zf3188 (ZDB-ALT-210414-10)(Df(Chr4:mir430)zf3188)
--
-- Please retain (Df(Chr4:mir430)zf3188) ZDB-ALT-210414-10 as the valid name/symbol/ID.


-- DB values before change:
-- data_alias : ZDB-DALIAS-190405-2,ZDB-ALT-190404-20,zf2099,zf2099,1,
-- external_note : ZDB-EXTNOTE-190404-4,ZDB-ALT-190404-20,"Multiple lines were created in which the entire multi-mir cluster called ""mir430"" was removed, but the author did not verify which line was maintained",feature,ZDB-PUB-131119-4,feature
-- feature : ZDB-ALT-190404-20,"Df(chr4:mir430a-18,mir430b-17)zf2099",zf2099,DEFICIENCY,"df(chr0000000004:mir0000000430a-0000000018,mir0000000430b-0000000017)zf0000002099",zf0000002099,2019-04-04 14:50:03.525976,194,2099,,f,f,f,f,Tg,
-- feature_assay : ZDB-ALT-190404-20,TALEN,embryos,92662
-- feature_history : ZDB-FHIST-190404-20,ZDB-ALT-190404-20,assigned,Not Specified,2019-04-04 14:50:03.525976,zf2099,zf2099,"",,
-- feature_history : ZDB-FHIST-190405-7,ZDB-ALT-190404-20,reassigned,Not Specified,2019-04-05 10:27:53.985459,"Df(chr4:mir430a-18,mir430b-17)zf2099","Df(chr4:mir430a-18,mir430b-17)zf2099",none,ZDB-DALIAS-190405-2,
-- feature_marker_relationship : ZDB-FMREL-190405-15,created by,ZDB-ALT-190404-20,ZDB-TALEN-140123-3
-- feature_marker_relationship : ZDB-FMREL-190405-16,created by,ZDB-ALT-190404-20,ZDB-TALEN-190405-2
-- feature_tracking : 75748,ZDB-ALT-190404-20,zf2099,zf2099,2019-04-04 14:50:03.525976
-- int_data_source : 117774,ZDB-ALT-190404-20,ZDB-LAB-000914-1
-- record_attribution : 87567032,ZDB-ALT-190404-20,ZDB-PUB-131119-4,,standard,,,
-- record_attribution : 87567033,ZDB-ALT-190404-20,ZDB-PUB-131119-4,,feature type,,,
-- record_attribution : 87567148,ZDB-ALT-190404-20,ZDB-PUB-180801-6,,standard,,,
-- record_attribution : 99448526,ZDB-ALT-190404-20,ZDB-PUB-190619-9,,standard,,,
-- record_attribution : 107387326,ZDB-ALT-190404-20,ZDB-PUB-210101-12,,standard,,,
-- updates : ZDB-PERS-981201-7,ZDB-ALT-190404-20,Public Note,"Multiple mutations were created in mir430, but author did not verify which mutation were contained in maintained lines",,"Multiple lines were created in which the entire multi-mir cluster called ""mir430"" was removed, but the author did not verify which line was maintained","Singer, Amy",1593561,2019-04-04 14:53:08.949
-- updates : ZDB-PERS-100329-1,ZDB-ALT-190404-20,record attribution,ZDB-PUB-180801-6,,Added direct attribution,"Paddock, Holly",1593653,2019-04-05 10:38:39.144
-- updates : ZDB-PERS-981201-7,ZDB-FMREL-190405-15,FeatureMarkerRelationship,"FeatureMarkerRelationship{zdbID='ZDB-FMREL-190405-15', type='created by', feature='Feature{zdbID='ZDB-ALT-190404-20', name='Df(chr4:mir430a-18,mir430b-17)zf2099', lineNumber='2099', labPrefix=FeaturePrefix{prefixString='zf', institute='Zebrafish Model Organism Database'}, abbreviation='zf2099', transgenicSuffix='Tg', isKnownInsertionSite=false, isDominantFeature=false, type=DEFICIENCY}', marker=MARKER
-- updates : ZDB-PERS-981201-7,ZDB-FMREL-190405-16,FeatureMarkerRelationship,"FeatureMarkerRelationship{zdbID='ZDB-FMREL-190405-16', type='created by', feature='Feature{zdbID='ZDB-ALT-190404-20', name='Df(chr4:mir430a-18,mir430b-17)zf2099', lineNumber='2099', labPrefix=FeaturePrefix{prefixString='zf', institute='Zebrafish Model Organism Database'}, abbreviation='zf2099', transgenicSuffix='Tg', isKnownInsertionSite=false, isDominantFeature=false, type=DEFICIENCY}', marker=MARKER
-- updates : ZDB-PERS-100329-1,ZDB-ALT-190404-20,record attribution,ZDB-PUB-190619-9,,Added direct attribution,"Paddock, Holly",1633995,2019-12-09 14:32:55.787
-- updates : ZDB-PERS-100329-1,ZDB-ALT-190404-20,record attribution,ZDB-PUB-200828-23,,Added direct attribution,"Paddock, Holly",1716926,2021-03-29 09:34:50.497
-- updates : ZDB-PERS-100329-1,ZDB-ALT-190404-20,record attribution,removed,ZDB-PUB-200828-23,Removed direct attribution,"Paddock, Holly",1716977,2021-03-29 10:46:17.357
-- updates : ZDB-PERS-100329-1,ZDB-ALT-190404-20,record attribution,ZDB-PUB-210101-12,,Added direct attribution,"Paddock, Holly",1757966,2022-01-11 08:30:52.125
-- zdb_active_data : ZDB-ALT-190404-20


-- Changing from ZDB-ALT-190404-20 to ZDB-ALT-210414-10

-- feature assay changes
-- cannot run: update feature_assay set featassay_feature_zdb_id = 'ZDB-ALT-210414-10' where featassay_feature_zdb_id = 'ZDB-ALT-190404-20';
-- because: Key (featassay_feature_zdb_id)=(ZDB-ALT-210414-10) already exists.
-- so, delete instead
delete from feature_assay where featassay_feature_zdb_id = 'ZDB-ALT-190404-20';

-- feature history changes
update feature_history set fhist_ftr_zdb_id = 'ZDB-ALT-210414-10' where fhist_ftr_zdb_id = 'ZDB-ALT-190404-20';

-- feature marker relationship changes
-- cannot run: update feature_marker_relationship set fmrel_ftr_zdb_id = 'ZDB-ALT-210414-10' where fmrel_ftr_zdb_id = 'ZDB-ALT-190404-20';
-- because: Key (fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)=(ZDB-ALT-210414-10, ZDB-TALEN-140123-3) already exists.
-- so, delete instead
delete from feature_marker_relationship where fmrel_ftr_zdb_id = 'ZDB-ALT-190404-20';


-- feature tracking changes (keep?)
update feature_tracking set ft_feature_zdb_id = 'ZDB-ALT-210414-10' where ft_feature_zdb_id = 'ZDB-ALT-190404-20';

-- int_data_source changes
-- cannot run: update int_data_source set ids_data_zdb_id = 'ZDB-ALT-210414-10' where ids_data_zdb_id = 'ZDB-ALT-190404-20';
-- because: Key (ids_data_zdb_id, ids_source_zdb_id)=(ZDB-ALT-210414-10, ZDB-LAB-000914-1) already exists.
-- so, delete instead
delete from int_data_source where ids_data_zdb_id = 'ZDB-ALT-190404-20';

-- record attribution changes
-- Delete duplicate before update: Key (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)=(ZDB-ALT-210414-10, ZDB-PUB-210101-12, standard) already exists.
delete from record_attribution where recattrib_data_zdb_id = 'ZDB-ALT-190404-20' and recattrib_source_zdb_id = 'ZDB-PUB-210101-12' and recattrib_source_type = 'standard';
update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-210414-10' where recattrib_data_zdb_id = 'ZDB-ALT-190404-20';

-- updates changes (keep?)
update updates set rec_id = 'ZDB-ALT-210414-10' where rec_id = 'ZDB-ALT-190404-20';

-- external note changes
update external_note set extnote_data_zdb_id = 'ZDB-ALT-210414-10' where extnote_data_zdb_id = 'ZDB-ALT-190404-20';

-- data_alias changes
update data_alias set dalias_data_zdb_id = 'ZDB-ALT-210414-10' where dalias_data_zdb_id = 'ZDB-ALT-190404-20';

-- DELETE original record
delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-190404-20';

-- Add redirect
insert into zdb_replaced_data(zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-190404-20', 'ZDB-ALT-210414-10');
