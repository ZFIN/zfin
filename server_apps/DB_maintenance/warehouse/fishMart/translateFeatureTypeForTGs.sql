begin work;

update statistics high for table gene_feature_result_view;
update statistics high for table fish_Annotation_Search;

update fish_annotation_Search
  set fas_fish_significance = 0;

update gene_feature_result_View
 set gfrv_affector_Type_display = 'Transgenic Insertion, non-allelic' 
 where not exists (Select 'x' from feature_marker_relationship
       	      	      	  where fmrel_ftr_Zdb_id = gfrv_affector_id
			  and fmrel_type = 'is allele of')
 and gfrv_affector_type_display = 'Transgenic Insertion';

--set explain on avoid_execute;

update fish_Annotation_Search
  set fas_fish_significance = (Select sum(fto_priority)
      			      	      from feature_Type_ordering, gene_feature_result_View
				      where fto_name = gfrv_affector_type_display
				       and fas_pk_id = gfrv_fas_id
				       group by gfrv_fas_id);

update gene_feature_result_view
  set gfrv_affector_type_display = 'Transgenic Insertion'
  where gfrv_affector_type_display = 'Transgenic Insertion, non-allelic' ;


update gene_feature_result_view
  set gfrv_affector_type_display = 'Transgenic Insertion'
  where gfrv_affector_type_display = 'Unspecified Transgenic Insertion' ;

update fish_annotation_search
  set fas_affector_type_group = fas_affector_type_group||", transgenic_insertion"
 where fas_affector_type_Group like '%transgenic_unspecified%';
  

update fish_annotation_search
  set fas_affector_type_group = replace(fas_affector_type_group,"_","");
  

commit work;