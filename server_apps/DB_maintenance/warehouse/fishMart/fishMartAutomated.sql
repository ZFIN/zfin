begin work ;

set PDQPRIORITY 20;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t',current year to second)
 where zflag_name = "regen_fishmart";


drop table functional_annotation;
--drop table fish_annotation_search_temp;
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
--drop table gene_feature_result_view_temp;
--drop table figure_term_fish_search_temp;

drop table xpat_figure_group;
drop table xpat_figure_group_member;




--delete from genotype_Experiment
-- where genox_zdb_id not in (select xpatex_genox_zdb_id from expression_experiment)
-- and genox_zdb_id not in (select phenox_genox_zdb_id from phenotype_Experiment);

create table functional_annotation(
       fa_pk_id serial8 not null constraint fa_pk_id_not_null,
       fa_all lvarchar(4000),
       fa_feature_order lvarchar(1000),
       fa_affector_Type_group lvarchar(1000),
       fa_gene_order lvarchar(1000),
       fa_geno_zdb_id varchar(50),
       fa_feature_alias lvarchar(380),
       fa_gene_alias lvarchar(1500),
       fa_gene_alt_alias lvarchar(1000),
       fa_geno_alias lvarchar(1000),
       fa_morph_alias lvarchar(1000),
       fa_construct_alias lvarchar(3000),
       fa_geno_name varchar(255),
       fa_geno_handle varchar(255) not null constraint fa_geno_handle_not_null,
       fa_genox_zdb_id varchar(50),
       fa_feature_group lvarchar(1000),
       fa_gene_group lvarchar(1500),
       fa_construct_group lvarchar(500),
       fa_morpholino_group lvarchar(380),
       fa_feature_group_id int8,
       fa_environment_group lvarchar(380),
       fa_pheno_term_group lvarchar(4000),
       fa_pheno_figure_group lvarchar(3000),
       fa_xpat_figure_group lvarchar(3000),
       fa_environment_group_is_standard_or_control boolean default 'f',
       fa_geno_is_wildtype boolean,
       fa_fish_significance int,
       fa_feature_significance int,
       fa_morph_significance int,
       fa_morph_member_count int,
       fa_gene_count int,
       fa_fish_parts_count int,
       fa_feature_count int,
       fa_pheno_term_count int,
       fa_pheno_figure_count int,
       fa_total_figure_count int
)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 16384 next size 16384;



create unique index fa_pk_id_index on functional_annotation(fa_pk_id)
  using btree in idxdbs2;

create index fa_geno_handle_index on functional_annotation(fa_geno_handle) using btree in idxdbs2;

create index fa_geno_index on functional_annotation(fa_geno_zdb_id)
 using btree in idxdbs3;

--create index fa_feature_group_index on functional_annotation(fa_feature_group)
-- using btree in idxdbs2;

create index fa_morpholino_group_index on functional_annotation(fa_morpholino_group)
 using btree in idxdbs1;


create table environment_group (eg_group_name lvarchar(380), 
       	     		        eg_genox_Zdb_id varchar(50) not null constraint eg_genox_zdb_id_not_null, 
				eg_group_pk_id serial8 not null constraint eg_group_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index eg_group_primary_key_index on environment_group (eg_group_pk_id)
 using btree in idxdbs2; 

alter table environment_group 
  add constraint primary key (eg_group_pk_id)
  constraint environment_Group_primary_key;

create index eg_group_name_index on environment_group (eg_group_name)
  using btree in idxdbs3;

create index eg_genox_zdb_index on environment_group (eg_genox_zdb_id)
  using btree in idxdbs3;

create table environment_group_member (egm_pk_id serial8 not null constraint egm_pk_id_not_null, 
       	     			      		 egm_group_id int8 not null constraint egm_group_id_not_null, 
						 egm_member_name varchar(50), 
						 egm_member_id varchar(50) not null constraint egm_member_id_not_null, 
       	     			      		  egm_genotype_id varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create index environment_group_member_id_index on environment_group_member (egm_member_id)
  using btree in idxdbs2;


create unique index environment_group_member_primary_key_index on environment_group_member (egm_pk_id)
  using btree in idxdbs2;

alter table environment_Group_member
  add constraint primary key (egm_pk_id) constraint environment_group_member_primary_key;


create table genox_group (gg_group_name lvarchar(380),		
       	     		 		gg_genotype_zdb_id varchar(50), 
					gg_group_pk_id serial8 not null constraint gg_group_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create index gg_group_name_index on genox_group (gg_group_name)
  using btree in idxdbs1;

create unique index gg_group_primary_key_index 
  on genox_group (gg_group_pk_id)
  using btree in idxdbs3;

create table genox_group_member (ggm_group_id int8 not null constraint ggm_group_id_not_null,  
       	     				      	     ggm_member_id varchar(50) not null constraint ggm_member_id_not_null, 
       	     			      		    ggm_genotype_id varchar(50),
						    ggm_pk_id serial8 not null constraint ggm_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index ggm_group_primary_key_index 
  on genox_group_member (ggm_pk_id)
  using btree in idxdbs3;

create table construct_group (cg_group_name lvarchar(500), 
       	     		     		    cg_geno_zdb_id varchar(50),
					    cg_genox_Zdb_id varchar(50), 
					    cg_group_pk_id serial8 not null constraint cg_group_pk_id_not_null, 
					    cg_group_order lvarchar(2000),
					    cg_geno_name varchar(255))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

--create index afg_group_name_index on affected_gene_group (afg_group_name)
--  using btree in idxdbs1;

create unique index construct_group_primary_key_index
   on construct_group (cg_group_pk_id)
  using btree in idxdbs1;

alter table construct_group
  add constraint primary key (cg_group_pk_id)
  constraint construct_group_primary_key;

create index cg_genox_zdb_index on construct_group (cg_genox_zdb_id)
  using btree in idxdbs2;

create index cg_geno_name_index on construct_group (cg_geno_name)
  using btree in idxdbs2;

create index cg_geno_zdb_index on construct_group (cg_geno_zdb_id)
  using btree in idxdbs2;

create table affected_gene_group (afg_group_name lvarchar(1500), 
       	     			 		 afg_geno_zdb_id varchar(50),
						 afg_genox_Zdb_id varchar(50), 
						 afg_group_pk_id serial8 not null constraint afg_group_pk_id_not_null, 
						 afg_group_order lvarchar(3000), 
						 afg_geno_name varchar(255))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;


create unique index affected_gene_group_primary_key_index
   on affected_Gene_group (afg_group_pk_id)
  using btree in idxdbs1;

alter table affected_gene_group
  add constraint primary key (afg_group_pk_id)
  constraint affected_gene_group_primary_key;

create index afg_genox_zdb_index on affected_gene_group (afg_genox_zdb_id)
  using btree in idxdbs2;

create index afg_geno_name_index on affected_gene_group (afg_geno_name)
  using btree in idxdbs2;


create index afg_geno_zdb_index on affected_gene_group (afg_geno_zdb_id)
  using btree in idxdbs2;

create table affected_gene_group_member (afgm_group_id int8 not null constraint afgm_group_id_not_null, 
       	     					       afgm_member_name varchar(50), 
						       afgm_member_id varchar(50) not null constraint afgm_member_id_not_null, 
						       afgm_pk_id serial8 not null constraint afgm_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index affected_gene_group_member_primary_key_index on affected_gene_group_member (afgm_pk_id)
  using btree in idxdbs2;

create index affected_gene_group_member_id_index on affected_Gene_group_member (afgm_member_id)
  using btree in idxdbs2;

create index affected_gene_group_member_group_id_index on affected_Gene_group_member (afgm_group_id)
  using btree in idxdbs2;



alter table affected_gene_Group_member
  add constraint primary key (afgm_pk_id) constraint affected_gene_group_member_primary_key;


create table construct_group_member (cgm_group_id int8, cgm_member_name varchar(50), cgm_member_id varchar(50), cgm_pk_id serial8,cgm_abbrev_order varchar(80))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index construct_group_member_primary_key_index on construct_group_member (cgm_pk_id)
  using btree in idxdbs2;

create index construct_group_member_id_index on construct_group_member (cgm_member_id)
  using btree in idxdbs2;


create index construct_group_member_group_id_index on construct_group_member (cgm_group_id)
  using btree in idxdbs2;

alter table construct_Group_member
  add constraint primary key (cgm_pk_id) constraint construct_group_member_primary_key;

create table feature_group (fg_group_name lvarchar(1000), 
       	     		   		  fg_group_order lvarchar(2000),
					  fg_genox_Zdb_id varchar(50), 
					  fg_geno_zdb_id varchar(50), 
					  fg_group_pk_id serial8 not null constraint fg_group_pk_id_not_null, 
					  fg_type_group varchar(255), 
					  fg_geno_name varchar(255))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create index fg_genox_zdb_index on feature_group (fg_genox_zdb_id)
  using btree in idxdbs1;

create index fg_geno_zdb_index on feature_group (fg_geno_zdb_id)
  using btree in idxdbs1;

create index fg_geno_name_index on feature_group (fg_geno_name)
  using btree in idxdbs1;

create unique index fg_group_id_index on feature_group (fg_group_pk_id)
  using btree in idxdbs3;

alter table feature_group
  add constraint primary key (fg_group_pk_id)
  constraint feature_group_primary_key;

create table morpholino_group (morphg_group_pk_id serial8 not null constraint morphg_group_pk_id_not_null, 
       	     		      			  morphg_group_name lvarchar(380), 
						  morphg_genox_Zdb_id varchar(50),
						  morphg_group_order lvarchar(500), 
						  morphg_geno_name varchar(255))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index morphg_group_id_index on morpholino_group (morphg_group_pk_id)
  using btree in idxdbs2;

create index morphg_geno_name_index on morpholino_group (morphg_geno_name)
  using btree in idxdbs2;

alter table morpholino_group
  add constraint primary key (morphg_group_pk_id)
  constraint morpholino_group_primary_key;

create table feature_group_member (fgm_group_id int8 not null constraint fgm_group_id_not_null, 
       	     			  		fgm_member_name varchar(50), 
						fgm_member_id varchar(50), 
       	     			  		fgm_genotype_id varchar(50), 
						fgm_significance int, 
						fgm_feature_type varchar(30), 
						fgm_pk_id serial8 not null constraint fgm_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create index feature_group_member_id_index on feature_group_member (fgm_member_id)
  using btree in idxdbs2;

create index feature_group_member_genotype_id_index on feature_group_member (fgm_genotype_id)
  using btree in idxdbs2;


create index feature_group_member_group_id_index on feature_group_member (fgm_group_id)
  using btree in idxdbs2;

create unique index feature_group_member_primary_key_index on feature_group_member (fgm_pk_id)
  using btree in idxdbs2;



alter table feature_group_member
  add constraint primary key (fgm_pk_id) constraint feature_group_member_primary_key;


create table morpholino_group_member (morphgm_pk_id serial8 not null constraint morphgm_pk_id_not_null, 
       	     			     		    morphgm_group_id int8 not null constraint morphgm_group_id_not_null, 
						    morphgm_member_name varchar(50), 
						    morphgm_member_id varchar(50), 
						    morphgm_significance int)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index morpholino_group_member_primary_key_index on morpholino_group_member (morphgm_pk_id)
  using btree in idxdbs2;

create index morpholino_group_member_id_index on morpholino_group_member (morphgm_member_id)
  using btree in idxdbs2;

create index morpholino_group_member_group_id_index on morpholino_group_member (morphgm_group_id)
  using btree in idxdbs2;

alter table morpholino_group_member
  add constraint primary key (morphgm_pk_id) constraint morpholino_group_member_primary_key;


create index morphg_group_name_index on morpholino_group (morphg_group_name)
  using btree in idxdbs2;

create index morphg_genox_zdb_index on morpholino_group (morphg_genox_zdb_id)
  using btree in idxdbs3;

create table genotype_group_member (ggm_pk_id serial8 not null constraint genogm_pk_id_not_null, 
       	     			   	      ggm_group_id int8 not null constraint genogm_group_id_not_null, 
					      ggm_member_name varchar(50), 
					      ggm_member_id varchar(50) not null constraint genogm_member_id_not_null, 
					      ggm_significance int)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index genotype_group_member_primary_key_index on genotype_group_member (ggm_pk_id)
  using btree in idxdbs2;

create index genotype_group_member_id_index on genotype_group_member (ggm_member_id)
  using btree in idxdbs2;

create index genotype_group_member_group_id_index on genotype_group_member (ggm_group_id)
  using btree in idxdbs2;

alter table genotype_group_member
  add constraint primary key (ggm_pk_id) constraint genotype_group_member_primary_key;


create table genotype_group (gg_group_name lvarchar(500), 
       	     		    		   gg_geno_name varchar(255), 
					   gg_geno_handle varchar(255), 
					   gg_group_pk_id serial8 not null constraint genog_Group_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;


create unique index genotype_group_primary_key_index on genotype_group (gg_group_pk_id)
  using btree in idxdbs3;

create  index genotype_handle_index on genotype_group (gg_geno_handle)
  using btree in idxdbs3;

alter table genotype_group
  add constraint primary key (gg_group_pk_id)
  constraint genotype_Group_primary_key;


create table term_group (tg_group_name lvarchar(5000), 
       	     			       tg_genox_group varchar(50), 
				       tg_group_pk_id serial8 not null constraint tg_group_pk_id_not_null, 
				       tg_geno_name varchar(255),
				       tg_geno_handle varchar(255))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index tg_genox_zdb_index on term_group (tg_genox_group)
  using btree in idxdbs3;

create unique index term_group_primary_key_index on term_group (tg_group_pk_id)
  using btree in idxdbs3;

create index term_geno_name_index on term_group (tg_geno_name)
  using btree in idxdbs3;

create index term_geno_handle_index on term_group (tg_geno_handle)
  using btree in idxdbs3;


alter table term_group
  add constraint primary key (tg_group_pk_id)
  constraint term_Group_primary_key;

create table term_group_member (tgm_pk_id serial8 not null constraint tgm_pk_id_not_null, 
       	     		       		  tgm_group_id int8 not null constraint tgm_group_id_not_null, 
					  tgm_member_name varchar(50), 
					  tgm_member_id varchar(50) not null constraint tgm_member_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index term_group_member_primary_key_index on term_group_member (tgm_pk_id)
  using btree in idxdbs2;

create index term_group_member_id_index on term_group_member (tgm_member_id)
  using btree in idxdbs2;

alter table term_group_member
  add constraint primary key (tgm_pk_id) constraint term_group_member_primary_key;

create table phenotype_figure_group (pfigg_group_name lvarchar(380), 
       	     			    		      pfigg_genox_Zdb_id varchar(50), 
						      pfigg_group_pk_id serial8 not null constraint pfigg_group_pk_id_not_null, 
						      pfigg_geno_name varchar(255),
						      pfigg_geno_handle varchar(255))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index pfigg_group_primary_key_index on phenotype_figure_group (pfigg_group_pk_id)
  using btree in idxdbs2;

create index term_group_member_group_id_index on term_group_member (tgm_group_id)
  using btree in idxdbs2;


create index pfigg_geno_handle_index on phenotype_figure_group (pfigg_geno_handle)
  using btree in idxdbs2;


create index pfigg_geno_name_index on phenotype_figure_group (pfigg_geno_name)
  using btree in idxdbs2;

alter table phenotype_Figure_Group
  add constraint primary key (pfigg_group_pk_id) constraint pfigg_group_primary_key;

create index pfigg_genox_zdb_index on phenotype_Figure_group (pfigg_genox_zdb_id)
  using btree in idxdbs3;

create table phenotype_figure_group_member (pfiggm_group_id int8 not null constraint pfiggm_group_id_not_null, 
       	     				   		    pfiggm_member_name varchar(50), 
							    pfiggm_member_id varchar(50) not null constraint pfiggm_member_id_not_null, 
							    pfiggm_pk_id serial8 not null constraint pfiggm_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;


create index pfig_group_id_index on
    phenotype_figure_group_member (pfiggm_group_id) using btree 
    ;

create index pfigg_group_name_index on phenotype_Figure_group (pfigg_group_name)
  using btree in idxdbs3;

create unique index pfigg_group_member_primary_key_index on phenotype_Figure_group_member (pfiggm_pk_id)
  using btree in idxdbs2;


create index pfig_group_member_id_index on phenotype_figure_group_member (pfiggm_member_id)
  using btree in idxdbs2;

alter table phenotype_figure_group_member
  add constraint primary key (pfiggm_pk_id) constraint phenotype_Figure_group_member_primary_key;


create table xpat_figure_group (xfigg_group_name lvarchar(380), 
       	     		       			 xfigg_genox_Zdb_id varchar(50),
						 xfigg_geno_handle varchar(255), 
						 xfigg_group_pk_id serial8 not null constraint xfigg_group_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;


create index xfigg_genox_zdb_index on xpat_Figure_group (xfigg_genox_zdb_id)
  using btree in idxdbs3;

create table xpat_figure_group_member (xfiggm_group_id int8 not null constraint xfiggm_group_id_not_null, 
       	     			      		       xfiggm_member_name varchar(50), 
						       xfiggm_member_id varchar(50) not null constraint xfiggm_member_id_not_null, 
						       xfiggm_pk_id serial8 not null constraint xfiggm_pk_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index xfigg_pk_id_index on xpat_Figure_group_member (xfiggm_pk_id)
  using btree in idxdbs1;

create index xfiggm_group_id_index on xpat_Figure_group_member (xfiggm_group_id)
  using btree in idxdbs3;

create index xfiggm_member_id_index on xpat_Figure_group_member (xfiggm_member_id)
  using btree in idxdbs1;



create index xfigg_group_name_index on xpat_Figure_group (xfigg_group_name)
  using btree in idxdbs3;

create unique index xfigg_group_id_index on xpat_Figure_group (xfigg_group_pk_id)
  using btree in idxdbs1;

create index xfigg_geno_handle_index on xpat_Figure_group (xfigg_geno_handle)
  using btree in idxdbs2;


create index fa_genox_zdb_id_index on functional_annotation (fa_genox_zdb_id)
  using btree in idxdbs2;

create index fa_geno_name_index
 on functional_annotation (fa_geno_name)
 using btree in idxdbs2;

alter table functional_annotation
  add constraint primary key (fa_pk_id)
  constraint functioanl_annotation_primary_key;



commit work ;begin work ;

set PDQPRIORITY 20;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('t',current year to second)
 where zflag_name = "regen_fishmart";


!echo "begin geneGroup.sql";
--set pdqpriority high;

--insert into genox_group (gg_genotype_Zdb_id)
--  select geno_zdb_id from genotype
-- where exists (Select 'x' from genotype_experiment, phenotype_Experiment
--       	      	      where genox_zdb_id = phenox_genox_zdb_id
--		      and geno_Zdb_id = genox_geno_zdb_id);

--update genox_group 
--  set gg_group_name = replace(replace(replace(substr(multiset (select distinct item genox_zdb_id
--							      	      from genotype_Experiment,phenotype_experiment
--							 	      where phenox_genox_zdb_id = genox_zdb_id
--							 	      and genox_geno_Zdb_id = gg_genotype_Zdb_id
--				)::lvarchar(380),11),""),"'}",""),"'","");

--insert into genox_group_member (ggm_group_id, ggm_member_id)
--  select gg_group_pk_id, genox_zdb_id
--    from genotype_experiment, genox_group
--    where genox_geno_zdb_id = gg_genotype_Zdb_id;


insert into affected_gene_group (afg_genox_zdb_id, afg_geno_zdb_id)
  select genox_zdb_id, genox_geno_zdb_id 
  from genotype_experiment;

insert into affected_gene_group (afg_geno_zdb_id)
  select distinct geno_zdb_id from genotype;

--insert into affected_gene_group (afg_geno_zdb_id, afg_genox_Zdb_id)
--  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
--    where not exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
--    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id);

--insert into affected_gene_group (afg_geno_zdb_id, afg_genox_Zdb_id)
--  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
--    where exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
--    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id);

select distinct b.mrkr_abbrev||"|"||b.mrkr_name as name, afg_genox_zdb_id as genox_id
							       	from genotype_experiment,marker a,marker b,
							       marker_relationship,
							  experiment_Condition, genotype, condition_Data_type, affected_gene_group
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and expcond_mrkr_Zdb_id = a.mrkr_zdb_id
							  and expcond_mrkr_zdb_id is not null
							  and mrel_mrkr_1_zdb_id = a.mrkr_Zdb_id
							  and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
							  and b.mrkr_Type like 'GENE%'
							  union
							  select distinct
							  mrkr_abbrev||"|"||mrkr_name as name, afg_genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, affected_gene_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and mrkr_type like 'GENE%'

into temp tmp_ordered_markers;

create index tmp_genox_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update tmp_ordered_markers
  set name = replace(name,"'","$");

update statistics high for table tmp_ordered_markers;

update affected_gene_group 
  set afg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = afg_genox_zdb_id
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");

drop table tmp_ordered_markers;


select distinct 
						  	   b.mrkr_Abbrev_order as name, afg_genox_zdb_id as genox_id
							       	from genotype_experiment,marker a,marker b,
							       marker_relationship,
							  experiment_Condition, genotype, condition_Data_type, affected_gene_group
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and expcond_mrkr_Zdb_id = a.mrkr_zdb_id
							  and expcond_mrkr_zdb_id is not null
							  and mrel_mrkr_1_zdb_id = a.mrkr_Zdb_id
							  and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
							  and b.mrkr_type like 'GENE%'
							  union
							  select distinct
							   mrkr_abbrev_order as name, afg_Genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, affected_gene_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and mrkr_type like 'GENE%'
into temp tmp_ordered_markers;

create index tmp_genox_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update tmp_ordered_markers
  set name = replace(name,"'","$");

update statistics high for table tmp_ordered_markers;

update affected_gene_group 
  set afg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = afg_genox_zdb_id
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");


update statistics high for table tmp_ordered_markers;



drop table tmp_ordered_markers;

select distinct 
						  	   mrkr_abbrev||"|"||mrkr_name as name, afg_geno_zdb_id as geno_id
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, affected_Gene_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and genofeat_geno_Zdb_id = afg_Geno_Zdb_id
							  and mrkr_type like 'GENE%'
							  and afg_genox_zdb_id is null
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update tmp_ordered_markers
  set name = replace(name,"'","$");

update statistics high for table tmp_ordered_markers;

update affected_gene_group 
  set afg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = afg_geno_zdb_id							  
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where afg_group_name is null
 and afg_genox_zdb_id is null;

update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");





drop table tmp_ordered_markers;

select distinct 
						  	   mrkr_abbrev_order as name, afg_geno_zdb_id as geno_id
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, affected_gene_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and fmrel_type in ('is allele of','markers missing','markers moved')
							  and genofeat_geno_Zdb_id = afg_Geno_Zdb_id
							  and mrkr_type like 'GENE%'
							  and afg_genox_zdb_id is null

into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;


update tmp_ordered_markers
  set name = replace(name,"'","$");

update affected_gene_group 
  set afg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = afg_geno_zdb_id
							  and afg_genox_zdb_id is null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where afg_group_order is null
 and afg_genox_zdb_id is null;


update affected_gene_group
  set afg_group_name = replace(afg_Group_name,"$","'");


drop table tmp_ordered_markers;


insert into affected_gene_group_member (afgm_group_id, afgm_member_name, afgm_member_id)
  select afg_group_pk_id, b.mrkr_abbrev, b.mrkr_zdb_id
   from genotype_experiment,marker a,marker b,affected_gene_group,
							       marker_relationship,
							  experiment_Condition, genotype, condition_Data_type
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and afg_genox_zdb_id = genox_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and expcond_mrkr_Zdb_id = a.mrkr_zdb_id
							  and expcond_mrkr_zdb_id is not null
							  and mrel_mrkr_1_zdb_id = a.mrkr_Zdb_id
							  and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
							  and b.mrkr_type like 'GENE%';

 insert into affected_gene_group_member (afgm_group_id, afgm_member_name, afgm_member_id)
   select afg_group_pk_id, mrkr_abbrev, mrkr_zdb_id					
   	  from marker, feature_marker_relationship,affected_gene_group,
							  genotype_Feature, genotype_experiment
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and afg_genox_zdb_id = genox_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = afg_genox_zdb_id
							  and mrkr_type like 'GENE%' ;

insert into affected_gene_group_member (afgm_group_id, afgm_member_name, afgm_member_id)
   select afg_group_pk_id, mrkr_abbrev, mrkr_zdb_id					
   	  from marker, feature_marker_relationship,affected_gene_group,
							  genotype_Feature
							  where afg_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and mrkr_type like 'GENE%' 
							  and afg_genox_zdb_id is null;
							  --and not exists (Select 'x' from genotype_Experiment
							   --   	  where genox_Geno_zdb_id = afg_geno_Zdb_id);




!echo "max octet length for gene_group_name";

select max(octet_length(afg_group_name))
 from affected_gene_group ;


!echo "begin environment group";

insert into environment_group (eg_genox_zdb_id)
  select distinct genox_zdb_id from genotype_experiment
    where exists (select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
    or exists (select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id)
    order by genox_zdb_id;

insert into environment_group (eg_genox_zdb_id)
  select distinct genox_zdb_id from genotype_experiment
    where not exists (select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and not exists (select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id)
   order by genox_Zdb_id;


update environment_group (eg_group_name)
  set eg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item cdt_name||" "||cdt_group from genotype_experiment,
							  experiment_Condition, condition_Data_type
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and genox_zdb_id = eg_genox_zdb_id
							  )::lvarchar(380),11),""),"'}",""),"'","");

--select first 5 * from environment_group;

insert into environment_group_member(egm_group_id, egm_member_name, egm_member_id)
  select eg_group_pk_id, cdt_name||" "||cdt_group, genox_zdb_id
    from condition_Data_type, genotype_experiment, experiment_Condition, environment_group
    where expcond_exp_zdb_id = genox_exp_zdb_id
   and expcond_cdt_Zdb_id = cdt_zdb_id
   and eg_genox_zdb_id = genox_zdb_id;

!echo "lvarchar length for environment group";

select max(octet_length(eg_group_name))
 from environment_group;

!echo "feature_group";


insert into feature_group (fg_geno_Zdb_id)
  select geno_zdb_id from genotype
 where not exists (Select 'x' from genotype_experiment where genox_geno_zdb_id = geno_Zdb_id);

insert into feature_group (fg_Geno_zdb_id, fg_genox_zdb_id)
  select distinct genox_geno_zdb_id, genox_zdb_id
    from genotype_Experiment;


create temp table tmp_ordered_markers (name lvarchar(1000), geno_id varchar(50))
 with no log;

insert into tmp_ordered_markers (name, geno_id)
select distinct feature.feature_name||"|"||feature.feature_abbrev||"|"||fp_prefix as name, 
							   genofeat_geno_Zdb_id as geno_id
							  from feature, genotype_feature, feature_group, feature_prefix
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
							  
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  and fp_pk_id = feature_lab_prefix_id;

insert into tmp_ordered_markers (name, geno_id)
  select distinct feature.feature_name||"|"||feature.feature_abbrev, 
							   genofeat_geno_Zdb_id
							  from feature, genotype_feature, feature_group
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  and feature_lab_prefix_id is null
;

select * from tmp_ordered_markers
  where geno_id = 'ZDB-GENO-120130-345';

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update feature_group 
  set fg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = fg_geno_zdb_id
							  and fg_geno_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where fg_group_name is null
 and fg_geno_zdb_id is not null;

drop table tmp_ordered_markers;


select distinct feature.feature_Abbrev_order as name, genofeat_geno_zdb_id as geno_id from feature, genotype_feature, feature_Group
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update feature_group 
  set fg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = fg_geno_zdb_id
							  and fg_geno_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where fg_group_order is null
 and fg_geno_zdb_id is not null;

drop table tmp_ordered_markers;


update feature_group (fg_type_group)
  set fg_type_group = replace(replace(replace(substr(multiset (select distinct 
						  	  item feature.feature_type from feature, genotype_feature
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  )::lvarchar(380),11),""),"'}",""),"'","")
 ;



insert into feature_group_member (fgm_group_id, fgm_member_name, fgm_member_id, fgm_genotype_id, fgm_significance, fgm_feature_type)
  select fg_group_pk_id, feature_name, feature_zdb_id, genofeat_geno_zdb_id, ftrtype_significance,  feature_type
    from feature_group, genotype_feature, feature, feature_type
    where fg_Geno_zdb_id = genofeat_geno_Zdb_id
    and ftrtype_name = feature_type
    and genofeat_feature_zdb_id = feature_zdb_id;

!echo "max octet length for feature_group_name";

select max(octet_length(fg_group_name))
 from feature_group ;

update feature_group_member
 set fgm_significance = 0 
where fgm_significance is null;

select * from feature_group
 where fg_geno_zdb_id = 'ZDB-GENO-120130-345';



!echo "begin morpholino group";

insert into morpholino_group (morphg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id);


insert into morpholino_group (morphg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and not exists (Select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id);

insert into morpholino_group (morphg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and exists (Select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id);

update morpholino_group (morphg_group_name)
  set morphg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item marker.mrkr_abbrev from marker, genotype_experiment,
							  experiment_Condition, genotype
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							   and expcond_mrkr_Zdb_id = mrkr_zdb_id
							  and genox_zdb_id = morphg_genox_zdb_id
							  order by marker.mrkr_abbrev)::lvarchar(380),11),""),"'}",""),"'","");



update morpholino_group
  set morphg_group_name = replace(morphg_group_name,",M","+M")
  where morphg_Group_name like '%,%';

insert into morpholino_group_member(morphgm_group_id, morphgm_member_name, morphgm_member_id)
  select morphg_group_pk_id, mrkr_abbrev, mrkr_Zdb_id
    from marker, genotype_experiment, experiment_Condition, morpholino_group
    where expcond_exp_zdb_id = genox_exp_zdb_id
   and expcond_mrkr_Zdb_id = mrkr_zdb_id
   and morphg_genox_zdb_id = genox_zdb_id;


!echo "lvarchar length for morpholino group";

select max(octet_length(morphg_group_name))
 from morpholino_group;



!echo "begin construct group";


insert into construct_group (cg_genox_zdb_id, cg_geno_zdb_id)
  select distinct genox_zdb_id, genox_geno_zdb_id from genotype_experiment
    where exists (Select 'x' from phenotype_Experiment
    	  	 	 where phenox_genox_zdb_id = genox_Zdb_id)
    and exists (Select 'x' from genotype_Feature, feature
    	       	      where genofeat_geno_zdb_id = genox_geno_zdb_id
		      and feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%');

insert into construct_group (cg_geno_zdb_id)
  select distinct geno_zdb_id from genotype
    where not exists (Select 'x' from genotype_Experiment where geno_zdb_id = genox_geno_zdb_id)
   and exists (Select 'x' from genotype_Feature, feature
    	       	      where feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%'
		      and geno_zdb_id = genofeat_geno_zdb_id);

insert into construct_group (cg_geno_zdb_id, cg_genox_Zdb_id)
  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
    where not exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id)
   and exists (Select 'x' from genotype_Feature, feature
    	       	      where genofeat_geno_zdb_id = genox_geno_zdb_id
		      and feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%');


insert into construct_group (cg_geno_zdb_id, cg_genox_Zdb_id)
  select distinct genox_geno_zdb_id,genox_Zdb_id from genotype_experiment
    where exists (Select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
    and not exists (select 'x' from phenotype_experiment where phenox_genox_Zdb_id = genox_Zdb_id)
   and exists (Select 'x' from genotype_Feature, feature
    	       	      where genofeat_geno_zdb_id = genox_geno_zdb_id
		      and feature_zdb_id = genofeat_feature_zdb_id
		      and feature_type like 'TRANSGENIC%');


select distinct
							   mrkr_abbrev||"|"||mrkr_name as name, cg_genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, construct_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = cg_genox_zdb_id
							  
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = cg_genox_zdb_id
							  and cg_genox_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_name is null
 and cg_genox_zdb_id is not null;

drop table tmp_ordered_markers;

select distinct
							   mrkr_abbrev_order as name, cg_genox_zdb_id as genox_id
							       from marker, feature_marker_relationship,
							  genotype_Feature, genotype_experiment, construct_group
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = cg_genox_zdb_id
							  
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (genox_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where genox_id = cg_genox_zdb_id
							  and cg_genox_zdb_id is not null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_order is null
 and cg_genox_zdb_id is not null;

drop table tmp_ordered_markers;


select distinct 
						  	   mrkr_abbrev||"|"||mrkr_name as name, cg_geno_zdb_id as geno_id
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, construct_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							 and cg_genox_zdb_id is null
							  and genofeat_geno_Zdb_id = cg_Geno_Zdb_id
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = cg_geno_zdb_id
							  and cg_genox_zdb_id is null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_name is null
 and cg_genox_zdb_id is null;

drop table tmp_ordered_markers;

select distinct 
						  	   mrkr_abbrev_order as name, cg_geno_zdb_id as geno_id 
							  from marker, 
							       feature_marker_relationship,
							       genotype_Feature, construct_group
							  where  genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and cg_genox_zdb_id is null
							  and genofeat_geno_Zdb_id = cg_Geno_Zdb_id
							  and mrkr_type like '%CONSTRCT%'
into temp tmp_ordered_markers;

create index tmp_geno_idx on tmp_ordered_markers (geno_id)
  using btree in idxdbs2;

update statistics high for table tmp_ordered_markers;

update construct_group 
  set cg_group_order = replace(replace(replace(substr(multiset (select distinct 
						  	  item name from tmp_ordered_markers
							  where geno_id = cg_geno_zdb_id
							  and cg_genox_zdb_id is null
							  order by 1
							  )::lvarchar(3000),11),""),"'}",""),"'","")
  where cg_group_order is null
 and cg_genox_zdb_id is null;


 insert into construct_group_member (cgm_group_id, cgm_member_name, cgm_member_id, cgm_abbrev_order)
   select cg_group_pk_id, mrkr_abbrev, mrkr_zdb_id, mrkr_Abbrev_order					
   	  from marker, feature_marker_relationship,construct_group,
							  genotype_Feature, genotype_experiment
							  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and cg_genox_zdb_id = genox_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and genox_zdb_id = cg_genox_zdb_id
							  and mrkr_type like '%CONSTRCT%' ;

insert into construct_group_member (cgm_group_id, cgm_member_name, cgm_member_id, cgm_abbrev_order)
   select cg_group_pk_id, mrkr_abbrev, mrkr_zdb_id, mrkr_abbrev_order					
   	  from marker, feature_marker_relationship,construct_group,
							  genotype_Feature
							  where cg_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrkr_zdb_id
							  and mrkr_type like '%CONSTRCT%' 
							  and cg_genox_zdb_id is null
							  and not exists (Select 'x' from genotype_Experiment
							      	  where genox_Geno_zdb_id = cg_geno_Zdb_id);



!echo "max octet length for construct_group_name";

select max(octet_length(cg_group_name))
 from construct_group ;


!echo "begin functionalAnnotation";

update marker
  set (mrkr_name,mrkr_abbrev) = (replace(mrkr_name,"+",","),replace(mrkr_abbrev,"+",","))
  where mrkr_abbrev like 'MO%'
 and mrkr_type = 'MRPHLNO';

--set pdqpriority high;

select distinct genox_zdb_id 
  from genotype_Experiment, experiment_condition, experiment
 where exp_Zdb_id = expcond_exp_zdb_id
 and exp_zdb_id = genox_exp_zdb_id
 and expcond_mrkr_zdb_id is not null
 into temp tmp_genox;

create index genox_idx on tmp_genox (genox_zdb_id)
  using btree in idxdbs3;


--update statistics high for table morpholino_group;
--update statistics high for table feature_group;

-- !!! INSERT INTO FUNCTIONAL ANNOTATION !!!---


--no morpholinos, but yes phenotypes
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name, fa_genox_zdb_id)
  select distinct geno_zdb_id, geno_handle,geno_display_name, genox_zdb_id
   from genotype, genotype_Experiment
   where genox_zdb_id not in (Select genox_zdb_id from tmp_genox)
   and genox_geno_Zdb_id = geno_Zdb_id
   and genox_Zdb_id in (select phenox_genox_zdb_id from phenotype_Experiment, phenotype_statement
       		       	       where phenos_phenox_pk_id = phenox_pk_id
			       and phenos_tag != 'normal');

--no morpholinos, but yes expression
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name, fa_genox_zdb_id)
  select distinct geno_zdb_id, geno_handle,geno_display_name, genox_Zdb_id
   from genotype, genotype_experiment
   where exists (Select 'x' 
   	     	    	   from genotype_Experiment 
			    where genox_geno_zdb_id = geno_Zdb_id)
    and not exists (Select 'x' from phenotype_experiment, genotype_experiment
    	    	   	   where phenox_genox_zdb_id = genox_zdb_id
			   and genox_geno_zdb_id = geno_zdb_id)
    and not exists (Select 'x' from genotype_Experiment, tmp_genox where genotype_experiment.genox_zdb_id =tmp_genox.genox_zdb_id
    	    	   	   and genotype_Experiment.genox_geno_Zdb_id = geno_Zdb_id) 
    and genox_geno_zdb_id = geno_Zdb_id;

--all genos reguardless
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name)
  select geno_zdb_id, geno_handle,geno_display_name
   from genotype
   where geno_is_wildtype = 'f';

--no pheno, no expression, yes genox (this should be zero)
!echo "zero rows should be inserted here:" ;
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name, fa_genox_zdb_id)
  select distinct genox_geno_zdb_id, geno_handle, geno_display_name,  genox_zdb_id
   from genotype_Experiment, genotype
   where not exists (Select 'x' 
   	     	    	   from phenotype_Experiment
			  where phenox_genox_zdb_id = genox_zdb_id)
   and not exists (Select 'x' from expression_Experiment
       	   	  	  where xpatex_genox_Zdb_id = genox_Zdb_id)
   and genox_geno_zdb_id = geno_Zdb_id;

--morphs with pheno
--set explain on;

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and exists (select 'x' from phenotype_experiment, phenotype_statement
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   and phenos_phenox_pk_id = phenox_pk_id
		   and phenos_tag != 'normal'
		   )
and geno_is_wildtype = 'f';
commit work ;