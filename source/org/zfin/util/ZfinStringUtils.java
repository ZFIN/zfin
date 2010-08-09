package org.zfin.util;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtils {

    /**
     * Replace a given string replace in a given string text with a string with.
     * <p/>
     * Example:
     * StringUtils.replace("AbCDeF", "abCD", gWE)       = "gweef"
     *
     * @param text    string
     * @param replace string
     * @param with    string
     * @return string
     */
    public static String recplaceCaseInsensitive(String text, String replace, String with) {
        if (text == null)
            return null;

        if (replace == null)
            return text;

        String replaceLowerCase = replace.toLowerCase();

        // get all the case insensitive matches
        String textLowerCase = text.toLowerCase();
        int position = textLowerCase.indexOf(replaceLowerCase);


        char[] characters = replace.toCharArray();
        for (char character : characters) {
    //        if (Character.isLowerCase(character))
//                char lowerCase = Character.toLowerCase(character);
  //          text = StringUtils.replaceChars(text, lowerCase, )
        }
        return null;
    }
}
