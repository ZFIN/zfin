create function zero_pad(src lvarchar)
  returning lvarchar
  with (variant)
  external name
    "/private/lib/c_functions/zero_pad.so"
  language c
  end function;
