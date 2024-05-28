package org.zfin.datatransfer.ncbi;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import lombok.Setter;
import org.apache.commons.csv.*;
import org.zfin.datatransfer.ncbi.dto.LoadFileDTOInterface;

/**
 * Generic class for reading files that are downloaded from NCBI.
 * The class is parameterized by the DTO class that will be used to represent the data.
 * The DTO class must implement the LoadFileDTOInterface and have 2 constructors:
 *  one that accepts a String[] and one that accepts no arguments.
 * The class will read the file and return a list of DTOs.
 *
 * @param <T> The DTO class that will be used to represent the data.
 */
@Setter
public class LoadFileReader<T extends LoadFileDTOInterface> {
    private Constructor<T> dtoConstructor;
    private boolean validateHeaders = true;
    private List<String> expectedHeaders;

    public LoadFileReader(Class<T> dtoClass) {
        // Assumes all DTO classes have a constructor that accepts String[]
        // and a default constructor (so we can get the list of expected headers)
        try {
            this.dtoConstructor = dtoClass.getConstructor(String[].class);
            initializeExpectedHeaders(dtoClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error invoking constructor for DTO class " + dtoClass.getName(), e);
        }
    }

    /**
     * Reads a file and returns a list of DTOs.
     * @param file The file to read.
     * @return A list of DTOs.
     * @throws IOException If there is an error reading the file.
     */
    public List<T> readFile(File file) throws IOException {
        List<T> geneData = new ArrayList<>();
        CSVParser parser;
        if (validateHeaders) {
            parser = new CSVParser(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file)),
                            "UTF-8"),
                    CSVFormat.TDF.withFirstRecordAsHeader());
        } else {
            parser = new CSVParser(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file)),
                            "UTF-8"),
                    CSVFormat.TDF);
        }

        assertHeadersMatchExpectedHeaders(parser);

        for (CSVRecord record : parser) {
            String[] values = new String[record.size()];
            for (int i = 0; i < record.size(); i++) {
                values[i] = record.get(i);
            }
            try {
                T recordDTO = dtoConstructor.newInstance((Object) values); // Cast to Object to match varargs
                if (recordDTO.includeThisRecord()) {
                    geneData.add(recordDTO);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error constructing DTO", e);
            }
        }
        parser.close();
        return geneData;
    }

    /**
     * Initializes the expected headers for the DTO class. Invokes the default constructor and calls the
     * expectedHeaders method to verify the file to load matches the expected headers.
     * @param dtoClass
     */
    private void initializeExpectedHeaders(Class<T> dtoClass) {
        Constructor<T> defaultConstructor = null;
        try {
            defaultConstructor = dtoClass.getDeclaredConstructor();
            T tempInstance = defaultConstructor.newInstance();
            this.expectedHeaders = tempInstance.expectedHeaders();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asserts that the headers in the file match the expected headers defined on the DTO class.
     * @param parser The CSVParser to check.
     */
    private void assertHeadersMatchExpectedHeaders(CSVParser parser) {
        if (!validateHeaders) {
            return;
        }
        Map<String, Integer> headers = parser.getHeaderMap();
        //skip if no expected headers
        if (expectedHeaders == null) {
            return;
        }

        if (headers.size() != expectedHeaders.size()) {
            throw new IllegalArgumentException("Header mismatch");
        }
        for(Map.Entry<String, Integer> entry : headers.entrySet()) {
            Integer expectedOffset = entry.getValue();
            if (!entry.getKey().equals(expectedHeaders.get(expectedOffset))) {
                throw new IllegalArgumentException("Header mismatch");
            }
        }
    }
}

