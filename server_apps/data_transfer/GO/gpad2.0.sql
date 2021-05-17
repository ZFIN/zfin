-- NOTE: this code only distributes ZFIN-curation only (not including noctua) GO annotations.

begin work ;

create temp table gpad_format_without_grouping (mrkrgoev_zdb_id text,
                                objectId text, -- geneId
                                negation text, -- NOT
                                relation text, -- RO:id
                                ontology_class_id text, -- GO:id
                                reference text, -- this will be grouped eventually in the next temp table.
                                evidence_type text, -- ECO:id
                                with_from text, -- will be grouped eventually,
                                interacting_taxon text, -- NCBITaxon
                                date_entered timestamp without time zone, -- date entered
                                assigned_by text default 'GOC:zfin_curators',
                                annotation_extention text,
                                annotation_properties text);

insert into gpad_format_without_grouping(mrkrgoev_zdb_id,
                                          objectId,
                                          negation,
                                          relation,
                                          ontology_class_id,
                                          reference,
                                          evidence_type,
                                          interacting_taxon,
                                          date_entered,
                                          assigned_by,
                                          annotation_extention,
                                          annotation_properties)
select mrkrgoev_zdb_id,
          'ZFIN:'||mrkrgoev_mrkr_zdb_id as objectId,
          (case when mrkrgoev_gflag_name = 'not'
                  then 'NOT'
                  else ''
                  end) as negation,
          (case when mrkrgoev_gflag_name = 'contributes to'
                  then (select term_ont_id from term where term_zdb_id = 'ZDB-TERM-180228-3')
                -- colocalizes with
                when mrkrgoev_gflag_name = 'colocalizes with'
                  then (select term_ont_id from term where term_zdb_id = 'ZDB-TERM-180228-2')
                -- molecular function
                when (select count(*) from term where term_ontology='molecular_function' and mrkrgoev_term_zdb_id = term_zdb_id) > 0
                  then (select term_ont_id from term where term_zdb_id = 'ZDB-TERM-180228-4')
                -- biological process
                when (select term_ont_id from term where term_ontology='biological_process' and mrkrgoev_term_zdb_id = term_zdb_id) is not null
                  then (select term_ont_id from term where term_zdb_id = 'ZDB-TERM-181002-287')
                --protein containing complex
                when mrkrgoev_term_zdb_id = 'ZDB-TERM-091209-16423'
                   then (select term_ont_id from term where term_zdb_id = 'ZDB-TERM-180228-1')
                -- cellular component and not protein containing complex
                when (select term_ont_id
                          from term
                          where term_zdb_id != 'ZDB-TERM-091209-16423'
                            and term_ontology = 'cellular_component'
                            and term_zdb_id = mrkrgoev_term_zdb_id) is not null
                   then 'RO:0001025'
                else ''
                end) as relation,
          (select term_ont_id from term where term_zdb_id = mrkrgoev_term_zdb_id) as ontology_class_id,
          (case when (select accession_no from publication where zdb_id = mrkrgoev_source_zdb_id) is not null
                then (select 'ZFIN:'||zdb_id||'|'||'PMID:'||accession_no
                      from publication
                      where zdb_id = mrkrgoev_source_zdb_id)
                else 'ZFIN:'||mrkrgoev_source_zdb_id
                end) as reference,
          (case when mrkrgoev_evidence_code = 'ISS'
             then 'ECO:0000262'
            else (select term_ont_id
            from term, eco_go_mapping
             where term_zdb_id = egm_term_zdb_id
             and mrkrgoev_evidence_code = egm_go_evidence_code)
             end) as evidence_type,
          '' as interacting_taxon,
          mrkrgoev_date_entered,
          'ZFIN' as assigned_by,
          null,
          'contributor-id=GOC:zfin_curators'
  from marker_go_term_evidence
  where mrkrgoev_annotation_organization = 1;


update gpad_format_without_grouping
  set with_from = (select STRING_AGG(distinct infgrmem_inferred_from,',')
                        from inference_group_member
                      where mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id
                      group by mrkrgoev_zdb_id);

\copy (select objectId, negation, relation, ontology_class_id, reference, evidence_type, with_from, interacting_taxon, date_entered::date, assigned_by, annotation_extention, annotation_properties from gpad_format_without_grouping) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/gpad.zfin' with delimiter as '	' null as '';


