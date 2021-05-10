package org.zfin.util;

import java.util.Collection;

/**
 * Class that allows to create formatted string lists from string items.
 * Default delimiter between two items is a comma and a space <, >.
 * The last delimiter is removed when the composed string is retrieved.
 */
public class ListFormatter {

    public final static char SINGLE_QUOTE = '\'';
    private StringBuilder builder;
    private String delimiter = ", ";
    // default escape character is empty
    private char escapeCharacter;

    public ListFormatter() {
    }

    public ListFormatter(String delimiter) {
        this.delimiter = delimiter;
    }

    public ListFormatter(char escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public ListFormatter(String delimiter, char escapeCharacter) {
        this.delimiter = delimiter;
        this.escapeCharacter = escapeCharacter;
    }

    /**
     * Add individual strings to the list and they get concatenated to the internal
     * string as configured by the constructor.
     *
     * @param item String
     */
    public void addItem(String item) {
        if (builder == null) {
            builder = new StringBuilder();
        }
        if (escapeCharacter != '\u0000')
            builder.append(escapeCharacter);
        builder.append(item);
        if (escapeCharacter != '\u0000')
            builder.append(escapeCharacter);
        builder.append(delimiter);
    }

    /**
     * Accept a list of strings and add the strings in their list order.
     *
     * @param list Collection
     */
    public void addStringList(Collection<String> list) {
        for (String item : list) {
            addItem(item);
        }
    }

    /**
     * Retrieve the formatted string after having added the items.
     *
     * @return String
     */
    public String getFormattedString() {
        if (builder == null)
            return "";

        int builderLength = builder.length();
        int delimiterLength = delimiter.length();
        if (builderLength > delimiterLength + 1) {
            // Remove the last delimiter
            builder.delete(builderLength - delimiterLength, builderLength);
        }
        return builder.toString();

    }
}
