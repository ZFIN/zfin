package org.zfin.infrastructure;

/**
 * Exception for ZDB IDs.
 */
public class InvalidZdbID extends RuntimeException {

    public static final String NULL_MESSSAGE = "'Null' is not a valid zdb active source ID";
    public static final String INCORRECT_PREFIX_MESSSAGE = " does not start with ";
    public static final String INCORRECT_TYPE_MESSSAGE = " has the wrong type. Allowed values are: ";

    public InvalidZdbID() {
        super(NULL_MESSSAGE);
    }

    public InvalidZdbID(String zdbID) {
        super(zdbID + INCORRECT_PREFIX_MESSSAGE + " <ZDB->.");
    }

    public InvalidZdbID(String zdbID, String allowedValues) {
        super(zdbID + INCORRECT_TYPE_MESSSAGE + allowedValues);
    }
}
