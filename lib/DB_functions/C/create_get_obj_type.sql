create function get_obj_type(src lvarchar)
  returning lvarchar
  with (not variant)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/get_obj_type.so"
  language c
  end function;
