/*
 * Defines get_obj_type(zdb_id) function.
 *
 * Given a ZDB ID, returns the object type in the ZDB ID.  The object type 
 * is the second part of 4 part ZDB ID.  For example in the ZDB ID
 *
 *   ZDB-GENE-020304-1
 *
 * GENE is the object type.
 *
 * If an invalidly formatted ZDB ID is passed in then NULL is returned.
 * In order to be validly formatted, a ZDB ID must
 * 1. Start with "ZDB-"
 * 2. The "ZDB-" must be followed by 1 to MAX_OBJ_SIZE characters that 
 *    are followed by a "-"
 */

#define		_REENTRANT	/* copied from zextend.c, not sure why */

#include <string.h>
#include <mi.h>
#include <milib.h>

#define MAX_OBJ_SIZE  11	/* size of zobjtype_name column + 1 */

mi_lvarchar *
get_obj_type(mi_lvarchar *zdbId,
	     MI_FPARAM *fparam) 
{
  char objType[MAX_OBJ_SIZE];		
  char *zdbIdString;
  char *objPtr, *zdbIdPtr;

  zdbIdString = mi_lvarchar_to_string(zdbId);

  /* Start after leading "ZDB-" and go until next "-" */
  objPtr = objType;

  if (! strncmp(zdbIdString, "ZDB-", 4)) {
    /* starts with "ZDB-", keep going */    
    zdbIdPtr = zdbIdString + 4;

    while (*zdbIdPtr && 
	   *zdbIdPtr != '-' && 
	   objPtr - objType < MAX_OBJ_SIZE) {
      *objPtr = *zdbIdPtr;
      objPtr++;
      zdbIdPtr++;
    }
    if (*zdbIdPtr != '-' || objPtr == objType) {
      /* doesn't end in a hyphen or has zero length return null */
      mi_fp_setreturnisnull(fparam, 0, MI_TRUE);
    }
  }
  else {
    /* doesn't start with ZDB-, return null */
    mi_fp_setreturnisnull(fparam, 0, MI_TRUE);
  }
  *objPtr = '\0';
  mi_free(zdbIdString);
  return mi_string_to_lvarchar(objType);
}
