create function scrub_char(src lvarchar)
  returning lvarchar
  with (not variant, parallelizable)
  external name
    "/private/lib/c_functions/scrub_char.so"
  language c
  end function;
