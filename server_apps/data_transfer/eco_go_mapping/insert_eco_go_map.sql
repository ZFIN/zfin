begin work ;

create temp table tmp_eco_map (evcode text, ecoterm text);

\copy tmp_eco_map from '<!--|TARGETROOT|-->/server_apps/data_transfer/eco_go_mapping/gafeco.txt' delimiter ',';

-- do nothing on conflict so the load is re-runnable: every mapping already present would
-- otherwise collide with egm_alternate_key_index and abort the whole transaction. Only new
-- rows are added -- the curated mappings that aren't in ECO's file (postGmakePostloaddb
-- DLOAD-672, ZFIN-9426) have to survive, so this deliberately does not delete first.
insert into eco_go_mapping (egm_term_zdb_id, egm_go_evidence_code)
 select distinct term_zdb_id, evcode
  from term, tmp_eco_map
   where term_ont_id = ecoterm
on conflict (egm_term_zdb_id, egm_go_evidence_code) do nothing;

commit work;
