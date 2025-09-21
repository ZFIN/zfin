--liquibase formatted sql
--changeset cmpich:ZFIN-9898

ALTER TABLE gff3_ncbi
    ADD UNIQUE (gff_pk_id)
;

ALTER TABLE gff3_ncbi_attribute
    ADD UNIQUE (gna_pk_id);

ALTER TABLE gff3_ncbi_attribute
    ADD CONSTRAINT fk_gff3_ncbi_1
        FOREIGN KEY (gna_gff_pk_id)
            REFERENCES gff3_ncbi (gff_pk_id)
;

DELETE FROM marker_assembly a USING (
    SELECT MIN(ctid) as ctid, ma_a_pk_id, ma_mrkr_zdb_id
    FROM marker_assembly
    GROUP BY ma_a_pk_id, ma_mrkr_zdb_id HAVING COUNT(*) > 1
) b
WHERE a.ma_a_pk_id = b.ma_a_pk_id
  AND a.ma_mrkr_zdb_id = b.ma_mrkr_zdb_id
  AND a.ctid <> b.ctid
;

ALTER table marker_assembly
    ADD UNIQUE (ma_a_pk_id, ma_mrkr_zdb_id);



-- marker gene as GRCz11 if they do not have a z12 association but have a sequence_feature_chromosome_location_generated record for ZFIN with GRCz11
insert into marker_assembly
select distinct gg.sfclg_data_zdb_id, 3 from sequence_feature_chromosome_location_generated as gg
    where gg.sfclg_assembly = 'GRCz11'
      and gg.sfclg_location_source = 'ZfinGbrowseStartEndLoader'
      and gg.sfclg_acc_num like 'ENSDARG%'
and not exists (
    select * from zfindb.public.sequence_feature_chromosome_location_generated as ss
    where gg.sfclg_data_zdb_id = ss.sfclg_data_zdb_id
    and ss.sfclg_assembly = 'GRCz12tu'
        )
and not exists (
    select * from db_link
             where dblink_linked_recid = gg.sfclg_data_zdb_id
    and dblink_acc_num = gg.sfclg_acc_num
    and dblink_fdbcont_zdb_id ='ZDB-FDBCONT-040412-1'
        )
and not exists (
    select * from marker_assembly as g
    where gg.sfclg_data_zdb_id = g.ma_mrkr_zdb_id
    and g.ma_a_pk_id =3
        )
;




-- correct wrong initial assignment
-- one-time correction script
update sequence_feature_chromosome_location_generated set sfclg_assembly = 'GRCz12tu' where sfclg_assembly = 'GRCz12';
