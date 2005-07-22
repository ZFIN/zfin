begin work ;

set constraints all deferred ;

alter table fish_image_stage
  drop constraint fimgstg_fimg_zdb_id_foregin_key ;

drop table fish_image;
drop table expression_pattern;
drop table expression_pattern_stage;
drop table expression_pattern_anatomy;
drop table expression_pattern_image;

rename table fx_expression_experiment
  to expression_experiment ;

drop trigger FX_expression_experiment_insert_trigger ;
drop trigger FX_expression_experiment_update_trigger ;

create trigger expression_experiment_insert_trigger insert on 
    expression_experiment referencing new as new_xpatex
    for each row
        (
	execute procedure p_insert_into_record_attribution_datazdbids (
			new_xpatex.xpatex_probe_feature_zdb_id,
			new_xpatex.xpatex_source_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids(
			new_xpatex.xpatex_gene_zdb_id,
			new_xpatex.xpatex_source_zdb_id),
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_xpatex.xpatex_zdb_id,
                        new_xpatex.xpatex_source_zdb_id) 
     );	

create trigger expression_experiment_update_trigger update of 
    xpatex_source_zdb_id on expression_experiment
    referencing new as new_xpatex
    for each row
        (
	 execute procedure p_insert_into_record_attribution_datazdbids (
                        new_xpatex.xpatex_probe_feature_zdb_id,
                        new_xpatex.xpatex_source_zdb_id),
        execute procedure p_insert_into_record_attribution_datazdbids(
                        new_xpatex.xpatex_gene_zdb_id,
                        new_xpatex.xpatex_source_zdb_id),
        execute procedure p_insert_into_record_attribution_tablezdbids (
                        new_xpatex.xpatex_zdb_id,
                        new_xpatex.xpatex_source_zdb_id)

     );	


rename table fx_expression_result
  to expression_result ;

drop trigger FX_expression_result_insert_trigger ;
drop trigger FX_expression_result_update_trigger ;

create trigger expression_result_insert_trigger insert on 
    expression_result referencing new as new_xpatres
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_xpatres.xpatres_start_stg_zdb_id,
			new_xpatres.xpatres_end_stg_zdb_id ),
	execute function scrub_char ( new_xpatres.xpatres_comments )
		into xpatres_comments
         );

create trigger expression_result_update_trigger update of 
    xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id
    on expression_result
    referencing new as new_xpatres
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_xpatres.xpatres_start_stg_zdb_id,
			new_xpatres.xpatres_end_stg_zdb_id ),
	execute function scrub_char ( new_xpatres.xpatres_comments )
		into xpatres_comments
     ) ;

rename table fx_figure
  to figure ;

drop trigger FX_figure_insert_trigger ;
drop trigger FX_figure_update_trigger ;

create trigger figure_insert_trigger insert on figure
    referencing new as new_fig
    for each row
        (
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_fig.fig_zdb_id,
			new_fig.fig_source_zdb_id)
) ;

create trigger figure_update_trigger update of fig_source_zdb_id 
    on figure
    referencing new as new_fig
    for each row
        (
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_fig.fig_zdb_id,
			new_fig.fig_source_zdb_id)
) ;

rename table fx_expression_pattern_figure
  to expression_pattern_figure ;

create table fimg_wo_ps (fimg_zdb_id varchar(50),
    fimg_fig_zdb_id varchar(50),
    fimg_label varchar(200),
    fimg_comments lvarchar,
    fimg_annotation lvarchar,
    fimg_width integer not null constraint fimg_width_not_null,
    fimg_height integer not null constraint fimg_height_not_null,
    fimg_fish_zdb_id varchar(50),
    fimg_view varchar(20) not null constraint fimg_view_not_null,
    fimg_direction varchar(30) not null constraint fimg_direction_not_null,
    fimg_form varchar(15) not null constraint fimg_form_not_null,
    fimg_preparation varchar(15) not null constraint fimg_preparation_not_null,
    fimg_owner_zdb_id varchar(50) not null constraint fimg_owner_zdb_id_not_null,
    fimg_external_name varchar(100),
    fimg_bkup_thumb blob,
    fimg_bkup_img blob,
    fimg_bkup_annot blob,
    has_annot boolean,
    fimg_image varchar(150) not null constraint fimg_image_not_null,
    fimg_image_with_annotation varchar(150),
    fimg_thumbnail varchar(150) not null constraint fimg_thumbnail_not_null,
    has_image boolean
  ) 
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  PUT fimg_bkup_thumb 
    in (smartbs1, smartbs2, smartbs3, smartbs4)(log), fimg_bkup_img 
    in (smartbs1, smartbs2, smartbs3, smartbs4)(log), fimg_bkup_annot 
    in (smartbs1, smartbs2, smartbs3, smartbs4)(log) 
  extent size 4096 next size 4096 lock mode row;

create index fimg_direction_index on fimg_wo_ps 
    (fimg_direction) using btree  in idxdbs2 ;
create index fimg_form_index on fimg_wo_ps 
    (fimg_form) using btree  in idxdbs2 ;
create index fimg_owner_zdb_id_index on
    fimg_wo_ps (fimg_owner_zdb_id) using btree  
    in idxdbs2 ;
create index fimg_preparation_index on fimg_wo_ps 
    (fimg_preparation) using btree  in idxdbs2 ;
create index fimg_view_index on fimg_wo_ps 
    (fimg_view) using btree  in idxdbs2 ;
create index fish_image_figure_foreign_key_index 
    on fimg_wo_ps (fimg_fig_zdb_id) using 
    btree  in idxdbs2 ;
create unique index fish_image_primary_key_index 
    on fimg_wo_ps (fimg_zdb_id) using btree 
     in idxdbs2 ;
create index fish_image_fimg_image_index 
    on fimg_wo_ps (fimg_image) using btree 
     in idxdbs3 ;
alter table fimg_wo_ps add constraint primary 
    key (fimg_zdb_id) constraint fish_image_primary_key 
     ;
alter table fimg_wo_ps add constraint (foreign 
    key (fimg_fig_zdb_id) references figure  on 
    delete cascade constraint fish_image_fig_foreign_key);
    

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_fish_zdb_id) references fish  on delete 
    cascade constraint fimg_fish_zdb_id_foreign_key);
    

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_zdb_id) references zdb_active_data  
    on delete cascade constraint fimg_zdb_id_foreign_key);
    

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_view) references fish_image_view  constraint 
    fimg_view_foreign_key);

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_direction) references fish_image_direction 
     constraint fimg_direction_foreign_key);

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_form) references fish_image_form  constraint 
    fimg_form_foreign_key);

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_preparation) references fish_image_preparation 
     constraint fimg_preparation_foreign_key);

alter table fimg_wo_ps add constraint (foreign 
    key (fimg_owner_zdb_id) references person  constraint 
    fimg_owner_zdb_id_foreign_key);

insert into fimg_wo_ps
  select * from fx_fish_image_private ;

set constraints all immediate ;

rename table fimg_wo_ps
  to fish_image ;

set constraints all deferred ;

alter table fish_image_stage
  add constraint (foreign key (fimgstg_fimg_zdb_id)
  references fish_image on delete cascade 
  constraint fimgstg_fimg_zdb_id_foregin_key);

set constraints all immediate ;

alter table fish_image_anatomy
  add constraint (foreign key (fimganat_fimg_zdb_id)
  references fish_image on delete cascade constraint
  fimganat_fimg_zdb_id) ;

drop table fx_fish_image_private ;

set triggers for zdb_object_type disabled ;

update zdb_object_type
  set zobjtype_home_zdb_id_column = 'xpatex_zdb_id'
  where zobjtype_name = 'XPAT' ;

update zdb_object_type
  set zobjtype_home_table = 'expression_experiment'
  where zobjtype_name = 'XPAT' ;

update zdb_object_type
  set zobjtype_home_table = 'figure'
  where zobjtype_name = 'FIG' ;

update zdb_object_type
  set zobjtype_home_table = 'expression_result'
  where zobjtype_name = 'XPATRES' ;

set triggers for zdb_object_type enabled ;

update statistics high for table expression_experiment ;
update statistics high for table expression_result ;
update statistics high for table figure;
update statistics high for table fish_image ;
update statistics high for table experiment ;
update statistics high for table experiment_condition ;
update statistics high for table experiment_unit;
update statistics high for table feature_experiment ;
update statistics high for table condition_data_type;
update statistics high for table condition;

commit work ;
--rollback work ;
