--liquibase formatted sql
--changeset rtaylor:ZFIN-9268.sql

-- restore the ID of the gene that was deleted
insert into zdb_active_data (zactvd_zdb_id) values ('ZDB-GENE-030131-5242');

-- remove the newly created gene:
delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-240617-1';

-- marker
INSERT INTO "public"."marker" ("mrkr_zdb_id", "mrkr_name", "mrkr_comments", "mrkr_abbrev", "mrkr_type", "mrkr_owner", "mrkr_name_order", "mrkr_abbrev_order") VALUES ('ZDB-GENE-030131-5242', 'si:dkey-90l23.1', NULL, 'si:dkey-90l23.1', 'GENE', 'ZDB-PERS-000417-1', 'si:dkey-0000000090l0000000023.0000000001', 'si:dkey-0000000090l0000000023.0000000001');

--data_alias
update data_alias set dalias_data_zdb_id = 'ZDB-GENE-030131-5242' where dalias_zdb_id in ('ZDB-DALIAS-030131-5245', 'ZDB-DALIAS-100921-24');

--data_note
update data_note set dnote_data_zdb_id = 'ZDB-GENE-030131-5242' where dnote_zdb_id = 'ZDB-DNOTE-240105-3';

-- db_link
update db_link set dblink_linked_recid = 'ZDB-GENE-030131-5242' where dblink_zdb_id = 'ZDB-DBLINK-140109-10794';
update db_link set dblink_linked_recid = 'ZDB-GENE-030131-5242' where dblink_zdb_id = 'ZDB-DBLINK-200123-20401';

-- insert lost db_links
insert into zdb_active_data (zactvd_zdb_id) values ('ZDB-DBLINK-151026-3662'), ('ZDB-DBLINK-171018-13289'), ('ZDB-DBLINK-230428-79768') ON CONFLICT DO NOTHING;
INSERT INTO "public"."db_link" ("dblink_linked_recid", "dblink_acc_num", "dblink_info", "dblink_zdb_id", "dblink_acc_num_display", "dblink_length", "dblink_fdbcont_zdb_id") VALUES ('ZDB-GENE-030131-5242', 'ENSDARG00000092057', 'uncurrated 10/26/2015', 'ZDB-DBLINK-151026-3662', 'ENSDARG00000092057', 0, 'ZDB-FDBCONT-061018-1');
INSERT INTO "public"."db_link" ("dblink_linked_recid", "dblink_acc_num", "dblink_info", "dblink_zdb_id", "dblink_acc_num_display", "dblink_length", "dblink_fdbcont_zdb_id") VALUES ('ZDB-GENE-030131-5242', 'ZDB-GENE-030131-5242', NULL, 'ZDB-DBLINK-171018-13289', 'ZDB-GENE-030131-5242', NULL, 'ZDB-FDBCONT-171018-1');
INSERT INTO "public"."db_link" ("dblink_linked_recid", "dblink_acc_num", "dblink_info", "dblink_zdb_id", "dblink_acc_num_display", "dblink_length", "dblink_fdbcont_zdb_id") VALUES ('ZDB-GENE-030131-5242', 'FP017164', 'uncurated: NCBI gene load 2023-04-28 19:43:06.263583-07', 'ZDB-DBLINK-230428-79768', 'FP017164', 55379, 'ZDB-FDBCONT-040412-36');

-- delete the new db_link that was created
delete from db_link where dblink_zdb_id = 'ZDB-DBLINK-240617-2';
delete from zdb_active_data where zactvd_zdb_id = 'ZDB-DBLINK-240617-2';

-- feature_marker_relationship
update feature_marker_relationship set fmrel_mrkr_zdb_id = 'ZDB-GENE-030131-5242' where fmrel_zdb_id = 'ZDB-FMREL-210121-12';

-- gene_description
INSERT INTO "public"."gene_description" ("gd_pk_id", "gd_gene_zdb_id", "gd_go_description", "gd_go_function_description", "gd_go_process_description", "gd_go_component_description", "gd_do_description", "gd_do_experimental_description", "gd_do_biomarker_description", "gd_do_orthology_description", "gd_orthology_description", "gd_description") VALUES (7745, 'ZDB-GENE-030131-5242', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Predicted to have carbohydrate binding activity.');

-- marker_history
update marker_history set mhist_mrkr_zdb_id = 'ZDB-GENE-030131-5242' where mhist_zdb_id in ('ZDB-NOMEN-100921-112', 'ZDB-NOMEN-100921-113');

-- marker_history_audit
update marker_history_audit set mha_mrkr_zdb_id = 'ZDB-GENE-030131-5242' where mha_pk_id in (35381, 109333);
delete from marker_history_audit where mha_pk_id = 304810;

-- marker_relationship
update marker_relationship set mrel_mrkr_1_zdb_id = 'ZDB-GENE-030131-5242' where mrel_zdb_id in ('ZDB-MREL-030131-5242', 'ZDB-MREL-050302-95', 'ZDB-MREL-100921-507');
update marker_relationship set mrel_mrkr_2_zdb_id = 'ZDB-GENE-030131-5242' where mrel_zdb_id = 'ZDB-MREL-210121-29';

-- paneled_markers
update paneled_markers set zdb_id = 'ZDB-GENE-030131-5242' where zdb_id = 'ZDB-GENE-041008-200';

-- record_attribution
INSERT INTO "public"."record_attribution" ("recattrib_pk_id", "recattrib_data_zdb_id", "recattrib_source_zdb_id", "recattrib_source_significance", "recattrib_source_type", "recattrib_created_at", "recattrib_modified_at", "recattrib_modified_count") VALUES (292896, 'ZDB-GENE-030131-5242', 'ZDB-PUB-030703-1', NULL, 'standard', NULL, NULL, NULL);
INSERT INTO "public"."record_attribution" ("recattrib_pk_id", "recattrib_data_zdb_id", "recattrib_source_zdb_id", "recattrib_source_significance", "recattrib_source_type", "recattrib_created_at", "recattrib_modified_at", "recattrib_modified_count") VALUES (112816095, 'ZDB-GENE-030131-5242', 'ZDB-PUB-030508-1', NULL, 'standard', '2023-02-21 08:22:53.25889', '2023-02-21 08:22:53.25889', 0);
update record_attribution set recattrib_data_zdb_id = 'ZDB-GENE-030131-5242' where recattrib_pk_id = 106406561;

-- sequence_feature_chromosome_location_generated
update sequence_feature_chromosome_location_generated
    set sfclg_data_zdb_id = 'ZDB-GENE-030131-5242'
    where sfclg_pk_id in (280589858, 281696336, 481893224, 481950555, 733657012, 733681913)
      and sfclg_data_zdb_id = 'ZDB-GENE-041008-200' ;

-- updates
delete from updates where rec_id = 'ZDB-GENE-240617-1';

-- zdb_replaced_data
delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-GENE-030131-5242' and zrepld_new_zdb_id = 'ZDB-GENE-041008-200';

-- zmap_pub_pan_mark
update zmap_pub_pan_mark set zdb_id = 'ZDB-GENE-030131-5242' where zdb_id = 'ZDB-GENE-041008-200';





                               
                               
                               


