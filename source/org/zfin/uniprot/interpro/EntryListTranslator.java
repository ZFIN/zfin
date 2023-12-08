package org.zfin.uniprot.interpro;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.dto.InterProProteinDTO;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class is used to convert the InterPro entry.list file into DTOs
 */
@Log4j2
public class EntryListTranslator {

    public static List<InterProProteinDTO> parseFile(File file) throws FileNotFoundException {
        List<InterProProteinDTO> results = new ArrayList<>();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split("\t");
            //skip header
            if (splitLine[0].equals("ENTRY_AC")) {
                continue;
            }
            results.add(new InterProProteinDTO(splitLine[0], splitLine[1], splitLine[2]));
        }
        scanner.close();
        return results;
    }

}














