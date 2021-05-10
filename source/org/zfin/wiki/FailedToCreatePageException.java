package org.zfin.wiki;

/**
 */
public class FailedToCreatePageException extends Exception{
    public FailedToCreatePageException(String pageTitle) {
        super(pageTitle) ;
    }
}
