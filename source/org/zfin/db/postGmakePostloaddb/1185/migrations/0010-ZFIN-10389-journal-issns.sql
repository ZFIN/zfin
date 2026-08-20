--liquibase formatted sql

-- ZFIN-10389: fill in missing ISSNs for eight journals. All eight had both ISSN
-- fields empty beforehand, so every statement below is a fill-in, never an overwrite.
--
-- journal holds exactly two ISSN slots, jrnl_print_issn and jrnl_online_issn, so the
-- ticket's three ISSN flavours map onto them like this:
--
--   * "Print ISSN"  -> jrnl_print_issn        (as labelled)
--   * "Online ISSN" -> jrnl_online_issn       (as labelled)
--   * "Linking ISSN" -> jrnl_print_issn       Science Bulletin's ISSN-L 2095-9273 is
--       also its print ISSN, and there is no linking-ISSN column to put it in.
--   * unqualified "ISSN" -> depends on the journal, since the ticket does not say and
--       the two columns are not interchangeable:
--         AIMS Molecular Science (AIMS Press), Inventions and Water (both MDPI) are
--         electronic-only, so their single ISSN is an e-ISSN -> jrnl_online_issn.
--         Journal of Microbes and Infections (Fudan University) is a print journal
--         -> jrnl_print_issn.
--
-- Those four unqualified values and the linking one are judgement calls; the six
-- explicitly labelled Print/Online values are not.

--changeset cmpich:0010-ZFIN-10389-journal-issns.sql

-- AIMS Molecular Science -- unqualified ISSN, electronic-only publisher
update journal set jrnl_online_issn = '2372-0301' where jrnl_zdb_id = 'ZDB-JRNL-210310-1';

-- Fish and Fisheries (Wiley) -- both labelled
update journal set jrnl_online_issn = '1467-2979',
                   jrnl_print_issn  = '1467-2960' where jrnl_zdb_id = 'ZDB-JRNL-180815-1';

-- Inventions (MDPI) -- unqualified ISSN, electronic-only journal
update journal set jrnl_online_issn = '2411-5134' where jrnl_zdb_id = 'ZDB-JRNL-181017-1';

-- Journal of Microbes and Infections (Fudan University) -- unqualified ISSN, print journal
update journal set jrnl_print_issn = '1673-6184' where jrnl_zdb_id = 'ZDB-JRNL-210617-1';

-- Process Biochemistry -- both labelled
update journal set jrnl_online_issn = '1873-3298',
                   jrnl_print_issn  = '1359-5113' where jrnl_zdb_id = 'ZDB-JRNL-181108-2';

-- Reproduction & Fertility (Bioscientifica) -- online only
update journal set jrnl_online_issn = '2633-8386' where jrnl_zdb_id = 'ZDB-JRNL-210706-1';

-- Science Bulletin -- online as labelled, linking ISSN stored as the print ISSN
update journal set jrnl_online_issn = '2095-9281',
                   jrnl_print_issn  = '2095-9273' where jrnl_zdb_id = 'ZDB-JRNL-190108-1';

-- Water (MDPI) -- unqualified ISSN, electronic-only journal
update journal set jrnl_online_issn = '2073-4441' where jrnl_zdb_id = 'ZDB-JRNL-220211-1';
