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



create table figure_term_fish_search (ftfs_pk_id serial8 not null constraint ftfs_pk_not_null,
       	     			        ftfs_fas_id int8 not null constraint ftfs_fas_id_not_null,
					ftfs_fa_id int8,
					ftfs_genox_zdb_id varchar(50),
					ftfs_geno_name varchar(255),
					ftfs_geno_handle varchar(255),
       	     			     	ftfs_fig_zdb_id varchar(50),
					ftfs_term_group lvarchar(3000)
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 16384 next size 16384;

create unique index ftfs_pk_id_index 
  on figure_term_fish_search(ftfs_pk_id)
  using btree in idxdbs3;

create index fas_genox_zdb_id_index 
  on figure_term_fish_search(ftfs_genox_zdb_id)
  using btree in idxdbs3;

create index ftfs_geno_name_index 
  on figure_term_fish_search(ftfs_geno_name)
  using btree in idxdbs3;

create index fas_fas_id_index 
  on figure_term_fish_search(ftfs_fas_id)
  using btree in idxdbs3;


create index ftfs_fa_id_index 
  on figure_term_fish_search(ftfs_fa_id)
  using btree in idxdbs3;

create index ftfs_fig_id_index 
  on figure_term_fish_search(ftfs_fig_zdb_id)
  using btree in idxdbs3;



create table fish_annotation_search (       
       fas_pk_id serial8 not null constraint fas_pk_id_not_null,
       fas_all lvarchar(2000),
       fas_all_with_spaces lvarchar(2000),
       fas_geno_name varchar(255),
       fas_geno_handle varchar(255),
       fas_genox_group lvarchar(380),
       fas_genotype_group varchar(250),
       fas_feature_group lvarchar(1000),
       fas_gene_group lvarchar(1000),
       fas_construct_group lvarchar(1000),
       fas_morpholino_group lvarchar(380),
       fas_pheno_term_group lvarchar(4000),
       fas_pheno_figure_group lvarchar(380),
       fas_xpat_figure_group lvarchar(380),
       fas_xpat_figure_count int,
       fas_feature_order lvarchar(1000),
       fas_feature_type_group lvarchar(500),
       fas_gene_order lvarchar(1000),
       fas_affector_group lvarchar(1000),
       fas_affector_order lvarchar(1000),
       fas_affector_type_group lvarchar(300),
       fas_fish_significance int,
       fas_feature_significance int,
       fas_morph_significance int,
       fas_gene_count int,
       fas_pheno_term_count int,
       fas_pheno_figure_count int,
       fas_total_figure_count int,
       fas_fish_parts_count int
)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 16384 next size 16384;


--create index fas_geno_name_index 
--  on fish_Annotation_search(fas_geno_name)
--  using btree in idxdbs3;

create index fas_geno_handle_index 
  on fish_Annotation_search(fas_geno_handle)
  using btree in idxdbs3;

create index fas_geno_group_index 
  on fish_Annotation_search(fas_genotype_group)
  using btree in idxdbs3;


create table gene_feature_result_view (
       gfrv_pk_id serial8 not null constraint gfrv_pk_id_not_null,
       gfrv_fa_id int8,
       gfrv_fas_id int8 not null constraint gfrv_fas_id_not_null,
       gfrv_geno_name varchar(255),
       gfrv_geno_handle varchar(255) not null constraint gfrv_geno_handle_not_null,
       gfrv_affector_id varchar(50),
       gfrv_affector_abbrev varchar(50),
       gfrv_affector_abbrev_order varchar(70),
       gfrv_affector_type_display varchar(60),
       gfrv_gene_abbrev varchar(80),
       gfrv_gene_abbrev_order varchar(100),
       gfrv_gene_Zdb_id varchar(50),
       gfrv_construct_name varchar(80),
       gfrv_construct_abbrev_order varchar(100),
       gfrv_construct_Zdb_id varchar(50)
)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 16384 next size 16384;

create unique index gene_feature_result_view_primary_key 
  on gene_feature_result_view (gfrv_pk_id)
  using btree in idxdbs3;

create table functional_annotation(
       fa_pk_id serial8 not null constraint fa_pk_id_not_null,
       fa_all lvarchar(4000),
       fa_feature_order lvarchar(1000),
       fa_affector_Type_group lvarchar(1000),
       fa_gene_order lvarchar(1000),
       fa_geno_zdb_id varchar(50),
       fa_feature_alias lvarchar(1000),
       fa_gene_alias lvarchar(3000),
       fa_gene_alt_alias lvarchar(1000),
       fa_geno_alias lvarchar(1000),
       fa_morph_alias lvarchar(1000),
       fa_construct_alias lvarchar(4000),
       fa_geno_name varchar(255),
       fa_geno_handle varchar(150) not null constraint fa_geno_handle_not_null,
       fa_genox_zdb_id varchar(50),
       fa_feature_group lvarchar(1000),
       fa_gene_group lvarchar(1000),
       fa_construct_group lvarchar(1000),
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

create table construct_group (cg_group_name lvarchar(4000), 
       	     		     		    cg_geno_zdb_id varchar(50),
					    cg_genox_Zdb_id varchar(50), 
					    cg_group_pk_id serial8 not null constraint cg_group_pk_id_not_null, 
					    cg_group_order lvarchar(4000),
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

create table affected_gene_group (afg_group_name lvarchar(3000), 
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
       	     		   		  fg_group_order lvarchar(1000),
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

delete from genotype_Experiment
 where genox_zdb_id not in (select xpatex_genox_zdb_id from expression_experiment)
 and genox_zdb_id not in (select phenox_genox_zdb_id from phenotype_Experiment);


create  index gene_feature_result_view_affector_display 
    on gene_feature_result_view (gfrv_affector_type_display) using 
    btree in idxdbs1;

create  index gene_feature_result_view_fas_id
    on gene_feature_result_view (gfrv_fas_id) using 
    btree in idxdbs3;



alter table fish_annotation_search 
add constraint primary key (fas_pk_id)
  constraint fish_Annotation_Search_primary_key;

alter table figure_term_fish_search
  add constraint primary key (ftfs_pk_id)
 constraint figure_term_fish_search_primary_key;

alter table figure_term_Fish_search
  add constraint (Foreign key (ftfs_fas_id)
  references fish_annotation_search
   constraint ftfs_fas_foreign_key);

alter table figure_term_Fish_search
  add constraint (Foreign key (ftfs_fa_id)
  references functional_annotation
   constraint ftfs_fa_id_foreign_key);

alter table figure_term_Fish_search
  add constraint (Foreign key (ftfs_genox_Zdb_id)
  references genotype_experiment
   constraint ftfs_genox_Zdb_id_foreign_key);


alter table figure_term_Fish_search
  add constraint (Foreign key (ftfs_fig_Zdb_id)
  references figure
   constraint ftfs_fig_Zdb_id_foreign_key);




alter table gene_feature_result_view
  add constraint primary key (gfrv_pk_id)
  constraint gene_feature_result_view_primary_key;


alter table gene_feature_Result_view
  add constraint (Foreign key (gfrv_fa_id)
  references functional_annotation
  constraint gfrv_fa_id_foreign_key);

alter table gene_feature_Result_view
  add constraint (Foreign key (gfrv_fas_id)
  references fish_annotation_search
  constraint gfrv_fas_id_foreign_key);

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


select distinct feature.feature_name||"|"||feature.feature_abbrev||"|"||fp_prefix as name, 
							   genofeat_geno_Zdb_id as geno_id
							  from feature, genotype_feature, feature_group, feature_prefix
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
							  
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  and fp_pk_id = feature_lab_prefix_id
							  
into temp tmp_ordered_markers;

insert into tmp_ordered_markers (name, geno_id)
  select distinct feature.feature_name||"|"||feature.feature_abbrev, 
							   genofeat_geno_Zdb_id
							  from feature, genotype_feature, feature_group
  	 	   	   				  where feature_zdb_id = genofeat_feature_zdb_id
			   				  and genofeat_geno_zdb_id = fg_geno_Zdb_id
							  and feature_lab_prefix_id is null
;

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
--set pdqpriority high;


--ZDB-GENO-110722-21


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


update statistics high for table morpholino_group;
update statistics high for table feature_group;

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

--all genos regaurdless
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

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, fg_group_name, 
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
and geno_is_wildtype = 't';


--set explain off;
--morphs with xpat
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and not exists (select 'x' from phenotype_Experiment
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   )
  and exists (select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and not exists (select 'x' from phenotype_Experiment
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   )
  and exists (select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 'f';

!echo "insert into functional annotation some morphs with no features";

--morphs with no features, yes phenotype
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and exists (select 'x' from phenotype_Experiment, phenotype_statement
    	       	        where phenox_genox_zdb_id = genox_zdb_id
			and phenos_phenox_pk_id = phenox_pk_id
			and phenos_tag != 'normal')
and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and exists (select 'x' from phenotype_Experiment, phenotype_statement
    	       	        where phenox_genox_zdb_id = genox_zdb_id
			and phenos_phenox_pk_id = phenox_pk_id
			and phenos_tag != 'normal')
and geno_is_wildtype = 'f';

--morphs with no features, yes expression, no phenotype.
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
 and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 'f';

--morphs with no features, yes expression, no phenotype.
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 'f';

update statistics high for table functional_annotation;



insert into functional_annotation (fa_geno_handle, fa_geno_name,  fa_genox_zdb_id, fa_geno_zdb_id)
  select distinct geno_handle, geno_handle, genox_zdb_id, genox_geno_zdb_id
    from genotype_experiment, genotype
    where not exists (Select 'x' from functional_Annotation where fa_genox_zdb_id = genox_zdb_id)
    and genox_geno_zdb_id = geno_zdb_id
    and not exists (Select 'x' from experiment_condition
    	    	   	   where expcond_exp_zdb_id = genox_exp_zdb_id
			   and expcond_mrkr_zdb_id is not null)
    and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
 and geno_is_wildtype = 't' ;

insert into functional_annotation (fa_geno_handle, fa_geno_name,  fa_genox_zdb_id, fa_geno_zdb_id)
  select distinct geno_handle, geno_display_name, genox_zdb_id, genox_geno_zdb_id
    from genotype_experiment, genotype
    where not exists (Select 'x' from functional_Annotation where fa_genox_zdb_id = genox_zdb_id)
    and genox_geno_zdb_id = geno_zdb_id
    and not exists (Select 'x' from experiment_condition
    	    	   	   where expcond_exp_zdb_id = genox_exp_zdb_id
			   and expcond_mrkr_zdb_id is not null)
    and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
 and geno_is_wildtype = 'f' ;


insert into feature_group_member (fgm_group_id, fgm_member_name, fgm_member_id, fgm_genotype_id, fgm_significance)
  select fg_group_pk_id,feature.feature_name,feature_zdb_id,fa_geno_zdb_id, ftrtype_significance
    from functional_annotation, feature_group, feature, genotype_Feature,feature_type
 where fa_feature_group = fg_group_name
 and feature_zdb_id = genofeat_feature_zdb_id
 and ftrtype_name = feature_type
 and fg_geno_zdb_id = genofeat_geno_zdb_id
 and fa_morpholino_group is not null;

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

!echo "XPAT FIGURE";

update statistics high for table functional_annotation;

set pdqpriority 80;

insert into xpat_figure_group (xfigg_genox_zdb_id, xfigg_geno_handle)
  select distinct xpatex_genox_zdb_id, geno_handle from expression_Experiment, experiment, genotype, genotype_Experiment
  where exp_name != '_Standard'
  and exp_name != '_Generic-control'
  and genox_zdb_id = xpatex_genox_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and genox_geno_zdb_id = geno_zdb_id
 and geno_is_wildtype = 't';

update statistics high for table xpat_figure_group;

select distinct xpatfig_fig_Zdb_id, xfigg_genox_zdb_id
  from expression_pattern_Figure, expression_Result, expression_experiment, xpat_Figure_group
  where xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and xpatres_xpatex_zdb_id = xpatex_zdb_id
  and xpatex_genox_zdb_id = xfigg_genox_zdb_id
into temp tmp_xpat;

create index xfig_genox_index
  on tmp_xpat (xfigg_genox_zdb_id)
  using btree in idxdbs1;

select count(*) as counter, xfigg_genox_zdb_id
  from tmp_xpat
  group by xfigg_genox_Zdb_id
   into temp tmp_count;

select count (distinct xpatfig_fig_zdb_id)
  from expression_pattern_figure, expression_Result, expression_experiment
  where xpatex_zdb_id = xpatres_xpatex_zdb_id
  and xpatres_zdb_id = xpatfig_xpatres_zdb_id
 and xpatex_genox_Zdb_id = 'ZDB-GENOX-041102-1429';

select * 
  from tmp_count
 where counter=29425;


select * from experiment, genotype_Experiment, genotype
 where genox_exp_zdb_id = exp_zdb_id
 and geno_zdb_id = genox_geno_zdb_id
and genox_zdb_id = 'ZDB-GENOX-041102-1429';

--set explain on avoid_Execute;
update xpat_figure_group
  set xfigg_group_name = replace(replace(replace(substr(multiset  (select item xpatfig_Fig_Zdb_id from tmp_xpat
      		       	 					 	 	  where tmp_xpat.xfigg_genox_zdb_id = xpat_figure_group.xfigg_genox_zdb_id
										  )::lvarchar,11),""),"'}",""),"'","");

--set explain off;
insert into xpat_figure_group_member (xfiggm_group_id, xfiggm_member_name, xfiggm_member_id)
  select distinct xfigg_group_pk_id, fig_label, xpatfig_fig_zdb_id
    from xpat_figure_group, expression_experiment, expression_result, expression_pattern_figure, figure
    where xpatex_genox_zdb_id = xfigg_genox_zdb_id
    and xpatex_zdb_id = xpatres_xpatex_zdb_id
    and xpatres_zdb_id = xpatfig_xpatres_zdb_id
    and fig_zdb_id = xpatfig_fig_zdb_id;

update functional_annotation
  set fa_xpat_figure_group = (select xfigg_group_name 
      			      	      from xpat_figure_group 
				      where fa_genox_zdb_id = xfigg_genox_zdb_id);

!echo "term group" ;

insert into term_group (tg_genox_group)
  select genox_zdb_id from genotype_Experiment
  where exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_zdb_id);

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select  distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

update statistics high for table term_group;
set pdqpriority 80;


create temp table tmp_term (phenox_genox_zdb_id varchar(50), term varchar(50), category char(2))
 with no log;

insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id as term, 1
  from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

--alter table all_term_contains
-- modify (alltermcon_container_zdb_id varchar(30) not null constraint alltermcon_container_zdb_id_not_null);

select replace(replace(replace(substr(multiset (select distinct item alltermcon_container_zdb_id
						  	    from phenotype_Experiment, phenotype_Statement, all_Term_contains
     							    where tg_genox_group = phenox_genox_zdb_id
     							    and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
     							    and phenox_pk_id = phenos_phenox_pk_id
							  and phenos_tag != 'normal'
						 )::lvarchar,11),""),"'}",""),"'","") as tg_name, tg_genox_group as ttg_genox_group
   from term_group 
into temp tmp_tg;


delete from tmp_Tg where tg_name is null;

create index tgtemp_genox on tmp_tg(ttg_genox_group)
  using btree in idxdbs2;

update statistics high for table tmp_Tg;

update term_group
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null );


!echo "max octet length term_Group_name";
select max(octet_length(tg_group_name)) from term_group; 

drop table tmp_tg;  

create index term_genox on tmp_term(phenox_genox_zdb_id)
  using btree in idxdbs3;

create index term_genox2 on tmp_term(term)
  using btree in idxdbs1;


insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id as term, 2
  from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_1_subterm_zdb_id is not null
     and not exists (Select 'x' from tmp_term where phenotype_experiment.phenox_genox_zdb_id = tmp_term.phenox_genox_zdb_id
     	     	    	    and alltermcon_container_zdb_id = tmp_term.term);


select phenox_genox_zdb_id, term
  from tmp_term
  where category =2
into temp tmp_term2;


create index term_genox3 on tmp_term2(phenox_genox_zdb_id)
  using btree in idxdbs2;

update statistics high for table tmp_term;
update statistics high for table tmp_term2;

select replace(replace(replace(substr(multiset (
							  select distinct item term from tmp_term2
							  where tg_genox_group = phenox_genox_zdb_id
						
							 )::lvarchar,11),""),"'}",""),"'","") as tg_name, tg_genox_group as ttg_genox_group
  from term_group
  where tg_genox_group in (Select phenox_genox_zdb_id from tmp_term2)
into temp tmp_tg;

delete from tmp_tg where tg_name = '';
delete from tmp_tg where tg_name is null;

create index tgtemp_Genox on tmp_tg(ttg_genox_group)
  using btree in idxdbs2;

update statistics high for table tmp_Tg;

update term_group 
  set tg_group_name = tg_group_name||(Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is not null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
    and exists (Select 'x' from tmp_term2 where phenox_genox_zdb_id = tg_genox_group);

update term_group 
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term2 where phenox_genox_zdb_id = tg_genox_group);

drop table tmp_tg;
--drop table tmp_Term;

insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id as term, 3 
  from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_2_superterm_zdb_id is not null
 and not exists (Select 'x' from tmp_term where phenotype_experiment.phenox_genox_zdb_id = tmp_term.phenox_genox_zdb_id
     	 		and phenos_entity_2_superterm_zdb_id = tmp_term.term)
;

update statistics high for table tmp_term;

select phenox_genox_zdb_id, term
  from tmp_term
  where category =3
into temp tmp_term3;


create index term_genox4 on tmp_term3(phenox_genox_zdb_id)
  using btree in idxdbs1;

update statistics high for table tmp_term;
update statistics high for table tmp_term3;

select replace(replace(replace(substr(multiset (
							  select distinct item term from tmp_term3
							  where tg_genox_group = phenox_genox_zdb_id
						
							 )::lvarchar,11),""),"'}",""),"'","") as tg_name,tg_genox_group as ttg_genox_group
  from term_group
  where tg_genox_group in (Select phenox_genox_zdb_id from tmp_term3)
into temp tmp_tg;

delete from tmp_tg where tg_name = '';
delete from tmp_tg where tg_name is null;

update term_group 
  set tg_group_name = tg_group_name||(Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is not null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
    and exists (Select 'x' from tmp_term3 where phenox_genox_zdb_id = tg_genox_group);

update term_group 
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term3 where phenox_genox_zdb_id = tg_genox_group);


--drop table tmp_term;
drop table tmp_tg;

insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id  as term, 4
 from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_2_subterm_zdb_id is not null
 and not exists (Select 'x' from tmp_term where phenotype_experiment.phenox_genox_zdb_id = tmp_term.phenox_genox_zdb_id
     	 		and phenos_entity_2_subterm_zdb_id = tmp_term.term);


update statistics high for table tmp_term;


select phenox_genox_zdb_id, term
  from tmp_term
  where category =4
into temp tmp_term4;

create index term_genox5 on tmp_term4(phenox_genox_zdb_id)
  using btree in idxdbs3;

update statistics high for table tmp_term;
update statistics high for table tmp_term4;

select replace(replace(replace(substr(multiset (
							  select distinct item term from tmp_term4
							  where tg_genox_group = phenox_genox_zdb_id
						
							 )::lvarchar,11),""),"'}",""),"'","") as tg_name,tg_genox_group as ttg_genox_group
  from term_group
  where tg_genox_group in (Select phenox_genox_zdb_id from tmp_term4)
into temp tmp_tg;


create index tgtemp_Genox on tmp_tg(ttg_genox_group)
  using btree in idxdbs2;

update statistics high for table tmp_Tg;

update term_group 
  set tg_group_name = tg_group_name||(Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is not null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term4 where phenox_genox_zdb_id = tg_genox_group);

update term_group 
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term4 where phenox_genox_zdb_id = tg_genox_group);

drop table tmp_term;
drop table tmp_tg;

set pdqpriority 0;

!echo "check lvarchars for term group name"
select max(octet_length(tg_group_name)) from term_group;


update statistics high for table functional_annotation;
update statistics high for table feature_group;

set pdqpriority 80;

update functional_annotation
  set fa_gene_group = (Select distinct afg_group_name 
      				   	   from affected_gene_group
      				   	   where afg_genox_Zdb_id = fa_genox_zdb_id)
  where fa_genox_zdb_id is not null
;

select distinct afg_group_name, afg_geno_zdb_id, afg_group_order
  from affected_gene_group, functional_annotation
 where afg_genox_Zdb_id is null
       and fa_genox_zdb_id is null
 and afg_geno_zdb_id = fa_geno_zdb_id
 into temp tmp_afg_to_update;

create index tmp_geno_id
  on tmp_afg_to_update(afg_geno_Zdb_id)
  using btree in idxdbs2;



update functional_annotation
  set fa_gene_group = (Select afg_group_name 
      				   	   from tmp_afg_To_update
      				   	   where afg_geno_Zdb_id = fa_geno_zdb_id
					   )
  where fa_genox_zdb_id is null
;


update functional_annotation
  set fa_construct_group = (Select distinct cg_group_name from construct_group 
      		       	 	 where cg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_construct_group is null;

update functional_annotation
  set fa_construct_group = (Select distinct cg_group_name from construct_group 
      		       	 	 where cg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_construct_group is null;


update functional_annotation
  set fa_feature_group = (Select distinct fg_group_name from feature_group 
      		       	 	 where fg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_feature_group is null;

update functional_annotation
  set fa_feature_group = (Select distinct fg_group_name from feature_group 
      		       	 	 where fg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_feature_group is null;


update functional_annotation
  set fa_affector_type_group = (Select distinct fg_type_group from feature_group 
      		       	 	 where fg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_feature_group is not null
and fa_affector_type_group is null;

update functional_annotation
  set fa_affector_type_group = (Select distinct fg_type_group from feature_group 
      		       	 	 where fg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_feature_group is not null
 and fa_affector_type_group is null;


update functional_annotation
  set fa_feature_order = (Select distinct fg_group_order from feature_group 
      		       	 	 where fg_geno_zdb_id = fa_geno_zdb_id 
				 and fa_genox_zdb_id is null)
  where fa_genox_zdb_id is null
  and fa_feature_order is null;

update functional_annotation
  set fa_feature_order = (Select distinct fg_group_order from feature_group 
      		       	 	 where fg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_feature_order is null;

update functional_annotation
  set fa_gene_order = (Select afg_group_order from tmp_afg_to_update
      		       	 	 where afg_geno_zdb_id = fa_geno_zdb_id 
				 )
  where fa_genox_zdb_id is null
  and fa_gene_order is null;

update functional_annotation
  set fa_gene_order = (Select distinct afg_group_order from affected_gene_group 
      		       	 	 where afg_genox_zdb_id = fa_genox_zdb_id 
				 and fa_genox_zdb_id is not null)
 where fa_genox_zdb_id is not null
 and fa_gene_order is null;



update statistics high for table phenotype_figure_group;

update statistics high for table environment_group;

update functional_annotation
 set fa_environment_group_is_standard_or_control = 't'
 where fa_genox_zdb_id in (select genox_zdb_id from genotype_experiment
       		      	 where genox_is_std_or_generic_control = 't');

update functional_annotation
  set fa_environment_group  = (select eg_group_name from environment_Group where eg_genox_zdb_id = fa_genox_zdb_id);

update functional_annotation
  set fa_pheno_term_group = (select tg_group_name from term_Group where tg_genox_group = fa_genox_zdb_id);


delete from functional_annotation
  where (fa_environment_group like '%chemical%'
  or fa_environment_group like '%pH%'
  or fa_environment_group like '%physical%'
  or fa_environment_group like '%physiological%'
  or fa_environment_group like '%salinity%'
  or fa_environment_group like '%temperature'
  or fa_environment_group like '%salinity'
  or fa_environment_group like '%temperature%')
  and fa_environment_group_is_standard_or_control = 'f';

update statistics high for table functional_annotation;
update statistics high for table feature_group;
update statistics high for table morpholino_group;
update statistics high for table affected_gene_group;
update statistics high for table environment_group;
update statistics high for table term_group;

update functional_annotation
   set fa_affector_type_group = 'zzzzzzzzzzzzzzzzzzzzzz'
   where fa_affector_type_group is null;


--update statistics high;

!echo "begin aliases.sql";

set pdqpriority 80;

select distinct dalias_alias, fg_Geno_zdb_id, fg_group_name as group_name, fg_group_pk_id as group_id
 from data_alias, feature_group,               
	feature_group_member 
 where dalias_data_zdb_id = fgm_member_id
 and fg_group_pk_id = fgm_group_id
 order by dalias_alias
into temp tmp_alias;



delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(group_id)
  using btree in idxdbs3;

update statistics high for table tmp_alias;

--drop table tmp_tg;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where fg_group_pk_id = group_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") 
							 as tg_name,fg_group_name as ttg_group_name, fg_geno_Zdb_id
  from feature_group
into temp tmp_tg;

delete from tmp_tg where tg_name is null;

--select first 6 * from tmp_tg;

create index tmp_fish on tmp_tg(fg_geno_zdb_id) 
  using btree in idxdbs1;

update statistics high for table tmp_tg;

update functional_annotation
  set fa_feature_alias = (select distinct tg_name 
      		       	 	 from tmp_tg 
				 where fg_geno_Zdb_id = fa_geno_Zdb_id) 
  where fa_feature_group is not null;

drop table tmp_tg;
drop table tmp_alias;

select distinct dalias_alias, cg_Geno_zdb_id, cg_group_name as group_name, cg_group_pk_id as group_id
 from data_alias, construct_group,               
	construct_group_member 
 where dalias_data_zdb_id = cgm_member_id
 and cg_group_pk_id = cgm_group_id
 order by dalias_alias
into temp tmp_alias;

insert into tmp_alias (dalias_alias, cg_geno_zdb_id, group_name, group_id)
  select distinct allnmend_name_end_lower, cg_geno_zdb_id, cg_group_name, cg_group_pk_id
    from all_name_ends, all_map_names, construct_group, construct_Group_member
    where allnmend_allmapnm_serial_id = allmapnm_serial_id
   and cg_group_pk_id = cgm_group_id
   and allmapnm_zdb_id = cgm_member_id;

delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(group_id)
  using btree in idxdbs3;

update statistics high for table tmp_alias;

--drop table tmp_tg;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where cg_group_pk_id = group_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") 
							 as tg_name,cg_group_name as ttg_group_name, cg_geno_Zdb_id
  from construct_group
into temp tmp_tg;

delete from tmp_tg where tg_name is null;

--select first 6 * from tmp_tg;

create index tmp_fish on tmp_tg(cg_geno_zdb_id) 
  using btree in idxdbs1;

update statistics high for table tmp_tg;

update functional_annotation
  set fa_construct_alias = (select distinct tg_name 
      		       	 	 from tmp_tg 
				 where cg_geno_Zdb_id = fa_geno_Zdb_id) 
  where fa_construct_group is not null;

select max(octet_length(fa_construct_alias))
  from functional_annotation;

drop table tmp_tg;
drop table tmp_alias;


select distinct dalias_alias, afg_group_name as group_name, afg_genox_zdb_id as genox, afg_geno_zdb_id as geno
from data_alias, affected_gene_group, 
	affected_gene_group_member
where dalias_data_zdb_id = afgm_member_id
and afg_group_pk_id = afgm_group_id
order by dalias_alias
into temp tmp_alias;

select distinct mrkr_zdb_id, genox_geno_zdb_id, genox_zdb_id
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature,
								      genotype_experiment
								  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in ( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of')
into temp tmp_mrkrs;


insert into tmp_mrkrs (mrkr_zdb_id, genox_geno_zdb_id, genox_zdb_id)
select distinct mrkr_zdb_id,genofeat_geno_zdb_id,''
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature
								  where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of');


update tmp_mrkrs
  set genox_zdb_id = null
 where genox_zdb_id = '';



insert into tmp_Alias (dalias_alias, group_name, genox, geno)
  select distinct mrkr_abbrev||"|"||mrkr_name, mrkr_abbrev||"|"||mrkr_name, genox_zdb_id, genox_geno_zdb_id
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature,
								      genotype_experiment
								  where genox_geno_zdb_id = genofeat_geno_zdb_id
							  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in ( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of');


insert into tmp_Alias (dalias_alias, group_name, geno)
   select distinct mrkr_abbrev||"|"||mrkr_name, mrkr_abbrev||"|"||mrkr_name, genofeat_geno_zdb_id
							  	 from marker, 
								      feature_marker_relationship,
								      marker_relationship,	
								      genotype_Feature
								  where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
							  and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
							  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
							  and mrkr_type in ( 'GENE','GENEP','REGION','EFG')
							  and mrel_type in 
							      ('contains engineered region','coding sequence of','promoter of')
;

create index mrkr on tmp_mrkrs(mrkr_zdb_id)
  using btree in idxdbs1;



insert into tmp_Alias (Dalias_alias, group_name, genox, geno)
select distinct dalias_alias, mrkr_Zdb_id, genox_Zdb_id, genox_geno_Zdb_id
   from data_Alias, tmp_mrkrs
 where mrkr_zdb_id = dalias_data_zdb_id
;


delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias1 on tmp_alias(geno)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(genox)
  using btree in idxdbs3;

update statistics high for table tmp_alias;

--drop table tmp_tg;

select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where genox = afg_Genox_zdb_id
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") as tg_name,
							 afg_group_name as ttg_group_name, afg_genox_zdb_id as fish
  from affected_gene_group
where afg_genox_zdb_id is not null
into temp tmp_tg;

create index tmp_fish on tmp_tg(fish) 
  using btree in idxdbs1;

update statistics high for table tmp_tg;
update statistics high for table affected_gene_group;

insert into tmp_tg (tg_name, ttg_Group_name, fish)
select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where geno = afg_Geno_zdb_id
							  and genox is null
							  order by dalias_alias
							 )::lvarchar(4000),11),""),"'}",""),"'","") as tg_name,
							 afg_group_name as ttg_group_name, afg_geno_zdb_id as fish
  from affected_gene_group
  where afg_genox_Zdb_id is null
 and not exists (Select 'x' from tmp_tg where fish = afg_geno_zdb_id);


delete from tmp_tg where tg_name is null;
--select first 6 * from tmp_tg;


update functional_annotation
  set fa_gene_alias = (select distinct tg_name from tmp_tg where fish = fa_genox_zdb_id
      		     and fish like 'ZDB-GENOX-%' )
 where fa_gene_group is not null
 and fa_genox_zdb_id is not null;

--'ZDB-GENO-110722-21'
update functional_annotation
  set fa_gene_alias = (select distinct tg_name from tmp_tg where fish = fa_geno_Zdb_id
      		    and fish like 'ZDB-GENO-%')
 where fa_gene_group is not null
 and fa_genox_zdb_id is null
and fa_gene_alias is null;

update functional_annotation
  set fa_all = (select distinct tg_name from tmp_tg where fish = fa_geno_Zdb_id
      		    and fish like 'ZDB-GENO-%')
 where fa_gene_group is null
 and fa_genox_zdb_id is null
and fa_gene_alias is null
 and exists (Select 'x' from tmp_tg where fish = fa_geno_Zdb_id)
 and fa_geno_Zdb_id is not null;

update functional_annotation
  set fa_gene_alt_alias = (select distinct tg_name from tmp_tg where fish = fa_geno_Zdb_id
      		    and fish like 'ZDB-GENO-%')
 where fa_gene_group is null
 and fa_genox_zdb_id is null
and fa_gene_alias is null
 and exists (Select 'x' from tmp_tg where fish = fa_geno_Zdb_id)
 and fa_geno_Zdb_id is not null;

--select * from functional_annotation 
--where fa_geno_Zdb_id = 'ZDB-GENO-110722-21';

drop table tmp_tg;
drop table tmp_alias;

select distinct dalias_alias, morphg_group_name as group_name
from data_alias, morpholino_group, 
	morpholino_group_member
where dalias_data_zdb_id = morphgm_member_id
and morphg_group_pk_id = morphgm_group_id
into temp tmp_alias;


delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias_name on tmp_alias(group_name)
  using btree in idxdbs3;

update statistics high for table tmp_alias;


select replace(replace(replace(substr(multiset (select distinct item dalias_alias from tmp_alias
							  where morphg_group_name = group_name
							  order by dalias_alias
							 )::lvarchar(380),11),""),"'}",""),"'","") 
							 as tg_name,morphg_group_name as ttg_group_name
  from morpholino_group
into temp tmp_tg;

delete from tmp_tg where tg_name is null;

--select first 6 * from tmp_tg;

update functional_annotation
  set fa_morph_alias = (select distinct tg_name 
      		       	       from tmp_tg 
			       where ttg_group_name = fa_morpholino_group)
  where fa_morpholino_group is not null;

drop table tmp_tg;
drop table tmp_alias;

select distinct dalias_alias, geno_zdb_id as group_name
from data_alias, genotype
where dalias_data_zdb_id =geno_zdb_id
into temp tmp_alias;


delete from tmp_alias where dalias_alias is null;

create index dalias on tmp_alias(dalias_alias)
  using btree in idxdbs3;

create index dalias3 on tmp_alias(group_name)
  using btree in idxdbs2;

update statistics high for table tmp_alias;


select replace(replace(replace(substr(multiset ( select distinct item dalias_alias from tmp_alias
							  where fa_geno_Zdb_id = group_name
							  order by dalias_alias
							 )::lvarchar(380),11),""),"'}",""),"'","") 
							 as tg_name,fa_geno_Zdb_id as ttg_group_name
  from functional_Annotation
into temp tmp_tg;

delete from tmp_tg where tg_name is null;
--select first 6 * from tmp_tg;

update functional_annotation
  set fa_geno_alias = (select distinct tg_name 
      		      	      from tmp_tg 
			      where ttg_group_name = fa_geno_Zdb_id
			      )
  where fa_geno_Zdb_id is not null;

drop table tmp_tg;
drop table tmp_alias;

--update functional_annotation
--  set fa_geno_alias = (replace(replace(replace(substr(multiset (select distinct item-
--						  	  dalias_alias from data_alias
--							  where dalias_data_zdb_id = fa_geno_Zdb_id
--							  and fa_geno_Zdb_id is not null
--							  )::lvarchar(10000),11),""),"'}",""),"'",""));


alter table functional_annotation
 modify (fa_geno_name varchar(150) not null constraint fa_geno_name_not_null);

update functional_annotation
  set fa_all = "sierra"
 where (fa_all is null
        or fa_all = '');



update functional_Annotation
  set fa_all = fa_all||","||fa_feature_group
 where fa_feature_group is not null
 ;

update functional_Annotation
  set fa_all = fa_all||","||fa_morpholino_group
 where fa_morpholino_group is not null;



update functional_Annotation
  set fa_all = fa_all||","||fa_gene_group
 where fa_gene_group is not null;



update functional_Annotation
  set fa_all = fa_all||","||fa_construct_group
 where fa_construct_group is not null;


update functional_annotation
  set fa_all = fa_all||","||fa_gene_alias
  where fa_gene_alias is not null;


update functional_annotation
  set fa_all = fa_all||","||fa_morph_alias
  where fa_morph_alias is not null;

select count(*) from functional_annotation 
where fa_all ='sierra';


update functional_annotation
  set fa_all = fa_all||","||fa_feature_alias
  where fa_feature_alias is not null;


update functional_annotation
  set fa_all = fa_all||","||fa_geno_alias
  where fa_geno_alias is not null;

update functional_annotation
  set fa_all = fa_all||","||fa_pheno_term_group
  where fa_pheno_term_group is not null;

!echo "functional_annotation with fa_all null";
select count(*) from functional_annotation
 where fa_all is null;

update functional_Annotation
  set fa_all = replace(fa_all,'sierra,','');

update functional_Annotation
  set fa_all = replace(fa_all,'sierra','');

--create index fa_all_bts_index
--  on functional_Annotation (fa_all bts_lvarchar_ops) USING BTS IN smartbs1;

--

update statistics high for table functional_annotation;
update statistics high for table feature_group;
update statistics high for table feature_group_member;
update statistics high for table affected_gene_group_member;
update statistics high for table affected_gene_group;
update statistics high for table morpholino_group;
update statistics high for table phenotype_experiment;

set pdqpriority 80;

update functional_annotation
 set fa_pheno_figure_count = 0;

update functional_annotation 
  set fa_pheno_figure_count = (Select count(distinct phenox_fig_Zdb_id) 
      			      from phenotype_experiment 
			      where phenox_genox_zdb_id = fa_genox_zdb_id)
  where fa_genox_zdb_id is not null;


update functional_annotation
 set fa_morph_member_count = 0;


update functional_annotation
  set fa_morph_member_count = (Select count(distinct morphgm_member_id) 
      			      from morpholino_group_member, morpholino_group 
			      where fa_morpholino_group = morphg_group_name 
			      and morphgm_group_id = morphg_group_pk_id
			      and morphg_genox_zdb_id = fa_genox_zdb_id)
where fa_genox_zdb_id is not null
 and fa_morph_member_count = 0
 and fa_morpholino_group is not null;

update functional_annotation
 set fa_feature_count = 0;

update functional_annotation
  set fa_feature_count = (Select count (distinct fgm_member_id) 
      		       	 from feature_group_member, feature_group 
			 where fgm_group_id = fg_group_pk_id
			 and fg_genox_zdb_id = fa_genox_zdb_id
			 and fg_genox_zdb_id is not null
)
where fa_genox_zdb_id is not null
and fa_feature_count = 0
 and fa_feature_group is not null;


update functional_annotation
  set fa_feature_count = (Select count (distinct fgm_member_id) 
      		       	 from feature_group_member, feature_group 
			 where fgm_group_id = fg_group_pk_id
			 and fg_geno_zdb_id = fa_geno_zdb_id
			 and fg_geno_zdb_id is not null
 )
where fa_genox_zdb_id is null
and fa_feature_count = 0
 and fa_feature_group is not null;

update functional_annotation
 set fa_gene_count = 0;

update functional_annotation
  set fa_gene_count = (Select count (distinct afgm_member_id) 
      		      from affected_gene_group_member, affected_gene_group 
		      where afg_group_pk_id = afgm_group_id
		      and afg_genox_zdb_id = fa_genox_zdb_id
		      and afg_genox_zdb_id is not null)
where fa_genox_zdb_id is not null
and fa_gene_count = 0
 and fa_gene_group is not null;

update functional_annotation
  set fa_gene_count = (Select count (distinct afgm_member_id) 
      		      from affected_gene_group_member, affected_gene_group 
		      where afg_group_pk_id = afgm_group_id
		      and afg_geno_zdb_id = fa_geno_zdb_id
		      and afg_geno_zdb_id is not null
		      and afg_genox_zdb_id is null)
where fa_genox_zdb_id is null
and fa_gene_count =0
 and fa_gene_group is not null;

update functional_annotation 
  set fa_feature_significance = 0;

update functional_annotation 
  set fa_morph_significance = 0;

update functional_annotation  
  set fa_feature_significance = (select sum(fgm_significance) from feature_group_member, feature_group
      			      		where fgm_group_id = fg_group_pk_id
					and fa_genox_zdb_id = fg_genox_zdb_id
					and fg_genox_zdb_id is not null
					and fa_genox_zdb_id is not null
					)
where fa_genox_zdb_id is not null
 and fa_feature_significance = 0;

update functional_annotation  
  set fa_feature_significance = (select sum(fgm_significance) from feature_group_member, feature_group
      			      		where fgm_group_id = fg_group_pk_id
					and fa_geno_zdb_id = fg_geno_zdb_id
					and fa_genox_zdb_id is null
					and fg_genox_zdb_id is null
					)
where fa_geno_zdb_id is not null
 and fa_genox_zdb_id is null
 and fa_feature_significance = 0;

update functional_annotation
  set fa_morph_significance = (select sum(morphgm_significance) from morpholino_group_member, morpholino_group
      			      	      where morphgm_group_id = morphg_group_pk_id
				      and morphg_group_name = fa_morpholino_group
				      and morphg_genox_zdb_id = fa_genox_zdb_id)
where fa_morph_significance = 0;

update functional_annotation
  set fa_feature_significance = fa_feature_significance + fa_morph_significance
      where fa_morpholino_group is not null;

update functional_annotation
  set fa_feature_significance = 0
 where fa_feature_significance is null;


update functional_annotation
set fa_feature_significance = 0
where fa_Feature_significance is null;

update functional_annotation
set fa_morph_significance = 0
where fa_morph_significance is null;

update functional_annotation
set fa_fish_significance = fa_feature_significance;

update functional_annotation 
 set fa_gene_count = 0
 where fa_gene_count is null;

update functional_annotation 
 set fa_feature_count = 0
 where fa_feature_count is null;

update functional_annotation 
 set fa_morph_member_count = 0
 where fa_morph_member_count is null;

update functional_annotation
 set fa_fish_parts_count = fa_feature_count + fa_morph_member_count ;

delete from fish_annotation_search;

update phenotype_figure_group
 set pfigg_geno_handle = (Select distinct fa_geno_handle
     		       	       from functional_annotation
			       where fa_genox_zdb_id = pfigg_genox_zdb_id);

update term_group
 set tg_geno_handle = (Select distinct fa_geno_handle
     		       	       from functional_annotation
			       where fa_genox_zdb_id = tg_genox_group);

insert into fish_annotation_search (fas_geno_name, 
       	    			   	fas_geno_handle,
       	    			   	fas_feature_group, 
					fas_gene_group, 
					fas_morpholino_group, 
					fas_construct_group, 
					fas_fish_parts_count, 
					fas_affector_type_group,
					fas_feature_order,
					fas_gene_order,
					fas_gene_count)
select distinct fa_geno_name, 
       		fa_geno_handle,
       		fa_feature_group, 
		fa_gene_group, 
		fa_morpholino_group, 
		fa_construct_group, 
		fa_fish_parts_count, 
		fa_affector_type_group,
		fa_feature_order, 
		fa_gene_order, 
		fa_gene_count
  from functional_annotation;



update fish_annotation_search
  set fas_all = "sierra"
 where (fas_all is null
        or fas_all = '');


update fish_annotation_search
  set fas_all = fas_all||","||fas_feature_group
 where fas_feature_group is not null
 ;


update fish_annotation_search
  set fas_all = fas_all||","||fas_morpholino_group
 where fas_morpholino_group is not null;



update fish_annotation_search
  set fas_all = fas_all||","||fas_gene_group
 where fas_gene_group is not null;



update fish_annotation_search
  set fas_all = fas_all||","||fas_construct_group
 where fas_construct_group is not null;



update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_gene_alias from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_gene_alt_alias from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alt_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_gene_alt_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_feature_alias from functional_annotation where fa_geno_handle = fas_geno_handle and fa_feature_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_feature_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_morph_alias from functional_annotation where fa_geno_name = fas_geno_name and fa_morph_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_morph_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_geno_alias from functional_annotation where fa_geno_name = fas_geno_name and fa_geno_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_geno_alias is not null);

update fish_annotation_search
  set fas_all = fas_all||","||(select distinct fa_construct_alias from functional_annotation where fa_geno_name = fas_geno_name and fa_construct_alias is not null)
 where exists (Select 'x' from functional_annotation where fa_geno_handle = fas_geno_handle and fa_construct_alias is not null);

update fish_annotation_search
  set fas_all = replace(fas_all,'sierra,','');

update fish_annotation_search
  set fas_all = replace(fas_all,'sierra','');

select count(*) from fish_annotation_search
 where fas_all is null;

!echo "should be zero";

select count(*) as counter, fas_geno_handle as geno_handle
 from fish_annotation_search
 group by fas_geno_handle
 having count(*) > 1
into temp tmp_dups;


select first 6 *
from fish_annotation_search, tmp_dups
 where fas_geno_handle = geno_handle
 order by fas_geno_handle;

delete from fish_Annotation_search
 where fas_geno_handle in (Select geno_handle from tmp_dups);

update statistics high for table fish_annotation_search;
update statistics high for table phenotype_figure_group;
update statistics high for table phenotype_figure_group_member;
update statistics high for table term_group;
update statistics high for table term_group_member;


--set explain on avoid_execute ;

update fish_annotation_Search
  set fas_pheno_term_group = replace(replace(replace(substr(multiset (select distinct item tgm_member_id
      		      						   from term_group,
								   term_group_member
								   where tgm_group_id = tg_group_pk_id
								   and tg_geno_handle = fas_geno_handle
							  )::lvarchar(1000),11),""),"'}",""),"'","");

--update fish_annotation_search
--  set fas_all = fas_all||","||fas_pheno_term_group
--  where fas_pheno_term_group is not null
-- and fas_all is not null;

update fish_annotation_Search
  set fas_pheno_figure_group = replace(replace(replace(substr(multiset (select distinct item pfiggm_member_id
      		      						   from phenotype_figure_group,
								   phenotype_figure_group_member
								   where pfiggm_group_id = pfigg_group_pk_id
								   and pfigg_geno_handle = fas_geno_handle
							  )::lvarchar(1000),11),""),"'}",""),"'","");

set explain on avoid_execute;
update fish_annotation_Search
  set fas_xpat_figure_group = replace(replace(replace(substr(multiset (select distinct item xfiggm_member_id
      		      						   from xpat_figure_group,
								   xpat_figure_group_member
								   where xfiggm_group_id = xfigg_group_pk_id
								   and xfigg_geno_handle = fas_geno_handle
							  )::lvarchar(4000),11),""),"'}",""),"'","")
;
set explain off;

update fish_annotation_search
  set fas_pheno_figure_count = (Select count(distinct pfiggm_member_id)
      			       	       from phenotype_figure_group, phenotype_Figure_group_member
				       where pfiggm_group_id = pfigg_group_pk_id
				       and pfigg_geno_handle = fas_geno_handle);


--update fish_annotation_search
--  set fas_xpat_figure_count = (Select count(distinct xfiggm_member_id)
--      			       	       from xpat_figure_group, xpat_Figure_group_member
--				       where xfiggm_group_id = xfigg_group_pk_id
--				       and xfigg_geno_handle = fas_geno_handle);

update statistics high for table fish_Annotation_search;

select distinct fa_genox_zdb_id, fa_geno_handle
from functional_annotation
where fa_genox_zdb_id is not null
 order by fa_genox_zdb_id
into temp tmp_genox;

create index fa_geno_handle_tmp_index
 on tmp_genox (fa_geno_handle)
  using btree in idxdbs3;

update fish_annotation_search
  set fas_genox_group = replace(replace(replace(substr(multiset (select distinct item fa_genox_zdb_id
      		      						   from tmp_genox
								   where fa_geno_handle = fas_geno_handle
								   order by fa_genox_zdb_id
							  )::lvarchar(1000),11),""),"'}",""),"'","");

update fish_annotation_Search
  set fas_genotype_group = replace(replace(replace(substr(multiset (select item geno_Zdb_id
      		      						   from genotype
								   where geno_handle = fas_geno_handle
								   
							  )::lvarchar(1000),11),""),"'}",""),"'","");

update fish_annotation_Search
  set fas_genotype_group = replace(replace(replace(substr(multiset (select distinct item genox_geno_Zdb_id
      		      						   from functional_annotation, genotype_Experiment
								   where fa_geno_handle = fas_geno_handle
								   and fa_genox_zdb_id = genox_zdb_id
								   
							  )::lvarchar(1000),11),""),"'}",""),"'","")

 where fas_genotype_group is null;

insert into genotype_group (gg_geno_name)
 select distinct geno_display_name from genotype;

update genotype_group
  set gg_group_name = replace(replace(replace(substr(multiset (select item geno_Zdb_id
      		      						   from genotype
								   where geno_handle = gg_geno_handle 
							  )::lvarchar(1000),11),""),"'}",""),"'","");

insert into genotype_group_member (ggm_group_id, ggm_member_name, ggm_member_id)
  select gg_group_pk_id, geno_Display_name, geno_Zdb_id
   from genotype_Group, genotype
   where geno_handle = gg_geno_handle;

select max(octet_length(fa_geno_handle))
  from functional_annotation;

!echo "max length for fas_all";

select max(octet_length(fas_all))
 from fish_annotation_search;

!echo "max length for fa_all" ;

select max(octet_length(fa_all))
 from functional_annotation;

update fish_annotation_Search
  set fas_all_with_spaces = fas_all;

-- because JDBC won't escape a :, replace with a $
update fish_annotation_search 
 set fas_all = replace(fas_all,':','$');

update fish_annotation_search 
 set fas_all = replace(fas_all,'|',' ');

update fish_annotation_search 
  set fas_all = replace(fas_all,',',' ');


update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'\',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'+',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'-',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,':',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'(',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,')',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'[',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,']',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'?',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'~',' ');

update fish_annotation_search
set fas_all_with_spaces = replace(fas_all_with_spaces,'.',' ');

update fish_annotation_search 
 set fas_all_with_spaces = replace(fas_all_with_spaces,'|',' ');

update fish_annotation_search 
  set fas_all_with_spaces = replace(fas_all_with_spaces,',',' ');

update fish_annotation_Search
  set fas_all = fas_all||" "||fas_all_with_spaces
  where fas_all is not null
and fas_all_with_spaces is not null;

update fish_annotation_search 
  set fas_pheno_term_group = replace(fas_pheno_term_group,","," ");

update fish_annotation_search
  set fas_feature_order = 'zzzzzzzzzzzzzzz'
 where faS_feature_order is null;

update fish_annotation_search
  set fas_gene_order = 'zzzzzzzzzzzzzzz'
 where faS_gene_order is null;


update fish_annotation_search
  set fas_affector_group = fas_feature_group
 where fas_feature_group is not null;

update fish_annotation_search
  set fas_affector_group = fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_group is null
 and fas_affector_group is null;

update fish_annotation_search
  set fas_affector_group = fas_affector_Group||","||fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_group is not null;


update fish_annotation_search
  set fas_affector_order = fas_feature_order
 where fas_feature_order is not null;

update fish_annotation_search
  set fas_affector_order = fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_order is null
 and fas_affector_order is null;

update fish_annotation_search
  set fas_affector_order = fas_affector_order||", zzzzzzzzzzzz, "||fas_morpholino_group
 where fas_morpholino_group is not null
 and fas_feature_order is not null;

update fish_annotation_search
  set fas_affector_order = 'zzzzzzzzzzzzz'
 where faS_affector_order is null;

update fish_annotation_Search
 set fas_affector_Type_group = fas_affector_Type_group||", morpholino"
 where fas_morpholino_group is not null
and fas_affector_type_group is not null;

update fish_annotation_Search
 set fas_affector_Type_group = "morpholino"
 where fas_morpholino_group is not null
and fas_feature_group is null;

update fish_annotation_search
  set fas_all = lower(fas_all)
 where fas_all is not null;

update fish_annotation_search
  set fas_pheno_term_group = lower(fas_pheno_term_group);

update fish_annotation_search
  set fas_affector_type_group = lower(fas_affector_type_group);


delete from gene_feature_result_view;

delete from functional_annotation
 where fa_geno_name like 'ZDB-GENO%';

delete from fish_Annotation_search
 where fas_geno_name like 'ZDB-GENO%';

delete from fish_annotation_search
 where fas_gene_group is null
 and fas_morpholino_group is null
 and fas_feature_group is null
 and fas_construct_group is null;

delete from functional_annotation
 where fa_gene_group is null
 and fa_morpholino_group is null
 and fa_feature_group is null
 and fa_construct_group is null;

set pdqpriority 30;

update statistics high for table morpholino_group_member;
update statistics high for table affected_gene_group_member;
update statistics high for table feature_group_member;
update statistics high for table construct_group_member;

insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type)
 where fa_geno_zdb_id = fg_geno_zdb_id
 and fa_genox_zdb_id is null
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and feature_Type not in ('TRANSGENIC_INSERTION','TRANSGENIC_UNSPECIFIED')
 and fas_geno_handle = fa_geno_handle;


select * from gene_feature_result_view
  where gfrv_affector_abbrev = 'b250';

insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type)
 where fa_genox_zdb_id = fg_genox_zdb_id
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and feature_Type not in ('TRANSGENIC_INSERTION','TRANSGENIC_UNSPECIFIED') 
 and fas_geno_handle = fa_geno_handle;



insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order,
       b.mrkr_zdb_id,
       b.mrkr_name,
       b.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type),
      outer (feature_marker_relationship d, outer marker b)     
 where fa_genox_zdb_id = fg_genox_zdb_id
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and d.fmrel_ftr_zdb_id = feature_zdb_id
 and d.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
 and d.fmrel_type like 'contains%'
 and fas_geno_handle = fa_geno_handle;

insert into gene_feature_result_view (gfrv_fa_id,
       gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order,
       b.mrkr_zdb_id,
       b.mrkr_name,
       b.mrkr_abbrev_order
 from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type,
      outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type),
      outer (feature_marker_relationship d, outer marker b)     
 where fa_geno_zdb_id = fg_geno_zdb_id
 and fa_genox_zdb_id is null
  and fgm_group_id = fg_group_pk_id
   and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and c.fmrel_ftr_zdb_id = feature_zdb_id
 and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 and c.fmrel_type = fmreltype_name
 and fmreltype_produces_affected_marker = 't'
 and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 and d.fmrel_ftr_zdb_id = feature_zdb_id
 and d.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
 and d.fmrel_type like 'contains%'
 and fas_geno_handle = fa_geno_handle;


----MORPHS------
insert into gene_feature_result_view (gfrv_fa_id,
gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
       gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       morphgm_member_id,
       c.mrkr_abbrev,
       c.mrkr_abbrev_order,
       'Morpholino',
       a.mrkr_zdb_id,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order    
  from functional_annotation,
       fish_annotation_search,
      morpholino_Group_member,
      morpholino_group,
      marker c,
      marker_relationship, marker a
  where fa_genox_zdb_id = morphg_genox_zdb_id
  and morphgm_group_id = morphg_group_pk_id
 and fa_morpholino_group is not null
 and morphgm_member_id = c.mrkr_zdb_id
 and c.mrkr_zdb_id = mrel_mrkr_1_zdb_id
 and a.mrkr_Zdb_id = mrel_mrkr_2_zdb_id
and fas_geno_handle = fa_geno_handle;


insert into gene_feature_result_view (gfrv_fa_id,
gfrv_fas_id,
       gfrv_geno_handle,
       gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display)
select distinct fa_pk_id,
       fas_pk_id,
       fa_geno_handle,
       fgm_member_id,
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display
  from functional_annotation,
       fish_annotation_search,
      feature_group,
      feature_group_member,
      feature,
      feature_type
  where fa_geno_zdb_id = fg_geno_zdb_id
  and fgm_group_id = fg_group_pk_id
 and fa_feature_group is not null
 and fgm_member_id = feature_zdb_id
 and feature_type = ftrtype_name
 and exists (Select 'x' from feature_marker_Relationship where fmrel_ftr_zdb_id = fgm_member_id and get_obj_type(fmrel_mrkr_zdb_id)='SSLP')
 and not exists (Select 'x' from feature_marker_Relationship where fmrel_ftr_zdb_id = fgm_member_id and get_obj_type(fmrel_mrkr_zdb_id)='GENE')
and fas_geno_handle = fa_geno_handle
;


update gene_feature_Result_view
set gfrv_gene_abbrev = null
 where gfrv_gene_zdb_id not like 'ZDB-GENE%';

update gene_feature_Result_view
set gfrv_gene_abbrev_order = null
 where gfrv_gene_zdb_id not like 'ZDB-GENE%';

update gene_feature_Result_view
set gfrv_gene_zdb_id = null
 where gfrv_gene_zdb_id not like 'ZDB-GENE%';

select a.gfrv_pk_id as id 
       from gene_feature_result_View b, gene_feature_result_view a
  	       	       where b.gfrv_geno_handle = a.gfrv_geno_handle
		       and b.gfrv_gene_zdb_id is not null
		       and a.gfrv_gene_zdb_id is null
		       and b.gfrv_affector_id = a.gfrv_affector_id
		       and a.gfrv_pk_id != b.gfrv_pk_id
		       and a.gfrv_construct_name is null
		       and b.gfrv_construct_name is null	          
into temp tmp_deletes;

create index id_index on tmp_deletes (id)
 using btree in idxdbs3;

delete from gene_feature_result_view
 where exists (Select 'x' from tmp_deletes where id = gfrv_pk_id);

select distinct gfrv_fas_id, gfrv_geno_handle, gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order
 from gene_feature_result_view
into temp tmp_gfrv;



delete from gene_feature_result_view;

insert into gene_feature_result_view (gfrv_fas_id, gfrv_geno_handle, gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order)
  select gfrv_fas_id, gfrv_geno_handle, gfrv_affector_id,
       gfrv_affector_abbrev,
       gfrv_affector_abbrev_order,
       gfrv_affector_type_display,
gfrv_gene_zdb_id,
       gfrv_gene_abbrev,
       gfrv_gene_abbrev_order,
       gfrv_construct_zdb_id,
       gfrv_construct_name,
       gfrv_construct_abbrev_order
    from tmp_gfrv;


select * from gene_feature_result_view
where gfrv_geno_handle = 'hi459Tg[2,1,1]';

select * from gene_feature_result_view
where gfrv_geno_handle ='sb15[U,U,U] t24412[2,1,1]TU';

select * from gene_feature_result_view
where gfrv_geno_handle ='p0[U,U,U] tm110b[2,2,U]';

select * from gene_feature_result_view
where gfrv_affector_abbrev ='b250';


!echo "records in fish_annotation_search not in gene_feature_result_view";
select * from fish_annotation_search
 where fas_pk_id not in (Select gfrv_fas_id from gene_feature_Result_view);

select * from gene_feature_result_view
 where gfrv_geno_handle = 'Df(LG03)c1033/c1033 (AB)';

!echo 'leftovers missing 1';
select count(*) from fish_annotation_search, gene_feature_result_view
where
fas_pk_id = gfrv_fas_id
and fas_geno_handle like '%[%[%'
and (Select count(*) from gene_feature_result_view a
                where a.gfrv_fas_id = fas_pk_id) < 2;

!echo 'leftovers missing 2';
select count(*) from fish_annotation_search, gene_feature_result_view
where
fas_pk_id = gfrv_fas_id
and fas_geno_handle like '%[%[%[%'
and (Select count(*) from gene_feature_result_view a
                where a.gfrv_fas_id = fas_pk_id) < 3;

!echo 'leftovers missing 3';
select count(*) from fish_annotation_search, gene_feature_result_view
where
fas_pk_id = gfrv_fas_id
and fas_geno_handle like '%[%[%[%['
and (Select count(*) from gene_feature_result_view a
                where a.gfrv_fas_id = fas_pk_id) < 4;


delete from figure_term_fish_Search;

select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id, alltermcon_container_zdb_id as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
  and phenos_entity_1_superterm_zdb_id is not null
 union
select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id, alltermcon_container_zdb_id as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
  and phenos_entity_1_subterm_zdb_id is not null
 union
select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id, alltermcon_container_zdb_id as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
  and phenos_entity_2_superterm_zdb_id is not null
 union
select distinct geno_handle, phenox_genox_zdb_id, phenox_fig_zdb_id,alltermcon_container_zdb_id  as term
  from phenotype_experiment, phenotype_statement, genotype_Experiment, genotype, all_term_contains
  where phenox_pk_id = phenos_phenox_pk_id
  and genox_geno_zdb_id = geno_Zdb_id
  and genox_zdb_id = phenox_genox_zdb_id
and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
  and phenos_entity_2_subterm_zdb_id is not null
into temp tmp_phenox;

create index geno_name_index
  on tmp_phenox(geno_handle)
 using btree in idxdbs1;

create index genox_index
  on tmp_phenox(phenox_genox_zdb_id)
 using btree in idxdbs3;



create index fig_index
  on tmp_phenox(phenox_fig_zdb_id)
 using btree in idxdbs2;
update statistics high for table tmp_phenox;

insert into figure_term_fish_search (ftfs_fas_id, ftfs_geno_handle, ftfs_fig_zdb_id, ftfs_genox_zdb_id)
 select distinct  fas_pk_id, fas_geno_handle, phenox_fig_Zdb_id, phenox_genox_zdb_id
    from fish_annotation_Search, phenotype_Experiment, functional_annotation
    where fas_geno_handle = fa_geno_handle
    and phenox_genox_zdb_id = fa_genox_zdb_id;


update statistics high for table figure_term_fish_Search;


--set explain on avoid_execute;
update figure_term_fish_search
  set ftfs_term_group = replace(replace(replace(substr(multiset (select distinct item term 
      		      						   from tmp_phenox, functional_annotation
								   where ftfs_geno_handle = fa_geno_handle
								   and fa_genox_zdb_id = phenox_genox_zdb_id
								   and ftfs_fig_zdb_id = phenox_fig_zdb_id
							  )::lvarchar(3000),11),""),"'}",""),"'","");

update figure_term_fish_search set ftfs_term_group = replace(ftfs_term_group,","," ");


update figure_term_fish_search
 set ftfs_term_group = lower(ftfs_term_group);


update statistics high for table gene_feature_result_view;
update statistics high for table fish_Annotation_Search;

update fish_annotation_Search
  set fas_fish_significance = 0;

update gene_feature_result_View
 set gfrv_affector_Type_display = 'Transgenic Insertion, non-allelic' 
 where not exists (Select 'x' from feature_marker_relationship
       	      	      	  where fmrel_ftr_Zdb_id = gfrv_affector_id
			  and fmrel_type = 'is allele of')
 and gfrv_affector_type_display = 'Transgenic Insertion';

--set explain on avoid_execute;

update fish_Annotation_Search
  set fas_fish_significance = (Select sum(fto_priority)
      			      	      from feature_Type_ordering, gene_feature_result_View
				      where fto_name = gfrv_affector_type_display
				       and fas_pk_id = gfrv_fas_id
				       group by gfrv_fas_id);

update gene_feature_result_view
  set gfrv_affector_type_display = 'Transgenic Insertion'
  where gfrv_affector_type_display = 'Transgenic Insertion, non-allelic' ;


update gene_feature_result_view
  set gfrv_affector_type_display = 'Transgenic Insertion'
  where gfrv_affector_type_display = 'Unspecified Transgenic Insertion' ;

update fish_annotation_search
  set fas_affector_type_group = fas_affector_type_group||", transgenic_insertion"
 where fas_affector_type_Group like '%transgenic_unspecified%';
  

update fish_annotation_search
  set fas_affector_type_group = replace(fas_affector_type_group,"_","");
  

select fas_pk_id, fas_geno_name 
  from fish_annotation_search
where exists (Select 'x' from genotype_background where genoback_geno_zdb_id = fas_genotype_Group)
  and fas_genotype_group is not null 
into temp tmp_fas;

create index fas_pk_id_tmp_index
  on tmp_fas(fas_pk_id) using  btree in idxdbs1;

update fish_annotation_search 
  set fas_geno_name = case when get_genotype_backgrounds_warehouse(fas_genotype_group) = '' 
      		      then fas_geno_name
		      else 
      		      	   fas_geno_name||" ("||get_genotype_backgrounds_warehouse(fas_genotype_group)||")"
		      end
 where exists (Select 'x' from tmp_fas where tmp_fas.fas_pk_id = fish_annotation_Search.fas_pk_id)
  and fas_genotype_group is not null;

update fish_annotation_Search
set fas_gene_order = 'zzzzzzzzzzzzzzzzzz'
 where fas_gene_order is null;

update fish_annotation_Search
set fas_feature_order = 'zzzzzzzzzzzzzzzzzz'
 where fas_feature_order is null;

update fish_annotation_Search
 set fas_fish_significance = '999999'
 where fas_Fish_significance = '0';

update fish_annotation_Search
 set fas_gene_count = '999999'
 where fas_gene_count = '0';

update fish_annotation_Search
 set fas_fish_parts_count = '999999'
 where fas_fish_parts_count = '0';

update fish_annotation_search set fas_fish_significance = '999999' where fas_fish_significance is null;

!echo 'delete wildtype lines should not be more than 42';
delete from fish_annotation_Search
 where fas_feature_group is null
 and fas_gene_group is null
and fas_morpholino_group is null
    and fas_construct_group is null
    ;
create index fish_annotation_search_fas_all_bts_index
  on fish_annotation_search (fas_pheno_term_group bts_lvarchar_ops,
			     fas_all bts_lvarchar_ops,
			     fas_affector_type_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs1;


create index figure_term_fish_search_term_group_bts_index
  on figure_term_fish_search (ftfs_term_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs1;


commit work; 