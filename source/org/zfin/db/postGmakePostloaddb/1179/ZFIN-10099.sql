--liquibase formatted sql
--changeset cmpich:ZFIN-10099.sql

-- ZDB-JRNL-110524-2: Frontiers in Neuroanatomy - Fix ISSNs and add NLM ID
UPDATE journal
SET jrnl_print_issn = '1662-5129',
    jrnl_online_issn = '1662-5129',
    jrnl_nlmid = '101477943'
WHERE jrnl_zdb_id = 'ZDB-JRNL-110524-2';

-- ZDB-JRNL-110524-1: Biomedical Optics Express - Add ISSN and NLM ID
UPDATE journal
SET jrnl_print_issn = '2156-7085',
    jrnl_nlmid = '101540630'
WHERE jrnl_zdb_id = 'ZDB-JRNL-110524-1';