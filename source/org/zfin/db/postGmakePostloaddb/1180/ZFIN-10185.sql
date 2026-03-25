--liquibase formatted sql
--changeset rtaylor:ZFIN-10185

drop table if exists lines_tmp;
drop table if exists tmp_addgene;
drop table if exists tmp_db_link;
drop table if exists tmp_expression_phenotype_term;
drop table if exists tmp_feature;
drop table if exists tmp_figure;
drop table if exists tmp_ftrchrdate;
drop table if exists tmp_ftrchrdate2;
drop table if exists tmp_ftrnote;
drop table if exists tmp_genox_restore;
drop table if exists tmp_ottens;
drop table if exists tmp_pub;
drop table if exists tmp_pub2;
drop table if exists tmp_pub3;
drop table if exists tmp_pub5;
drop table if exists tmp_rnac_zfin;
drop table if exists tmpcne;
drop table if exists do_oevdispupdate_temp_first;
drop table if exists do_oevdispupdate_temp_second;
drop table if exists ensdarg_temp;
drop table if exists pre_fmrel_temp;
drop table if exists pre_gene_temp;
drop table if exists xpat_exp_details_generated_temp;
drop table if exists xpat_results_generated_temp;
drop table if exists feature_temp;
drop table if exists genox_fish_annotation_search_temp;
drop table if exists linkage_member_temp;

drop function if exists p_update_BurgessLinn_genotype_names();
