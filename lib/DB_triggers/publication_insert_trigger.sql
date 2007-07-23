 create trigger publication_insert_trigger 
   insert on publication 
     referencing new as new_publication
     for each row (
       execute function scrub_char(new_publication.title)
         into title,
       execute function scrub_char(new_publication.accession_no)
         into accession_no,
       execute function scrub_char(new_publication.pubmed_authors)
         into pubmed_authors,
	 execute function scrub_char(new_publication.pub_doi)
         into pub_doi,
       execute function lower(new_publication.authors)
         into pub_authors_lower,
       execute function scrub_char(new_publication.jtype)
         into jtype,
       execute function scrub_char(new_publication.zdb_id) 
	into publication.pub_mini_ref,
       execute function scrub_char(new_publication.pub_mini_ref)
        into publication.pub_mini_ref,
       execute function scrub_char(new_publication.pub_pages) 
	into publication.pub_pages,
       execute function get_pub_mini_ref(new_publication.zdb_id) 
	 into publication.pub_mini_ref,
       execute function get_pub_default_permissions 
		(new_publication.pub_jrnl_zdb_id)
         into publication.pub_can_show_images
     );
