create function get_obj_type(src lvarchar)
  returning varchar(10)
  with (not variant)
  external name
    "/private/lib/c_functions/get_obj_type.so"
  language c
  end function;
