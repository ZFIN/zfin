-- MERGE
-- ZDB-LAB-180420-1 -> ZDB-LAB-180420-2
-- ZDB-LAB-180503-2 -> ZDB-LAB-180503-3
-- ZDB-LAB-180720-1 -> ZDB-LAB-180720-2
-- ZDB-LAB-040813-1 -> ZDB-LAB-040813-3
-- ZDB-LAB-150304-1 -> ZDB-LAB-150304-2
-- ZDB-LAB-180305-2 -> ZDB-LAB-180305-3
-- ZDB-LAB-180517-1 -> ZDB-LAB-180517-2
-- ZDB-LAB-180228-1 -> ZDB-LAB-180228-2
-- ZDB-LAB-180129-1 -> ZDB-LAB-180129-2
-- ZDB-LAB-180213-1 -> ZDB-LAB-180213-2
-- ZDB-LAB-170906-2 -> ZDB-LAB-170906-3
-- ZDB-LAB-180517-3 -> ZDB-LAB-180517-4
-- ZDB-LAB-180206-2 -> ZDB-LAB-180206-3

update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180420-2' where ids_source_zdb_id = 'ZDB-LAB-180420-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180503-3' where ids_source_zdb_id = 'ZDB-LAB-180503-2';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180720-2' where ids_source_zdb_id = 'ZDB-LAB-180720-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-040813-3' where ids_source_zdb_id = 'ZDB-LAB-040813-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-150304-2' where ids_source_zdb_id = 'ZDB-LAB-150304-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180305-3' where ids_source_zdb_id = 'ZDB-LAB-180305-2';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180517-2' where ids_source_zdb_id = 'ZDB-LAB-180517-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180228-2' where ids_source_zdb_id = 'ZDB-LAB-180228-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180129-2' where ids_source_zdb_id = 'ZDB-LAB-180129-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180213-2' where ids_source_zdb_id = 'ZDB-LAB-180213-1';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-170906-3' where ids_source_zdb_id = 'ZDB-LAB-170906-2';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180517-4' where ids_source_zdb_id = 'ZDB-LAB-180517-3';
update int_data_source set ids_source_zdb_id = 'ZDB-LAB-180206-3' where ids_source_zdb_id = 'ZDB-LAB-180206-2';

update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180420-2' where idsup_supplier_zdb_id = 'ZDB-LAB-180420-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180503-3' where idsup_supplier_zdb_id = 'ZDB-LAB-180503-2';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180720-2' where idsup_supplier_zdb_id = 'ZDB-LAB-180720-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-040813-3' where idsup_supplier_zdb_id = 'ZDB-LAB-040813-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-150304-2' where idsup_supplier_zdb_id = 'ZDB-LAB-150304-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180305-3' where idsup_supplier_zdb_id = 'ZDB-LAB-180305-2';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180517-2' where idsup_supplier_zdb_id = 'ZDB-LAB-180517-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180228-2' where idsup_supplier_zdb_id = 'ZDB-LAB-180228-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180129-2' where idsup_supplier_zdb_id = 'ZDB-LAB-180129-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180213-2' where idsup_supplier_zdb_id = 'ZDB-LAB-180213-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-170906-3' where idsup_supplier_zdb_id = 'ZDB-LAB-170906-2';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180517-4' where idsup_supplier_zdb_id = 'ZDB-LAB-180517-3';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-LAB-180206-3' where idsup_supplier_zdb_id = 'ZDB-LAB-180206-2';

update int_person_lab set target_id = 'ZDB-LAB-180420-2' where target_id = 'ZDB-LAB-180420-1';
update int_person_lab set target_id = 'ZDB-LAB-180503-3' where target_id = 'ZDB-LAB-180503-2';
update int_person_lab set target_id = 'ZDB-LAB-180720-2' where target_id = 'ZDB-LAB-180720-1';
update int_person_lab set target_id = 'ZDB-LAB-040813-3' where target_id = 'ZDB-LAB-040813-1';
update int_person_lab set target_id = 'ZDB-LAB-150304-2' where target_id = 'ZDB-LAB-150304-1';
update int_person_lab set target_id = 'ZDB-LAB-180305-3' where target_id = 'ZDB-LAB-180305-2';
update int_person_lab set target_id = 'ZDB-LAB-180517-2' where target_id = 'ZDB-LAB-180517-1';
update int_person_lab set target_id = 'ZDB-LAB-180228-2' where target_id = 'ZDB-LAB-180228-1';
update int_person_lab set target_id = 'ZDB-LAB-180129-2' where target_id = 'ZDB-LAB-180129-1';
update int_person_lab set target_id = 'ZDB-LAB-180213-2' where target_id = 'ZDB-LAB-180213-1';
update int_person_lab set target_id = 'ZDB-LAB-170906-3' where target_id = 'ZDB-LAB-170906-2';
update int_person_lab set target_id = 'ZDB-LAB-180517-4' where target_id = 'ZDB-LAB-180517-3';
update int_person_lab set target_id = 'ZDB-LAB-180206-3' where target_id = 'ZDB-LAB-180206-2';

update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180420-2' where laut_lab_zdb_id = 'ZDB-LAB-180420-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180503-3' where laut_lab_zdb_id = 'ZDB-LAB-180503-2';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180720-2' where laut_lab_zdb_id = 'ZDB-LAB-180720-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-040813-3' where laut_lab_zdb_id = 'ZDB-LAB-040813-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-150304-2' where laut_lab_zdb_id = 'ZDB-LAB-150304-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180305-3' where laut_lab_zdb_id = 'ZDB-LAB-180305-2';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180517-2' where laut_lab_zdb_id = 'ZDB-LAB-180517-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180228-2' where laut_lab_zdb_id = 'ZDB-LAB-180228-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180129-2' where laut_lab_zdb_id = 'ZDB-LAB-180129-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180213-2' where laut_lab_zdb_id = 'ZDB-LAB-180213-1';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-170906-3' where laut_lab_zdb_id = 'ZDB-LAB-170906-2';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180517-4' where laut_lab_zdb_id = 'ZDB-LAB-180517-3';
update lab_address_update_tracking set laut_lab_zdb_id = 'ZDB-LAB-180206-3' where laut_lab_zdb_id = 'ZDB-LAB-180206-2';

update mapped_marker set lab = 'ZDB-LAB-180420-2' where lab = 'ZDB-LAB-180420-1';
update mapped_marker set lab = 'ZDB-LAB-180503-3' where lab = 'ZDB-LAB-180503-2';
update mapped_marker set lab = 'ZDB-LAB-180720-2' where lab = 'ZDB-LAB-180720-1';
update mapped_marker set lab = 'ZDB-LAB-040813-3' where lab = 'ZDB-LAB-040813-1';
update mapped_marker set lab = 'ZDB-LAB-150304-2' where lab = 'ZDB-LAB-150304-1';
update mapped_marker set lab = 'ZDB-LAB-180305-3' where lab = 'ZDB-LAB-180305-2';
update mapped_marker set lab = 'ZDB-LAB-180517-2' where lab = 'ZDB-LAB-180517-1';
update mapped_marker set lab = 'ZDB-LAB-180228-2' where lab = 'ZDB-LAB-180228-1';
update mapped_marker set lab = 'ZDB-LAB-180129-2' where lab = 'ZDB-LAB-180129-1';
update mapped_marker set lab = 'ZDB-LAB-180213-2' where lab = 'ZDB-LAB-180213-1';
update mapped_marker set lab = 'ZDB-LAB-170906-3' where lab = 'ZDB-LAB-170906-2';
update mapped_marker set lab = 'ZDB-LAB-180517-4' where lab = 'ZDB-LAB-180517-3';
update mapped_marker set lab = 'ZDB-LAB-180206-3' where lab = 'ZDB-LAB-180206-2';

update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180420-2' where recattrib_source_zdb_id = 'ZDB-LAB-180420-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180503-3' where recattrib_source_zdb_id = 'ZDB-LAB-180503-2';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180720-2' where recattrib_source_zdb_id = 'ZDB-LAB-180720-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-040813-3' where recattrib_source_zdb_id = 'ZDB-LAB-040813-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-150304-2' where recattrib_source_zdb_id = 'ZDB-LAB-150304-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180305-3' where recattrib_source_zdb_id = 'ZDB-LAB-180305-2';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180517-2' where recattrib_source_zdb_id = 'ZDB-LAB-180517-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180228-2' where recattrib_source_zdb_id = 'ZDB-LAB-180228-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180129-2' where recattrib_source_zdb_id = 'ZDB-LAB-180129-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180213-2' where recattrib_source_zdb_id = 'ZDB-LAB-180213-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-170906-3' where recattrib_source_zdb_id = 'ZDB-LAB-170906-2';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180517-4' where recattrib_source_zdb_id = 'ZDB-LAB-180517-3';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180206-3' where recattrib_source_zdb_id = 'ZDB-LAB-180206-2';

update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180420-2' where recattrib_source_zdb_id = 'ZDB-LAB-180420-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180503-3' where recattrib_source_zdb_id = 'ZDB-LAB-180503-2';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180720-2' where recattrib_source_zdb_id = 'ZDB-LAB-180720-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-040813-3' where recattrib_source_zdb_id = 'ZDB-LAB-040813-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-150304-2' where recattrib_source_zdb_id = 'ZDB-LAB-150304-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180305-3' where recattrib_source_zdb_id = 'ZDB-LAB-180305-2';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180517-2' where recattrib_source_zdb_id = 'ZDB-LAB-180517-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180228-2' where recattrib_source_zdb_id = 'ZDB-LAB-180228-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180129-2' where recattrib_source_zdb_id = 'ZDB-LAB-180129-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180213-2' where recattrib_source_zdb_id = 'ZDB-LAB-180213-1';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-170906-3' where recattrib_source_zdb_id = 'ZDB-LAB-170906-2';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180517-4' where recattrib_source_zdb_id = 'ZDB-LAB-180517-3';
update record_attribution set recattrib_source_zdb_id = 'ZDB-LAB-180206-3' where recattrib_source_zdb_id = 'ZDB-LAB-180206-2';

update return_recs set return_recid = 'ZDB-LAB-180420-2' where return_recid = 'ZDB-LAB-180420-1';
update return_recs set return_recid = 'ZDB-LAB-180503-3' where return_recid = 'ZDB-LAB-180503-2';
update return_recs set return_recid = 'ZDB-LAB-180720-2' where return_recid = 'ZDB-LAB-180720-1';
update return_recs set return_recid = 'ZDB-LAB-040813-3' where return_recid = 'ZDB-LAB-040813-1';
update return_recs set return_recid = 'ZDB-LAB-150304-2' where return_recid = 'ZDB-LAB-150304-1';
update return_recs set return_recid = 'ZDB-LAB-180305-3' where return_recid = 'ZDB-LAB-180305-2';
update return_recs set return_recid = 'ZDB-LAB-180517-2' where return_recid = 'ZDB-LAB-180517-1';
update return_recs set return_recid = 'ZDB-LAB-180228-2' where return_recid = 'ZDB-LAB-180228-1';
update return_recs set return_recid = 'ZDB-LAB-180129-2' where return_recid = 'ZDB-LAB-180129-1';
update return_recs set return_recid = 'ZDB-LAB-180213-2' where return_recid = 'ZDB-LAB-180213-1';
update return_recs set return_recid = 'ZDB-LAB-170906-3' where return_recid = 'ZDB-LAB-170906-2';
update return_recs set return_recid = 'ZDB-LAB-180517-4' where return_recid = 'ZDB-LAB-180517-3';
update return_recs set return_recid = 'ZDB-LAB-180206-3' where return_recid = 'ZDB-LAB-180206-2';

update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180420-2' where sfp_source_zdb_id = 'ZDB-LAB-180420-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180503-3' where sfp_source_zdb_id = 'ZDB-LAB-180503-2';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180720-2' where sfp_source_zdb_id = 'ZDB-LAB-180720-1';
-- this one has overlapping entries so just delete the old one
delete from source_feature_prefix where sfp_prefix_id = 39 and sfp_source_zdb_id = 'ZDB-LAB-040813-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-150304-2' where sfp_source_zdb_id = 'ZDB-LAB-150304-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180305-3' where sfp_source_zdb_id = 'ZDB-LAB-180305-2';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180517-2' where sfp_source_zdb_id = 'ZDB-LAB-180517-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180228-2' where sfp_source_zdb_id = 'ZDB-LAB-180228-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180129-2' where sfp_source_zdb_id = 'ZDB-LAB-180129-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180213-2' where sfp_source_zdb_id = 'ZDB-LAB-180213-1';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-170906-3' where sfp_source_zdb_id = 'ZDB-LAB-170906-2';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180517-4' where sfp_source_zdb_id = 'ZDB-LAB-180517-3';
update source_feature_prefix set sfp_source_zdb_id = 'ZDB-LAB-180206-3' where sfp_source_zdb_id = 'ZDB-LAB-180206-2';

update updates set rec_id = 'ZDB-LAB-180420-2' where rec_id = 'ZDB-LAB-180420-1';
update updates set rec_id = 'ZDB-LAB-180503-3' where rec_id = 'ZDB-LAB-180503-2';
update updates set rec_id = 'ZDB-LAB-180720-2' where rec_id = 'ZDB-LAB-180720-1';
update updates set rec_id = 'ZDB-LAB-040813-3' where rec_id = 'ZDB-LAB-040813-1';
update updates set rec_id = 'ZDB-LAB-150304-2' where rec_id = 'ZDB-LAB-150304-1';
update updates set rec_id = 'ZDB-LAB-180305-3' where rec_id = 'ZDB-LAB-180305-2';
update updates set rec_id = 'ZDB-LAB-180517-2' where rec_id = 'ZDB-LAB-180517-1';
update updates set rec_id = 'ZDB-LAB-180228-2' where rec_id = 'ZDB-LAB-180228-1';
update updates set rec_id = 'ZDB-LAB-180129-2' where rec_id = 'ZDB-LAB-180129-1';
update updates set rec_id = 'ZDB-LAB-180213-2' where rec_id = 'ZDB-LAB-180213-1';
update updates set rec_id = 'ZDB-LAB-170906-3' where rec_id = 'ZDB-LAB-170906-2';
update updates set rec_id = 'ZDB-LAB-180517-4' where rec_id = 'ZDB-LAB-180517-3';
update updates set rec_id = 'ZDB-LAB-180206-3' where rec_id = 'ZDB-LAB-180206-2';

update updates set old_value = 'ZDB-LAB-180420-2' where old_value = 'ZDB-LAB-180420-1';
update updates set old_value = 'ZDB-LAB-180503-3' where old_value = 'ZDB-LAB-180503-2';
update updates set old_value = 'ZDB-LAB-180720-2' where old_value = 'ZDB-LAB-180720-1';
update updates set old_value = 'ZDB-LAB-040813-3' where old_value = 'ZDB-LAB-040813-1';
update updates set old_value = 'ZDB-LAB-150304-2' where old_value = 'ZDB-LAB-150304-1';
update updates set old_value = 'ZDB-LAB-180305-3' where old_value = 'ZDB-LAB-180305-2';
update updates set old_value = 'ZDB-LAB-180517-2' where old_value = 'ZDB-LAB-180517-1';
update updates set old_value = 'ZDB-LAB-180228-2' where old_value = 'ZDB-LAB-180228-1';
update updates set old_value = 'ZDB-LAB-180129-2' where old_value = 'ZDB-LAB-180129-1';
update updates set old_value = 'ZDB-LAB-180213-2' where old_value = 'ZDB-LAB-180213-1';
update updates set old_value = 'ZDB-LAB-170906-3' where old_value = 'ZDB-LAB-170906-2';
update updates set old_value = 'ZDB-LAB-180517-4' where old_value = 'ZDB-LAB-180517-3';
update updates set old_value = 'ZDB-LAB-180206-3' where old_value = 'ZDB-LAB-180206-2';

update updates set new_value = 'ZDB-LAB-180420-2' where new_value = 'ZDB-LAB-180420-1';
update updates set new_value = 'ZDB-LAB-180503-3' where new_value = 'ZDB-LAB-180503-2';
update updates set new_value = 'ZDB-LAB-180720-2' where new_value = 'ZDB-LAB-180720-1';
update updates set new_value = 'ZDB-LAB-040813-3' where new_value = 'ZDB-LAB-040813-1';
update updates set new_value = 'ZDB-LAB-150304-2' where new_value = 'ZDB-LAB-150304-1';
update updates set new_value = 'ZDB-LAB-180305-3' where new_value = 'ZDB-LAB-180305-2';
update updates set new_value = 'ZDB-LAB-180517-2' where new_value = 'ZDB-LAB-180517-1';
update updates set new_value = 'ZDB-LAB-180228-2' where new_value = 'ZDB-LAB-180228-1';
update updates set new_value = 'ZDB-LAB-180129-2' where new_value = 'ZDB-LAB-180129-1';
update updates set new_value = 'ZDB-LAB-180213-2' where new_value = 'ZDB-LAB-180213-1';
update updates set new_value = 'ZDB-LAB-170906-3' where new_value = 'ZDB-LAB-170906-2';
update updates set new_value = 'ZDB-LAB-180517-4' where new_value = 'ZDB-LAB-180517-3';
update updates set new_value = 'ZDB-LAB-180206-3' where new_value = 'ZDB-LAB-180206-2';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180420-1', 'ZDB-LAB-180420-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180503-2', 'ZDB-LAB-180503-3', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180720-1', 'ZDB-LAB-180720-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-040813-1', 'ZDB-LAB-040813-3', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-150304-1', 'ZDB-LAB-150304-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180305-2', 'ZDB-LAB-180305-3', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180517-1', 'ZDB-LAB-180517-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180228-1', 'ZDB-LAB-180228-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180129-1', 'ZDB-LAB-180129-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180213-1', 'ZDB-LAB-180213-2', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-170906-2', 'ZDB-LAB-170906-3', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180517-3', 'ZDB-LAB-180517-4', 'merged');
insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-LAB-180206-2', 'ZDB-LAB-180206-3', 'merged');

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180420-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180503-2';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180720-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-040813-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-150304-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180305-2';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180517-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180228-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180129-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180213-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-170906-2';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180517-3';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-LAB-180206-2';

delete from lab where zdb_id = 'ZDB-LAB-180420-1';
delete from lab where zdb_id = 'ZDB-LAB-180503-2';
delete from lab where zdb_id = 'ZDB-LAB-180720-1';
delete from lab where zdb_id = 'ZDB-LAB-040813-1';
delete from lab where zdb_id = 'ZDB-LAB-150304-1';
delete from lab where zdb_id = 'ZDB-LAB-180305-2';
delete from lab where zdb_id = 'ZDB-LAB-180517-1';
delete from lab where zdb_id = 'ZDB-LAB-180228-1';
delete from lab where zdb_id = 'ZDB-LAB-180129-1';
delete from lab where zdb_id = 'ZDB-LAB-180213-1';
delete from lab where zdb_id = 'ZDB-LAB-170906-2';
delete from lab where zdb_id = 'ZDB-LAB-180517-3';
delete from lab where zdb_id = 'ZDB-LAB-180206-2';

-- DIFFERENTIATE ZDB-LAB-140305-1 AND ZDB-LAB-180604-2
update lab set name = 'Cao Lab, Weill Cornell Medical College' where zdb_id = 'ZDB-LAB-180604-2';

-- ADD UNIQUE CONSTRAINT
alter table lab add constraint name_unique unique (name);
