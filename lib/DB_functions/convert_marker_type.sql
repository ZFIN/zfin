CREATE OR REPLACE FUNCTION convert_marker_type(
    oldGeneId VARCHAR,
    newGeneType VARCHAR,
    submitterId VARCHAR DEFAULT NULL
) RETURNS text AS $$
DECLARE
    currentType VARCHAR;
    markerAbbrev VARCHAR;
    markerName VARCHAR;
    extractedDate VARCHAR;
    newGeneId VARCHAR;
    nomenId VARCHAR;
    daliasId VARCHAR;
    table_name_column_pair RECORD;
    affected_rows INTEGER;
    affected_tables TEXT := '';
BEGIN

    select mrkr_abbrev, mrkr_name into markerAbbrev, markerName from marker where mrkr_zdb_id = oldGeneId;

    if markerAbbrev is null then
        raise exception 'Marker % does not exist', oldGeneId;
    end if;

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
        select newGeneId, markerName || '_temp', mrkr_comments, markerAbbrev || '_temp', newGeneType, mrkr_owner, mrkr_name_order, mrkr_abbrev_order
        from marker
        where mrkr_zdb_id = oldGeneId;

-- create alias and nomenclature history (is this necessary for ID change? or only for name change?)
    insert into zdb_active_data values(daliasId);
    insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
        values (daliasId, newGeneId, markerAbbrev, '1');

    insert into zdb_active_data values(nomenId);
    insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date,
                                mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
        values (nomenId, newGeneId, 'renamed', 'same marker', now(),
                markerName, markerAbbrev, 'ID changed from ' || oldGeneId || ' to ' || newGeneId, daliasId);
    -- end of alias / nomenclature

    update zfin_ensembl_gene
        set zeg_id_name = replace(zeg_id_name, 'gene_id=' || oldGeneId || ';', 'gene_id=' || newGeneId || ';'),
            zeg_gene_zdb_id = newGeneId
        where zeg_gene_zdb_id = oldGeneId;
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    IF affected_rows > 0 THEN
        affected_tables := affected_tables || 'zfin_ensembl_gene: ' || affected_rows || E'\n';
    END IF;

-- tables that have dependencies on marker and the column name (could be foreign key or just a column)
    FOR table_name_column_pair IN (SELECT *
       FROM (VALUES
                 ('record_attribution', 'recattrib_data_zdb_id'),
                 ('accession_bank', 'accbk_defline'),
                 ('antibody', 'atb_zdb_id'),
                 ('clean_expression_fast_search', 'cefs_mrkr_zdb_id'),
                 ('clone', 'clone_mrkr_zdb_id'),
                 ('construct_component', 'cc_component_zdb_id'),
                 ('construct_marker_relationship', 'conmrkrrel_mrkr_zdb_id'),
                 ('data_alias', 'dalias_data_zdb_id'),
                 ('data_note', 'dnote_data_zdb_id'),
                 ('db_link', 'dblink_acc_num'),
                 ('db_link', 'dblink_linked_recid'),
                 ('expression_experiment2', 'xpatex_gene_zdb_id'),
                 ('expression_experiment2', 'xpatex_probe_feature_zdb_id'),
                 ('external_note', 'extnote_data_zdb_id'),
                 ('external_reference', 'exref_data_zdb_id'),
                 ('feature_marker_relationship', 'fmrel_mrkr_zdb_id'),
                 ('fish_str', 'fishstr_str_zdb_id'),
                 ('fluorescent_marker', 'fm_mrkr_zdb_id'),
                 ('fpprotein_construct', 'fc_mrkr_zdb_id'),
                 ('fpprotein_efg', 'fe_mrkr_zdb_id'),
                 ('gene_description', 'gd_gene_zdb_id'),
                 ('genedom_family_member', 'gfammem_mrkr_zdb_id'),
                 ('linkage_membership', 'lnkgm_member_1_zdb_id'),
                 ('linkage_membership', 'lnkgm_member_2_zdb_id'),
                 ('linkage_membership_search', 'lms_member_1_zdb_id'),
                 ('linkage_membership_search', 'lms_member_2_zdb_id'),
                 ('linkage_single', 'lsingle_member_zdb_id'),
                 ('genotype_figure_fast_search', 'gffs_morph_zdb_id'),
                 ('int_data_source', 'ids_data_zdb_id'),
                 ('int_data_supplier', 'idsup_data_zdb_id'),
                 ('mapped_marker', 'marker_id'),
                 ('marker_annotation_status', 'mas_mrkr_zdb_id'),
                 ('marker_assembly', 'ma_mrkr_zdb_id'),
                 ('marker_go_term_evidence', 'mrkrgoev_mrkr_zdb_id'),
                 ('marker_history', 'mhist_mrkr_zdb_id'),
                 ('marker_history_audit', 'mha_mrkr_zdb_id'),
                 ('marker_relationship', 'mrel_mrkr_1_zdb_id'),
                 ('marker_relationship', 'mrel_mrkr_2_zdb_id'),
                 ('marker_sequence', 'seq_mrkr_zdb_id'),
                 ('marker_to_protein', 'mtp_mrkr_zdb_id'),
                 ('ortholog', 'ortho_zebrafish_gene_zdb_id'),
                 ('paneled_markers', 'zdb_id'),
                 ('phenotype_observation_generated', 'psg_mrkr_zdb_id'),
                 ('primer_set', 'marker_id'),
                 ('reference_protein', 'rp_gene_zdb_id'),
                 ('sequence_feature_chromosome_location_generated', 'sfclg_data_zdb_id'),
                 ('snp_download', 'snpd_mrkr_zdb_id'),
                 ('transcript', 'tscript_mrkr_zdb_id'),
                 ('unique_location', 'ul_data_zdb_id'),
                 ('updates', 'rec_id'),
                 ('url_ref', 'urlref_data_zdb_id'),
                 ('variant_flanking_sequence', 'vfseq_data_zdb_id'),
                 ('xpat_exp_details_generated', 'xedg_gene_zdb_id'),
                 ('zdb_replaced_data', 'zrepld_new_zdb_id'),
                 ('zmap_pub_pan_mark', 'zdb_id')
            ) AS table_pairs (table_name, fk_column_name))
        LOOP

            EXECUTE format('UPDATE %I SET %I = $1 WHERE %I = $2',
                           table_name_column_pair.table_name, table_name_column_pair.fk_column_name, table_name_column_pair.fk_column_name)
                USING newGeneId, oldGeneId;

            GET DIAGNOSTICS affected_rows = ROW_COUNT;
            IF affected_rows > 0 THEN
                affected_tables := affected_tables || table_name_column_pair.table_name || '.' || table_name_column_pair.fk_column_name || ': ' || affected_rows || E'\n';
            END IF;

        END LOOP;

    -- update ui schema
    UPDATE ui.omim_zfin_association set oza_zfin_gene_zdb_id = newGeneId where oza_zfin_gene_zdb_id = oldGeneId;
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    IF affected_rows > 0 THEN
        affected_tables := affected_tables || 'ui.omim_zfin_association: ' || affected_rows || E'\n';
    END IF;

    UPDATE ui.phenotype_zfin_association set pza_gene_zdb_id = newGeneId where pza_gene_zdb_id = oldGeneId;
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    IF affected_rows > 0 THEN
        affected_tables := affected_tables || 'ui.phenotype_zfin_association: ' || affected_rows || E'\n';
    END IF;

    UPDATE ui.publication_expression_display set ped_gene_zdb_id = newGeneId where ped_gene_zdb_id = oldGeneId;
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    IF affected_rows > 0 THEN
        affected_tables := affected_tables || 'ui.publication_expression_display.ped_gene_zdb_id: ' || affected_rows || E'\n';
    END IF;

    UPDATE ui.publication_expression_display set ped_antibody_zdb_id = newGeneId where ped_antibody_zdb_id = oldGeneId;
    GET DIAGNOSTICS affected_rows = ROW_COUNT;
    IF affected_rows > 0 THEN
        affected_tables := affected_tables || 'ui.publication_expression_display.ped_antibody_zdb_id: ' || affected_rows || E'\n';
    END IF;

    --------------------------------------------------
    -- THIS IS WHERE THE ACTUAL ID CHANGE HAPPENS
    -- -----------------------------------------------
    --
    delete from zdb_active_data where zactvd_zdb_id = oldGeneId;
    insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values (oldGeneId, newGeneId);
    update marker set mrkr_name = markerName, mrkr_abbrev = markerAbbrev where mrkr_zdb_id = newGeneId;
    -- ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    -- END OF ID CHANGE


    -- Record the type change in updates
    INSERT INTO updates (submitter_id, rec_id, field_name, old_value, new_value, comments, upd_when)
        VALUES (submitterId, newGeneId, 'marker type', currentType, newGeneType,
                'Converted from ' || oldGeneId || ' (' || currentType || ') to ' || newGeneId || ' (' || newGeneType || ')',
                now());

    perform regen_genox_marker(newGeneId);

    return newGeneId || E'\n\nRows updated:\n' || COALESCE(NULLIF(affected_tables, ''), '(none)');

END;
$$ LANGUAGE plpgsql;
