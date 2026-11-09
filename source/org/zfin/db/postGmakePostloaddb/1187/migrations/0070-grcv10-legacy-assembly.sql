--liquibase formatted sql
--changeset rtaylor:grcv10-legacy-assembly

-- "GRCv10" is not a genome release. It is the default that sequence_feature_chromosome_location_generated.sfclg_assembly
-- was given when the column was added, left on the Vega and NCBI start/end locations when the Ensembl and ZFIN GBrowse
-- ones were relabeled GRCz11. 62,173 location rows carry it, and it was the only assembly name in that table that the
-- assembly table did not list. Listing it as a legacy assembly (last in order, not current, no GCF identifier, like
-- "GRCz11 or older") lets those rows be joined to assembly and gives sfclg_assembly something to refer to.
insert into assembly (a_pk_id, a_name, a_gcf_identifier, a_order, a_is_current)
select (select coalesce(max(a_pk_id), 0) + 1 from assembly), 'GRCv10', null, 50, false
 where not exists (select 1 from assembly where a_name = 'GRCv10');

--rollback delete from assembly where a_name = 'GRCv10';
