--liquibase formatted sql
--changeset pm:ZFIN-6437

alter table feature_community_contribution
add  fcc_nmd_apparent text;