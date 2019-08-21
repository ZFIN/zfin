begin work ;

create temp table tmp_eco_map (evcode text, ecoterm text);

\copy tmp_eco_map from '<!--|TARGETROOT|-->/server_apps/data_transfer/eco_go_mapping/gafeco.txt' delimiter ',';

insert into eco_go_mapping (egm_term_zdb_id, egm_go_evidence_code)
 select distinct term_zdb_id, evcode
  from term, tmp_eco_map
   where term_ont_id = ecoterm;

commit work;
