drop trigger if exists publication_trigger on publication;

create or replace function publication()
returns trigger as
$BODY$
declare title publication.title%TYPE := scrub_char(NEW.title);
	accession_no publication.accession_no%TYPE := scrub_char(NEW.accession_no);
	pubmed_authors publication.pubmed_authors%TYPE := scrub_char(NEW.authors);
	pub_doi publication.pub_doi%TYPE := scrub_char(NEW.pub_doi);
	pub_authors_lower publication.pub_authors_lower%TYPE := lower(NEW.authors);
	jtype publication.jtype%TYPE := scrub_char(NEW.jtype);
	pub_mini_ref publication.pub_mini_ref%TYPE := scrub_char(get_pub_mini_ref(NEW.pub_mini_ref));
	pub_pages publication.pub_pages%TYPE := scrub_char(NEW.pub_pages);
	pub_can_show_images publication.pub_can_show_images%TYPE := get_pub_default_permissions(NEW.pub_jrnl_zdb_id);

begin

     NEW.title = title;
    
     NEW.pubmed_authors = pubmed_authors;

     NEW.jtype = jtype;

     NEW.authors = pub_authors_lower;

     NEW.pub_pages = pub_pages;

     NEW.pub_doi = pub_doi;
	
     NEW.pub_mini_ref = pub_mini_ref;

     NEW.pub_can_show_images = pub_can_show_images;

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;




create trigger publication_trigger before insert or update on publication
 for each row 
 execute procedure publication();


