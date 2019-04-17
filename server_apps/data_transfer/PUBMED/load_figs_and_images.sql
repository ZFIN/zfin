begin work ;

create temp table tmp_figs_to_load (pub_zdb_id text,
       pmc_id text,
       file_path_directory text, 
       fig_label text,
       fig_caption text,
       img_filename text);

\copy tmp_figs_to_load from  'figsToLoad.txt' with delimiter '|' ;

create temp table tmp_figs_to_load_with_ids (pub_zdb_id text,
       pmc_id text,
       file_path_directory text, 
       fig_label text,
       fig_caption text,
       fig_zdb_id text,
       img_zdb_id text,
       img_filename text);

insert into tmp_figs_to_load_with_ids (pub_zdb_id,      
                                       pmc_id,
                                       file_path_directory,
                                       fig_label,
                                       fig_caption,
                                       img_filename)
  select pub_zdb_id,
         pmc_id,
         file_path_directory,
         fig_label,
         fig_caption,
         img_filename
    from tmp_figs_to_load;

update tmp_figs_to_load_with_ids
  set fig_zdb_id = get_id('FIG');

update tmp_figs_to_load_with_ids
  set img_zdb_id = get_id('IMAGE');


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
                   img_thumbnail)
select img_zdb_id, 
       fig_zdb_id, 
       img_filename,
       '-1',
       '-1',
       'not specified',
       'not specified',
       'not specified',
       'not specified',
       'ZDB-PERS-030612-1',
       pub_zdb_id||img_filename,
       pub_zdb_id||img_filename
   from tmp_figs_to_load_with_ids;



commit work;

--rollback work;
