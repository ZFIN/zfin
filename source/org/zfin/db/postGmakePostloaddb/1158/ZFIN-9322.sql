--liquibase formatted sql
--changeset cmpich:ZFIN-9322.sql
-- copy the attached file with the ensembl IDs into the local folder

create table temp_ensdarg (
    id text
);

\copy temp_ensdarg FROM 'obsoleted-ensembl-ensdarg-ids.txt';

-- db_link records that will leave the associated gene without an ensdarg
select distinct dl1.dblink_linked_recid from db_link as dl1, temp_ensdarg where not exists (
    select * from db_link as dl2 where dl2.dblink_linked_recid = dl1.dblink_linked_recid
    and dl2.dblink_acc_num != dl1.dblink_acc_num
    and dl2.dblink_fdbcont_zdb_id = dl1.dblink_fdbcont_zdb_id
    )
AND dl1.dblink_acc_num = id;

select count(distinct dl1.dblink_linked_recid) from db_link as dl1, temp_ensdarg where not exists (
        select * from db_link as dl2 where dl2.dblink_linked_recid = dl1.dblink_linked_recid
                                       and dl2.dblink_acc_num != dl1.dblink_acc_num
                                       and dl2.dblink_fdbcont_zdb_id = dl1.dblink_fdbcont_zdb_id
    )
                                                                            AND dl1.dblink_acc_num = id;

-- db_link records that will leave the associated gene with at least one more non-obsoleted ensdarg
select distinct dl1.dblink_linked_recid from db_link as dl1, temp_ensdarg where exists (
    select * from db_link as dl2 where dl2.dblink_linked_recid = dl1.dblink_linked_recid
    and dl2.dblink_acc_num != dl1.dblink_acc_num
    and dl2.dblink_fdbcont_zdb_id = dl1.dblink_fdbcont_zdb_id
    and dl1.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
    )
AND dl1.dblink_acc_num = id;

select * from temp_ensdarg where id like '%.%';