-- merge_transcripts(source_id, target_id)
-- Merges one transcript into another: moves all dependent data from source to target,
-- adds a replacement record, and deletes the source transcript.

create or replace function merge_transcripts(
    source_id text,
    target_id text
) returns void as $$
declare
    source_abbrev text;
    target_abbrev text;
    source_gene text;
    target_gene text;
begin
    -- Validate both transcripts exist
    select mrkr_abbrev into source_abbrev from marker where mrkr_zdb_id = source_id and mrkr_type = 'TSCRIPT';
    if source_abbrev is null then
        raise exception 'Source transcript % not found', source_id;
    end if;

    select mrkr_abbrev into target_abbrev from marker where mrkr_zdb_id = target_id and mrkr_type = 'TSCRIPT';
    if target_abbrev is null then
        raise exception 'Target transcript % not found', target_id;
    end if;

    raise notice 'Merging transcript % (%) into % (%)', source_id, source_abbrev, target_id, target_abbrev;

    -- Move db_link records (skip duplicates that already exist on target)
    delete from db_link
    where dblink_linked_recid = source_id
      and exists (
          select 1 from db_link d2
          where d2.dblink_linked_recid = target_id
            and d2.dblink_acc_num = db_link.dblink_acc_num
            and d2.dblink_fdbcont_zdb_id = db_link.dblink_fdbcont_zdb_id
      );
    update db_link set dblink_linked_recid = target_id
    where dblink_linked_recid = source_id;

    -- Move record attributions (skip duplicates)
    insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
    select target_id, recattrib_source_zdb_id
    from record_attribution
    where recattrib_data_zdb_id = source_id
      and recattrib_source_zdb_id not in (
          select recattrib_source_zdb_id from record_attribution where recattrib_data_zdb_id = target_id
      );
    delete from record_attribution where recattrib_data_zdb_id = source_id;

    -- Move aliases
    update data_alias set dalias_data_zdb_id = target_id
    where dalias_data_zdb_id = source_id;

    -- Move marker_relationships that don't already exist on the target
    -- (e.g., clone contains transcript)
    update marker_relationship
    set mrel_mrkr_2_zdb_id = target_id
    where mrel_mrkr_2_zdb_id = source_id
      and not exists (
          select 1 from marker_relationship mr2
          where mr2.mrel_mrkr_2_zdb_id = target_id
            and mr2.mrel_mrkr_1_zdb_id = marker_relationship.mrel_mrkr_1_zdb_id
            and mr2.mrel_type = marker_relationship.mrel_type
      );
    -- Delete remaining (duplicates of what target already has)
    delete from marker_relationship where mrel_mrkr_2_zdb_id = source_id;

    -- Move marker_relationships where source is mrkr_1 (unlikely for transcripts but be safe)
    update marker_relationship
    set mrel_mrkr_1_zdb_id = target_id
    where mrel_mrkr_1_zdb_id = source_id
      and not exists (
          select 1 from marker_relationship mr2
          where mr2.mrel_mrkr_1_zdb_id = target_id
            and mr2.mrel_mrkr_2_zdb_id = marker_relationship.mrel_mrkr_2_zdb_id
            and mr2.mrel_type = marker_relationship.mrel_type
      );
    delete from marker_relationship where mrel_mrkr_1_zdb_id = source_id;

    -- Move updates/history
    update updates set rec_id = target_id where rec_id = source_id;

    -- Add replacement record for URL redirection
    insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
    values (source_id, target_id);

    -- Delete source transcript record
    delete from transcript where tscript_mrkr_zdb_id = source_id;
    delete from marker where mrkr_zdb_id = source_id;
    delete from zdb_active_data where zactvd_zdb_id = source_id;

    raise notice 'Merge complete: % -> %', source_id, target_id;
end;
$$ language plpgsql;
