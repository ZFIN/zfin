/**********************************************************************
 *  csoget.c -- make a CSO request look like an MITdir finger query
 *
 * Copyright 1994 by the Massachusetts Institute of Technology
 * For copying and distribution information, please see the file
 * <mit-copyright.h>.
 **********************************************************************/
#include "mit-copyright.h"

static char rcsid[]="$Header$";

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <string.h>
#include <ctype.h>

#define WRITE_OR_CONT(fd, buf, nbytes) if (write(fd, buf, nbytes) < 0) continue

int
csoget(query, host, origbuf, origbufsize)
     char *query, *host, *origbuf;
     int origbufsize;
{
  struct hostent *hp;
  struct sockaddr_in sname;
  u_short portnum;
  int sock, red=1, total=0, bufsize;
  char *remote_addr;
  char *buf;

  /* resolve hostname */
  hp = gethostbyname(host);
  if (!hp) return(-1);

  /* build socket name for connect() */
  memset(&sname, 0, sizeof(sname));
  sname.sin_family = AF_INET;
  memcpy(&(sname.sin_addr), hp->h_addr, hp->h_length);

  /* Try port 106 first (MITdir limiter), then 105 (standard port) */
  for (portnum=108; portnum >= 105; portnum -= 3)
    {
      sname.sin_port = htons(portnum);

      /* create socket */
      sock = socket(PF_INET, SOCK_STREAM, 0);

      /* connect */
      if (connect(sock, (struct sockaddr *)&sname, sizeof(sname)) < 0)
	continue;

      /* send ident */
      remote_addr = getenv("REMOTE_ADDR");
      if (remote_addr)
        {
          WRITE_OR_CONT(sock, "id NET ", 7);
          WRITE_OR_CONT(sock, remote_addr, strlen(remote_addr));
          WRITE_OR_CONT(sock, "\n", 1);
        }

      /* get motd */
      WRITE_OR_CONT(sock, "help web motd\n", 14);

      /* send query */
      WRITE_OR_CONT(sock, "query ", 6);
      WRITE_OR_CONT(sock, query, strlen(query));
      WRITE_OR_CONT(sock, "\nquit\n", 6);

      /* receive result */
      buf=origbuf;
      bufsize=origbufsize;
      do
        {
          /* read more bytes into remaining buffer */
          red = read(sock, buf, bufsize);
          if (red > 0)
	    {
	      buf += red;
	      bufsize -= red;
	      total += red;
	    }
        } while(bufsize > 1 && red > 0);

      /* Write end-of-string marker */
      buf[0] = '\0';
      close(sock);
      /*
       * Return success except for read error
       * or "Query refused" (an MITdir vagary)
       */
      if (red == 0 && !strstr(origbuf, "Query refused")) return(total);
  }
  return(-1);
}

/* Clean up the CSO results to look more like a finger query. */

int
finger(query, host, buf, bufsize)
     char *query, *host, *buf;
     int bufsize;
{
  char *textline1, *textline2, *colon1, *colon2;
  int old_seq=0, new_seq=0, repeat_line=0;

  /* Get the raw CSO results */
  if (csoget(query, host, buf, bufsize) == -1) return(-1);

  /* MITdir vagary: Results might already be cleaned up */
  if (!isdigit(buf[0]) && buf[0] != '-') return(strlen(buf));

  /* Clean each line */
  for(textline1=buf;
      textline1 > (char *) 1;
      textline1=strchr(textline1, '\012' /* LF */) + 1)
    {
      do
	{
	  repeat_line=0;

	  colon1=strpbrk(textline1, ":\012");
	  if (!colon1 || *colon1 == '\012') continue;
	  if (!strncmp(colon1-3, "200", 3)) /* query results */
	    {
	      colon2=strpbrk(1+colon1, ":\012");
	      if (!colon2 || *colon2 == '\012'	/* "Ok." or "Bye!" */
		  || !strncmp(colon2, ":motd:", 6)) /* skip motd */
		{
		  textline2=strchr(1+colon1, '\012' /* LF */);
		  if (textline2)
		    {
		      /* Remove everything */
		      strcpy(textline1, textline2+1);
		      repeat_line=1; /* don't skip a line */
		    }
		  else
		    {
		      *textline1='\0';
		      break;
		    }
		}
	      else
		{
		  /* insert extra newline beginning of each result */
		  new_seq=atoi(1+colon1);
		  if (old_seq != new_seq)
		    {
		      old_seq = new_seq;
		      *(textline1++) = '\012';
		    }
		  /* Skip the -200:4: part */
		  strcpy(textline1, 1+colon2);
		}
	    }
	  else
	    {
	      /* Line like "102:There was 1 match to your request"
		 or error or whatever */
	      strcpy(textline1, 1+colon1);
	    }
	} while (repeat_line && *textline1);
    }
  return(strlen(buf));
}

#ifdef TEST_CSOGET

main(argc, argv)
     int argc;
     char *argv[];
{
  char buf[BUFSIZ];
  static char *sep="--------------------------------------------------------";

  puts(sep);
  csoget(argv[1], argv[2], buf, BUFSIZ);
  puts(buf);
  puts(sep);
  finger(argv[1], argv[2], buf, BUFSIZ);
  puts(buf);
  puts(sep);
  exit(0);
}

#endif
