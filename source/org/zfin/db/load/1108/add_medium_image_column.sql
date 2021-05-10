--liquibase formatted sql
--changeset sierra:add_medium_image_column.sql

alter table image 
 add img_medium text;

update image
 set img_medium = replace(img_thumbnail, 'thumb', 'medium');

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

update publication_file
  set pf_file_name = (select year_string||'/'||pub_zdb_id||'/'||pub_zdb_id||'.pdf' from tmp_new_pub_loc where pub_zdb_id = pf_pub_zdb_id)
 where pf_file_name is not null;
