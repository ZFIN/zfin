/*
 * Defines zero_pad_int() function.
 * 
 * Input:
 *    unsigned int ::  positive integer for padding
 *    int          ::  width of the string with padding
 *
 * Output:
 *    mi_varchar   ::  padded string  
 *
 */


#include <mi.h>
#include <milib.h>

#define		EXCEPTION(msg)	mi_db_error_raise (NULL, MI_EXCEPTION, msg)

mi_lvarchar *
zero_pad_int (int unpaddedInt, short int width)
{
  char outputString[width + 1];
  int i;   /* index for array outputString[] */

  if (unpaddedInt < 0)
    EXCEPTION ("The padding integer has to be positive.");

  else if (width < 0)
    EXCEPTION ("The padding width has to be positive.");

  else {
    /* initalize the array for safety */
    for ( i = 0; i <= width; i++ )
      outputString[i] = '\0';
    
    /* Read the right most digit one at a time and shift right the input 
       integer. Add '0' to meet the width if needed.   
    */
    for (i = width - 1; i >= 0; i--) {
      outputString[i] =  (unpaddedInt % 10) + '0';
      unpaddedInt /= 10;
    }
    
    if (unpaddedInt)
      EXCEPTION ("The integer exceeded the padding width.");
    
    else 
      return (mi_string_to_lvarchar(outputString));
  }
}
