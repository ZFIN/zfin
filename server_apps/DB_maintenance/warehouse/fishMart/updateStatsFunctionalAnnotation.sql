!echo "start update stats for functional annotation";

update statistics high for table functional_annotation;
update statistics high for table feature_group;
update statistics high for table feature_group_member;
update statistics high for table affected_gene_group_member;
update statistics high for table affected_gene_group;
update statistics high for table str_group;
update statistics high for table phenotype_experiment;