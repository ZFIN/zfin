create function zero_pad_int(src int, width smallint)
  returning varchar(20)
  with (variant)
  external name
    "/private/lib/c_functions/zero_pad_int.so"
  language c
  end function;

