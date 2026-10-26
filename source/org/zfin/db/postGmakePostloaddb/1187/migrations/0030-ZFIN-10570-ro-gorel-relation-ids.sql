--liquibase formatted sql

-- ZFIN-10570 / ZFIN-10569: give the two ZFIN-local annotation-extension relations
-- the IDs GO uses for them, so the GO Central GPAD load can match them by ID.
--
--   ROZ:0000002 results_in_determination_of -> RO:0002349    (RO "results in determination of")
--   ROZ:0000001 results_in_division_of      -> GOREL:0001019 (GO "results_in_division_of")
--
-- The legacy GOA GAF load wrote these relations by name, which matched the ROZ
-- placeholders. The GPAD load writes them by ID, which matched nothing, so 4
-- DANRE-mod lines were rejected ("RO term ... does not exist").
--
-- Renamed in place rather than through zfin-ro.obo: the ontology loader matches
-- terms by ID, so a new ID in the file would create a new term row, and
-- fixAnnotationsUponOntologyLoad.sql does not repoint annotation-extension
-- relations. Updating term_ont_id keeps each term's ZDB ID, so the 2 existing
-- annotations on ROZ:0000001 stay attached. zfin-ro.obo
-- (/research/zusers/informix/Curation/zfin-ro.obo) must carry the same two IDs
-- before the next load-zro run, or that load recreates the ROZ terms.
--
-- Idempotent: each update only runs while the ROZ ID is still present and the
-- new ID is not yet taken (term_ont_id is unique).

--changeset cmpich:ZFIN-10570-ro-gorel-relation-ids
--comment Rename ROZ:0000002 to RO:0002349 and ROZ:0000001 to GOREL:0001019 in zfin_ro

update term
   set term_ont_id = 'RO:0002349'
 where term_ont_id = 'ROZ:0000002'
   and term_ontology = 'zfin_ro'
   and not exists (select 1 from term t where t.term_ont_id = 'RO:0002349');

update term
   set term_ont_id = 'GOREL:0001019'
 where term_ont_id = 'ROZ:0000001'
   and term_ontology = 'zfin_ro'
   and not exists (select 1 from term t where t.term_ont_id = 'GOREL:0001019');
