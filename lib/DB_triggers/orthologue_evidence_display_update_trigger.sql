 create trigger orthologue_evidence_display_update_trigger 
 update of oevdisp_gene_zdb_id
  on orthologue_evidence_display
  referencing new as new_oevdisp
  for each row
	(execute procedure p_check_zdb_id_in_genedom
		(new_oevdisp.oevdisp_gene_zdb_id)) ;
