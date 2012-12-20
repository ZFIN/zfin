/*
 * Defines get_time() function.
 *
 */
#include <sys/time.h>
#include <time.h>
#include <string.h>
#include <mi.h>
#include <milib.h>

mi_lvarchar *
get_time() 
{
  time_t timer;
  struct timeval tv;
  char   tbuf[30], sbuf[20], msbuf[10];

  time(&timer);
  gettimeofday(&tv, NULL);

  strftime(sbuf, 20, "%T", localtime(&timer));
 
  sprintf(msbuf, "%d", tv.tv_usec/1000);
  sprintf(tbuf, "%s.%s", sbuf, msbuf);

  return (mi_string_to_lvarchar(tbuf));

}
