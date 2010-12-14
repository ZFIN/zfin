package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * StringUtils version for GWT.
 */
public class StringUtils implements IsSerializable {
    public static final String NULL = "null";

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * <p/>
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }


    /**
     * <p>Checks if a trimmed String is empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isEmptyTrim(null)      = true
     * StringUtils.isEmptyTrim("")        = true
     * StringUtils.isEmptyTrim(" ")       = true
     * StringUtils.isEmptyTrim("bob")     = false
     * StringUtils.isEmptyTrim("  bob  ") = false
     * </pre>
     * <p/>
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmptyTrim(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     *
     * <pre>
     * StringUtils.isNotEmptyTrim(null)      = false
     * StringUtils.isNotEmptyTrim("")        = false
     * StringUtils.isNotEmptyTrim(" ")       = false
     * StringUtils.isNotEmptyTrim("bob")     = true
     * StringUtils.isNotEmptyTrim("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmptyTrim(String str) {
        return !StringUtils.isEmptyTrim(str);
    }


    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }

    /**
     * <p>Compares two Strings, returning <code>true</code> if they are equal.</p>
     * <p/>
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case sensitive.</p>
     * <p/>
     * <pre>
     * StringUtils.equals(null, null)   = true
     * StringUtils.equals(null, "abc")  = false
     * StringUtils.equals("abc", null)  = false
     * StringUtils.equals("abc", "abc") = true
     * StringUtils.equals("abc", "ABC") = false
     * </pre>
     *
     * @param str1 the first String, may be null
     * @param str2 the second String, may be null
     * @return <code>true</code> if the Strings are equal, case sensitive, or
     *         both <code>null</code>
     * @see java.lang.String#equals(Object)
     */
    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    /**
     * Checks if a string is 'null' and treats it like null;
     * Needed for bizarre behavior in GWT
     *
     * @param one string
     * @param two string
     * @return boolean
     */
    public static boolean equalsWithNullString(String one, String two) {
        if (one != null && one.equals(NULL))
            one = null;
        if (two != null && two.equals(NULL))
            two = null;
        return equals(one, two);
    }

}
