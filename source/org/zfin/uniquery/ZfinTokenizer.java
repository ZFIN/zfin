package org.zfin.uniquery;


import org.apache.lucene.analysis.CharTokenizer;

import java.io.Reader;


/**
 * Implements custom tokenization. LetterTokenizer is too strict and
 * breaks some words where we don't want to.
 * <p/>
 * NOTE: This tokenizer will probably only work well for ascii English text.
 */
public class ZfinTokenizer extends CharTokenizer {
    public ZfinTokenizer(Reader in) {
        super(in);
    }

    protected boolean isTokenChar(char c) {
        if (Character.isWhitespace(c)) {
            return false;
        }
        if (c == ':') {
            return false;
        }
        if (c == ';') {
            return false;
        }
        if (c == ',') {
            return false;
        }
        if (c == '.') {
            return false;
        }
        if (c == '(') {
            return false;
        }
        if (c == ')') {
            return false;
        }
        return true;
    }
}
