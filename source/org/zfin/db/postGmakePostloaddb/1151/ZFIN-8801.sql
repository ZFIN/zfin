--liquibase formatted sql
--changeset rtaylor:ZFIN-8801.sql

CREATE TABLE foreign_db_contains_validation_rule (
    fdbcvr_pk_id SERIAL PRIMARY KEY,
    fdbcvr_rule_name VARCHAR(255) NOT NULL,
    fdbcvr_rule_description TEXT,
    fdbcvr_rule_reference_url VARCHAR(255),
    fdbcvr_rule_pattern TEXT,
    fdbcvr_fdbcont_zdb_id VARCHAR(255) NOT NULL,
    CONSTRAINT fk_foreign_db_contains FOREIGN KEY (fdbcvr_fdbcont_zdb_id)
        REFERENCES foreign_db_contains (fdbcont_zdb_id) ON DELETE CASCADE
);
CREATE INDEX idx_foreign_db_contains ON foreign_db_contains_validation_rule (fdbcvr_fdbcont_zdb_id);

-- Fix one-off lower case genbank:
update db_link set dblink_acc_num = 'CAD60672', dblink_acc_num_display='CAD60672' where dblink_acc_num = 'cad60672';


-- Defining rules for the following foreign_db_contains records:
    -- fdbcont_organism_common_name	fdbcont_zdb_id	fdbcont_fdbdt_id	fdbcont_primary_blastdb_zdb_id	fdbcont_fdb_db_id	fdb_db_pk_id	fdb_db_name	fdb_db_query	fdb_url_suffix	fdb_db_display_name	fdb_db_significance
    -- Zebrafish	ZDB-FDBCONT-040412-37	3	ZDB-BLASTDB-090929-3	15	15	GenBank	http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Search&db=Nucleotide&doptcmdl=GenBank&term=		GenBank	2
    -- Zebrafish	ZDB-FDBCONT-040412-36	1	ZDB-BLASTDB-090929-5	15	15	GenBank	http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Search&db=Nucleotide&doptcmdl=GenBank&term=		GenBank	2
    -- Zebrafish	ZDB-FDBCONT-040412-42	2		16	16	GenPept	http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=protein&cmd=search&term=		GenPept	3
    -- Zebrafish	ZDB-FDBCONT-040412-47	2	ZDB-BLASTDB-071128-22	40	40	UniProtKB	http://www.uniprot.org/uniprot/		UniProtKB	0

-- GenBank accessions must begin with at least one letter and be followed by a series of only numbers. More specifically, the following rules apply
-- we could get even more specific, but this should work for now
-- (1 letter + 5 numerals) or
-- (2 letters + 6 numerals) or
-- (2 letters + 8 numerals) or
-- (3 letters + 5 numerals) or
-- (3 letters + 7 numerals) or
-- (4 letters + 8 or more numerals) or
-- (6 letters + 9 or more numerals) or
-- (5 letters + 7 numerals)
INSERT INTO foreign_db_contains_validation_rule (fdbcvr_rule_name, fdbcvr_rule_description, fdbcvr_rule_reference_url, fdbcvr_rule_pattern, fdbcvr_fdbcont_zdb_id)
VALUES ('GenBank',
        'GenBank accessions must begin with at least one letter and be followed by a series of only numbers.',
        'https://zfin.atlassian.net/wiki/spaces/doc/pages/5266079747/Validation+Rules+for+Foreign+DB+Accessions',
        '^' ||
        '[A-Z]{1}[0-9]{5}|' || -- 1 letter + 5 numerals
        '[A-Z]{2}[0-9]{6}|' || -- 2 letters + 6 numerals
        '[A-Z]{2}[0-9]{8}|' || -- 2 letters + 8 numerals
        '[A-Z]{3}[0-9]{5}|' || -- 3 letters + 5 numerals
        '[A-Z]{3}[0-9]{7}|' || -- 3 letters + 7 numerals
        '[A-Z]{4}[0-9]{8,}|' || -- 4 letters + 8 or more numerals
        '[A-Z]{6}[0-9]{9,}|' || -- 6 letters + 9 or more numerals
        '[A-Z]{5}[0-9]{7}' || -- 5 letters + 7 numerals
        '$',
        'ZDB-FDBCONT-040412-37');

-- For now, just duplicate the rule for the other GenBank accession (ZDB-FDBCONT-040412-36)
INSERT INTO foreign_db_contains_validation_rule (fdbcvr_rule_name, fdbcvr_rule_description, fdbcvr_rule_reference_url, fdbcvr_rule_pattern, fdbcvr_fdbcont_zdb_id)
VALUES ('GenBank',
        'GenBank accessions must begin with at least one letter and be followed by a series of only numbers.',
        'https://zfin.atlassian.net/wiki/spaces/doc/pages/5266079747/Validation+Rules+for+Foreign+DB+Accessions',
        '^' ||
        '[A-Z]{1}[0-9]{5}|' || -- 1 letter + 5 numerals
        '[A-Z]{2}[0-9]{6}|' || -- 2 letters + 6 numerals
        '[A-Z]{2}[0-9]{8}|' || -- 2 letters + 8 numerals
        '[A-Z]{3}[0-9]{5}|' || -- 3 letters + 5 numerals
        '[A-Z]{3}[0-9]{7}|' || -- 3 letters + 7 numerals
        '[A-Z]{4}[0-9]{8,}|' || -- 4 letters + 8 or more numerals
        '[A-Z]{6}[0-9]{9,}|' || -- 6 letters + 9 or more numerals
        '[A-Z]{5}[0-9]{7}' || -- 5 letters + 7 numerals
        '$',
        'ZDB-FDBCONT-040412-36');

-- UniProtKB accession numbers consist of 6 or 10 alphanumerical characters in the format below:
INSERT INTO foreign_db_contains_validation_rule (fdbcvr_rule_name, fdbcvr_rule_description, fdbcvr_rule_reference_url, fdbcvr_rule_pattern, fdbcvr_fdbcont_zdb_id)
VALUES ('UniProtKB',
        'UniProtKB accession numbers consist of 6 or 10 alphanumerical characters (details at uniprot.org/help/accession_numbers)',
        'https://zfin.atlassian.net/wiki/spaces/doc/pages/5266079747/Validation+Rules+for+Foreign+DB+Accessions',
        '^' ||
        '[OPQ][0-9][A-Z0-9]{3}[0-9]|' ||
        '[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}' ||
        '$',
        'ZDB-FDBCONT-040412-47');

-- And re-use it again for GenPept:
INSERT INTO foreign_db_contains_validation_rule (fdbcvr_rule_name, fdbcvr_rule_description, fdbcvr_rule_reference_url, fdbcvr_rule_pattern, fdbcvr_fdbcont_zdb_id)
VALUES ('GenPept',
        'GenPept accessions must begin with at least one letter and be followed by a series of only numbers.',
        'https://zfin.atlassian.net/wiki/spaces/doc/pages/5266079747/Validation+Rules+for+Foreign+DB+Accessions',
        '^' ||

        -- GenPept accessions accept the same rules as GenBank accessions
        '(?:' || -- non-capturing group
        '[A-Z]{1}[0-9]{5}|' || -- 1 letter + 5 numerals
        '[A-Z]{2}[0-9]{6}|' || -- 2 letters + 6 numerals
        '[A-Z]{2}[0-9]{8}|' || -- 2 letters + 8 numerals
        '[A-Z]{3}[0-9]{5}|' || -- 3 letters + 5 numerals
        '[A-Z]{3}[0-9]{7}|' || -- 3 letters + 7 numerals
        '[A-Z]{4}[0-9]{8,}|' || -- 4 letters + 8 or more numerals
        '[A-Z]{6}[0-9]{9,}|' || -- 6 letters + 9 or more numerals
        '[A-Z]{5}[0-9]{7}' || -- 5 letters + 7 numerals
        ')' ||

        -- GenPept accessions also accept the uniprot format:
        '|' ||
        '(?:' || -- non-capturing group
        '[OPQ][0-9][A-Z0-9]{3}[0-9]|' ||
        '[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}' ||
        ')' ||
        '$',
        'ZDB-FDBCONT-040412-42');


-- For reference, this query will find rule violations:
-- select * from (
--
-- select db_link.*,
-- regexp_match(dblink_acc_num, fdbcvr_rule_pattern) as match,
-- foreign_db_contains_validation_rule.fdbcvr_rule_name as ruleid,
-- foreign_db_contains_validation_rule.fdbcvr_rule_pattern as pattern
-- from db_link left join foreign_db_contains_validation_rule on dblink_fdbcont_zdb_id = foreign_db_contains_validation_rule.fdbcvr_fdbcont_zdb_id
--
-- ) as subq
-- where ruleid is not null and match is null
