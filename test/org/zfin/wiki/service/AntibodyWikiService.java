package org.zfin.wiki.service;

import org.zfin.TestConfiguration;
import org.zfin.wiki.Label;
import org.zfin.wiki.RemotePage;
import org.zfin.wiki.RemotePageSummary;


/**
 */
public class AntibodyWikiService {


    public static final String END_OF_BEGINNING_String = "<ac:rich-text-body>";

    private static AntibodyWikiWebService service;

    public static void main(String[] args) throws Exception {
        TestConfiguration.configure();
        service = AntibodyWikiWebService.getInstance();

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
        System.out.println("Number of Community Antibodies: " + numberOfCAntibodies);

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

