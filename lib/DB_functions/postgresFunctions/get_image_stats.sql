create function
get_image_stats(
  filepath lvarchar)

  returning integer, integer;	-- width, height in pixels

  -- Given the name of a file containing an image, return the width and 
  -- height of the image in pixels.  Calls a shell script to get these values.
  -- If the shell script can't read the graphics file (it can't read .avi files
  -- for instance), then 0,0 is returned.
  --
  -- The shell script is invoked with the sysexec function.  The shell 
  -- script returns an lvarchar containing the width and height values 
  -- separated by a space.  This function splits that lvarchar into the 
  -- two integers returned by this routine.

  define scriptReturn       lvarchar;
  define width		    integer;
  define height		    integer;
  define col		    integer;

  let scriptReturn = sysexec("get_image_stats", filepath);
  
  let col = 1;
  while (substring(scriptReturn from col for 1) <> " ")
    let col = col + 1;
  end while

  let width  = substring(scriptReturn from 1 for col - 1)::integer;
  let height = substring(scriptReturn from col + 1)::integer;

  return width, height;

end function;
