create function get_id(src lvarchar)
  returning varchar(50)
  with (variant)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/get_id.so"
  language c
  end function;
