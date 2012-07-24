

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
       	     		      			  morphg_group_name lvarchar(200), 
						  morphg_genox_Zdb_id varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create unique index morphg_group_id_index on morpholino_group (morphg_group_pk_id)
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

delete from genotype_Experiment
 where genox_zdb_id not in (select xpatex_genox_zdb_id from expression_experiment)
 and genox_zdb_id not in (select phenox_genox_zdb_id from phenotype_Experiment);

create table tmp_genox(genox_zdb_id varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9964 next size 9964;

create index genox_idx on tmp_genox (genox_zdb_id)
  using btree in idxdbs3;

