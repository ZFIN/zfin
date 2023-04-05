--liquibase formatted sql
--changeset rtaylor:ZFIN-8519.sql

INSERT INTO curation_topic ("curtopic_name") VALUES ('Disease Xenograft'),
                                                    ('Tox. Drug Characterization/Treatment'),
                                                    ('Tox. Environmental Contamination'),
                                                    ('Tox. Nanomaterials'),
                                                    ('Tox. Natural Product Characterization');
