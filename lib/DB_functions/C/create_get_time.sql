create function get_time()
  returning varchar(30)
  with (not variant)
  external name
    "/private/lib/c_functions/get_time.so"
  language c
  end function;
