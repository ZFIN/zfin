begin work ;

execute procedure set_session_params();

update fish_image
  set fimg_bkup_thumb = null
  where fimg_bkup_thumb is not null;


drop trigger fx_Expression_experiment_insert_trigger ;
drop trigger fx_Expression_experiment_update_trigger ;
drop trigger fx_Expression_result_insert_trigger ;
drop trigger fx_Expression_result_update_trigger ;
drop trigger fx_figure_insert_trigger ;
drop trigger fx_figure_update_trigger ;


create trigger FX_expression_experiment_insert_trigger insert on 
    FX_expression_experiment referencing new as new_xpatex
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

create trigger FX_expression_experiment_update_trigger update of 
    xpatex_source_zdb_id on FX_expression_experiment
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

create trigger FX_expression_result_insert_trigger insert on 
    FX_expression_result referencing new as new_xpatres
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_xpatres.xpatres_start_stg_zdb_id,
			new_xpatres.xpatres_end_stg_zdb_id ),
	execute function scrub_char ( new_xpatres.xpatres_comments )
		into xpatres_comments
         );

create trigger FX_expression_result_update_trigger update of 
    xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id
    on FX_expression_result
    referencing new as new_xpatres
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_xpatres.xpatres_start_stg_zdb_id,
			new_xpatres.xpatres_end_stg_zdb_id ),
	execute function scrub_char ( new_xpatres.xpatres_comments )
		into xpatres_comments
     ) ;

create trigger FX_figure_insert_trigger insert on fx_figure
    referencing new as new_fig
    for each row
        (
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_fig.fig_zdb_id,
			new_fig.fig_source_zdb_id)
) ;

create trigger FX_figure_update_trigger update of fig_source_zdb_id 
    on fx_figure
    referencing new as new_fig
    for each row
        (
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_fig.fig_zdb_id,
			new_fig.fig_source_zdb_id)
) ;

update statistics for procedure;
update statistics high for table fx_expression_experiment ;
update statistics high for table fx_Expression_result ;
update statistics high for table fx_figure ;

update fish_image
  set fimg_comments = null
  where fimg_comments like 'none given%';

update fish_image
  set fimg_external_name = fimg_comments
  where fimg_comments like 'm%' ;

update fish_image
  set fimg_comments = null
  where fimg_comments like 'm%' ;

alter table fx_expression_experiment
 modify (xpatex_direct_submission_date datetime year to second) ;

alter table fx_fish_image_private
  drop constraint fx_fish_image_private_alternate_key ;

drop index fish_image_private_alternate_key_index ;

set constraints all deferred ;

--load any missing fish to the feature-experiment table

insert into feature_experiment (featexp_zdb_id, featexp_exp_zdb_id,
	featexp_genome_feature_zdb_id)
  select get_id('FEATEXP'), 'ZDB-EXP-041102-1', xpat_stock_zdb_id
    from expression_pattern
    where xpat_stock_zdb_id not in (select featexp_genome_feature_zdb_id
					from feature_experiment) 
    group by xpat_Stock_zdb_id ;
   
insert into zdb_active_data 
  select featexp_zdb_id 
	from feature_experiment
   	where not exists (Select 'x'
				from zdb_active_data
				where zactvd_zdb_id = featexp_zdb_id);
set constraints all immediate ;

delete from expression_pattern
  where xpat_source_zdb_id = 'ZDB-PUB-010410-2';

delete from expression_pattern
  where xpat_source_zdb_id = 'ZDB-PUB-040819-2';


--!echo NEEDS TO BE ZERO

--select xpat_source_zdb_id from expression_pattern
--  where exists (select 'x'
--		  from fx_expression_experiment
--		  where xpatex_probe_feature_zdb_id = 
--			xpat_probe_zdb_id
--		and xpatex_gene_zdb_id = 
--			xpat_gene_zdb_id
--		and xpatex_source_zdb_id = 
-- 			xpat_source_zdb_id
--		and xpatex_assay_name = 
--			xpat_assay_name);

alter table fx_figure 
  modify (fig_label varchar(150) not null constraint
   fig_label_not_null) ;

alter table fish_image
  add (fimg_figure_zdb_id varchar(50) before fimg_image) ;

alter table fish_image
  add (fimg_label varchar(200) before fimg_image) ;

commit work ;
begin work ;
------------------------NEW_FIGURES------------------------------------
--each unique stage range/xpat_id that is associated with a photo,
--gets a figure record...the counter groups the xpatfimgs together
--so that we don't get duplicate xpat_id/stage-ranges as new figures.

update statistics high for table fx_figure;
update statistics high for table fish_image;

create temp table tmp_figs (counter int,
				fig_zdb_id varchar(50),
				fig_source_zdb_id varchar(50),
				xpat_id varchar(50), 
				xpat_start varchar(50), 
				xpat_end varchar(50),
				fig_caption lvarchar,
				fig_comments lvarchar,
				fig_label varchar(150),
				fig_probe_counter integer,
				xpat_source_zdb_id varchar(50))

with no log ;

create unique index fig_index 
  on tmp_figs (fig_zdb_id) ;

create unique index xpat_fig_index 
  on tmp_figs (xpat_id, xpat_start, xpat_end) ;

--clean up comments from not expression xpatstg_comments

update expression_pattern_stage
  set xpatstg_expression_found = 'f' 
  where xpatstg_comments = 'Not expressed.' ;

update expression_pattern_stage
  set xpatstg_expression_found = 'f' 
  where xpatstg_comments = 'no expression' ;

update expression_pattern_stage
  set xpatstg_expression_found = 'f' 
  where xpatstg_comments = 'no expression<br>' ;

update expression_pattern_stage
  set xpatstg_comments = null
  where xpatstg_comments = 'Not expressed.'  
  and xpatstg_expression_found = 'f';

update expression_pattern_stage
  set xpatstg_comments = null
  where xpatstg_comments = 'no expression'
  and xpatstg_expression_found = 'f';  

update expression_pattern_stage
  set xpatstg_comments = null
  where xpatstg_comments = 'no expression<br>'
  and xpatstg_expression_found = 'f';

update expression_pattern_stage
  set xpatstg_comments = null
  where xpatstg_comments = 'No comments.';

----
----"Thisse Figure has images"||" "||xpatstg_xpat_zdb_id||xpatstg_start_stg_zdb_id||xpatstg_end_stg_zdb_id

insert into tmp_figs (counter,fig_zdb_id,
			xpat_id, xpat_start, xpat_end, fig_probe_counter, 
			xpat_source_zdb_id)
  select count(*),get_id('FIG'),
	xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id, '0', xpat_source_zdb_id
    from expression_pattern_stage, expression_pattern
    where exists (select * from expression_pattern_image
			where xpatstg_xpat_zdb_id = xpatfimg_xpat_zdb_id
			and xpatstg_start_stg_zdb_id = 
				xpatfimg_xpat_start_stg_zdb_id
			and xpatstg_end_stg_zdb_id = 
				xpatfimg_xpat_end_stg_zdb_id)
    and xpatstg_xpat_zdb_id = xpat_zdb_id
    group by xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id, xpat_source_zdb_id;

update statistics high for table tmp_figs;

--!echo should be 0

--select count(*) from tmp_figs where xpat_source_zdb_id is null;

create temp table tmp_Fig_foo (fig_zdb_id varchar(50),
				xpat_id varchar(50),
				maxlabel int,
				stg_start varchar(50),
				stg_end varchar(50),
				xpat_pub varchar(50))
with no log ;

create index fig_foo_index 
  on tmp_fig_foo (fig_zdb_id) ;

create index fig_xpat_foo_index
  on tmp_fig_foo (xpat_id) ;

--label the figures with images

!echo executing proc to label figures

execute procedure get_max_labels_thisse() ;

--put back teh xpatstg_comments as caption

update tmp_figs
  set fig_caption = (select xpatstg_comments
			from expression_pattern_stage
   			where xpatstg_xpat_zdb_id = xpat_id
     			and xpatstg_start_stg_zdb_id = xpat_start
     			and xpatstg_end_stg_zdb_id = xpat_end);

update statistics high for table tmp_fig_foo ;

update tmp_figs
  set fig_label = (select maxlabel
			from tmp_fig_foo
			where tmp_fig_foo.fig_zdb_id = tmp_Figs.fig_zdb_id);


update statistics high for table tmp_figs ;


--"Text Only Display No Image Has Comments has anat"||" "||xpatstg_xpat_zdb_id||xpatstg_start_stg_zdb_id||xpatstg_end_stg_zdb_id,

insert into tmp_figs (counter, fig_zdb_id, fig_label,
			xpat_id, xpat_start, xpat_end)
  select count(*),get_id('FIG'), 
	"text only",
	xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id
    from expression_pattern_stage
    where not exists (select * from expression_pattern_image
			where xpatstg_xpat_zdb_id = xpatfimg_xpat_zdb_id
			and xpatstg_start_stg_zdb_id = 
				xpatfimg_xpat_start_stg_zdb_id
			and xpatstg_end_stg_zdb_id = 
				xpatfimg_xpat_end_stg_zdb_id)
     and exists (select * from expression_pattern_anatomy
			where xpatanat_xpat_zdb_id = xpatstg_xpat_zdb_id
			and xpatanat_xpat_start_stg_zdb_id = 
				xpatstg_start_stg_zdb_id
			and xpatanat_xpat_end_stg_zdb_id =
				xpatstg_end_stg_zdb_id)
     and xpatstg_comments is not null 
     group by xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id;


--"Text Only Display No Image No Comments has anat"||" "||xpatstg_xpat_zdb_id||xpatstg_start_stg_zdb_id||xpatstg_end_stg_zdb_id,

insert into tmp_figs (counter, fig_zdb_id, fig_label,
			xpat_id, xpat_start, xpat_end)
  select count(*),get_id('FIG'), 
	"text only",
	xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id
    from expression_pattern_stage
    where not exists (select * from expression_pattern_image
			where xpatstg_xpat_zdb_id = xpatfimg_xpat_zdb_id
			and xpatstg_start_stg_zdb_id = 
				xpatfimg_xpat_start_stg_zdb_id
			and xpatstg_end_stg_zdb_id = 
				xpatfimg_xpat_end_stg_zdb_id)
     and exists (select * from expression_pattern_anatomy
			where xpatanat_xpat_zdb_id = xpatstg_xpat_zdb_id
			and xpatanat_xpat_start_stg_zdb_id = 
				xpatstg_start_stg_zdb_id
			and xpatanat_xpat_end_stg_zdb_id =
				xpatstg_end_stg_zdb_id)
     and xpatstg_comments is null 
     group by xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id;

--update the source and the cap again for new figs

update tmp_figs
  set fig_caption = (select xpatstg_comments
			from expression_pattern_stage
   where xpatstg_xpat_zdb_id = xpat_id
     and xpatstg_start_stg_zdb_id = xpat_start
     and xpatstg_end_stg_zdb_id = xpat_end);

update tmp_Figs
  set xpat_source_zdb_id = (select xpat_source_zdb_id
				from expression_pattern
				where xpat_id = xpat_zdb_id)
  where xpat_source_zdb_id is null ;


create temp table tmp_pub_figs (fig_zdb_id varchar(50), 
				fig_comments lvarchar(256),
				xpat_source varchar(50),
				fig_label varchar(65))
with no log ;

--"Text Only Display No Image No Comments No Anat", GELI for non-direct submission pubs.

!echo tmp_pub_figs GELI for not unpublished

insert into tmp_pub_figs (fig_zdb_id, fig_comments,
		xpat_source, fig_label)
select get_id('FIG'), 'GELI',
	xpat_source_zdb_id, "text only"
from expression_pattern, expression_pattern_stage, publication	
where not exists (select * from expression_pattern_image
			where xpatstg_xpat_zdb_id = xpatfimg_xpat_zdb_id
			and xpatstg_start_stg_zdb_id = 
				xpatfimg_xpat_start_stg_zdb_id
			and xpatstg_end_stg_zdb_id = 
				xpatfimg_xpat_end_stg_zdb_id)
and not exists (select * from expression_pattern_anatomy
			where xpatstg_xpat_zdb_id = xpatanat_xpat_zdb_id
			and xpatanat_xpat_Start_stg_zdb_id = 
				xpatstg_start_stg_zdb_id
			and xpatanat_xpat_end_stg_zdb_id =
				xpatstg_end_stg_zdb_id)
and xpatstg_xpat_zdb_id = xpat_zdb_id
and xpat_source_zdb_id = publication.zdb_id
and jtype != 'Unpublished'
and xpatstg_comments is null
group by xpat_source_zdb_id ;

!echo UNPUBLISHED GELI BY PROBE NEXT comments is null

insert into tmp_figs (counter, fig_zdb_id, fig_label,
			xpat_id)
  select count(*),get_id('FIG'),
	"text only",
	xpatstg_xpat_zdb_id
    from expression_pattern_stage, expression_pattern, publication
    where not exists (select * from expression_pattern_image
			where xpatstg_xpat_zdb_id = xpatfimg_xpat_zdb_id
			and xpatstg_start_stg_zdb_id = 
				xpatfimg_xpat_start_stg_zdb_id
			and xpatstg_end_stg_zdb_id = 
				xpatfimg_xpat_end_stg_zdb_id)
     and not exists (select * from expression_pattern_anatomy
			where xpatanat_xpat_zdb_id = xpatstg_xpat_zdb_id
			and xpatanat_xpat_start_stg_zdb_id = 
				xpatstg_start_stg_zdb_id
			and xpatanat_xpat_end_stg_zdb_id =
				xpatstg_end_stg_zdb_id)
     and xpatstg_comments is null 
     and xpat_zdb_id = xpatstg_xpat_zdb_id
     and xpat_source_zdb_id = publication.zdb_id
     and jtype = 'Unpublished'
     group by xpatstg_xpat_zdb_id;

!echo UNPUBLISHED GELI but has comment BY PROBE NEXT comments is not null

insert into tmp_figs (counter, fig_zdb_id, fig_comments, fig_label,
			xpat_id, xpat_start, xpat_end)
  select count(*),get_id('FIG'),'whole org',
	"text only",
	xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id
    from expression_pattern_stage, expression_pattern, publication
    where not exists (select * from expression_pattern_image
			where xpatstg_xpat_zdb_id = xpatfimg_xpat_zdb_id
			and xpatstg_start_stg_zdb_id = 
				xpatfimg_xpat_start_stg_zdb_id
			and xpatstg_end_stg_zdb_id = 
				xpatfimg_xpat_end_stg_zdb_id)
     and not exists (select * from expression_pattern_anatomy
			where xpatanat_xpat_zdb_id = xpatstg_xpat_zdb_id
			and xpatanat_xpat_start_stg_zdb_id = 
				xpatstg_start_stg_zdb_id
			and xpatanat_xpat_end_stg_zdb_id =
				xpatstg_end_stg_zdb_id)
     and xpatstg_comments is not null 
     and xpat_zdb_id = xpatstg_xpat_zdb_id
     and xpat_source_zdb_id = publication.zdb_id
     and jtype = 'Unpublished'
     group by xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id;

update tmp_figs
  set fig_caption = (select xpatstg_comments
			from expression_pattern_stage
   			where xpatstg_xpat_zdb_id = xpat_id
     			and xpatstg_start_stg_zdb_id = xpat_start
     			and xpatstg_end_stg_zdb_id = xpat_end
     			and xpatstg_comments is not null);

--give new figs attribution to thisse load so people know why they have
--weird captions that seem duplicated to the user.

--!echo update fig source

update tmp_figs
  set fig_source_zdb_id = (select xpat_source_zdb_id
				from expression_pattern
				where xpat_id = xpat_Zdb_id);


--!echo insert into zdb_active data with figs

insert into zdb_active_data (zactvd_zdb_id)
  select fig_zdb_id 
  from tmp_figs
  where fig_zdb_id not in (select * from zdb_active_data) ;

insert into record_attribution (recattrib_source_zdb_id, 
	recattrib_data_zdb_id)
  select fig_source_zdb_id, fig_zdb_id
     from tmp_figs
     where not exists (select *
			from record_attribution
			where recattrib_source_zdb_id = fig_source_zdb_id
			and recattrib_data_zdb_id = fig_zdb_id) ;

insert into zdb_active_data (zactvd_zdb_id)
  select fig_zdb_id 
  from tmp_pub_figs
  where fig_zdb_id not in (select * from zdb_active_data) ;

insert into record_attribution (recattrib_source_zdb_id, 
	recattrib_data_zdb_id)
  select xpat_source, fig_zdb_id
     from tmp_pub_figs
     where not exists (select *
			from record_attribution
			where recattrib_source_zdb_id = xpat_source
			and recattrib_data_zdb_id = fig_zdb_id) ;


alter table fx_figure
  drop constraint fx_figure_alternate_key ;

drop index figure_alternate_key_index ;

update statistics high for table fx_figure;
update statistics high for table tmp_figs;

--make the figs

insert into fx_figure (fig_zdb_id,
    fig_source_zdb_id,
    fig_caption,
    fig_comments,
    fig_label)
  select fig_zdb_id, fig_source_zdb_id, 
		fig_caption, 
		fig_comments,
		fig_label
	from tmp_figs 
	where xpat_id is not null;

--anything int tmp_pub_figs is a geli--unpublished jrnls can't have
--geli, so they're all lumped into tods, even though most have
--no image/anat/comments

insert into fx_figure (fig_zdb_id,
    fig_caption,
    fig_source_zdb_id,
    fig_comments,
    fig_label)
  select fig_zdb_id, 
	fig_comments,
	xpat_source, 
	"GELI",
	"text only"
    from tmp_pub_figs ;

set constraints for fish_image_anatomy disabled ;

set constraints for expression_pattern_image disabled ;

set constraints for fish_image_stage disabled ;

set constraints for fish_image disabled  ;

--update fimg with figure_zdb_id for existing image recs
--do this with temp columns in xpat_image

--add figures to existing images, update images w/o figures

alter table expression_pattern_image 
 add (xpatfimg_fig_zdb_id varchar(50)) ;

!echo adding fig to xpatfimg

update expression_pattern_image
  set xpatfimg_fig_zdb_id = (Select fig_zdb_id 
		  from tmp_figs
		  where xpat_id = xpatfimg_xpat_zdb_id	
		  and xpat_start = xpatfimg_xpat_start_stg_zdb_id
		  and xpat_end = xpatfimg_xpat_end_stg_zdb_id) ;

--set fig_id to group records, or to equal fimg_zdb_id
--echo if still null

--this next part works because each image only has one stage range
--associated with it.

update statistics high for table tmp_figs ;
update statistics high for table fish_image ;
update statistics high for table expression_pattern_image ;

create temp table tmp_fimg_fig (fimg_zdb_id varchar(50), 
				fig_zdb_id varchar(50),
				counter integer)
with no log ;

insert into tmp_fimg_fig (counter, fimg_zdb_id, fig_zdb_id)
  select count(*), xpatfimg_fimg_zdb_id, xpatfimg_fig_zdb_id
	from expression_pattern_image
	group by xpatfimg_fimg_zdb_id, xpatfimg_fig_zdb_id ;

create unique index tmp_fimg_fig_index
  on tmp_fimg_fig(fimg_zdb_id, fig_zdb_id) ;

update statistics high for table fish_image ;
update statistics high for table tmp_fimg_fig ;

create temp table tmp_bad_images (counter integer,
				  fimg varchar(50))
with no log ;


insert into tmp_bad_images (counter, fimg)
 select count(*) as counter,tmp_fimg_fig.fimg_zdb_id
  from tmp_fimg_fig, fish_image
  where tmp_fimg_fig.fimg_zdb_id = fish_image.fimg_zdb_id
  group by tmp_fimg_fig.fimg_zdb_id 
  having count(*) > 1;

create temp table tmp_bad_fig (fig varchar(50), image varchar(50))
with no log ;

create temp table tmp_good_fig (gfig varchar(50), gimage varchar(50), 
	bfig varchar(50))
with no log ;

insert into tmp_bad_fig (fig, image)
select min(fig_zdb_id), fimg_zdb_id
  from tmp_fimg_fig
  where fimg_zdb_id in (select fimg from tmp_bad_images)
  group by fimg_zdb_id ;

insert into tmp_good_fig (gfig, gimage)
select fig_zdb_id, fimg_zdb_id
  from tmp_fimg_fig
  where fimg_zdb_id in (select fimg from tmp_bad_images) 
  and fig_zdb_id not in (select fig from tmp_bad_fig);

update tmp_good_fig
  set bfig = (select fig 
		from tmp_bad_fig
		where image = gimage);

delete from tmp_fimg_fig
  where fig_zdb_id in (Select fig from tmp_bad_fig);

update fish_image 
  set fimg_figure_zdb_id = (select fig_zdb_id
				from tmp_fimg_fig
				where tmp_fimg_fig.fimg_zdb_id = 
					fish_image.fimg_zdb_id)
  where fimg_figure_zdb_id is null ;

update fish_image
  set fimg_label = fimg_zdb_id 
  where fimg_figure_zdb_id is not null
  and fimg_label is null ;

update fish_image
  set fimg_label = 'Thisse Image'||" "||fimg_label
  where fimg_figure_zdb_id is not null ;


set constraints for fish_image enabled  ;

set constraints for fish_image_stage enabled ;

set constraints for fish_image_anatomy enabled ;

set constraints for expression_pattern_image enabled ;

set constraints all immediate ;

alter table fx_fish_image_private
  modify (fimgp_label varchar(200)) ;

alter table fx_fish_image_private
  modify (fimgp_fig_zdb_id varchar(50) ) ;

commit work ;

begin work;

---------------------FISH_IMAGES-------------------------------------------

insert into fx_fish_image_private (fimgp_zdb_id,
    fimgp_fig_zdb_id,
    fimgp_comments,
    fimgp_label,
    fimgp_image,
    fimgp_annotation,
    fimgp_image_with_annotation,
    fimgp_thumbnail,
    fimgp_width,
    fimgp_height,
    fimgp_fish_zdb_id,
    fimgp_view,
    fimgp_direction,
    fimgp_form,
    fimgp_preparation,
    fimgp_owner_zdb_id,
    fimgp_external_name,
    has_image,
    fimgp_bkup_img,
    fimgp_bkup_thumb,
    fimgp_bkup_annot 
  )
  select fimg_zdb_id,
    fimg_figure_zdb_id,
    fimg_comments,
    fimg_label,
    fimg_image,
    fimg_annotation,
    fimg_image_with_annotation,
    fimg_thumbnail,
    fimg_width,
    fimg_height,
    fimg_fish_zdb_id,
    fimg_view varchar,
    fimg_direction varchar,
    fimg_form varchar,
    fimg_preparation,
    fimg_owner_zdb_id,
    fimg_external_name,
    has_image,
    fimg_bkup_img,
    fimg_bkup_thumb,
    fimg_bkup_annot 
	from fish_image;

---------------------FX_EXPRESSION_EXPERIMENT--------------------------------

alter table fx_expression_result
  add (source varchar(50)) ;

update statistics high for table feature_experiment;
update statistics high for table fx_expression_experiment;

set constraints all deferred ;

--assume all direct submission data is in WT environment

insert into fx_expression_experiment (xpatex_zdb_id,
    xpatex_source_zdb_id,
    xpatex_assay_name,
    xpatex_probe_feature_zdb_id,
    xpatex_gene_zdb_id,
    xpatex_featexp_zdb_id,
    xpatex_direct_submission_date,
    xpatex_comments)
 select xpat_zdb_id, 
	xpat_source_zdb_id, 
	xpat_assay_name, 
	xpat_probe_zdb_id,
	xpat_gene_zdb_id,
	featexp_zdb_id,
	xpat_direct_submission_date,
	xpat_comments
   from expression_pattern, feature_experiment
   where featexp_genome_feature_zdb_id = xpat_stock_zdb_id
	and featexp_exp_zdb_id = 'ZDB-EXP-041102-1';

--WT fish: 'ZDB-FISH-030619-2'

update statistics high for table fx_expression_experiment ;
update statistics high for table fx_figure ;
update statistics high for table feature_experiment;

----------------------MERGE_XPATANAT_XPATSTG-----------------------------

create temp table tmp_xpat (
	xpat_zdb_id 			varchar(50),
	xpat_start_stg_zdb_id		varchar(50),
	xpat_end_stg_zdb_id		varchar(50),
	xpat_anatomy_item_zdb_id	varchar(50),
	xpat_expression_found		boolean,
	xpat_comments			varchar(255)
) with no log ;

--!echo tmp_xpat putting all anats into xpatstgs first merge table

insert into tmp_xpat (xpat_zdb_id,
	xpat_start_stg_zdb_id,
	xpat_end_stg_zdb_id,
	xpat_anatomy_item_zdb_id,
	xpat_expression_found,
	xpat_comments)
select xpatstg_xpat_zdb_id,
	xpatstg_start_stg_zdb_id,
	xpatstg_end_stg_zdb_id,
	xpatanat_anat_item_zdb_id,
	xpatstg_expression_found,
	xpatstg_comments
   from expression_pattern_stage,
     outer (expression_pattern_anatomy)
   where xpatstg_xpat_zdb_id = xpatanat_xpat_zdb_id
   and xpatstg_start_stg_zdb_id = xpatanat_xpat_start_stg_zdb_id
   and xpatstg_end_stg_zdb_id = xpatanat_xpat_end_stg_zdb_id;

update statistics high for table fx_Expression_experiment ;

--update nulls w/new anat items
!echo whole org

update tmp_xpat
  set xpat_anatomy_item_zdb_id = (select anatitem_zdb_id 
				 from anatomy_item 
				 where anatitem_name = 'whole organism')
  where xpat_anatomy_item_zdb_id is null 
	and exists (select 'x'
			from tmp_figs 
			where xpat_id = xpat_zdb_id 
			and xpat_start = xpat_start_stg_zdb_id
			and xpat_end = xpat_end_stg_zdb_id
			and fig_comments = 'whole org') ;

update statistics high for table expression_pattern_stage;

!echo not specified

update tmp_xpat
  set xpat_anatomy_item_zdb_id = 'ZDB-ANAT-041102-1'
  where xpat_anatomy_item_zdb_id is null ;

--select xpat_zdb_id from tmp_xpat
-- where xpat_zdb_id not in (select xpat_Zdb_id from expression_pattern);

--select count(*) from tmp_xpat 
--where xpat_expression_found is null ;


--fill up new merge table, xpatres

--select count (*) from tmp_xpat 
--where xpat_anatomy_item_zdb_id is null ;


alter table fx_expression_result
  add (newone boolean) ;

!echo xpatres being inserted

insert into fx_expression_result (xpatres_zdb_id,
				xpatres_xpatex_zdb_id,
				xpatres_anat_item_zdb_id,
				xpatres_start_stg_zdb_id,
				xpatres_end_stg_zdb_id,
				xpatres_expression_found,
				xpatres_comments, newone)
  select get_id('XPATRES'), 
	xpat_zdb_id,
	xpat_anatomy_item_zdb_id,
	xpat_start_stg_zdb_id,
	xpat_end_stg_zdb_id,
	xpat_expression_found,
	xpat_comments, 't'
	from tmp_xpat ;

--select count (*) from fx_expression_result 
--where xpatres_anat_item_zdb_id is null ;

update statistics high for table fx_expression_result ;
update statistics high for table tmp_figs;
update statistics high for table fx_expression_experiment ;

-------------------------------------------------------
--xpatres, xpatex made
-------------------------------------------------------

create temp table tmp_fx_expression_pattern_figure (
	fxxpatres_zdb_id varchar(50) not null,
	fig_zdb_id varchar(50),
	counter int,
	comment varchar(10))
with no log ;

--!echo count of good figs

--select count(*) from tmp_figs ;

--!echo count of geli figs

--select count(*) from tmp_pub_figs ;

--!echo count of xpatres recs

--select count(*) from fx_expression_result ;

create unique index tmpfxfigxpates_pk_index
  on tmp_fx_expression_pattern_figure (fxxpatres_zdb_id, fig_zdb_id)
  using btree in idxdbs3 ;

insert into tmp_fx_expression_pattern_figure (fxxpatres_zdb_id,
    fig_zdb_id, counter, comment)
  select xpatres_zdb_id, fig_zdb_id, count(*), 'nGELIPUBls'
   from tmp_figs, fx_expression_result
   where xpat_id = xpatres_xpatex_zdb_id
     and xpat_start = xpatres_start_stg_zdb_id
     and xpat_end = xpatres_end_stg_zdb_id
     and xpat_start is not null
     and xpat_end is not null
   group by xpatres_zdb_id, fig_zdb_id ;


update statistics high for table tmp_fx_expression_pattern_figure ;

insert into tmp_fx_expression_pattern_figure (fxxpatres_zdb_id,
    fig_zdb_id, counter, comment)
  select xpatres_zdb_id, fig_zdb_id, count(*), 'TODuPUBls'
   from tmp_figs, fx_expression_result
   where xpat_id = xpatres_xpatex_zdb_id
     and xpat_start is null
     and xpat_end is null
     and xpatres_zdb_id not in (select fxxpatres_zdb_id 
				  from tmp_fx_expression_pattern_figure)
   group by xpatres_zdb_id, fig_zdb_id ;

create index tmppubfigs_fig_index 
  on tmp_pub_figs (fig_zdb_id) ;

create index tmppubfigs_source_index
  on tmp_pub_figs (xpat_sourcE) ;

create temp table tmp_not_in_GELI_res (xpatres_id varchar(50), 
					xpatex_id varchar(50),
					xpatex_source_id varchar(50))
with no log ;

create index notinGeli_xpatres_index
  on tmp_not_in_GELI_res (xpatres_id) ;

create index notinGeli_xpatsource_index
  on tmp_not_in_GELI_res (xpatex_source_id);

insert into tmp_not_in_GELI_res (xpatres_id,xpatex_source_id)
  select distinct xpatres_zdb_id, xpatex_source_zdb_id
     from fx_expression_result, fx_expression_experiment 
     where xpatex_zdb_id = xpatres_xpatex_zdb_id
     and newone = 't'
     and xpatres_zdb_id not in (select fxxpatres_zdb_id
				from tmp_fx_expression_pattern_figure);

!echo GELI/xpatres to figxpatres

insert into tmp_fx_expression_pattern_figure (fxxpatres_zdb_id,
    fig_zdb_id)
  select xpatres_id, fig_zdb_id
   from  tmp_pub_figs, tmp_not_in_GELI_res   
     where xpat_source = xpatex_source_id ;

create index tmp_fx_xpatex_fig_xpatres_id
  on tmp_fx_expression_pattern_figure (fxxpatres_zdb_id)
  using btree in idxdbs1 ;

!echo all the xpatresfigs inserted now

insert into fx_expression_pattern_figure (xpatfig_xpatres_zdb_id,
    xpatfig_fig_zdb_id)
  select distinct fxxpatres_zdb_id, fig_zdb_id
     from tmp_fx_expression_pattern_figure ;

insert into zdb_active_data (zactvd_zdb_id)  
  select xpatres_zdb_id 
	from fx_expression_result
	where xpatres_zdb_id not in (select zactvd_zdb_id
					from zdb_active_data) ;

--add comments for Peiran from old thisse data

--update fx_expression_result
--  set xpatres_comments = (select fig_caption
--			    from fx_figure, fx_expression_pattern_figure
--			    where fig_zdb_id = xpatfig_fig_zdb_id
--			    and xpatfig_xpatres_zdb_id = xpatres_zdb_id
--			    and lower(fig_caption) in (
--				'not spacially restricted', 
--				'basal level') ;  
--  where xpatres_comments is null ;

set constraints all immediate ;

commit work ;
begin work ;

alter table fx_expression_result
  drop source ;

alter table fx_expression_result
  drop newone ;

update statistics high for table expression_pattern_stage ;

UPDATE STATISTICS FOR PROCEDURE;

set constraints all deferred;

--change the pub attribution to direct sub pub.

update fx_expression_experiment
  set xpatex_source_zdb_id = 'ZDB-PUB-050309-6'
  where xpatex_source_zdb_id = 'ZDB-PUB-011214-7' ;

UPDATE STATISTICS FOR PROCEDURE;

update fx_figure
  set fig_source_zdb_id = 'ZDB-PUB-050309-6'
  where fig_source_zdb_id = 'ZDB-PUB-011214-7' ;

update experiment
  set exp_source_zdb_id = 'ZDB-PUB-050309-6'
  where exp_source_zdb_id = 'ZDB-PUB-011214-7' ;

delete from record_attribution
  where recattrib_source_zdb_id = 'ZDB-PUB-011214-7'
  and recattrib_data_zdb_id like 'ZDB-XPAT-%' ;

update record_attribution
  set recattrib_source_zdb_id = 'ZDB-PUB-050309-6'
  where recattrib_source_zdb_id = 'ZDB-PUB-011214-7'
  and recattrib_data_zdb_id like 'ZDB-IMAGE-%' ;

delete from record_attribution
  where recattrib_source_zdb_id = 'ZDB-PUB-011214-7'
  and recattrib_data_zdb_id like 'ZDB-FIG-%' ;

delete from record_attribution
  where recattrib_source_zdb_id = 'ZDB-PUB-011214-7'
  and recattrib_data_zdb_id like 'ZDB-GENE-%' ;

delete from record_attribution
  where recattrib_source_zdb_id = 'ZDB-PUB-011214-7'
  and recattrib_data_zdb_id like 'ZDB-EST-%' ;

set constraints all immediate ;

--select * from record_Attribution
--  where recattrib_source_zdb_id = 'ZDB-PUB-011214-7';

alter table fx_figure
  add (fig_full_label varchar(50));

drop trigger fx_figure_insert_trigger ;

drop trigger fx_figure_update_trigger ;

create trigger fx_figure_insert_trigger insert on 
    fx_figure referencing new as new_fig
    for each row
        (
        execute procedure 
	p_insert_into_record_attribution_tablezdbids(new_fig.fig_zdb_id,
				new_fig.fig_source_zdb_id ),
	execute function zero_pad(new_fig.fig_label) into fig_full_label);

create trigger fx_figure_update_trigger update of 
    fig_source_zdb_id, fig_label on fx_figure referencing new 
    as new_fig
    for each row
        (
        execute procedure 
	p_insert_into_record_attribution_tablezdbids(new_fig.fig_zdb_id,
						new_fig.fig_source_zdb_id ),
	execute function zero_pad(new_fig.fig_label) into fig_full_label);

update fx_figure
  set fig_full_label = zero_pad(fig_label) ;

--make double sure we only have text only labels

update fx_figure
  set fig_caption = 'Unillustrated author statements.'
  where fig_comments != 'GELI'
  and fig_label = 'text only' ;

update fx_figure
  set fig_caption = 'Unillustrated author statements.'
  where fig_comments != 'GELI'
  and fig_label = 'T.O.D.' ;

update fx_figure
  set fig_label = 'text only'
  where fig_label = 'text only';

update fx_figure
  set fig_label = 'text only'
  where fig_label = 'T.O.D.';

--update fx_figure
--  set fig_caption = null
--  where not exists (select 'x' from fx_fish_image_private
--			where fimgp_fig_zdb_id = fig_zdb_id)
--  and fig_comments != 'GELI'
--  and fig_label not in ('text only display', 'T.O.D.', 'text only') ;

update fx_figure
  set fig_label = 'Fig. '||fig_label
  where fig_label != 'text only'
  and fig_label not like 'Fig.%' ;

--add significance to condition_data_type

alter table condition_data_type
  add (cdt_significance integer);

update condition_data_type
  set cdt_significance = '1'
  where cdt_name = 'morpholino' ;

update condition_data_type
  set cdt_significance = '2'
  where cdt_name != 'morpholino' ;

alter table condition_data_type
  modify (cdt_significance integer not null constraint
        cdt_significance_not_null);

--move xpatex comment to pub page.
update statistics high for table publication;

update publication
  set pub_errata_and_notes = "The cDNA and in situ hybridizations for Fast Release clones (high throughput analysis) have not been double checked.  Mistakes may occur.  Please contact <a href='mailto:thisse@titus.u-strasbg.fr'>C and B Thisse</a> if you detect anything wrong.  PCR protocol available on the probe details page."
  where zdb_id = 'ZDB-PUB-040907-1'; 

commit work ;
begin work ;

--drop unnecessary colums

alter table fx_Expression_experiment
  drop xpatex_comments ;

alter table publication
   add (pub_acknowledgment lvarchar);

update statistics high for table fx_figure ;

update fx_figure
  set fig_caption = null
  where fig_caption like '%lease click on individual images for details%';

create index fig_comments_index
  on fx_figure (fig_comments) 
  using btree in idxdbs3 ;

update fx_figure
  set fig_caption = 'This is a summary of gene expression assays reported in this publication. Associated figures and anatomical structures have not yet been added to ZFIN.'
  where fig_comments = 'GELI';

unload to good_fig
  select * from tmp_good_fig; 

unload to bad_fig
  select * from tmp_bad_fig;

update fx_expression_pattern_figure
 set xpatfig_fig_zdb_id = (select distinct gfig
			     from tmp_good_fig
  			     where bfig = xpatfig_fig_zdb_id)
 where xpatfig_fig_zdb_id in (select fig from tmp_bad_fig);

--rollback work ;
commit work ;