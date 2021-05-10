create or replace function p_disease_annotation_term_is_from_do (vTermZdbId text) 
returns void as $$

declare ontology term.term_ontology%TYPE;

begin
ontology = (select term_ontology from term where term_Zdb_id = vTermZdbId);

if (ontology != 'disease_ontology')
 then 
      raise exception 'FAIL!: disease annotation must used DO term' ;
                
 end if ;

end
$$ LANGUAGE plpgsql;
