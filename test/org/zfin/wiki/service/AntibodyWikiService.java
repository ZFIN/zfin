package org.zfin.wiki.service;

import org.zfin.TestConfiguration;
import org.zfin.wiki.RemotePage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 */
public class AntibodyWikiService {


    public static final String END_OF_BEGINNING_String = "<ac:rich-text-body>";

    private static AntibodyWikiWebService service;
    private static List<String> antibodies = new ArrayList<>(220);
    private static Map<String, String> entryMap = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        readAntibodyFile();
        TestConfiguration.configure();
        readEntryFile();
        antibodies.forEach(antibody -> {
                    try {
                        createNewPage(antibody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        service = AntibodyWikiWebService.getInstance();

/*
        updatePage("Ab3-mki67");
        RemotePageSummary[] pages = service.getAllPagesForSpace("AB");
        int numberOfCAntibodies = 0;
        for (RemotePageSummary pageSummary : pages) {
            RemotePage page = service.getPage(pageSummary.getId());
            if (!service.pageHasLabel(page, Label.ZFIN_ANTIBODY_LABEL.getValue())) {
                System.out.println(numberOfCAntibodies);
                numberOfCAntibodies++;
                updatePage(page.getTitle());
            }

        }
*/
        //System.out.println("Number of Community Antibodies: " + numberOfCAntibodies);

    }

    private static void readAntibodyFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("community-antibodies-live-template.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                antibodies.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void readEntryFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("ab-dump.txt")))) {
            String line;
            Map<String, Integer> versionMap = new LinkedHashMap<>();
            while ((line = br.readLine()) != null) {
                String antibodyName = getAntibodyName(line);
                if (antibodyName == null)
                    System.out.println("Could not find ab name in line " + line);
                line = line.substring(antibodyName.length());
                int version = getVersion(line);
                Integer currentVersion = versionMap.get(antibodyName);
                if (currentVersion == null) {
                    versionMap.put(antibodyName, version);
                    entryMap.put(antibodyName, line);
                    currentVersion = version;
                }
                if (version > currentVersion) {
                    versionMap.put(antibodyName, version);
                    entryMap.put(antibodyName, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int getVersion(String line) {
        String[] token = line.split("\t");
        String version = token[1];
        return Integer.parseInt(version.substring(version.lastIndexOf(".") + 1));
    }

    private static String getAntibodyName(String line) {
        for (String name : antibodies) {
            if (line.startsWith(name))
                return name;
        }
        return null;
    }

    private static void createNewPage(String antibodyName) throws Exception {
/*
        RemotePage page = service.getPageForTitleAndSpace(antibodyName, "AB");
        if (page == null)
            System.out.println("Could not find antibody: " + antibodyName);
*/

/*
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        String uri = entryMap.get(antibodyName);
        uri = uri.substring(uri.indexOf("<metadata>"));
        InputSource is = new InputSource(new StringReader(uri));
        Document doc = builder.parse(is);
*/
        String newContents = getNewContents(antibodyName);


    }

    private static String getNewContents(String antibodyName) {
        String isotope = getEntry("AntibodyIsotype", antibodyName);
        String comments = getEntry("Comments", antibodyName);
        String otherInfo = getEntry("OtherInfo", antibodyName);
        String recognizedMol = getEntry("RecognizedTargetMolecules", antibodyName);
        String host = getEntry("HostOrganism", antibodyName);
        String immuno = getEntry("ImmunogenOrganism", antibodyName);
        String zfinGenes = getEntry("ZFINGenes", antibodyName);
        String type = getEntries("AntibodyType", antibodyName);
        String works = getEntries("WorksOnZebrafish", antibodyName);
        String anatomicalStructures = getEntry("AnatomicalStructuresRecognized", antibodyName);
        String suppliers = getEntry("Suppliers", antibodyName);
        int numberOfAssays = getNumOfAssays("Assays Tested", antibodyName);
        if (numberOfAssays > 2)
            System.out.println(antibodyName + " has " + numberOfAssays + " tested assays");
        if (numberOfAssays > 0) {
            for (int index = 0; index < numberOfAssays; index++) {
                String preparation = getEntryFromAssay("Assays Tested", "Prep", index, antibodyName, numberOfAssays);
                String assays = getEntryFromAssay("Assays Tested", "Assays", index, antibodyName, numberOfAssays);
                String worked = getEntryFromAssay("Assays Tested", "Worked", index, antibodyName, numberOfAssays);
                String assayNotes = getSingleEntryFromAssay("Assays Tested", "AssayNotes", index, antibodyName, numberOfAssays);
                String halt = null;
                halt = null;
            }
        }

        return null;
//        String
    }

    private static String getSingleEntryFromAssay(String entryName, String subEntryName, int index, String antibodyName, int total) {
        String dbContents = entryMap.get(antibodyName);
        if (dbContents == null)
            return null;
        String regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<metadata|map>";

        if (index == total - 1)
            regExpression += "\\s*<entry>\\s*<string>" + index + "</string>\\s*";
        else
            regExpression += ".*<entry>\\s*<string>" + index + "</string>\\s*";

        regExpression += "<entry>\\s*<string>" + entryName + "</string>\\s*<metadata|map>.*<entry>\\s*<string>" + index + "</string>\\s*" +
                "<map>.*\\s*<entry>\\s*<string>" + subEntryName + "</string>\\s*" +
                "<string>([^<]*)</string>\\s*</entry>";
        if ((index == total - 1 && total > 1) || (index > 0 && total > 2 && index != total - 1))
            regExpression += ".*<entry>\\s*<string>" + (index - 1) + "</string>\\s*";

        Pattern p = Pattern.compile("(.*)" + regExpression + "(.*)");
        Matcher m = p.matcher(dbContents);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

    private static String getEntryFromAssay(String entryName, String subEntryName, int index, String antibodyName, int total) {
        String dbContents = entryMap.get(antibodyName);
        if (dbContents == null)
            return null;
        String metaDataOrMap = "metadata";
        if(dbContents.contains("<map>"))
            metaDataOrMap = "map";
        String regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<"+metaDataOrMap+">";
        if (index == total - 1)
            regExpression += "\\s*<entry>\\s*<string>" + index + "</string>\\s*";
        else
            regExpression += ".*<entry>\\s*<string>" + index + "</string>\\s*";

        if (dbContents.contains("<WikiReference>"))
            regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<"+metaDataOrMap+">.*<entry>\\s*<string>" + index + "</string>\\s*" +
                    "<"+metaDataOrMap+">.*\\s*<entry>\\s*<string>" + subEntryName + "</string>\\s*<WikiReference>\\s*<nonWiki>[^<]*</nonWiki>\\s*" +
                    "<wiki>([^<]*)</wiki>\\s*</WikiReference>\\s*</entry>";
        else
            regExpression += "<"+metaDataOrMap+">.*\\s*<entry>\\s*<string>" + subEntryName + "</string>\\s*<list>\\s*" +
                    "<string>([^<]*)</string>\\s*</list>\\s*</entry>";
        if ((index == total - 1 && total > 1) || (index > 0 && total > 2 && index != total - 1))
            regExpression += ".*<entry>\\s*<string>" + (index - 1) + "</string>\\s*";

        Pattern p = Pattern.compile("(.*)" + regExpression + "(.*)");
        Matcher m = p.matcher(dbContents);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

    private static int getNumOfAssays(String entryName, String antibodyName) {
        String dbContents = entryMap.get(antibodyName);
        if (dbContents == null)
            return 0;
        String regExpression = "<entry>\\s*<string>" + entryName + "</string>.*<entry>\\s*<string>rowCount</string>\\s*<int>([^<]*)</int>\\s*</entry>";
        Pattern p = Pattern.compile("(.*)" + regExpression + "(.*)");
        Matcher m = p.matcher(dbContents);
        if (m.find()) {
            return Integer.parseInt(m.group(2));
        }
        return 0;
    }

    private static String getEntry(String entryName, String antibodyName) {
        String dbContents = entryMap.get(antibodyName);
        if (dbContents == null)
            return null;
        String regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<string>([^<]*)</string>\\s*</entry>";
        Pattern p = Pattern.compile("(.*)" + regExpression + "(.*)");
        Matcher m = p.matcher(dbContents);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

    private static String getEntries(String entryName, String antibodyName) {
        String dbContents = entryMap.get(antibodyName);
        if (dbContents == null)
            return null;
        String regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<list>\\s*<string>([^<]*)</string>\\s*</list>\\s*</entry>";
        if (dbContents.contains("WikiReference"))
            regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<WikiReference>\\s*<nonWiki>[^<]*</nonWiki>\\s*<wiki>([^<]*)</wiki>\\s*</WikiReference>\\s*</entry>";
        Pattern p = Pattern.compile("(.*)" + regExpression + "(.*)");
        Matcher m = p.matcher(dbContents);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

    private static void updatePage(String antibodyName) throws Exception {
        RemotePage page = service.getPageForTitleAndSpace(antibodyName, "AB");
        String contents = page.getContent();
        int startBeginningMacro = contents.indexOf("<ac:structured-macro ac:name=\"table-plus\"");
        // find the following string after the first string
        int startEndMacro = contents.indexOf(END_OF_BEGINNING_String, startBeginningMacro);
        if (startBeginningMacro == -1)
            return;
        String newContents = contents.substring(0, startBeginningMacro);
        String tableContents = contents.substring(startEndMacro + END_OF_BEGINNING_String.length());
        String secondRemovalString = "</ac:rich-text-body></ac:structured-macro>";
        tableContents = tableContents.replaceFirst(secondRemovalString, "");
        newContents += tableContents;
        service.updatePageForAntibody(newContents, page, false);
        System.out.println(page.getTitle());
    }
}

