select x.dbprimary_acc, gsi.stable_id 
 from gene_stable_id gsi, gene g, xref x 
 where gsi.gene_id = g.gene_id 
   and g.display_xref_id = x.xref_id 
   and x.external_db_id in (2510,2530) 
 group by x.dbprimary_acc having count(x.dbprimary_acc) = 1 
 order by gsi.stable_id
;

