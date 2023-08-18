CREATE OR REPLACE FUNCTION convert_gene_to_ncrna(
    geneId VARCHAR
) RETURNS text AS $$
DECLARE
    markerAbbrev VARCHAR;
    extractedDate VARCHAR;
    ncrnagId VARCHAR;
    nomenId VARCHAR;
    daliasId VARCHAR;
BEGIN

select mrkr_abbrev into markerAbbrev from marker where mrkr_zdb_id = geneId;

-- Extract the date part from the gene ID
extractedDate := get_date_from_id(geneId, 'YYMMDD');

-- get the backdated ID for the new ncrna
ncrnagId := get_backdated_id('NCRNAG', extractedDate);

--notify the ID change
raise notice 'ID to change from % to %', geneId, ncrnagId;


-- get the backdated ID for the new nomenclature
nomenId := get_id('NOMEN');

-- get the backdated ID for the new dalias
daliasId := get_id('DALIAS');

raise notice 'daliasId: %', daliasId;
raise notice 'nomenId: %', nomenId;

INSERT INTO zdb_active_data (zactvd_zdb_id) VALUES (ncrnagId);

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_comments, mrkr_abbrev, mrkr_type, mrkr_owner, mrkr_name_order)
select ncrnagId, mrkr_name || '_temp', mrkr_comments, mrkr_abbrev || '_temp', 'NCRNAG', mrkr_owner, mrkr_name_order
from marker
where mrkr_zdb_id = geneId;

-- updates as performed if running merge_markers.pl
update marker_history set mhist_mrkr_zdb_id = ncrnagId where mhist_mrkr_zdb_id = geneId;
update marker_history_audit set mha_mrkr_zdb_id = ncrnagId where mha_mrkr_zdb_id = geneId;
update marker_relationship set mrel_mrkr_1_zdb_id = ncrnagId where mrel_mrkr_1_zdb_id = geneId;
update data_alias set dalias_data_zdb_id = ncrnagId where dalias_data_zdb_id = geneId;
update db_link set dblink_linked_recid = ncrnagId where dblink_linked_recid = geneId;
update paneled_markers set zdb_id = ncrnagId where zdb_id = geneId;
update sequence_feature_chromosome_location_generated set sfclg_data_zdb_id = ncrnagId where sfclg_data_zdb_id = geneId;
update record_attribution set recattrib_data_zdb_id = ncrnagId where recattrib_data_zdb_id = geneId;

-- not in merge_markers.pl

-- zmap_pub_pan_mark table
update zmap_pub_pan_mark set zdb_id= ncrnagId where zdb_id = geneId;

-- unique_location table
update unique_location set ul_data_zdb_id = ncrnagId where ul_data_zdb_id = geneId;

-- updates table
update updates set rec_id = ncrnagId where rec_id = geneId;

-- end of updates not in merge_markers.pl


-- create alias and nomenclature history (is this necessary for ID change? or only for name change?)
insert into zdb_active_data values(daliasId);
insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
values (daliasId, ncrnagId, markerAbbrev, '1');

insert into zdb_active_data values(nomenId);
insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date,
                            mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
values (nomenId, ncrnagId, 'renamed', 'same marker', now(),
        markerAbbrev, markerAbbrev, 'ID changed from ' || geneId || ' to ' || ncrnagId, daliasId);
-- end of alias / nomenclature

-- delete db_link for "AGR Gene" foreign DB
delete from zdb_active_data
where exists(select 1 from db_link
             where dblink_zdb_id = zactvd_zdb_id
               and dblink_linked_recid = ncrnagId
               and dblink_acc_num = geneId
               and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-171018-1');


delete from zdb_replaced_data where zrepld_old_zdb_id = geneId;
update zdb_replaced_data set zrepld_new_zdb_id = ncrnagId where zrepld_new_zdb_id = geneId;
delete from zdb_active_data where zactvd_zdb_id = geneId;
insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values (geneId, ncrnagId);

update marker set mrkr_name = markerAbbrev, mrkr_abbrev = markerAbbrev where mrkr_zdb_id = ncrnagId;

perform regen_genox_marker(ncrnagId);

return ncrnagId;

END;
$$ LANGUAGE plpgsql;
