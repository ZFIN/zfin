--liquibase formatted sql
--changeset sierra:fishFullNameUpdate.sql


update fish
 set fish_full_name = get_fish_full_name(fish_zdb_id, fish_genotype_zdb_id, fish_name);

