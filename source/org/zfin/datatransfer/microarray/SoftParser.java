package org.zfin.datatransfer.microarray ;

import java.util.Set;

/**
 */
public interface SoftParser {

    String parseLine(String line) ;

    Set<String> parseUniqueNumbers(String fileName, int column) ;

    Set<String> parseUniqueNumbers(String fileName, int column,String[] includePattern)  ;

    Set<String> parseUniqueNumbers(String fileName, int column,String[] includePattern,String[] excludePattern)  ;

    void setAlwaysUseExistingFile(boolean alwaysUseExistingFile) ;

    boolean isAlwaysUseExistingFile() ;
}
