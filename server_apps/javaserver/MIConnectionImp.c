#include <StubPreamble.h>
#include <javaString.h>
#include <bool.h>
#include <mi.h>

#include <string.h>

#include "MIConnection.h"
#include "Statement.h"
#include "ResultSet.h"

struct Hjava_lang_Object* MIConnection_openConnection (struct HMIConnection *this, Hjava_lang_String* database, Hjava_lang_String* user, Hjava_lang_String* password)
{
    char *db, *us, *pass;
    MI_CONNECTION* conn;

	if (database != NULL)
		db = allocCString (database);
	else
		db = NULL;
	if (user != NULL)
		us = allocCString (user);
	else
		us = NULL;
	if (password != NULL)
		pass = allocCString (password);
	else
		pass = NULL;
	
    conn = mi_open (db, us, pass);
	
	if (pass != NULL)
		free (pass);
	if (us != NULL)
		free (us);
	if (db != NULL)
		free (db);
	
    if (conn == NULL)
    {
	return NULL;
    }
    unhand (this)->connection = (struct Hjava_lang_Object*) conn;
    return (struct Hjava_lang_Object*) conn;
}

void MIConnection_close (struct HMIConnection* this)
{
    if (unhand (this)->connection != NULL)
    {
	mi_close (unhand (this)->connection);
	unhand (this)->connection = NULL;
    }
}


static int getResult (MI_CONNECTION* conn)
{
    int result;
    while ((result = mi_get_result (conn)) == MI_DML)
	    /* Do nothing */ ;
    return result;
}

long Statement_doQuery (struct HStatement* this, Hjava_lang_String* statement)
{
    MI_CONNECTION* conn;
    int result;
    char *st;
    
    conn = (MI_CONNECTION*) (unhand (this)-> connection);
    st = allocCString (statement);
    result = mi_exec (conn, st, 0);
    free (st);
    if (result != MI_OK)
	return result;
    result = getResult (conn);
    if (result != MI_ROWS)
    {
	mi_query_finish (conn);
    }
    return result;
}

long ResultSet_getNext (struct HResultSet* this)
{
    MI_CONNECTION* conn;
    MI_ROW* result;
    MI_ROW_DESC* rDesc;
    int error;
    int intResult;

    if (unhand (this)->nomore == TRUE)
	return 0;
    
    conn = (MI_CONNECTION*) (unhand (this)-> connection);
    result = mi_next_row (conn, & error);
    while (result == NULL)
    {
	if (error == MI_ERROR)
	{
	    mi_query_finish (conn);
	    unhand (this)->nomore = FALSE;
	    unhand (this)->row = (struct Hjava_lang_Object*) NULL;
	    unhand (this)->rowDesc = (struct Hjava_lang_Object*) NULL;
	    return MI_ERROR;
	}
	else
	{
	    intResult = getResult (conn);
	    if (intResult == MI_NO_MORE_RESULTS)
	    {
		mi_query_finish (conn);
		unhand (this)->nomore = TRUE;
		return 0;
	    }
	    else if (intResult == MI_DDL)
	    {
		mi_query_finish (conn);
		unhand (this)->nomore = FALSE;
		unhand (this)->row = (struct Hjava_lang_Object*) NULL;
		unhand (this)->rowDesc = (struct Hjava_lang_Object*) NULL;
		return MI_DDL;
	    }
	}
	/* Only reach this if no error, more results coming, and not DDL result */
	result = mi_next_row (conn, & error);
    }
    rDesc = mi_get_row_desc (result);
    if (rDesc == NULL)
    {
	unhand (this)->nomore = FALSE;
	unhand (this)->row = (struct Hjava_lang_Object*) NULL;
	unhand (this)->rowDesc = (struct Hjava_lang_Object*) NULL;
	unhand (this)->colCount = -1;
	return MI_ERROR;
    }
    
    unhand (this)->nomore = FALSE;
    unhand (this)->row = (struct Hjava_lang_Object*) result;
    unhand (this)->rowDesc = (struct Hjava_lang_Object*) rDesc;
    unhand (this)->colCount = mi_column_count (rDesc);
    return MI_OK;
}

Hjava_lang_String* ResultSet_nativeGetString (struct HResultSet* this, long columnIndex)
{
    int result;
    char* retbuf;
    int retlen;

    if (columnIndex < 1 || columnIndex > unhand (this)->colCount)
	return NULL;
    result = mi_value (unhand (this)->row, columnIndex - 1, & retbuf, & retlen);
    switch (result)
    {
      case MI_NULL_VALUE:
	unhand (this)->wasnull = TRUE;
	unhand (this)->wascomposite = FALSE;
	return NULL;
	
      case MI_NORMAL_VALUE:
	unhand (this)->wasnull = FALSE;
	unhand (this)->wascomposite = FALSE;
 	return makeJavaString (retbuf, strlen (retbuf));
	
      case MI_ROW_VALUE:
	unhand (this)->wasnull = FALSE;
	unhand (this)->wascomposite = TRUE;
	return NULL;
	
      default:
	unhand (this)->wasnull = FALSE;
	unhand (this)->wascomposite = FALSE;
	return NULL;
    }
}

void ResultSet_close (struct HResultSet* this)
{
    if (unhand (this)-> connection != NULL)
    {
	mi_query_finish (unhand (this)->connection);
	unhand (this)->connection = NULL;
    }
}


long Statement_doUpdate (struct HStatement* this, Hjava_lang_String* statement)
{
    MI_CONNECTION* conn;
    int result;
    char *st;

    conn = (MI_CONNECTION*) (unhand (this)-> connection);
    st = allocCString (statement);
    result = mi_exec (conn, st, 0);
    free (st);
    if (result != MI_OK)
	return result;
    result = mi_get_result (conn);
/*    printf ("Result from mi_get_result for update was %d\n", result); */
    if (result == MI_DML)
	unhand (this)->count = mi_result_row_count (conn);
    else
	unhand (this)->count = 0;
    mi_query_finish (conn);
    return result;
}



