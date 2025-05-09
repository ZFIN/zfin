create or replace function create_str_marker(strType varchar, geneZdbId varchar, targetGeneName varchar, ownerID varchar) RETURNS void AS $$

declare
    currentIndex integer;
    strRow       crispr_id%rowtype;
    strName      varchar;
    mrelID      varchar;
    fmrelID      varchar;
    featureId      varchar;
begin
    for currentIndex in
        select si.max_index
        from str_index as si
        where si.gene_zdb_id = geneZdbId
        loop
            for strRow in
                select *
                from crispr_id as ci
                where ci.zdb_id = geneZdbId
                loop
                    strName = strType || currentIndex + 1 || '-' || targetGeneName;
                    insert into marker (mrkr_abbrev, mrkr_type, mrkr_zdb_id, mrkr_name, mrkr_owner) values (strName, strType, strRow.get_id, strName, ownerID);
                    insert into marker_sequence (seq_mrkr_zdb_id, seq_type, seq_sequence) values (strRow.get_id, 'Nucleotide', strRow.sequence2);
                    select get_id('MREL') into mrelID from single;
                    insert into zdb_active_data values (mrelID);
                    insert into marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id) values
                                            (mrelID, 'knockdown reagent targets gene',strRow.get_id, geneZdbId);
                    insert into record_attribution (recattrib_data_zdb_id, recattrib_source_type, recattrib_source_zdb_id) values
                                            (mrelID, 'standard', 'ZDB-PUB-191214-4');
                    for featureId in
                        select feature_zdb_id from gene_allele_mutation_detail
                            where zdb_id = geneZdbId and feature_zdb_id is not null
                    loop
                            select get_id('FMREL') into fmrelID from single;
                            insert into zdb_active_data values (fmrelID);
                        insert into feature_marker_relationship
                            (fmrel_zdb_id, fmrel_type, fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id)
                            values (fmrelID, 'created by', featureId, strRow.get_id);
                        end loop;
                    currentIndex := currentIndex + 1;
                end loop;
        end loop;
end

$$ LANGUAGE plpgsql;
