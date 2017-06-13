create or replace function get_sortable_authors_lower(vZdbId varchar(50)) 
	returns varchar(200) as $vSortableAuthorsLower$

  declare vSortableAuthorsLower varchar(200) := (select substr(pub_authors_lower,1,200)
			from publication
			where zdb_id = vZdbId);
  begin 
  return vSortableAuthorsLower ;

end
$vSortableAuthorsLower$ LANGUAGE plpgsql
