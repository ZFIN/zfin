create or replace function get_pub_citation(pubZdbId text) returns text as $$

    declare citation text := '';
            authors text = '';
            pubYear varchar(15) = '';
            title text = '';
            journal text = '';
            volume text = '';
            pages text = '';

    begin

    select publication.authors, extract (year from pub_date), publication.title, jrnl_name, pub_volume, pub_pages
    into authors, pubYear, title, journal, volume, pages
    from publication
    left outer join journal on pub_jrnl_zdb_id = jrnl_zdb_id
    where zdb_id = pubZdbId;

    citation = authors ||
        coalesce(' (' || pubYear || ')', '') ||
        ' ' || title || '.' ||
        coalesce(' ' || journal || '.', '') ||
        coalesce(' ' || volume, '') ||
        coalesce(':' || pages, '');

    return citation;

    end $$
language plpgsql
