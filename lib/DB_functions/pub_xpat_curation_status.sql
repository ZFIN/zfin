create or replace function 
pub_xpat_curation_status (
  pubZdbId       text)

  returns varchar(25) as $xpat_cur_status$ 

  -- Returns one of four cuartion status. 
  --  1. No Expression Curation  
  --  2. Text Only Curated
  --  3. Light Figure Curated
  --  4. Full Figure Curated
  --
  -- A -746 error is returned if
  --   The parameter is null.  
  --   The parameter is not pub zdb_id  
 

  declare xpat_cur_status  varchar(25);
   zdb_pub_count       integer;
   figure_count        integer;
   text_fig_count      integer;
   caption_fig_count   integer;
   image_fig_count     integer;

begin
  
  -- Check that the parameter is not null
  if (pubZdbId = '') then
    raise exception 'Parameter is null';
  end if;

  
  -- Check that the parameter is a pub zdb_id
  select count(*)
    into zdb_pub_count
    from publication
    where zdb_id = pubZdbId;
    
  if (zdb_pub_count = 0) then
    raise exception 'Parameter is not in the Publication table';
  end if;
  
  
  select count(*)
    into figure_count
    from figure
    where fig_source_zdb_id = pubZdbId;
    
  if (figure_count != 0) then

    select count(*)
      into text_fig_count
      from figure
      where fig_source_zdb_id = pubZdbId
        and fig_label = 'text only';
    
    select count(*)
      into caption_fig_count
      from figure
      where fig_source_zdb_id = pubZdbId
        and fig_caption is not null
        and fig_caption <> ''
        and fig_label <> 'text only';

    select count(*)
      into image_fig_count
      from figure, image
      where fig_source_zdb_id = pubZdbId
        and fig_zdb_id = img_fig_zdb_id;
  

    if (image_fig_count + caption_fig_count != 0) then
       xpat_cur_status = 'Full Figure Curated';
    
    else

      if (figure_count - text_fig_count = 0) then

         xpat_cur_status = 'Text Only Curated';

      else

         xpat_cur_status = 'Light Figure Curated';

      end if ;   
      
    end if ; 
  
  else
  
     xpat_cur_status = "No Expression Curation";
  
  end if;

  return xpat_cur_status;

end 

$xpat_cur_status$ LANGUAGE plpgsql
