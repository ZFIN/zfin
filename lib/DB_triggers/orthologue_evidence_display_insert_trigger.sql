create trigger orthologue_evidence_display_insert_trigger insert 
  on orthologue_evidence_display
  referencing new as new_oevdisp
  for each row 
 	(execute procedure p_check_zdb_id_in_genedom
		(new_oevdisp.oevdisp_gene_zdb_id)) ;
