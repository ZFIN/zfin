# this is a MySql query against the Ensembl database.
#use danio_rerio_core_19_2;

select distinct zdb.dbprimary_acc, gsi.stable_id
from  object_xref zo, xref zdb, gene_stable_id gsi, gene
where zdb.external_db_id = 2510
and  zdb.xref_id = zo.xref_id 
and  gsi.gene_id = gene.gene_id
and  gene.display_xref_id = zo.object_xref_id
order by 1;

