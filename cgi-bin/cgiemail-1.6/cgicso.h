/**********************************************************************
 *  cgicso.h -- header for libcgi CSO stuff
 *
 * Copyright 1994 by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#ifndef _CGICSO_H
#define _CGICSO_H
#include "mit-copyright.h"

/* Some sites restrict finger access by IP address.
 * At such sites, allowing any host to be specified by the HTML form
 * is a security hole, and the following information must be hard-coded.
 */

#define CGI_CSO_HARDCODE
#define CGI_CSO_FINGERHOST "mitdir.mit.edu"

#endif /* _CGICSO_H */
