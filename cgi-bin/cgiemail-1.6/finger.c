/**********************************************************************
 *  finger.c -- get a finger query
 *
 * Copyright 1994 by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#include "mit-copyright.h"

static char rcsid[]="$Header$";

#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <string.h>

int
finger(query, host, buf, bufsize)
     char *query, *host, *buf;
     int bufsize;
{
  struct hostent *hp;
  struct sockaddr_in sname;
  struct servent *sp;
  int sock, red=1, total=0;

  /* resolve hostname */
  hp = gethostbyname(host);
  if (!hp) return(-1);

  /* resolve portname */
  sp = getservbyname("finger", "tcp");

  /* build socket name for connect() */
  memset(&sname, 0, sizeof(sname));
  sname.sin_family = AF_INET;
  memcpy(&(sname.sin_addr), hp->h_addr, hp->h_length);
  sname.sin_port = sp->s_port;

  /* create socket */
  sock = socket(PF_INET, SOCK_STREAM, 0);

  /* connect */
  if (connect(sock, &sname, sizeof(sname)) < 0) return(-1);

  /* send query */
  write(sock, query, strlen(query));
  write(sock, "\n", 1);

  /* receive result */
  while(bufsize > 1 && red > 0)
    {
      /* read more bytes into remaining buffer */
      red = read(sock, buf, bufsize);
      if (red > 0)
	{
	  buf += red;
	  bufsize -= red;
	  total += red;
	}
    }
  buf[0] = '\0';
  close(sock);
  return(total);
}
