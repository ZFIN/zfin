--liquibase formatted sql
--changeset sierra:feature_aud.sql

create table feature_audit (id int,
                         REV int,
                         REVTYPE int,
                         feature_zdb_id text,
                         feature_name text,
                         feature_abbrev text,
                         feature_type text,
                         feature_name_order text,
                         feature_date_entered timestamp without time zone,
                         feature_lap_prefix_id bigint,
                         feature_line_number text,
                         feature_df_transloc_complex_prefix text,
                         feature_dominant boolean,
                         feature_unspecified boolean,
                         feature_unrecovered boolean,
                         feature_known_insertion_site boolean,
                         feature_tg_suffix text)
;

