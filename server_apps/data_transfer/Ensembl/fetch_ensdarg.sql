# this is a MySql query against the Ensembl database.
#use danio_rerio_core_19_2;

select distinct zdb.dbprimary_acc, gsi.stable_id
from   object_xref lo,
      object_xref zo, 
      xref zdb,
      xref ll,
      gene_stable_id gsi,
      gene,
      transcript trans
where zdb.external_db_id = 2510
and   ll.external_db_id = 1300
and   ll.xref_id  = lo.xref_id 
and   zdb.xref_id = zo.xref_id
and   lo.ensembl_id = zo.ensembl_id 
and   gsi.gene_id = gene.gene_id
and  trans.display_xref_id = gene.display_xref_id 
and  zo.ensembl_id = trans.translation_id
order by 1;


