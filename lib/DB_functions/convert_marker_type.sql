CREATE OR REPLACE FUNCTION convert_marker_type(
    oldGeneId VARCHAR,
    newGeneType VARCHAR
) RETURNS text AS $$
DECLARE
    currentType VARCHAR;
    markerAbbrev VARCHAR;
    extractedDate VARCHAR;
    newGeneId VARCHAR;
    nomenId VARCHAR;
    daliasId VARCHAR;
BEGIN

-- What is the current type of geneId
currentType := get_obj_type(oldGeneId);

-- if currentType and geneType are the same, throw exception
if currentType = newGeneType then
    raise exception 'Gene % is already of type %', oldGeneId, newGeneType;
end if;

-- geneType must be one of NCRNAG, GENE, etc.
if newGeneType not in (SELECT marker_type from marker_types) then
    raise exception 'geneType must be one of the types in marker_types table (eg. NCRNAG, GENE,  etc.)';
end if;

select mrkr_abbrev into markerAbbrev from marker where mrkr_zdb_id = oldGeneId;

-- Extract the date part from the gene ID
extractedDate := get_date_from_id(oldGeneId, 'YYMMDD');

-- get the backdated ID for the new ncrna
newGeneId := get_backdated_id(newGeneType, extractedDate);

--notify the ID change
raise notice 'ID to change from % to %', oldGeneId, newGeneId;

-- get the backdated ID for the new nomenclature
nomenId := get_id('NOMEN');

-- get the backdated ID for the new dalias
daliasId := get_id('DALIAS');

raise notice 'daliasId: %', daliasId;
raise notice 'nomenId: %', nomenId;

INSERT INTO zdb_active_data (zactvd_zdb_id) VALUES (newGeneId);

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_comments, mrkr_abbrev, mrkr_type, mrkr_owner, mrkr_name_order, mrkr_abbrev_order)
select newGeneId, mrkr_name || '_temp', mrkr_comments, mrkr_abbrev || '_temp', newGeneType, mrkr_owner, mrkr_name_order, mrkr_abbrev_order
from marker
where mrkr_zdb_id = oldGeneId;

-- UPDATES AS PERFORMED IF RUNNING: merge_markers.pl
update marker_history set mhist_mrkr_zdb_id = newGeneId where mhist_mrkr_zdb_id = oldGeneId;
update marker_history_audit set mha_mrkr_zdb_id = newGeneId where mha_mrkr_zdb_id = oldGeneId;
update marker_relationship set mrel_mrkr_1_zdb_id = newGeneId where mrel_mrkr_1_zdb_id = oldGeneId;
update data_alias set dalias_data_zdb_id = newGeneId where dalias_data_zdb_id = oldGeneId;
update db_link set dblink_linked_recid = newGeneId where dblink_linked_recid = oldGeneId;
update paneled_markers set zdb_id = newGeneId where zdb_id = oldGeneId;
update sequence_feature_chromosome_location_generated set sfclg_data_zdb_id = newGeneId where sfclg_data_zdb_id = oldGeneId;
update record_attribution set recattrib_data_zdb_id = newGeneId where recattrib_data_zdb_id = oldGeneId;
-- END OF merge_markers.pl UPDATES

-- UPDATES NOT IN: merge_markers.pl
-- zmap_pub_pan_mark table
update zmap_pub_pan_mark set zdb_id= newGeneId where zdb_id = oldGeneId;

-- unique_location table
update unique_location set ul_data_zdb_id = newGeneId where ul_data_zdb_id = oldGeneId;

-- updates table
update updates set rec_id = newGeneId where rec_id = oldGeneId;
-- END OF UPDATES NOT IN merge_markers.pl


-- create alias and nomenclature history (is this necessary for ID change? or only for name change?)
insert into zdb_active_data values(daliasId);
insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
values (daliasId, newGeneId, markerAbbrev, '1');

insert into zdb_active_data values(nomenId);
insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date,
                            mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
values (nomenId, newGeneId, 'renamed', 'same marker', now(),
        markerAbbrev, markerAbbrev, 'ID changed from ' || oldGeneId || ' to ' || newGeneId, daliasId);
-- end of alias / nomenclature

-- delete db_link for "AGR Gene" foreign DB
delete from zdb_active_data
where exists(select 1 from db_link
             where dblink_zdb_id = zactvd_zdb_id
               and dblink_linked_recid = newGeneId
               and dblink_acc_num = oldGeneId
               and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-171018-1');

delete from zdb_replaced_data where zrepld_old_zdb_id = oldGeneId;
update zdb_replaced_data set zrepld_new_zdb_id = newGeneId where zrepld_new_zdb_id = oldGeneId;

-- delete from expression_experiment2 in case of foreign key constraint
create temp table expression_experiment2_rows2change as select * from expression_experiment2 where xpatex_gene_zdb_id = oldGeneId;
if exists(select 1 from expression_experiment2_rows2change) then
    raise warning 'There are expression experiments for this gene. Will update with new gene ID in expression_experiment2 table.';
    delete from expression_experiment2 where xpatex_gene_zdb_id = oldGeneId;
end if;
-- finished with expression_experiment2 delete, will re-add later

create temp table expression_figure_stage_rows2change as select * from expression_figure_stage where efs_xpatex_zdb_id in (select xpatex_zdb_id from expression_experiment2_rows2change);
if exists(select 1 from expression_figure_stage_rows2change) then
    raise warning 'There are expression_figure_stages for this gene. Will update with new gene ID in expression_figure_stage table.';
--     delete from expression_figure_stage where efs_xpatex_zdb_id in (select xpatex_zdb_id from expression_experiment2_rows2change);
end if;

-- feature_marker_relationship
create temp table feature_marker_relationship_rows2change as select * from feature_marker_relationship where fmrel_mrkr_zdb_id = oldGeneId;
if exists(select 1 from feature_marker_relationship_rows2change) then
    raise warning 'There are feature_marker_relationships for this gene. Will update with new gene ID in feature_marker_relationship table.';
    delete from feature_marker_relationship where fmrel_mrkr_zdb_id = oldGeneId;
end if;
-- end of feature_marker_relationship handling, will re-add later

-- gene_description
create temp table gene_description_rows2change as select * from gene_description where gd_gene_zdb_id = oldGeneId;
if exists(select 1 from gene_description_rows2change) then
    raise warning 'There are gene_descriptions for this gene. Will update with new gene ID in gene_description table.';
    delete from gene_description where gd_gene_zdb_id = oldGeneId;
end if;
-- end of gene_description handling, will re-add later

-- snp_download (snpd_mrkr_zdb_id is foreign key)
create temp table snp_download_rows2change as select * from snp_download where snpd_mrkr_zdb_id = oldGeneId;
if exists(select 1 from snp_download_rows2change) then
    raise warning 'There are snp_downloads for this gene. Will update with new gene ID in snp_download table.';
    delete from snp_download where snpd_mrkr_zdb_id = oldGeneId;
end if;
-- end of snp_download handling, will re-add later

create temp table ui.publication_expression_display_rows2change as select * from ui.publication_expression_display where ped_gene_zdb_id = oldGeneId;
if exists(select 1 from ui.publication_expression_display_rows2change) then
    raise info 'There are ui.publication_expression_display for this gene. Will update with new gene ID in snp_download table.';
    delete from ui.publication_expression_display where ped_gene_zdb_id = oldGeneId;
end if;

--------------------------------------------------
-- THIS IS WHERE THE ACTUAL ID CHANGE HAPPENS
-- -----------------------------------------------
-- TODO: can the delete be changed to an update with cascades?
delete from zdb_active_data where zactvd_zdb_id = oldGeneId;
insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values (oldGeneId, newGeneId);
update marker set mrkr_name = markerAbbrev, mrkr_abbrev = markerAbbrev where mrkr_zdb_id = newGeneId;
-- ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
-- END OF ID CHANGE


-- re-add expression_experiment2 rows
update expression_experiment2_rows2change set xpatex_gene_zdb_id = newGeneId;
insert into expression_experiment2 (select * from expression_experiment2_rows2change);
drop table expression_experiment2_rows2change;
-- finished with re-add of expression_experiment2 rows

-- re-add expression_figure_stage rows
insert into expression_figure_stage (select * from expression_figure_stage_rows2change);
drop table expression_figure_stage_rows2change;
-- end of re-add expression_figure_stage rows

-- re-add feature_marker_relationship rows
update feature_marker_relationship_rows2change set fmrel_mrkr_zdb_id = newGeneId;
insert into feature_marker_relationship (select * from feature_marker_relationship_rows2change);
drop table feature_marker_relationship_rows2change;
-- end of re-add feature_marker_relationship rows

-- re-add gene_description rows
update gene_description_rows2change set gd_gene_zdb_id = newGeneId;
insert into gene_description (select * from gene_description_rows2change);
drop table gene_description_rows2change;
-- end of re-add gene_description rows

-- re-add snp_download rows
update snp_download_rows2change set snpd_mrkr_zdb_id = newGeneId;
insert into snp_download (select * from snp_download_rows2change);
drop table snp_download_rows2change;
-- end of re-add snp_download rows

-- re-add ui.publication_expression_display rows
update ui.publication_expression_display_rows2change set ped_gene_zdb_id = newGeneId;
insert into ui.publication_expression_display (select * from ui.publication_expression_display_rows2change);
drop table ui.publication_expression_display_rows2change;
-- end of re-add ui.publication_expression_display rows

-- update accession_bank
update accession_bank set accbk_defline = replace(accbk_defline, oldGeneId || ' ', newGeneId || ' ') where accbk_defline like '%' || oldGeneId || ' %';

-- expression_experiment
update expression_experiment set xpatex_gene_zdb_id = newGeneId where xpatex_gene_zdb_id = oldGeneId;

-- xpat_exp_details_generated
update xpat_exp_details_generated set xedg_gene_zdb_id = newGeneId where xedg_gene_zdb_id = oldGeneId;

-- zfin_ensembl_gene
update zfin_ensembl_gene set zeg_id_name = replace(zeg_id_name, 'gene_id=' || oldGeneId || ';', 'gene_id=' || newGeneId || ';'),
                             zeg_gene_zdb_id = newGeneId
                         where zeg_gene_zdb_id = oldGeneId;


perform regen_genox_marker(newGeneId);

return newGeneId;

END;
$$ LANGUAGE plpgsql;
