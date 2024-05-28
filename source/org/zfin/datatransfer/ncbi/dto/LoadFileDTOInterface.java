package org.zfin.datatransfer.ncbi.dto;

import java.util.List;

/**
 * Interface for DTOs that are used to load files from NCBI.
 * Implementing classes should provide a list of expected headers and a method to determine if the record should be included.
 * The constructors are called through reflection and should accept a String[] of values.
 * Also it should have a default constructor (used for calling expectedHeaders on an instance).
 */
public interface LoadFileDTOInterface {
    public List<String> expectedHeaders();
    public boolean includeThisRecord();
}
