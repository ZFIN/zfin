begin work ;
!echo "PHENO FIGURE";

update statistics high for table phenotype_figure_group;
update statistics high for table functional_annotation;

set pdqpriority 80;

insert into phenotype_figure_group (pfigg_genox_zdb_id)
  select genox_zdb_id from genotype_Experiment
   where exists (Select 'x' from phenotype_Experiment,phenotype_statement 
   	 		where phenos_phenox_pk_id = phenox_pk_id
			and phenos_tag != 'normal'
			and phenox_genox_zdb_id = genox_zdb_id);

update phenotype_figure_group
  set pfigg_group_name = replace(replace(replace(substr(multiset (select distinct item phenox_Fig_Zdb_id from phenotype_Experiment
     		      								 where phenox_genox_zdb_id = pfigg_Genox_zdb_id
										  )::lvarchar(380),11),""),"'}",""),"'","");

insert into phenotype_figure_group_member (pfiggm_group_id, pfiggm_member_name, pfiggm_member_id)
  select pfigg_group_pk_id, fig_label, phenox_fig_zdb_id
    from phenotype_figure_group, phenotype_Experiment, figure
    where phenox_genox_zdb_id = pfigg_genox_zdb_id
    and fig_zdb_id = phenox_fig_zdb_id;

update functional_annotation
  set fa_pheno_figure_group = (select pfigg_group_name 
      			      	      from phenotype_figure_group 
				      where fa_genox_zdb_id = pfigg_genox_zdb_id);


--rollback work ;

commit work ;