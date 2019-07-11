--liquibase formatted sql
--changeset sierra:update_image_with_annotation_paths.sql

create temp table tmp_new_img_name (img_zdb_id text,
       pub_zdb_id text,
       year_string text)
;

insert into tmp_new_img_name (img_zdb_id, pub_zdb_id, year_string)
 select img_zdb_id, 
        fig_source_zdb_id, 
        case when fig_source_zdb_id like 'ZDB-PUB-9%' 
          then '19'||substring(fig_source_zdb_id, 9,2) 
          else '20'||substring(fig_source_zdb_id, 9,2) 
          end
   from image, figure where img_fig_zdb_id = fig_zdb_id;

create index img_zdb_id_index on tmp_new_img_name (img_zdb_id);

create index pub_zdb_id_index on tmp_new_img_name (pub_zdb_id);

update image 
  set img_image_with_annotation = (select year_string||'/'||pub_zdb_id||'/'||img_image_with_annotation
                    from tmp_new_img_name where tmp_neW_img_name.img_zdb_id = image.img_zdb_id)
  where img_fig_zdb_id is not null
 and img_is_video_still = 'f';
