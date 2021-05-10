--liquibase formatted sql
--changeset xshao:ZFIN-5903

update annual_stats
  set as_type = "All Publications"
  where as_type = "Publications";

update annual_stats
  set as_type = "Images"
  where as_type = "Images (phenotype and expression patterns)";

update annual_stats
  set as_type = "Genes with expression data"
  where as_type = "Genes wiht expression data";

update annual_stats 
 set as_type = "Genes with Non-IEA GO Annotation"
 where as_type like "Genes with Non-IEA GO%";

update annual_stats
  set as_type = "Total GO Annotations"
  where as_type = "Total GO annotations";

update annual_stats
  set as_section = "Functional Annotation" 
  where as_type in ("Genes with Non-IEA GO Annotation", 
                    "Total GO Annotations",
                    "Genes with IEA GO annotations",
                    "Genes with GO annotations",
                    "Genes with OMIM phenotypes");

