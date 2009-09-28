package org.zfin.wiki;

/**
 * Exception thrown when failed to login to wiki.
 */
public class WikiLoginException extends Exception {

    public WikiLoginException(Exception e) {
        super("Failed to login to wiki", e);
    }

    public WikiLoginException(String message) {
        super(message);
    }
}
