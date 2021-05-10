package org.zfin.wiki;

/**
 */
public class PageDoesNotExistException extends Exception {

    public PageDoesNotExistException(String title){
        super("Page ["+title+"] can not be found.") ;
    }
}
