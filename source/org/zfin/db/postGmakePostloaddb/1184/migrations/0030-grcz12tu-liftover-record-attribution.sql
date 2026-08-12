--liquibase formatted sql
--changeset cmpich:0030-grcz12tu-liftover-record-attribution.sql splitStatements:false

-- Attribute the GRCz12tu sequence_feature_chromosome_location rows inserted by
-- 0020-grcz11-to-grcz12tu-liftover-welltested.sql.
--
-- 0020 minted the sfcl rows and registered them in zdb_active_data, but did not
-- write record_attribution. That matters for display: Refresh-GBrowse-Tracks_d
-- §G (server_apps/data_transfer/Ensembl/updateSequenceFeatureChromosomeLocation.sql)
-- re-imports sfcl into sequence_feature_chromosome_location_generated with
--     left outer join record_attribution on recattrib_data_zdb_id = sfcl_zdb_id
-- so an unattributed sfcl row propagates with sfclg_pub_zdb_id NULL and the
-- location renders on the feature page without a citation. Every other sfcl row
-- in the table is attributed (GRCz11 42464/42464, GRCz12tu 36811/36811 before
-- 0020), so the unattributed lifted rows are the anomaly.
--
-- A lifted GRCz12tu placement is the same assertion as its GRCz11 source
-- placement expressed in new coordinates, so it inherits that row's pub(s).
-- Where a feature has several GRCz11 sfcl rows citing different pubs, all
-- distinct pubs carry over -- the unique alternate key
-- (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
-- collapses duplicates.
--
-- Targeting: GRCz12tu rows with NO attribution at all. That is exactly the 0020
-- set; pre-existing GRCz12tu rows are already attributed and are left alone.
-- recattrib_pk_id comes from record_attribution_recattrib_pk_id_seq.
-- ON CONFLICT DO NOTHING + the NOT EXISTS guard make this safe to re-run.

insert into record_attribution
  (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select distinct z12.sfcl_zdb_id, r11.recattrib_source_zdb_id, 'standard'
  from sequence_feature_chromosome_location z12
  join sequence_feature_chromosome_location z11
       on z11.sfcl_feature_zdb_id = z12.sfcl_feature_zdb_id
      and z11.sfcl_assembly = 'GRCz11'
  join record_attribution r11
       on r11.recattrib_data_zdb_id = z11.sfcl_zdb_id
 where z12.sfcl_assembly = 'GRCz12tu'
   and not exists (select 1
                     from record_attribution existing
                    where existing.recattrib_data_zdb_id = z12.sfcl_zdb_id)
on conflict (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
   do nothing;
