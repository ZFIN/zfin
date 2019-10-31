--liquibase formatted sql
--changeset pm:DLOAD-638

delete from marker_go_term_evidence where mrkrgoev_annotation_organization=4;