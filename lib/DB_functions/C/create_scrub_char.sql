create function scrub_char(src varchar(255))
  returning varchar(244)
  with (not variant, parallelizable)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/scrub_char.so"
  language c
  end function;
