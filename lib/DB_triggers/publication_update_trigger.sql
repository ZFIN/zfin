create trigger publication_update_trigger update of
    title,accession_no , pub_doi, pubmed_authors, authors,
    pub_mini_ref, jtype, pub_pages, zdb_id
    on publication referencing new as new_publication
    for each row
        (
        execute function scrub_char(new_publication.title
    ) into publication.title,
        execute function scrub_char(new_publication.accession_no
    ) into publication.accession_no,
        execute function scrub_char(new_publication.pub_doi
    ) into publication.pub_doi,
        execute function scrub_char(new_publication.pubmed_authors
    ) into publication.pubmed_authors,
        execute function lower(new_publication.authors
    ) into publication.pub_authors_lower,
        execute function scrub_char(new_publication.jtype
    ) into publication.jtype,
        execute function get_pub_mini_ref(new_publication.zdb_id
    ) into publication.pub_mini_ref,
        execute function scrub_char(new_publication.pub_mini_ref
    ) into publication.pub_mini_ref,
        execute function scrub_char(new_publication.pub_pages
    ) into publication.pub_pages);