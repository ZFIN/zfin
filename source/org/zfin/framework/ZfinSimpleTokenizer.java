package org.zfin.framework;

import freemarker.template.utility.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Tokenize a given text via the StandardAnalyzer logic from Lucene.
 * It's meant to be just a wrapper for the awkward Token class that does not really allow for
 * retrieving the tokens as strings.
 * Use this Wrapper class only for short amount of text as it is not designed to perform speedy with
 * big text streams.
 */
public class ZfinSimpleTokenizer implements Iterator<String> {

    private String text;
    private List<Token> tokenList = new ArrayList<Token>();
    private List<String> stringList = new ArrayList<String>();
    private int index;

    //replace commas within names with the @ symbol?
    private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?\\\"\\,]";
    private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
    private static final String REPLACEMENT_STRING = "\\\\$0";
    private static final List<String> charactersToRemove = Arrays.asList("-", ":", "\\(", "\\)", "\\[", "\\]", "\\.");

    private static Logger logger = Logger.getLogger(ZfinSimpleTokenizer.class);

    public ZfinSimpleTokenizer(String text) {
        this.text = text;
        Analyzer analyzer = new WhitespaceAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
        try {
            Token token;
            while ((token = tokenStream.next()) != null) {
                tokenList.add(token);
                String tokenString = token.termText();
                String replacedCharacters = replaceSpecialCharactersWithWhiteSpaces(tokenString);
                if (!stringList.contains(replacedCharacters) && StringUtils.isNotEmpty(tokenString))
                    Collections.addAll(stringList, getWhiteSpaceTokens(replacedCharacters));
            }
        } catch (IOException e) {
            logger.error("Error while getting next token from token stream!");
        }
    }

    private String[] getWhiteSpaceTokens(String text) {

        //on the back end, commas are replaced by spaces because of the multiset stuff
        text = text.replace(",", " ");

        String[] tokens = text.split(" ");
        List<String> nonEmptyTokens = new ArrayList<String>(tokens.length);
        for (String token : tokens)
            if (StringUtils.isNotEmpty(token))
                nonEmptyTokens.add(token);
        return nonEmptyTokens.toArray(new String[nonEmptyTokens.size()]);
    }

    /**
     * Escape lucene specific characters
     *
     * @param token string to be worked on.
     * @return replaced string
     */
    public static String getSpecialCharacterReplacement(String token) {
        //a special replacement because escaping a colon makes JDBC think that
        //it's a query parameter
        token = token.replace(":", "$");

        //the informix escape for single quotes is necessary here
        token = token.replace("'", "''");

        String escaped = LUCENE_PATTERN.matcher(token).replaceAll(REPLACEMENT_STRING);
        return escaped;
    }


    public static String replaceSpecialCharactersWithWhiteSpaces(String token) {
        for (String removeMe : charactersToRemove)
            token = token.replaceAll(removeMe, " ");
        return token;
    }


    public String[] getTokens(String value) {
        if (value.contains("-")) {
            return getTokensByCharacter(value, "-");
        }
        if (value.contains(":")) {
            return getTokensByCharacter(value, ":");
        }
        if (Character.isDigit(value.charAt(0))) {
            char[] characters = value.toCharArray();
            StringBuilder charToken = new StringBuilder();
            int index = 0;
            String numeralString = null;
            for (char character : characters) {
                if (Character.isDigit(character) || Character.toLowerCase(character) == '.') {
                    charToken.append(character);
                    index++;
                } else {
                    numeralString = charToken.toString();
                    break;
                }
            }
            if (index == value.length())
                return new String[]{charToken.toString()};
            else
                return new String[]{numeralString, value.substring(index)};
        }
        return new String[]{value};
    }

    private String[] getTokensByCharacter(String value, String tokenCharacter) {
        String[] values = value.split(tokenCharacter);
        List<String> tokens = new ArrayList<String>();
        for (String individualToken : values) {
            Collections.addAll(tokens, getTokens(individualToken));
        }
        String[] returnArray = new String[tokens.size()];
        return tokens.toArray(returnArray);
    }

    public boolean hasNext() {
        int numberOfStringTokens = stringList.size();
        return index < numberOfStringTokens;
    }

    /**
     * Retrieve the next element and increment the internal index.
     *
     * @return
     */
    public String next() {
        return stringList.get(index++);
    }

    public int getNumberOfTokens() {
        return stringList.size();
    }

    public String getText() {
        return text;
    }

    public int getIndex() {
        return index;
    }

    public List<String> getTokenList() {
        return stringList;
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove an element from this iterator.");
    }
}
