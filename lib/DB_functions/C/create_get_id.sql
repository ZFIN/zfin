create function get_id(src lvarchar)
  returning lvarchar
  with (variant)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/get_id.so"
  language c
  end function;
