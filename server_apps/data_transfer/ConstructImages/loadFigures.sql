begin work;

create temp table tmp_mrkr_figure(tmp_figure_zdb_id varchar(50), tmp_mrkr_zdb_id varchar(50));

\copy tmp_mrkr_figure from constructFigure.unl;

update tmp_mrkr_figure set tmp_mrkr_zdb_id = (select dalias_data_zdb_id from data_alias where dalias_alias=tmp_mrkr_zdb_id);

--!echo load figure tables
create temp table tmp_figure(tmp_fig_mrkr text,tmp_figure_zdb_id text, tmp_figure_pub_zdb_id text);

insert into tmp_figure select tmp_figure_zdb_id,tmp_figure_zdb_id, 'ZDB-PUB-120111-1' from tmp_mrkr_figure;
update tmp_figure set tmp_figure_zdb_id=get_id('FIG');
insert into zdb_active_data select tmp_figure_zdb_id from tmp_figure;
insert into figure (fig_zdb_id,fig_source_zdb_id, fig_comments) select tmp_figure_zdb_id,tmp_figure_pub_zdb_id,tmp_fig_mrkr from tmp_figure;

\copy ( select m.tmp_mrkr_zdb_id,t.tmp_figure_zdb_id from tmp_mrkr_figure m, tmp_figure t where trim(m.tmp_figure_zdb_id)=trim(t.tmp_fig_mrkr);
insert into construct_figure (consfig_construct_zdb_id, consfig_fig_zdb_id) select m.tmp_mrkr_zdb_id,t.tmp_figure_zdb_id from tmp_mrkr_figure m, tmp_figure t where trim(m.tmp_figure_zdb_id)=trim(t.tmp_fig_mrkr)) to tmpfigmkr;

--!echo load image tables
create table tmp_image(tmp_img_zdb_id text,tmp_img_width integer, tmp_img_height integer, tmp_fig_zdb_id text,tmp_img_owner_zdb_id text,tmp_image_img varchar(150),tmp_img_thumbnail varchar(150));

\copy tmp_image from images.unl;


update tmp_image set tmp_fig_zdb_id=(select tmp_figure_zdb_id from tmp_figure where tmp_fig_mrkr=tmp_img_zdb_id); 
update tmp_image set tmp_img_owner_zdb_id='ZDB-PERS-960805-161';
update tmp_image set tmp_img_zdb_id=get_id('IMAGE');
insert into zdb_active_data select tmp_img_zdb_id from tmp_image;
insert into image (img_zdb_id, img_fig_zdb_id,img_width,img_height,img_view,img_direction,img_form,img_preparation,img_owner_zdb_id, img_image,img_thumbnail) select tmp_img_zdb_id,tmp_fig_zdb_id,tmp_img_width,tmp_img_height,"not specified","not specified","not specified","not specified",tmp_img_owner_zdb_id,tmp_img_zdb_id||'.jpg',tmp_img_zdb_id||'--thumb'||'.jpg' from tmp_image;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)select tmp_img_zdb_id,'ZDB-PUB-120111-1' from tmp_image;

\copy (select * from tmp_image) to tmpimage ;
\copy (select REPLACE(tmp_image_img,'.jpg',''),tmp_img_zdb_id from tmp_image) to img_oldname_2_newname.txt ;
\copy (selcet * from tmp_figure) to tmpfig ;
--rollback work;

create temp table tmp_dblink (dblink_id text, cnstrctid text,accnum text,fdbcontid text);

insert into tmp_dblink select m.tmp_mrkr_zdb_id, m.tmp_mrkr_zdb_id, SUBSTR(tmp_fig_mrkr,6),'ZDB-FDBCONT-130419-1' from tmp_figure t,tmp_mrkr_figure m where trim(m.tmp_figure_zdb_id)=trim(t.tmp_fig_mrkr);

update tmp_dblink set dblink_id=get_id('DBLINK');
update tmp_dblink set fdbcontid=(select fdbcont_zdb_id from foreign_db_contains, foreign_db where  fdbcont_fdb_db_id=fdb_db_pk_id and fdb_db_name = 'zfishbook-constructs');
insert into zdb_active_data select dblink_id from tmp_dblink;

insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_zdb_id,dblink_acc_num_display,dblink_fdbcont_zdb_id) select cnstrctid,accnum,dblink_id,accnum,fdbcontid from tmp_dblink;

\copy (select * from tmp_dblink) to 'newdblink';

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)select dblink_id,'ZDB-PUB-120111-1' from tmp_dblink;


 
commit work;
