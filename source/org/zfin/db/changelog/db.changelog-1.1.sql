--liquibase formatted sql

--preconditions onFail:HALT onError:HALT

--changeset staylor:xpatRefactorNewXpatTables

create table expression_experiment2
  ( xpatex_pk_id serial8 not null constraint xpatx2_pk_id_not_null,
    xpatex_zdb_id varchar(50) not null constraint xpatx2_zdb_id_not_null,
    xpatex_fig_zdb_id varchar(50) not null constraint xpatx2_fig_zdb_id_not_null,
    xpatex_assay_name varchar(40) not null constraint xpatx2_assay_name_not_null,
    xpatex_probe_feature_zdb_id varchar(50),
    xpatex_gene_zdb_id varchar(50),
    xpatex_direct_submission_date datetime year to day
        default current year to day not null constraint xpatx2_direct_submission_date_is_not_null,
    xpatex_dblink_zdb_id varchar(50),
    xpatex_genox_zdb_id varchar(50),
    xpatex_atb_zdb_id varchar(50),

    check ((xpatex_gene_zdb_id IS NOT NULL ) OR (xpatex_atb_zdb_id IS NOT NULL )
              ) constraint expression_experiment2_gene_or_antibody_must_exist
  )
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
  extent size 4096 next size 4096 lock mode row;
;


create table expression_result2
  ( xpatres_pk_id serial8 not null constraint expression_result2_pk_id_not_null,
   xpatres_zdb_id varchar(50) not null constraint expression_result2_zdb_id_not_null,
    xpatres_xpatex_id int8 not null constraint expression_result2_xpatex_id_not_null,
    xpatres_xpatex_zdb_id varchar(50) not null constraint expression_result2_xpatex_zdb_id_not_null,
    xpatres_start_stg_zdb_id varchar(50) not null constraint expression_result2start_zdb_id_not_null,
    xpatres_end_stg_zdb_id varchar(50) not null constraint expression_result2_end_zdb_id_not_null,
    xpatres_expression_found boolean
        default 't' not null constraint expression_result2_found_not_null,
    xpatres_comments varchar(255),
    xpatres_superterm_zdb_id varchar(50) not null constraint expression_result2_superterm_zdb_id_not_null,
    xpatres_subterm_zdb_id varchar(50),
    xpatres_pato_term_zdb_id varchar(50)
  )
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
  extent size 4096 next size 4096 lock mode row;
;


insert into expression_experiment2 (xpatex_zdb_id,
       	    			   xpatex_fig_zdb_id, xpatex_assay_name,
 	xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id,
	xpatex_direct_submission_date, xpatex_dblink_zdb_id,
	xpatex_genox_zdb_id, xpatex_atb_zdb_id)
 select distinct xpatex_zdb_id,
 	xpatfig_fig_zdb_id, xpatex_assay_name,
 	xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id,
	xpatex_direct_submission_date, xpatex_dblink_zdb_id,
	xpatex_genox_zdb_id, xpatex_atb_zdb_id
   from expression_Experiment, expression_result, expression_pattern_figure
   where xpatex_zdb_id = xpatres_xpatex_zdb_id
   and xpatres_zdb_id = xpatfig_xpatres_Zdb_id;


insert into expression_result2 (xpatres_zdb_id,xpatres_xpatex_id, xpatres_xpatex_zdb_id, xpatres_start_stg_zdb_id,
       	    		       	xpatres_end_stg_zdb_id, xpatres_expression_found,
				xpatres_comments,
				xpatres_superterm_zdb_id, xpatres_subterm_zdb_id)
select xpatres_zdb_id,xpatex_pk_id, xpatres_xpatex_zdb_id, xpatres_start_stg_zdb_id,
       	    		       	xpatres_end_stg_zdb_id, xpatres_expression_found,
				xpatres_comments,
				xpatres_superterm_zdb_id, xpatres_subterm_zdb_id
  from expression_Result,expression_pattern_figure,expression_experiment2
  where xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and xpatres_xpatex_zdb_id = xpatex_zdb_id
  and xpatfig_fig_zdb_id = xpatex_fig_zdb_id;

--changeset staylor:xpatRefactorNewXpatTableConstraints



create unique index expression_experiment_alternate_ky_index
    on expression_experiment2 (xpatex_fig_zdb_id,
    xpatex_genox_zdb_id,xpatex_assay_name,xpatex_probe_feature_zdb_id,
    xpatex_gene_zdb_id,xpatex_dblink_zdb_id,xpatex_atb_zdb_id)
    using btree in idxdbs2;
create index expression_experiment_assay_name_foreign_ky_index
    on expression_experiment2 (xpatex_assay_name) using
    btree in idxdbs1;
create index expression_experiment_marker_foreign_ky_index
    on expression_experiment2 (xpatex_probe_feature_zdb_id)
    using btree in idxdbs2;
create unique index expression_experiment_primary_ky_index
    on expression_experiment2 (xpatex_pk_id) using
    btree in idxdbs2;
create index expression_experiment_source_foreign_ky_index
    on expression_experiment2 (xpatex_fig_zdb_id)
    using btree in idxdbs1;
create index expression_experiment_dblink_foreign_ky
    on expression_experiment2 (xpatex_dblink_zdb_id)
    using btree in idxdbs3;
create index expression_experiment_atb_zdb_id_index on expression_experiment2
    (xpatex_atb_zdb_id) using btree in idxdbs3;
create index xpatex_featexp_zdb_id_foreign_ky_index
    on expression_experiment2 (xpatex_genox_zdb_id)
    using btree in idxdbs3 ;
create index xpatex_gene_zdb_id_foreign_ky_index
    on expression_experiment2 (xpatex_gene_zdb_id) using
    btree in idxdbs3 ;
alter table expression_experiment2 add constraint unique
    (xpatex_fig_zdb_id,xpatex_genox_zdb_id,xpatex_assay_name,
    xpatex_probe_feature_zdb_id,xpatex_gene_zdb_id,xpatex_dblink_zdb_id,
    xpatex_atb_zdb_id) constraint xpatex_alternate_key
     ;
alter table expression_experiment2 add constraint primary
    key (xpatex_pk_id) constraint xpatex_primary_key
     ;


alter table expression_experiment2 add constraint (foreign
    key (xpatex_gene_zdb_id) references marker  constraint
    expression_experiment_gene_zdb_id_foreign_key);
alter table expression_experiment2 add constraint (foreign
    key (xpatex_probe_feature_zdb_id) references marker
     on delete cascade constraint xpatex_probe_feature_foreign_key);


alter table expression_experiment2 add constraint (foreign
    key (xpatex_atb_zdb_id) references antibody  on
    delete cascade constraint expression_experiment_atb_zdb_id_foreign_key);

alter table expression_experiment2 add constraint (foreign
    key (xpatex_assay_name) references expression_pattern_assay
     constraint expression_experiment_assay_foregin_key);
alter table expression_experiment2 add constraint (foreign
    key (xpatex_genox_zdb_id) references fish_experiment
     constraint expression_experiment_genox_foreign_key);
alter table expression_experiment2 add constraint (foreign
    key (xpatex_dblink_zdb_id) references db_link
    constraint expression_experiment_dblink_foreign_key);

alter table expression_experiment2 add constraint (foreign
    key (xpatex_fig_zdb_id) references figure
     on delete cascade constraint expression_experiment_fig_foreign_key);


create unique index xpatres_alternate_key_index
    on expression_result2 (xpatres_xpatex_id,xpatres_superterm_zdb_id,
    xpatres_start_stg_zdb_id,xpatres_end_stg_zdb_id,xpatres_expression_found,
    xpatres_subterm_zdb_id) using btree in idxdbs2;
create index xpatres_end_stg_foreign_key_index
    on expression_result2 (xpatres_end_stg_zdb_id) using
    btree in idxdbs3 ;
create unique index xpatres_primary_key_index
    on expression_result2 (xpatres_pk_id) using btree
    in idxdbs3 ;
create index xpatres_start_stg_foreign_key_index
    on expression_result2 (xpatres_start_stg_zdb_id)
    using btree  in idxdbs2 ;
create index xpatres_subterm_foreign_key_index
    on expression_result2 (xpatres_subterm_zdb_id) using
    btree in idxdbs3 ;
create index xpatres_superterm_foreign_key_index
    on expression_result2 (xpatres_superterm_zdb_id)
    using btree in idxdbs3 ;
create index xpatres_xpatex_foreign_key_index
    on expression_result2 (xpatres_xpatex_id) using
    btree  in idxdbs2;
alter table expression_result2 add constraint unique
    (xpatres_xpatex_id,xpatres_superterm_zdb_id,xpatres_start_stg_zdb_id,
    xpatres_end_stg_zdb_id,xpatres_expression_found,xpatres_subterm_zdb_id)
    constraint xpatres_alternate_key  ;
alter table expression_result2 add constraint primary
    key (xpatres_pk_id) constraint xpatres_primary_key
     ;


alter table expression_result2 add constraint (foreign
    key (xpatres_superterm_zdb_id) references term
     constraint xpatres_term_foreign_key);

alter table expression_result2 add constraint (foreign
    key (xpatres_start_stg_zdb_id) references stage
     constraint xpatres_start_stg_foreign_key);

alter table expression_result2 add constraint (foreign
    key (xpatres_end_stg_zdb_id) references stage
    constraint xpatres_end_stg_foreign_key);

--alter table expression_result2 add constraint (foreign
--    key (xpatres_zdb_id) references zdb_active_data
--     on delete cascade constraint xpatres_zdb_active_data_foreign_key);

alter table expression_result2 add constraint (foreign
    key (xpatres_xpatex_id) references expression_experiment2
     on delete cascade constraint xpatres_xpatex_foreign_key);

alter table expression_result2 add constraint (foreign
    key (xpatres_subterm_zdb_id) references term  constraint
    expression_result_subterm_zdb_id_foreign_key);

--changeset staylor:xpatRefactorNewXpatTableTriggers


create trigger xpatex_update_trigger
    update of xpatex_fig_zdb_id on expression_experiment2
    referencing new as new_xpatex
    for each row
        (
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_probe_feature_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_gene_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_atb_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_insert_into_record_attribution_tablezdbids(new_xpatex.xpatex_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_check_efg_wt_expression(new_xpatex.xpatex_genox_zdb_id
    ,new_xpatex.xpatex_gene_zdb_id ));


create trigger xpatex_insert_trigger
    insert on expression_experiment2 referencing new
    as new_xpatex
    for each row
        (
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_probe_feature_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_gene_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_atb_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_insert_into_record_attribution_tablezdbids(new_xpatex.xpatex_zdb_id
    ,new_xpatex.xpatex_fig_zdb_id ),
        execute procedure p_check_efg_wt_expression(new_xpatex.xpatex_genox_zdb_id
    ,new_xpatex.xpatex_gene_zdb_id ));


create trigger expression_result_insert_trigger insert
    on expression_result2 referencing new as new_xpatres

    for each row
        (
        execute procedure p_stg_hours_consistent(new_xpatres.xpatres_start_stg_zdb_id
    ,new_xpatres.xpatres_end_stg_zdb_id ),
        execute function scrub_char(new_xpatres.xpatres_comments
    ) into expression_result2.xpatres_comments,
        execute procedure p_check_fx_postcomposed_terms(new_xpatres.xpatres_superterm_zdb_id
    ,new_xpatres.xpatres_subterm_zdb_id ),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatres.xpatres_superterm_zdb_id
    ),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatres.xpatres_subterm_zdb_id
    ));

create trigger expression_result_update_trigger update
    of xpatres_start_stg_zdb_id,xpatres_end_stg_zdb_id,xpatres_superterm_zdb_id,
    xpatres_subterm_zdb_id on expression_result2 referencing
    new as new_xpatres
    for each row
        (
        execute procedure p_stg_hours_consistent(new_xpatres.xpatres_start_stg_zdb_id
    ,new_xpatres.xpatres_end_stg_zdb_id ),
        execute function scrub_char(new_xpatres.xpatres_comments
    ) into expression_result2.xpatres_comments,
        execute procedure p_check_fx_postcomposed_terms(new_xpatres.xpatres_superterm_zdb_id
    ,new_xpatres.xpatres_subterm_zdb_id ),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatres.xpatres_superterm_zdb_id
    ),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatres.xpatres_subterm_zdb_id
    ));

UPDATE DATABASECHANGELOG SET TAG = 'xpatRefactor' WHERE DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM (SELECT DATEEXECUTED FROM DATABASECHANGELOG) AS X);

