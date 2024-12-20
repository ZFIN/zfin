--liquibase formatted sql
--changeset rtaylor:ZFIN-9408.sql

-- Fix these labs countries based on their addresses:
--       zdb_id       |                                                                    address
-- -------------------+------------------------------------------------------------------------------------------------------------------------------------------------
--  ZDB-LAB-231017-1  | 109 Carrigan Dr, 120A Marsh Life Science, Burlington VT 05405
--  ZDB-LAB-230830-1  | 10901 N. Torrey Pines Rd, LaJolla, CA 92037
--  ZDB-LAB-220112-2  | Victor Chang Cardiac Research Institute, 405 Liverpool St, Darlinghurst 2010
--  ZDB-LAB-210719-1  | Robert-Rössle-Straße 10\r                                                                                                                   +
--                    | 13125 Berlin
--  ZDB-LAB-220210-1  | University of Queensland, Institute for Molecular Bioscience\r                                                                                +
--                    | 306 Carmody Rd, St Lucia QLD 4072\r                                                                                                           +
--                    | Brisbane, QLD, Australia
--  ZDB-LAB-230308-1  | Comparative metabolic physiology lab, Brodie Building room G-34, Brandon University, 270 18th Street Brandon, MB, R7A 6A9
--  ZDB-LAB-220328-2  | Kenney/Sheetz Lab UTMB\r                                                                                                                      +
--                    | 14th and Strand, 6.160 MRB \r                                                                                                                 +
--                    | Galveston, TX 77555-0645
--  ZDB-LAB-220929-1  | The Affiliated Kangning Hospital of Wenzhou Medical University, No. 1 Shengjin Road, Lucheng District, Wenzhou City, Zhejiang Province, China.
--  ZDB-LAB-220420-1  | Boston College\r                                                                                                                              +
--                    | Biology Department\r                                                                                                                          +
--                    | 140 Commonwealth Ave\r                                                                                                                        +
--                    | Chestnut Hill MA 02467
--  ZDB-LAB-970730-83 | THE INGHAM LAB IN SHEFFIELD CLOSED IN 2013
--  ZDB-LAB-230628-1  | Biochemistry and Molecular Genetics Department, \r                                                                                            +
--                    | 170 Villarroel Street, 08036 Barcelona, Spain

update lab set country = 'US' where zdb_id = 'ZDB-LAB-231017-1';
update lab set country = 'US' where zdb_id = 'ZDB-LAB-230830-1';
update lab set country = 'AU' where zdb_id = 'ZDB-LAB-220112-2';
update lab set country = 'DE' where zdb_id = 'ZDB-LAB-210719-1';
update lab set country = 'AU' where zdb_id = 'ZDB-LAB-220210-1';
update lab set country = 'CA' where zdb_id = 'ZDB-LAB-230308-1';
update lab set country = 'US' where zdb_id = 'ZDB-LAB-220328-2';
update lab set country = 'CN' where zdb_id = 'ZDB-LAB-220929-1';
update lab set country = 'US' where zdb_id = 'ZDB-LAB-220420-1';
update lab set country = 'GB' where zdb_id = 'ZDB-LAB-970730-83';
update lab set country = 'ES' where zdb_id = 'ZDB-LAB-230628-1';

-- This gives a report of users who don't have a country set, but have labs with countries set.
-- It filters out those with multiple countries set for their labs to avoid ambiguity.

-- SELECT
--     person.zdb_id AS person_zdb_id,
--     person.full_name AS person_name,
--     string_agg(lab.zdb_id, '|') AS lab_zdb_ids,
--     string_agg(lab."name", '|') AS lab_names,
--     person_country,
--     string_agg(DISTINCT (lab.country), ',') AS lab_countries,
--     count(DISTINCT (lab.country)) num_countries
-- FROM
--     person
--     LEFT JOIN int_person_lab ON person.zdb_id = int_person_lab.source_id
--     LEFT JOIN lab ON target_id = lab.zdb_id
-- WHERE
--     person_country IS NULL
--     OR person_country = ''
-- GROUP BY
--     person.zdb_id,
--     person.full_name
-- HAVING
--     count(DISTINCT (lab.country)) = 1;

-- This should correct around 1118 records.
WITH person_country_fixes AS (
    SELECT
        person.zdb_id AS person_zdb_id,
        string_agg(DISTINCT (lab.country), ',') AS lab_countries
    FROM
        person
            LEFT JOIN int_person_lab ON person.zdb_id = int_person_lab.source_id
            LEFT JOIN lab ON target_id = lab.zdb_id
    WHERE
        person_country IS NULL
       OR person_country = ''
    GROUP BY
        person.zdb_id
    HAVING
        count(DISTINCT (lab.country)) = 1)
UPDATE
    person
SET
    person_country = person_country_fixes.lab_countries
FROM
    person_country_fixes
WHERE
    person.zdb_id = person_country_fixes.person_zdb_id;




