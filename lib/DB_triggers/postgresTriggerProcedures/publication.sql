drop trigger if exists publication_trigger on publication;

create or replace function publication()
returns trigger as
$BODY$
declare title publication.title%TYPE;
	accession_no publication.accession_no%TYPE;
	pubmed_authors publication.pubmed_authors%TYPE;
	pub_doi publication.pub_doi%TYPE;
	pub_authors_lower publication.pub_authors_lower%TYPE;
	jtype publication.jtype%TYPE;
	pub_mini_ref publication.pub_mini_ref%TYPE;
	pub_pages publication.pub_pages%TYPE;
	pub_can_show_images publication.pub_can_show_images%TYPE;

begin

     title = (select scrub_char(NEW.title));
     NEW.title = title;

     accession_no = (select scrub_char(NEW.accession_no));
     NEW.accession_no = accession_no;

     pubmed_authors = (select scrub_char(NEW.pubmed_authors));
     NEW.pubmed_authors = pubmed_authors;

     jtype = (select scrub_char(NEW.jtype));
     NEW.jtype = jtype;

     pub_authors_lower = lower(NEW.authors);
     NEW.authors = pub_authors_lower;

     pub_pages = (select scrub_char(NEW.pub_pages));
     NEW.pub_pages = pub_pages;

     pub_doi = (select scrub_char(NEW.pub_doi));
     NEW.pub_doi = pub_doi;
	
     pub_mini_ref = (select get_pub_mini_ref(NEW.pub_mini_ref));
     pub_mini_ref = (select scrub_char(pub_mini_ref));
     NEW.pub_mini_ref = pub_mini_ref;

     pub_can_show_images = (select get_pub_default_permissions(NEW.pub_jrnl_zdb_id));
     NEW.pub_can_show_images = pub_can_show_images;

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger publication_trigger before insert or update on publication
 for each row
 execute procedure publication();


