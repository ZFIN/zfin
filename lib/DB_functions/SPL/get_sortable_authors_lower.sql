create function get_sortable_authors_lower(vZdbId varchar(50)) 
	returning varchar(200);

  define vSortableAuthorsLower varchar(200) ;
  
  let vSortableAuthorsLower = (select substr(pub_authors_lower,1,200)
			from publication
			where zdb_id = vZdbId);

  return vSortableAuthorsLower ;

end function ;