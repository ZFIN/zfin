create function get_phenotype_popup_link_html( phenosPkId integer)
  returning lvarchar;  

  define resultHtml lvarchar;

  define superterm lvarchar;
  define subterm lvarchar;
  define quality lvarchar;
  define relatedSuperterm lvarchar;
  define relatedSubterm lvarchar;
  define tag lvarchar;  

  select superterm.term_name,
         subterm.term_name,
         quality.term_name,
         related_superterm.term_name,
         related_subterm.term_name,
         phenos_tag
  into superterm,
       subterm,
       quality,
       relatedSuperterm,
       relatedSubterm,
       tag
  from phenotype_statement, 
       term superterm,
       outer term subterm,
       term quality, 
       outer term related_superterm,
       outer term related_subterm
  where phenos_pk_id = phenosPkId
    and superterm.term_zdb_id = phenos_entity_1_superterm_zdb_id
    and subterm.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and quality.term_zdb_id = phenos_quality_zdb_id
    and related_superterm.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and related_subterm.term_zdb_id = phenos_entity_2_subterm_zdb_id
  ;
 
  let resultHtml = '<a title="view phenotype popup" class="popup-link data-popup-link" href="/action/phenotype/phenotype-statement-popup?id=' || phenosPkId || '">';

--  let resultHtml = resultHtml || superterm;

--  if (subterm is not null) then
--    let resultHtml = resultHtml || '&nbsp;' || subterm;
--  end if

--  let resultHtml = resultHtml || '&nbsp;' || quality;

--  if (relatedSuperterm is not null) then 
--    let resultHtml = resultHtml|| '&nbsp;' || relatedSuperterm;
--  end if

--  if (relatedSubterm is not null) then
--    let resultHtml = resultHtml || '&nbsp;' || relatedSubterm;
--  end if

--  let resultHtml = resultHtml || ',&nbsp;' || tag;
  let resultHtml = resultHtml || '</a>';
   
  return resultHtml;

end function;