--liquibase formatted sql
--changeset cmpich:DLOAD-547.sql

INSERT INTO go_evidence_code (goev_code, goev_name, goev_display_order)
  VALUES ('ISO', 'Inferred from Sequence Orthology', 17);