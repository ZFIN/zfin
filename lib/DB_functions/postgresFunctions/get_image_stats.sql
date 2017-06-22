create or replace function get_image_stats(
  filepath text, OUT width int, out height int)
as $image$

  -- Given the name of a file containing an image, return the width and 
  -- height of the image in pixels.  Calls a shell script to get these values.
  -- If the shell script can't read the graphics file (it can't read .avi files
  -- for instance), then 0,0 is returned.
  --
  -- The shell script is invoked with the sysexec function.  The shell 
  -- script returns an lvarchar containing the width and height values 
  -- separated by a space.  This function splits that lvarchar into the 
  -- two integers returned by this routine.

  declare scriptReturn       text;
   	  col		    integer;
  begin 

   scriptReturn = sysexec("get_image_stats", filepath);
  
   col = 1;

   while (substring(scriptReturn from col for 1) <> " ") loop
     	 col = col + 1;
   end loop;

   width  = substring(scriptReturn from 1 for col - 1)::integer;
   height = substring(scriptReturn from col + 1)::integer;

  end
$image$ LANGUAGE plpgsql;

