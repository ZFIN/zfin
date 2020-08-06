create or replace function get_expression_image_sort_order(imgZdbId text) returns integer as $$

    declare jtype text := '';
            authors text := '';
            score integer := 1;

    begin

    select publication.authors, publication.jtype
    into authors, jtype
    from publication
    inner join figure on figure.fig_source_zdb_id = publication.zdb_id
    inner join image on image.img_fig_zdb_id = figure.fig_zdb_id
    where image.img_zdb_id = imgZdbId;

    if (jtype = 'Unpublished') then
      if (substring(authors for 6) = 'Thisse') then
        score := 100;
      else
        score := 10;
      end if;
    end if;

    return score;

    end
$$ language plpgsql