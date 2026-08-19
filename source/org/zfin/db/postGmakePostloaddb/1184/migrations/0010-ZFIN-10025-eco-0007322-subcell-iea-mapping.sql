--liquibase formatted sql
--changeset rtaylor:ZFIN-10025-eco-0007322-subcell-iea-mapping

-- ZFIN-10025: map ECO:0007322 -> IEA in eco_go_mapping.
--
-- The unified DANRE-mod GPAD load translates each row's ECO code to a ZFIN
-- 3-letter GO evidence code via eco_go_mapping (GpadParser.postProcessing); an
-- unmapped ECO makes it reject the row. ECO:0007322 ("curator inference used in
-- automatic assertion") is a granular automatic-assertion code that is NOT in
-- GO's flat gaf-eco-mapping.txt, so it was never loaded into our table -- yet it
-- is the code the file uses for UniProtKB-SubCell IEAs (~17,350 rows).
--
-- IEA is the correct target: it is the automatic-assertion sibling of
-- ECO:0000501 and ECO:0000256 (both already mapped to IEA), and it matches how
-- these SubCell annotations are already stored in ZFIN (pub ZDB-PUB-120306-4 /
-- GO_REF:0000044). Adding this row lets those rows load (clearing ~17,350 load
-- errors) and match their stored IEA counterparts (preventing ~24,283 spurious
-- removals when the load's per-source removal runs).
--
-- Idempotent via the (egm_term_zdb_id, egm_go_evidence_code) unique index.

insert into eco_go_mapping (egm_term_zdb_id, egm_go_evidence_code)
  select term_zdb_id, 'IEA'
    from term
   where term_ont_id = 'ECO:0007322'
     and not exists (
       select 1 from eco_go_mapping m
        where m.egm_term_zdb_id = term.term_zdb_id
          and m.egm_go_evidence_code = 'IEA'
     );
