DROP TRIGGER IF EXISTS publication_update_trigger
ON publication;

CREATE OR REPLACE FUNCTION publication_update()
  RETURNS trigger AS $$
  BEGIN
    NEW.title = scrub_char(NEW.title);
    NEW.accession_no = scrub_char(NEW.accession_no);
    NEW.pubmed_authors = scrub_char(NEW.pubmed_authors);
    NEW.pub_doi = scrub_char(NEW.pub_doi);
    NEW.jtype = scrub_char(NEW.jtype);
    NEW.authors = scrub_char(NEW.authors);
    NEW.pub_authors_lower = lower(NEW.authors);
    NEW.pub_pages = scrub_char(NEW.pub_pages);
    NEW.pub_mini_ref = scrub_char(get_pub_mini_ref(NEW.zdb_id));
    NEW.keywords = scrub_char(NEW.keywords);
    NEW.pub_acknowledgment = scrub_char(NEW.pub_acknowledgment);
    NEW.pub_volume = scrub_char(NEW.pub_volume);
    NEW.pub_errata_and_notes = scrub_char(NEW.pub_errata_and_notes);
    RETURN NEW;
  END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER publication_update_trigger
BEFORE  UPDATE ON publication
FOR EACH ROW
EXECUTE PROCEDURE publication_update();


