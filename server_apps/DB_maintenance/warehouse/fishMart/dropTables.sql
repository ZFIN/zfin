begin work ;


drop table functional_annotation;
drop table fish_annotation_search;
drop table morpholino_group;
drop table morpholino_group_member;

drop table feature_group;
drop table feature_group_member;
drop table genox_group;
drop table genox_group_member;
drop table environment_group;

drop table environment_group_member;

drop table affected_gene_group;
drop table affected_gene_group_member;

drop table genotype_group;
drop table genotype_group_member;

drop table term_group;
drop table term_group_member;

drop table phenotype_figure_group;
drop table phenotype_figure_group_member;
drop table construct_group;
drop table construct_group_member;
drop table gene_feature_result_view;
drop table figure_term_fish_search;

drop table xpat_figure_group;
drop table xpat_figure_group_member;

drop table feature_type_ordering;


commit work ;