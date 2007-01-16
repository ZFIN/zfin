begin work ;

select lotofile(obofile_text, "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/gene_ontology.obo!","server")
  from obo_file
  where obofile_name = 'gene_ontology.obo' ;

select lotofile(obofile_text, "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality.obo!","server")
  from obo_file
  where obofile_name = 'quality.obo' ;

select lotofile(obofile_text, "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/zebrafish_anatomy.obo!","server")
  from obo_file
  where obofile_name = 'zebrafish_anatomy.obo' ;

commit work ;