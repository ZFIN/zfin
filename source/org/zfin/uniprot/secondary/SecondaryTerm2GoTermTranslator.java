package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * This class is used to convert the InterPro to GO translation file into a list of InterPro2GoTerm objects.
 */
@Log4j2
public class SecondaryTerm2GoTermTranslator {
    public enum SecondaryTermType {
        InterPro,
        EC,
        UniProtKB,
    }

    public static List<SecondaryTerm2GoTerm> convertTranslationFileToUnloadFile(String ipToGoTranslationFile, SecondaryTermType fileType) throws FileNotFoundException {
        if (fileType == SecondaryTermType.InterPro || fileType == SecondaryTermType.EC) {
            return parseIP2GoOrEC2GOTerms(ipToGoTranslationFile, fileType);
        } else if (fileType == SecondaryTermType.UniProtKB) {
            return parseSPKW2GOTerms(ipToGoTranslationFile);
        } else {
            throw new RuntimeException("Unknown file type: " + fileType);
        }
    }

    private static List<SecondaryTerm2GoTerm> parseIP2GoOrEC2GOTerms(String ipToGoTranslationFile, SecondaryTermType fileType) throws FileNotFoundException {
        List<String> badGoIDs = getGoIDsToIgnore();

        Map<String, GenericTerm> allGoIDs = getOntologyRepository().getGoTermsToZdbID();

        List<SecondaryTerm2GoTerm> results = new ArrayList<>();
        Scanner scanner = new Scanner(new File(ipToGoTranslationFile));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.startsWith(fileType.name())) {
                String[] splitLine = line.split(" > ");
                String[] ip = splitLine[0].split("[: ]");
                String[] termId = splitLine[1].split(" ; ");
                String[] term = termId[0].split("GO:");
                String[] id = termId[1].split(":");

                if (!badGoIDs.contains(termId[1])) {
                    GenericTerm termObject = allGoIDs.get(termId[1]);
                    results.add(new SecondaryTerm2GoTerm(ip[1],"", term[1], id[1], termObject == null ? null : termObject.getZdbID()));
                }
            }
        }
        scanner.close();
        return results;
    }


    private static List<SecondaryTerm2GoTerm> parseSPKW2GOTerms(String convertToGoTranslationFile) throws FileNotFoundException {
        List<String> badGoIDs = getGoIDsToIgnore();

        Map<String, GenericTerm> allGoIDs = getOntologyRepository().getGoTermsToZdbID();

        List<SecondaryTerm2GoTerm> results = new ArrayList<>();
        Scanner scanner = new Scanner(new File(convertToGoTranslationFile));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.startsWith(SecondaryTermType.UniProtKB.name())) {
                String[] splitLine = line.split(" > ");
                String spIDandName = splitLine[0];

                String[] spkw = spIDandName.split(" ");
                String[] spkwId = spkw[0].split(":");
                String spID = spkwId[1];

                String[] termId = splitLine[1].split(" ; ");
                String[] term = termId[0].split("GO:");
                String[] id = termId[1].split(":");

                String[] spkwNamePieces = spIDandName.split(spID);
                String spkwName = spkwNamePieces[1].trim(); // This will remove both leading and trailing spaces

                if (!badGoIDs.contains(termId[1])) {
                    GenericTerm termObject = allGoIDs.get(termId[1]);
                    SecondaryTerm2GoTerm entry = new SecondaryTerm2GoTerm(spID, spkwName, term[1], id[1], termObject == null ? null : termObject.getZdbID());
                    results.add(entry);
                }
            }
        }
        scanner.close();
        return results;
    }

    private static List<String> getGoIDsToIgnore() {
        List<String> ignoreGoIDs = new ArrayList<>();
        ignoreGoIDs.addAll(getOntologyRepository()
                .getObsoleteAndSecondaryTermsByOntologies(Ontology.GO_MF, Ontology.GO_BP, Ontology.GO_CC)
                .stream()
                .map(GenericTerm::getOboID)
                .toList());

        // FB case: 6392 -- not to map GO:0005515
        ignoreGoIDs.add("GO:0005515");
        ignoreGoIDs.add("GO:0005488");

        return ignoreGoIDs;
    }

    public static void main(String[] args) throws IOException {
        String toGoTranslationFile = args[0];
        List<SecondaryTerm2GoTerm> results = convertTranslationFileToUnloadFile(toGoTranslationFile, SecondaryTermType.EC);
        StringBuilder sb = new StringBuilder();
        for (SecondaryTerm2GoTerm result : results) {
            sb.append(result.dbAccession()).append("|").append(result.goTermName()).append("|").append(result.goID()).append("\n");
        }
        FileUtils.writeStringToFile(new File("/tmp/ec_mrkrgoterm.unl"), sb.toString(), "UTF-8");
    }
}














