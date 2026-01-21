SELECT mrkrgoev_zdb_id,
       mrkr_abbrev,
       term_name,
       mrkrgoev_source_zdb_id,
       mrkrgoev_annotation_organization_created_by,
       mrkrgoev_evidence_code,
       case term_is_obsolete when true then 'TRUE' else 'FALSE' end as term_is_obsolete,
       case term_is_secondary when true then 'TRUE' else 'FALSE' end as term_is_secondary,
       case
          when mrkrgoev_evidence_code in ('IMP', 'IEP', 'IGI', 'IPI', 'IDA') then 'TRUE'
          else 'FALSE'
        end as evidence_is_experimental
FROM marker_go_term_evidence,
     term,
     marker
WHERE mrkrgoev_term_zdb_id = term_zdb_id
  AND (term_is_obsolete = true or term_is_secondary = true)
  AND mrkrgoev_annotation_organization_created_by = 'ZFIN'
  AND mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
order by evidence_is_experimental desc, mrkr_abbrev, term_name, mrkrgoev_zdb_id;
