--liquibase formatted sql
--changeset sierra:update_image_and_pdf_paths.sql


create temp table tmp_new_img_name (img_zdb_id text,
       pub_zdb_id text,
       year_string text)
;

create temp table tmp_new_pub_loc (pub_zdb_id text,
       year_string text);

insert into tmp_new_pub_loc (pub_zdb_id, year_string)
  select zdb_id,
         case when zdb_id like 'ZDB-PUB-9%' 
          then '19'||substring(zdb_id, 9,2) 
          else '20'||substring(zdb_id, 9,2) 
          end
   from publication
 where exists (select 'x' from publication_file
                      where pf_pub_zdb_id = zdb_id 
                      );

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
  set img_image = (select '/'||year_string||'/'||pub_zdb_id||'/'||img_image 
                    from tmp_new_img_name where tmp_neW_img_name.img_zdb_id = image.img_zdb_id)
  where img_fig_zdb_id is not null;

update image 
  set img_thumbnail = (select year_string||'/'||pub_zdb_id||'/'||img_thumbnail
                    from tmp_new_img_name where tmp_neW_img_name.img_zdb_id = image.img_zdb_id)
  where img_fig_zdb_id is not null;

update image 
  set img_medium = (select year_string||'/'||pub_zdb_id||'/'||replace(img_thumbnail, 'thumb','medium')
                    from tmp_new_img_name where tmp_neW_img_name.img_zdb_id = image.img_zdb_id)
  where img_fig_zdb_id is not null;

update publication_file
  set pf_file_name = (select year_string||'/'||pub_zdb_id||'/'||pub_zdb_id||'.pdf' from tmp_new_pub_loc where pub_zdb_id = pf_pub_zdb_id)
 where pf_file_name is not null;

select img_image from image  where img_zdb_id like 'ZDB-IMAGE-19%' limit 5;

select pf_file_name from publication_file limit 5;
