update fish_annotation_search
 set fas_fish_zdb_id = (Select fish_Zdb_id
                                from fish
                                where fish_handle = fas_line_handle)
where fas_line_handle is not null
and fas_line_handle != ''
and fas_line_handle not like 'ZDB-ALT%';

update fish_annotation_search_temp
 set fas_fish_zdb_id = (Select fish_Zdb_id
                                from fish
                                where fish_handle = fas_line_handle)
where fas_line_handle is not null
and fas_line_handle != ''
and fas_line_handle not like 'ZDB-ALT%';

update fish_annotation_search_backup
 set fas_fish_zdb_id = (Select fish_Zdb_id
                                from fish
                                where fish_handle = fas_line_handle)
where fas_line_handle is not null
and fas_line_handle != ''
and fas_line_handle not like 'ZDB-ALT%';

update gene_feature_Result_view
 set gfrv_fish_zdb_id = (Select fas_fish_zdb_id from 
 			     fish_Annotation_Search
			     where fas_pk_id = gfrv_fas_id);

update gene_feature_Result_view_temp
 set gfrv_fish_zdb_id = (Select fas_fish_zdb_id from 
 			     fish_Annotation_Search
			     where fas_pk_id = gfrv_fas_id);


update gene_feature_Result_view_backup
 set gfrv_fish_zdb_id = (Select fas_fish_zdb_id from 
 			     fish_Annotation_Search
			     where fas_pk_id = gfrv_fas_id);

update figure_Term_fish_search
  set ftfs_fish_zdb_id = (select fas_fish_Zdb_id
      		       	 	 from fish_Annotation_search
				 where fas_pk_id = ftfs_fas_id);

update figure_Term_fish_search_temp
  set ftfs_fish_zdb_id = (select fas_fish_Zdb_id
      		       	 	 from fish_Annotation_search
				 where fas_pk_id = ftfs_fas_id);

update figure_Term_fish_search_backup
  set ftfs_fish_zdb_id = (select fas_fish_Zdb_id
      		       	 	 from fish_Annotation_search
				 where fas_pk_id = ftfs_fas_id);