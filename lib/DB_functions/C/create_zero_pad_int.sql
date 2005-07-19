create function zero_pad_int(src int, width int)
  returning varchar(20)
  with (variant)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/zero_pad_int.so"
  language c
  end function;
