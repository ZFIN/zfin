/*
 * scrub_char 
 *
 * Scrubs an input character string for characters/conditions that should
 * not occur in most ZFIN char and varchar columns.  It removes any
 * thing that it finds.  This includes:
 *  o Stripping leading blanks
 *  o Stripping trailing blanks
 *  o Condensing multiple spaces/control characters in the middle of a string 
 *    into a single space
 *  o Stripping out all control characters.  This would include tabs, newlines,
 *    carriage returns and everything else between 0 and 040.
 *  o This condenses strings that contain only control characters and 
 *    spaces into the empty string.  It leaves empty strings alone.
 * 
 * The incoming string must be < 2048 characters long.  2048 is the limit of
 * LVARCHAR columns in IDS 9.3 and before.  This limit goes up to 32K in 
 * IDS 9.4.  The right thing to do here is to change the code to handle
 * incoming strings of ANY size.  However, I don't want to put the time in
 * to figure out how to allocate memory in a user defined function.
 *
 * This routine throws an exception if the scrubbed string is >= 2048 characters
 * in length.
 */


#define		_REENTRANT

#include <mi.h>

				/* WARNING: 2048 is hardcoded in 1 place below */
#define		MAXLEN		2048
#define		NO_MEMORY(fun)	mi_db_error_raise(NULL, MI_SQL,\
				"UGEN2", "FUNCTION%s", #fun, NULL)
#define		EXCEPTION(msg)	mi_db_error_raise (NULL, MI_EXCEPTION, msg)



mi_lvarchar *
scrub_char(mi_lvarchar *unscrubbed_lv) 
{
  unsigned char scrubbed[MAXLEN+1], *unscrubbed, *scrubbedCur, *unscrubbedCur;

  /* Why MAXLEN+1?  It simplifies the "embedded/trailing crap" code below,
   * which can increment scrubbedCur by 2.
   *  
   * I apologize for the obtuse code (and for hacks like MAXLEN+1).  Since 
   * this function is called by many triggers in ZFIN, I decided to make it 
   * as fast as I could, at the expense of readability. 
   */

  if (!(unscrubbed = (unsigned char *) mi_lvarchar_to_string(unscrubbed_lv))) {
    NO_MEMORY(scrub_char);
  }	
  unscrubbedCur = unscrubbed;
  scrubbedCur = scrubbed;

  /* strip leading crap */
  while (*unscrubbedCur && 
	 *unscrubbedCur <= ' ') {
    unscrubbedCur++;
  }

  /* remove embedded and trailing crap, condensing as we go */
  while (*unscrubbedCur &&
	 scrubbedCur - scrubbed < MAXLEN) {
    if (*unscrubbedCur > ' ') {
      *scrubbedCur = *unscrubbedCur;
      scrubbedCur++;
      unscrubbedCur++;
    }
    else { /* embedded/trailing crap found.  condense/remove it. */
      do {
	unscrubbedCur++;
      } while (*unscrubbedCur &&
	       *unscrubbedCur <= ' ');
      if (*unscrubbedCur) {
	*scrubbedCur = ' ';
	scrubbedCur++;
	*scrubbedCur = *unscrubbedCur;
	scrubbedCur++;
	unscrubbedCur++;
      }
    }
  }

  if (scrubbedCur - scrubbed < MAXLEN) {
    *scrubbedCur = '\0';
  }
  else {
    EXCEPTION("scrub_char: scrubbed string must be less than 2048 characters");
  }

  return mi_string_to_lvarchar((char *)scrubbed);
}
