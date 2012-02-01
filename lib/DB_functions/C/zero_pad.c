/*
 * Defines zero_pad(unpadded) function.
 *
 * This routine exists to allow us to sort strings with embedded numbers
 * in them.  This pads any embedded number string with leading zeros, out
 * to a predefined width.  It also converts the string to lower case.  Thus
 *
 *   hox2a45, and
 *   Hox5a2a
 *   hox10a123
 *
 * become
 *
 *   hox0000000002a0000000045
 *   hox0000000005a0000000002a
 *   hox0000000010a0000000123
 *
 * The first set will not sort in the order you want them to, while the second
 * pair will.
 *
 * Note that this routine will stumble on negative and floating point numbers.
 */

#define		_REENTRANT      /* copied from zextend.c, not sure why */

#include <string.h>
#include <ctype.h>
#include <mi.h>
#include <milib.h>

#define         NO_MEMORY(fun)  mi_db_error_raise(NULL, MI_SQL,\
                                "UGEN2", "FUNCTION%s", #fun, NULL)
#define         EXCEPTION(msg)  mi_db_error_raise (NULL, MI_EXCEPTION, msg)
#define         DEFAULT_PADDING 10
#define		MAXLEN		1000


mi_lvarchar *
zero_pad(mi_lvarchar *unpaddedLvar)
{
  char *unpaddedStr, *unpaddedPtr, *paddedPtr, *digitPtr;
  char paddedStr[MAXLEN], errorText[MAXLEN];
  int numberWidth, paddingWidth, paddingIdx;

  /* Convert from LVARCHAR to C string */
  if (!(unpaddedStr = mi_lvarchar_to_string(unpaddedLvar))) {
    NO_MEMORY(zero_pad);
  }

  /* Walk the string, copying non-numbers, padding numbers */
  unpaddedPtr = unpaddedStr;
  paddedPtr = paddedStr;

  while (*unpaddedPtr) {

    if (isdigit(*unpaddedPtr)) {
      /* Found an embedded number; scan to end of it */
      digitPtr = unpaddedPtr;
      unpaddedPtr++;
      while (isdigit(*unpaddedPtr)) {
	unpaddedPtr++;
      }
      /* insert leading zeros */
      numberWidth = unpaddedPtr - digitPtr;
      /* At this point we could check if the number is wider than
       * DEFAULT_PADDING, and then throw an exception.  However, this code
       * ignores that case and just copies the full number from the unpadded
       * to the padded string.
       */
      paddingWidth = DEFAULT_PADDING - numberWidth;
      for (paddingIdx = 0;
	   paddingIdx < paddingWidth;
	   paddingIdx++ ) {
	*paddedPtr = '0';
	paddedPtr++;
      }
      /* copy number */
      while (digitPtr != unpaddedPtr) {
	*paddedPtr = *digitPtr;
	digitPtr++;
	paddedPtr++;
      }
    }
    else {      /* just a regular character, copy it. */

      /* special case to ignore the '*' character
         if the current char is '*', move up in the
         unpaddedPtr string without changing the
         paddedPtr at all */
      if (*unpaddedPtr != '*') {
	*paddedPtr = tolower(*unpaddedPtr);
	paddedPtr++;
      }
      unpaddedPtr++;

    }
    if (paddedPtr - paddedStr > MAXLEN) {
      /* ERROR Condition:  Padded string is too long */
      sprintf (errorText,
	       "Padded string overran maximum string length of %d", MAXLEN);
      EXCEPTION(errorText);
    }
  }
  *paddedPtr = '\0';
  return mi_string_to_lvarchar(paddedStr);
}
