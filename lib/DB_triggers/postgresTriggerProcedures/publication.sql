DROP TRIGGER IF EXISTS publication_trigger
ON publication;

CREATE OR REPLACE FUNCTION publication()
  RETURNS trigger AS $$
  BEGIN
    NEW.title = scrub_char(NEW.title);
    NEW.accession_no = scrub_char(NEW.accession_no);
    NEW.pubmed_authors = scrub_char(NEW.pubmed_authors);
    NEW.pub_doi = scrub_char(NEW.pub_doi);
    NEW.jtype = scrub_char(NEW.jtype);
    NEW.pub_authors_lower = lower(NEW.authors);
    NEW.pub_pages = scrub_char(NEW.pub_pages);
    NEW.pub_mini_ref = scrub_char(get_pub_mini_ref(NEW.zdb_id));
    NEW.pub_can_show_images = get_pub_default_permissions(NEW.pub_jrnl_zdb_id);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER publication_trigger
BEFORE INSERT OR UPDATE ON publication
FOR EACH ROW
EXECUTE PROCEDURE publication();


