package org.zfin.wiki.service;

import org.apache.commons.lang.StringUtils;
import org.zfin.TestConfiguration;
import org.zfin.wiki.RemotePage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private static String template;

    public static void main(String[] args) throws Exception {
        readAntibodyFile();
        TestConfiguration.configure();
        readEntryFile();
        service = AntibodyWikiWebService.getInstance();
        antibodies.forEach(antibody -> {
                    try {
                        createNewPage(antibody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

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
        try (BufferedReader br = new BufferedReader(new FileReader(new File("ab-dump-tm.txt")))) {
            String line;
            Map<String, Integer> versionMap = new LinkedHashMap<>();
            while ((line = br.readLine()) != null) {
                String antibodyName = getAntibodyName(line);
                if (antibodyName == null) {
                    System.out.println("Could not find ab name");
                    continue;
                }
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
        String version = token[2];
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
        if (entryMap.keySet().contains(antibodyName)) {
            String halt = null;
            replacePage(antibodyName, newContents);
        }
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
        String template = readTemplate();
        template = replaceVariable("antibodyName", antibodyName, template);
        template = replaceVariable("otherNames", otherInfo, template);
        template = replaceVariable("doesItWork", works, template);
        template = replaceVariable("hostOrganism", host, template);
        template = replaceVariable("immunoOrganism", immuno, template);
        template = replaceVariable("isotope", isotope, template);
        template = replaceVariable("type", type, template);
        template = replaceVariable("structures", anatomicalStructures, template);
        template = replaceVariable("targetMolecules", recognizedMol, template);
        template = replaceVariable("recognizedZfinGenes", zfinGenes, template);
        template = replaceVariable("suppliers", suppliers, template);

        int numberOfAssays = getNumOfAssays("Assays Tested", antibodyName);
/*
        if (numberOfAssays > 2)
            System.out.println(antibodyName + " has " + numberOfAssays + " tested assays");
*/
        if (numberOfAssays > 0) {
            template += "<h3>Assays Tested </h3><table><tbody>\n" +
                    "\n" +
                    "<tr>\n" +
                    "\n" +
                    "<th>\n" +
                    "<p> Assay </p></th>\n" +
                    "\n" +
                    "<th>\n" +
                    "<p> Prep </p></th>\n" +
                    "\n" +
                    "<th>\n" +
                    "<p>Worked </p></th>\n" +
                    "\n" +
                    "<th>\n" +
                    "<p> Notes </p></th>\n" +
                    "</tr></tbody>";
            for (int index = 0; index < numberOfAssays; index++) {
                String preparation = getEntryFromAssay("Assays Tested", "Prep", index, antibodyName, numberOfAssays);
                String assay = getEntryFromAssay("Assays Tested", "Assays", index, antibodyName, numberOfAssays);
                String worked = getEntryFromAssay("Assays Tested", "Worked", index, antibodyName, numberOfAssays);
                String assayNotes = getSingleEntryFromAssay("Assays Tested", "AssayNotes", index, antibodyName, numberOfAssays);
                template = addAssayRow(assay, preparation, worked, assayNotes, template);
            }
            template += "</table>";
        }

        if (StringUtils.isNotEmpty(comments)) {
            template += "<h3>Notes</h3>\n" +
                    " \n" +
                    "<ul> ";
            template += "<li>" + comments + "</li>";
            template += "</ul>";

        }
        //System.out.println("Contents : " + template);
        return template;
    }

    private static String addAssayRow(String assay, String preparation, String worked, String assayNotes, String template) {
        preparation = makeNullSafe(preparation);
        assay = makeNullSafe(assay);
        worked = makeNullSafe(worked);
        assayNotes = makeNullSafe(assayNotes);
        template += "<tr>";
        template += "<td>" + assay + "</td>";
        template += "<td>" + preparation + "</td>";
        template += "<td>" + worked + "</td>";
        template += "<td>" + assayNotes + "</td>";
        template += "</tr>";
        return template;
    }

    private static String makeNullSafe(String preparation) {
        if (preparation == null)
            preparation = "";
        return preparation;
    }

    private static String replaceVariable(String variableName, String value, String template) {
        value = makeNullSafe(value);
        // create real hyperlinks from [] notation
        StringBuffer buffer = new StringBuffer();
        if (value.contains("[") && value.contains("|")) {
            String[] tokens = value.split("\\[");
            for (String token : tokens) {
                if (token.length() == 0)
                    continue;
                int pipeIndex = token.indexOf("|");
                String displayName = token.substring(0, pipeIndex).trim();
                if (displayName != null && variableName.equals("recognizedZfinGenes") && displayName.startsWith("_")) {
                    displayName = displayName.replaceFirst("_", "");
                    if (displayName.lastIndexOf("_") == displayName.length() - 1)
                        displayName = displayName.substring(0, displayName.length() - 1);
                }

                int endHref = token.indexOf("]");
                String href = token.substring(pipeIndex + 1, endHref).trim();
                buffer.append("<a href=\"");
                buffer.append(href);
                buffer.append("\" >");
                buffer.append(displayName);
                buffer.append("</a>");
                if (token.length() > endHref)
                    buffer.append(token.substring(endHref + 1));
            }
        } else {
            buffer.append(value);
        }
        String val = buffer.toString();
        if (variableName.equals("antibodyName") && value.contains("&"))
            val = val.replace("&", "&amp;");

        return template.replace("{" + variableName + "}", val);
    }

    private static String readTemplate() {
        if (template != null)
            return template;

        StringBuilder sb = new StringBuilder();

        try {
            Files.lines(Paths.get("template.antibody"), StandardCharsets.UTF_8).forEach(line -> {
                sb.append(line);
                sb.append("\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        template = sb.toString();
        return template;
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
        if (dbContents.contains("<map>"))
            metaDataOrMap = "map";
        String regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<" + metaDataOrMap + ">";
        if (index == total - 1)
            regExpression += "\\s*<entry>\\s*<string>" + index + "</string>\\s*";
        else
            regExpression += ".*<entry>\\s*<string>" + index + "</string>\\s*";

        if (dbContents.contains("<WikiReference>"))
            regExpression = "<entry>\\s*<string>" + entryName + "</string>\\s*<" + metaDataOrMap + ">.*<entry>\\s*<string>" + index + "</string>\\s*" +
                    "<" + metaDataOrMap + ">.*\\s*<entry>\\s*<string>" + subEntryName + "</string>\\s*<WikiReference>\\s*<nonWiki>[^<]*</nonWiki>\\s*" +
                    "<wiki>([^<]*)</wiki>\\s*</WikiReference>\\s*</entry>";
        else
            regExpression += "<" + metaDataOrMap + ">.*\\s*<entry>\\s*<string>" + subEntryName + "</string>\\s*<list>\\s*" +
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

    private static void replacePage(String antibodyName, String contents) throws Exception {
        RemotePage page = service.getPageForTitleAndSpace(antibodyName, "AB");
        System.out.print(page.getTitle() + "\t |   ");
        service.updatePageForAntibody(contents, page, false);
        System.out.println(page.getTitle());
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

