create function get_phenotype_statement_text( phenosPkId integer)
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
 
  let resultHtml = '';

  let resultHtml = resultHtml || superterm;

  if (subterm is not null) then
    let resultHtml = resultHtml || ' ' || subterm;
  end if

  let resultHtml = resultHtml || ' ' || quality;

  if (relatedSuperterm is not null) then 
    let resultHtml = resultHtml|| ' ' || relatedSuperterm;
  end if

  if (relatedSubterm is not null) then
    let resultHtml = resultHtml || ' ' || relatedSubterm;
  end if

  return resultHtml;

end function;