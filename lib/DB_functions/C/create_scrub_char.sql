create function scrub_char(src lvarchar)
  returning lvarchar
  with (not variant, parallelizable)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/scrub_char.so"
  language c
  end function;
