package org.zfin.datatransfer.microarray ;

import java.util.Set;

/**
 */
public interface SoftParser {

    public String parseLine(String line) ;

    public Set<String> parseUniqueNumbers(String fileName, int column) ;

    public Set<String> parseUniqueNumbers(String fileName, int column,String[] includePattern)  ;

    public Set<String> parseUniqueNumbers(String fileName, int column,String[] includePattern,String[] excludePattern)  ;
}
