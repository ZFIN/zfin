/**********************************************************************
 *  cgicso -- send cso via cgi
 *
 * Copyright 1994 by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#include "mit-copyright.h"

#include <stdlib.h>
#include "cgi.h"

main() { exit(  cgi_standard_cso()  ); }
