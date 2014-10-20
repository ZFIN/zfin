!echo "xpatfig in fish annotation search";

update fish_annotation_Search_temp
 set fas_xpat_figure_count = 0;

update fish_annotation_search_temp
  set fas_xpat_figure_count = (Select count(distinct xfiggm_member_id)
      			       	       from xpat_figure_group, xpat_Figure_group_member
				       where xfiggm_group_id = xfigg_group_pk_id
				       and xfigg_geno_handle = fas_line_handle)
    where exists (select 'x' from xpat_figure_group
    	  	 	 where xfigg_geno_handle = fas_line_handle);

!echo "done addXpatCounts";