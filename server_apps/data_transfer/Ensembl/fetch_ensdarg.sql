begin work;
create temporary table tmp_unizdb
select xref.dbprimary_acc tuz_zdb
 from  gene_stable_id gsi, gene, xref
 where gsi.gene_id = gene.gene_id
 and gene.display_xref_id = xref.xref_id
 and xref.external_db_id in (2510,2530)
 group by 1 having count(*) = 1
;

select distinct xref.dbprimary_acc, gsi.stable_id
 from  gene_stable_id gsi, gene, xref, tmp_unizdb
where gsi.gene_id = gene.gene_id
  and gene.display_xref_id = xref.xref_id
  and xref.external_db_id in (2510,2530)
  and xref.dbprimary_acc  = tuz_zdb
order by 2
;
