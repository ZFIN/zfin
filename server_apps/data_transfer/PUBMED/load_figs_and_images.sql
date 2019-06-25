begin work ;

create temp table tmp_figs_to_load (pub_zdb_id text,
       pmc_id text,
       file_path_directory text, 
       fig_label text,
       fig_caption text,
       img_filename text,
       img_thumbnail text,
       img_medium text);

\copy tmp_figs_to_load from 'figsToLoad.txt' DELIMITER '|' ;

create temp table tmp_figs_to_load_with_ids (pub_zdb_id text,
       pmc_id text,
       file_path_directory text, 
       fig_label text,
       fig_caption text,
       fig_zdb_id text,
       img_zdb_id text,
       img_filename text,
       img_thumbnail text, 
       img_medium text);

insert into tmp_figs_to_load_with_ids (pub_zdb_id,      
                                       pmc_id,
                                       file_path_directory,
                                       fig_label,
                                       fig_caption,
                                       img_filename,
                                       img_thumbnail,
                                       img_medium)
  select pub_zdb_id,
         pmc_id,
         file_path_directory,
         fig_label,
         fig_caption,
         img_filename,
         img_thumbnail,
         img_medium
    from tmp_figs_to_load;

delete from tmp_figs_to_load_with_ids
  where not exists (Select 'x' from publication
                           where pub_can_show_images = 't'
                           and zdb_id = pub_zdb_id);

update tmp_figs_to_load_with_ids
  set fig_zdb_id = get_id('FIG');

update tmp_figs_to_load_with_ids
  set img_zdb_id = get_id('IMAGE');

update tmp_figs_to_load_with_ids
 set fig_caption = replace (fig_caption, '&&&&&', '|');

insert into zdb_active_data (zactvd_zdb_id)
  select fig_zdb_id from tmp_figs_to_load_with_ids;

insert into zdb_active_data (zactvd_zdb_id)
  select img_zdb_id from tmp_figs_to_load_with_ids;

insert into figure (fig_zdb_id, fig_source_zdb_id, fig_caption, fig_label)
  select fig_zdb_id, pub_zdb_id, fig_caption, fig_label 
    from tmp_figs_to_load_with_ids;

insert into image (img_zdb_id, 
                   img_fig_zdb_id, 
                   img_label, 
                   img_width, 
                   img_height,
                   img_view, 
                   img_direction, 
                   img_form, 
                   img_preparation, 
                   img_owner_zdb_id,
                   img_image,
                   img_thumbnail,
                   img_medium)
select img_zdb_id, 
       fig_zdb_id, 
       fig_label,
       '-1',
       '-1',
       'not specified',
       'not specified',
       'not specified',
       'not specified',
       'ZDB-PERS-030612-1',
       img_filename,
       img_thumbnail,
       img_medium
   from tmp_figs_to_load_with_ids;

update image
 set img_is_video_still = 't' where img_image like '%.avi' or img_image like '%.mov' or img_image like '%.wmv' or img_image like '%.mp4';  


commit work;

--rollback work;
