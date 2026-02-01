create or replace function create_str_marker(strType varchar, ownerID varchar) RETURNS void AS $$

declare
    currentIndex integer;
    strRow       crispr_id%rowtype;
    strIndexRow       str_index%rowtype;
    strName      varchar;
    mrelID      varchar;
    fmrelID      varchar;
    featureId      varchar;
    targetGeneName      varchar;
    talenSeq1    varchar;
    talenSeq2    varchar;
begin
    for strIndexRow in
        select *
        from str_index
        order by gene_zdb_id
        loop
            raise notice 'GeneID: %', strIndexRow.gene_zdb_id;
            currentIndex = strIndexRow.max_index;
            for strRow in
                select *
                from crispr_id as ci
                where ci.zdb_id = strIndexRow.gene_zdb_id
                loop
                    raise notice 'STR_ID: %', strRow.get_id;
                    -- skip if marker already exists by zdb_id
                    if not exists (select 1 from marker where mrkr_zdb_id = strRow.get_id) then
                        select mrkr_abbrev from marker where mrkr_zdb_id = strIndexRow.gene_zdb_id into targetGeneName;
                        strName = strType || (currentIndex + 1) || '-' || targetGeneName;
                        raise notice 'STR Name: %', strName;
                        -- skip if marker with same abbreviation already exists
                        if not exists (select 1 from marker where mrkr_abbrev = strName) then
                            insert into marker (mrkr_abbrev, mrkr_type, mrkr_zdb_id, mrkr_name, mrkr_owner) values (strName, strType, strRow.get_id, strName, ownerID);
                            -- For TALEN, get both sequences from temp_talen using zko_id
                            if strType = 'TALEN' then
                                select sequence1, sequence2 into talenSeq1, talenSeq2 from temp_talen where zko_id = strRow.zko_id limit 1;
                                insert into marker_sequence (seq_mrkr_zdb_id, seq_type, seq_sequence, seq_sequence_2) values (strRow.get_id, 'Nucleotide', talenSeq1, talenSeq2);
                            else
                                insert into marker_sequence (seq_mrkr_zdb_id, seq_type, seq_sequence) values (strRow.get_id, 'Nucleotide', strRow.sequence2);
                            end if;
                            select get_id('MREL') into mrelID from single;
                            insert into zdb_active_data values (mrelID);
                            insert into marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id) values
                                (mrelID, 'knockdown reagent targets gene',strRow.get_id, strIndexRow.gene_zdb_id);
                            insert into record_attribution (recattrib_data_zdb_id, recattrib_source_type, recattrib_source_zdb_id) values
                                (mrelID, 'standard', 'ZDB-PUB-191214-4');
                            for featureId in
                                -- Match by zko_id to ensure correct CRISPR-feature association
                                select feature_zdb_id from gene_allele_mutation_detail
                                where zko_id = strRow.zko_id and feature_zdb_id is not null
                                loop
                                    select get_id('FMREL') into fmrelID from single;
                                    insert into zdb_active_data values (fmrelID);
                                    insert into feature_marker_relationship
                                    (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)
                                    values (fmrelID, 'created by', featureId, strRow.get_id);
                                end loop;
                            currentIndex := currentIndex + 1;
                        else
                            raise notice 'Skipping existing abbreviation: %', strName;
                        end if;
                    else
                        raise notice 'Skipping existing marker: %', strRow.get_id;
                    end if;
                end loop;
        end loop;
end

$$ LANGUAGE plpgsql;
