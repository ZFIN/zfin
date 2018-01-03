SELECT szm_term_ont_id,
       szm_object_type,
       szm_term_name,
       term_name
FROM   so_zfin_mapping,
       term
WHERE  term_ont_id = szm_term_ont_id
       AND term_name != szm_term_name;