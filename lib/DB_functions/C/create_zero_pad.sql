create function zero_pad(src lvarchar)
  returning lvarchar
  with (variant)
  external name
    "<!--|ROOT_PATH|-->/lib/DB_functions/zero_pad.so"
  language c
  end function;
