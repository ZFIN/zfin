create function get_id(src varchar(50))
  returning varchar(50)
  with (variant)
  external name
    "/private/lib/c_functions/get_id.so"
  language c
  end function;
