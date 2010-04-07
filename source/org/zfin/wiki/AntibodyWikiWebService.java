package org.zfin.wiki;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.presentation.AntibodyPresentation;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.presentation.SourcePresentation;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;

import java.io.*;
import java.util.*;

/**
 */
public class AntibodyWikiWebService extends WikiWebService {

    private final Logger logger = Logger.getLogger(AntibodyWikiWebService.class);

    // Antibody Homepage page ID
    private final long PARENT_PAGE_ID = 131090;

    // from the "antibody" template in the antibody space
    // todo: move to a file
    private final static File ANTIBODY_TEMPLATE_FILE = FileUtil.createFileFromStrings(ZfinProperties.getWebRootDirectory(),"WEB-INF","conf","antibody.template");
    private String antibodyTemplateData = null;

    enum ReturnStatus {
        ERROR, CREATE, UPDATE, DROP, NOCHANGE
    }


    private static AntibodyWikiWebService instance = null;

    private AntibodyWikiWebService() { }

    public static AntibodyWikiWebService getInstance() throws WikiLoginException {
        if (instance == null) {
            instance = new AntibodyWikiWebService();
        }
        if (instance.login()) {
            return instance;
        } else {
            throw new WikiLoginException("login result was false");
        }
    }

    public RemotePage getPageForAntibodyName(String pageTitle) throws PageDoesNotExistException {
        RemotePage page = null;

        try {
            page = service.getPage(token, Space.ANTIBODY.getValue(), pageTitle);
            boolean isZfinAntibodyLabeledPage = pageHasLabel(page, Label.ZFIN_ANTIBODY_LABEL.getValue());
            if (isZfinAntibodyLabeledPage == true) {
                logger.debug("is ZFIN Antibody Page: " + pageTitle);
            } else {
                logger.error("Non-ZFIN Antibody Page: " + pageTitle);
                throw new PageDoesNotExistException(pageTitle);
            }
        } catch (java.rmi.RemoteException e) {
            logger.debug("antibody not found, creating:" + pageTitle);
        }
        return page;
    }

    public RemotePage getPageForAntibody(Antibody antibody) throws PageDoesNotExistException {
        return getPageForAntibodyName(getWikiTitleFromAntibody(antibody));
    }

    public void setZfinAntibodyPageMetaData(RemotePage page) throws Exception {
        // we label these pages zfin_antibody so that we know they came from ZFIN
        service.addLabelByName(token, Label.ZFIN_ANTIBODY_LABEL.getValue(), page.getId());

        // only zfin-users may edit these
        RemoteContentPermission[] remoteContentPermissions = new RemoteContentPermission[1];
        RemoteContentPermission remoteContentPermission = new RemoteContentPermission();
        remoteContentPermission.setGroupName(Group.ZFIN_USERS.getValue());
        remoteContentPermission.setType(Permission.EDIT.getValue());
        remoteContentPermissions[0] = remoteContentPermission;
        service.setContentPermissions(token, page.getId(), Permission.EDIT.getValue(), remoteContentPermissions);
    }

    /**
     * Maps the antibody name to the wiki title page.
     *
     * @param name The antibody name to match.
     * @return The wiki page name.
     */
    private String getWikiTitleFromAntibodyName(String name) {
        return name.replaceAll("\\/", "-").toLowerCase();
    }

    private String getWikiTitleFromAntibody(Antibody antibody) {
        return antibody.getAbbreviation().replaceAll("\\/", "-");
    }


    public void clearAntibodyTemplate() {
        antibodyTemplateData = null;
    }

    public String getAntibodyTemplate() throws IOException {
        File file = null ;
        try {
            if (antibodyTemplateData == null) {
                file = ANTIBODY_TEMPLATE_FILE;
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                StringBuffer stringBuffer = new StringBuffer();
                String buffer;
                while ((buffer = bufferedReader.readLine()) != null) {
                    stringBuffer.append(buffer).append(FileUtil.LINE_SEPARATOR);
                }
                antibodyTemplateData = stringBuffer.toString();
            }
        } catch (IOException e) {
            String errorString =  "Failed to read template file"+FileUtil.LINE_SEPARATOR ;
            if(file!=null){
                errorString += "exists: " + file.exists() +FileUtil.LINE_SEPARATOR ;
                errorString += "absolute path: " + file.getAbsolutePath() +FileUtil.LINE_SEPARATOR ;
                errorString += "canonical path: " + file.getCanonicalPath() +FileUtil.LINE_SEPARATOR ;
                errorString += "path: " + file.getPath() +FileUtil.LINE_SEPARATOR ;
                errorString += "name: " + file.getName() +FileUtil.LINE_SEPARATOR ;
                errorString += "is directory: " + file.isDirectory() +FileUtil.LINE_SEPARATOR ;
                errorString += "is file: " + file.isFile() +FileUtil.LINE_SEPARATOR ;
                errorString += "is hidden: " + file.isHidden() +FileUtil.LINE_SEPARATOR ;
                errorString += "is absolute: " + file.isAbsolute() +FileUtil.LINE_SEPARATOR ;
                errorString += "parent: " + file.getParent() +FileUtil.LINE_SEPARATOR ;
                errorString += "can read: " + file.canRead() +FileUtil.LINE_SEPARATOR ;
                errorString += "can write: " + file.canWrite() +FileUtil.LINE_SEPARATOR ;
            }
            logger.error(errorString,e);
            throw e ;
        }
        return antibodyTemplateData;
    }

    /**
     * @param antibody Antibody to mangle name of.
     * @return Returns the case-preserved suffix of an antibody if they start with ab- or anti-
     */
    public String getUserFriendlyAntibodyName(Antibody antibody){
        String antibodyName = antibody.getName() ;
        if( antibodyName.toLowerCase().startsWith("ab")
                ||
                antibodyName.toLowerCase().startsWith("anti")
                ){
            return antibodyName.substring(antibodyName.indexOf("-")+1);
        }
        return null ;
    }

    /**
     * @param antibody Antibody source.
     * @return The page content as a String
     */
    public String createWikiPageContentForAntibodyFromTemplate(Antibody antibody) throws IOException {
        String content = null;
        content = getAntibodyTemplate();

        AntibodyService antibodyService = new AntibodyService(antibody);

        // name
        // should leave this unchanged to force a constraint
//        content = content.replace("{page-info:title}",antibody.getName()) ;
        StringBuilder antibodyNameString = new StringBuilder();
        antibodyNameString.append("[") ;
        antibodyNameString.append(antibody.getName()) ;
        antibodyNameString.append("|") ;
        antibodyNameString.append("http://zfin.org/action/antibody/detail?antibody.zdbID=") ;
        antibodyNameString.append(antibody.getZdbID()) ;
        antibodyNameString.append("]") ;
        antibodyNameString.append(" from the [ZFIN antibody database|http://zfin.org/action/antibody/search].") ;
        content = content.replace("{text-data:AntibodyName}{page-info:title}{text-data}", antibodyNameString.toString());

        // aliases
        String userFriendlyAntibodyName = getUserFriendlyAntibodyName(antibody) ;
        StringBuilder aliasStringBuilder = new StringBuilder();
        if (antibody.getAliases() != null) {
            Iterator<MarkerAlias> markerAliasIterator = antibody.getAliases().iterator();
            while (markerAliasIterator.hasNext()) {
                aliasStringBuilder.append(markerAliasIterator.next().getAlias());
                if (markerAliasIterator.hasNext()) {
                    aliasStringBuilder.append(" , ");
                }
            }

        }

        if (userFriendlyAntibodyName != null) {
            if(aliasStringBuilder.length()>0){
                aliasStringBuilder.append(" , ");
            }
            aliasStringBuilder.append(userFriendlyAntibodyName) ;
            aliasStringBuilder.append(" ");
            if(false==userFriendlyAntibodyName.equals(userFriendlyAntibodyName.toLowerCase())){
                aliasStringBuilder.append("{hidden-data}");
                aliasStringBuilder.append(userFriendlyAntibodyName.toLowerCase()) ;
                aliasStringBuilder.append("{hidden-data}") ;
                aliasStringBuilder.append(" ");
            }
        }

        if(StringUtils.isNotEmpty(aliasStringBuilder.toString())){
            content = content.replace("{text-data:OtherInfo}{text-data}", aliasStringBuilder.toString());
        }

        // always works on zebrafish
        content = content.replace("{list-data:WorksOnZebrafish}{list-option}yes{list-option}{list-option}no{list-option}{list-data}", "yes");

        // host species
        content = content.replace("{text-data:HostOrganism}{text-data}", (antibody.getHostSpecies() != null ? antibody.getHostSpecies() : ""));

        // antibody isotope
        if (antibody.getHeavyChainIsotype() != null) {
            content = content.replace("{text-data:AntibodyIsotype}{text-data}", antibody.getHeavyChainIsotype());
        } else if (antibody.getLightChainIsotype() != null) {
            content = content.replace("{text-data:AntibodyIsotype}{text-data}", antibody.getLightChainIsotype());
        }

        // anatomical structures 
        StringBuilder anatomyStringBuilder = new StringBuilder();
        Set<String> antibodyLinks = new TreeSet<String>(new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        for (AnatomyLabel anatomyLabel : antibodyService.getAntibodyLabelings()) {
            antibodyLinks.add(AnatomyItemPresentation.getWikiLink(anatomyLabel.getAnatomyItem()));
            if(anatomyLabel.getSecondaryAnatomyItem()!=null){
                antibodyLinks.add(AnatomyItemPresentation.getWikiLink(anatomyLabel.getSecondaryAnatomyItem()));
            }
            if(anatomyLabel.getCellularComponent()!=null){
                antibodyLinks.add(AnatomyItemPresentation.getWikiLink(anatomyLabel.getCellularComponent()));
            }
        }
        for(String antibodyLink : antibodyLinks){
            anatomyStringBuilder.append(antibodyLink);
            anatomyStringBuilder.append(" &nbsp;");
        }
        content = content.replace("{text-data:AnatomicalStructuresRecognized}{text-data}", anatomyStringBuilder.toString());

        // target molecules: we don't do these, only user input
//        content = content.replace("{text-data:RecognizedTargetMolecules}{text-data}",antibody.getHostSpecies()) ;

        // ZFIN genes
        StringBuilder zfinGeneStringBuilder = new StringBuilder();
        for (MarkerRelationship zfinGeneRelationship : antibodyService.getSortedAntigenRelationships()) {
            zfinGeneStringBuilder.append(MarkerPresentation.getWikiLink(zfinGeneRelationship.getFirstMarker()));
            zfinGeneStringBuilder.append(" &nbsp;");
        }
        content = content.replace("{text-data:ZFINGenes}{text-data}", zfinGeneStringBuilder.toString());

        // Antibody Page
        content = content.replace("{text-data:ZFINAntibody}{text-data}", AntibodyPresentation.getWikiLink(antibody));

        // Supplier page 
        StringBuilder supplierStringBuilder = new StringBuilder();
        if(antibody.getSuppliers()!=null){
            for (MarkerSupplier supplier : antibody.getSuppliers()) {
                supplierStringBuilder.append(SourcePresentation.getWikiLink(supplier.getOrganization()));
                supplierStringBuilder.append(" &nbsp;");
            }
        }
        content = content.replace("{text-data:Suppliers}{text-data}", supplierStringBuilder.toString());

        // public comments
        StringBuilder publicCommentsStringBuilder = new StringBuilder();
        publicCommentsStringBuilder.append(" * ");
        publicCommentsStringBuilder.append("Imported from ZFIN Antibody page " + AntibodyPresentation.getWikiLink(antibody));
        publicCommentsStringBuilder.append(FileUtil.LINE_SEPARATOR);
        // create a sorted list of these
        Set<AntibodyExternalNote> antibodyExternalNotes = new TreeSet<AntibodyExternalNote>(antibody.getExternalNotes());
        if (antibodyExternalNotes != null && antibodyExternalNotes.size() > 0) {
            for (AntibodyExternalNote externalNote : antibodyExternalNotes) {
                publicCommentsStringBuilder.append(" * ");
                publicCommentsStringBuilder.append(externalNote.getNote());
                publicCommentsStringBuilder.append(" (");
                publicCommentsStringBuilder.append(PublicationPresentation.getWikiLink(externalNote.getSinglePubAttribution().getPublication()));
                publicCommentsStringBuilder.append(")");
                publicCommentsStringBuilder.append(FileUtil.LINE_SEPARATOR);
            }
        } else {
            publicCommentsStringBuilder.append("No notes imported.").append(FileUtil.LINE_SEPARATOR);
        }
        publicCommentsStringBuilder.append(" * ") ;
        publicCommentsStringBuilder.append("[") ;
        publicCommentsStringBuilder.append("Citations for ") ;
        publicCommentsStringBuilder.append(antibody.getName()) ;
        publicCommentsStringBuilder.append(" at ZFIN ") ;
        publicCommentsStringBuilder.append("|") ;
        publicCommentsStringBuilder.append("http://zfin.org//action/antibody/publication-list?orderBy=author&antibody.zdbID=") ;
        publicCommentsStringBuilder.append(antibody.getZdbID()) ;
        publicCommentsStringBuilder.append("]") ;
        publicCommentsStringBuilder.append(FileUtil.LINE_SEPARATOR);
        content = content.replace("{text-data:Comments|type=area|width=400px|height=150px}{text-data}", publicCommentsStringBuilder.toString());


        StringBuilder assaysTestedStringBuilder = new StringBuilder() ;
        for(String assayName: antibodyService.getDistinctAssayNames()){
            assaysTestedStringBuilder.append(" | ") ;
            assaysTestedStringBuilder.append(assayName.toLowerCase()) ;
            assaysTestedStringBuilder.append(" | ") ;
            assaysTestedStringBuilder.append(" | ") ;
            assaysTestedStringBuilder.append("yes") ;
            assaysTestedStringBuilder.append(" | ") ;
            assaysTestedStringBuilder.append(" from ") ;
            assaysTestedStringBuilder.append("[ZFIN curation|") ;
            assaysTestedStringBuilder.append("http://zfin.org/action/antibody/detail?antibody.zdbID=") ;
            assaysTestedStringBuilder.append(antibody.getZdbID()) ;
            assaysTestedStringBuilder.append("]") ;
            assaysTestedStringBuilder.append(" | ") ;
            assaysTestedStringBuilder.append(FileUtil.LINE_SEPARATOR) ;
        }
        content = content.replace("{table:Assays Tested}", assaysTestedStringBuilder.toString());



        return content;
    }

    public ReturnStatus synchronizeAntibodyWithWiki(Antibody antibody) throws FileNotFoundException{
        logger.debug("processing antibody: " + antibody.getName());
        try {
            RemotePage page;
            try {
                page = getPageForAntibody(antibody);
            } catch (PageDoesNotExistException e) {
                // if the page doesn't exist, will throw this exception
                page = createPageForAntibody(antibody);
            }
            if (page == null) {
                createPageForAntibody(antibody);
                return ReturnStatus.CREATE;
            } else {
                String newContent = createWikiPageContentForAntibodyFromTemplate(antibody);
                // have to handle the "\r" case here, because contents are sometimes stored with the alternate line-endings
                if (newContent.replaceAll("\n", " ").replaceAll("\r", "").equals(page.getContent().replaceAll(FileUtil.LINE_SEPARATOR, " "))) {
                    return ReturnStatus.NOCHANGE;
                } else {
                    updatePageForAntibody(newContent, page);
                    return ReturnStatus.UPDATE;
                }
            }
        }
        catch (FileNotFoundException e) {
            throw e ;
        }
        catch (Exception e) {
            logger.error("Couldnot synchronoize the Antibody with the wiki for antibody: "+antibody,e);
            return ReturnStatus.ERROR;
        }
    }


    /**
     * Synchronizes the antibody wiki and the antibodies in ZFIN, adding, updating, and dropping where appropriate.
     *
     * 1. Loads all antibodies from ZFIN.
     * 2. Loads template file.
     * 3. From each antibody, renders the wiki page and evaluates if there are any changes (new or update) and
     *    updates/creates if necessary.
     * 4. Counts the file set of antibodies and deterimines if any pages need to be removed
     *    (will need to be manually remoevd if comments).
     *
     * @throws FileNotFoundException Thrown if unable to find the template file.
     */
    public void synchronizeAntibodiesOnWikiWithZFIN() throws FileNotFoundException{
        if (false == ZfinProperties.isPushToWiki()) {
            logger.info("not authorized to push antibodies to wiki");
            return;
        }
        List<Antibody> antibodies = RepositoryFactory.getAntibodyRepository().getAllAntibodies();
        if (CollectionUtils.isEmpty(antibodies)) {
            logger.error("no antibodies returned");
            return;
        }

        HashMap<String, Antibody> zfinAntibodyHashMap = new HashMap<String, Antibody>();
        WikiSynchronizationReport wikiSynchronizationReport = new WikiSynchronizationReport(true);
        String pageTitle;
        clearAntibodyTemplate();
        for (Antibody antibody : antibodies) {
            // containers
            pageTitle = getWikiTitleFromAntibody(antibody);
            zfinAntibodyHashMap.put(pageTitle, antibody);
            ReturnStatus returnStatus = synchronizeAntibodyWithWiki(antibody);
            switch (returnStatus) {
                case CREATE:
                    wikiSynchronizationReport.addCreatedPage(pageTitle);
                    break;
                case UPDATE:
                    wikiSynchronizationReport.addUpdatedPage(pageTitle);
                    break;
                case ERROR:
                    wikiSynchronizationReport.addErrorPage(pageTitle);
                    break;
                case NOCHANGE:
                    wikiSynchronizationReport.addNoChangePage(pageTitle);
                    break;
                default:
                    logger.error("returned bad status: " + returnStatus + " for antibody " + antibody.getName());
                    wikiSynchronizationReport.addErrorPage(pageTitle);
                    break;
            }
//            pushAntibodyWithWiki(antibody);
        }


        try {
            wikiSynchronizationReport = validateAntibodiesOnWikiWithZFIN(zfinAntibodyHashMap, wikiSynchronizationReport);
        } catch (Exception e) {
            logger.error("Error validating antibodies", e);
        }

        // send mail reporting
        emailReport(antibodies.size(), wikiSynchronizationReport);

    }

    private void emailReport(int numAntibodies, WikiSynchronizationReport wikiSynchronizationReport) {
        StringBuilder mailMessageStringBuilder = new StringBuilder();
        mailMessageStringBuilder.append(numAntibodies + " antibodies pushed to wiki from ZFIN");
        mailMessageStringBuilder.append(FileUtil.LINE_SEPARATOR);
        mailMessageStringBuilder.append(wikiSynchronizationReport.toString());
        mailMessageStringBuilder.append(FileUtil.LINE_SEPARATOR);
        (new IntegratedJavaMailSender()).sendMail("Antibody Wiki Notes", mailMessageStringBuilder.toString(), ZfinProperties.getAdminEmailAddresses());
    }

    public RemotePage updatePageForAntibody(Antibody antibody, String oldName) throws Exception {
        if (false == ZfinProperties.isPushToWiki()) {
            logger.info("not authorized to push to wiki by ZfinProperties");
            return null;
        }

        String newTitle = getWikiTitleFromAntibody(antibody);
        String oldTitle = getWikiTitleFromAntibodyName(oldName);
        RemotePage page = getPageForAntibodyName(oldTitle);

        if (false == newTitle.equals(oldTitle)) {
            // set the title
            page.setTitle(newTitle);
        }

        return updatePageForAntibody(createWikiPageContentForAntibodyFromTemplate(antibody), page);
    }

    private RemotePage updatePageForAntibody(String newContent, RemotePage page) throws Exception {
        page.setContent(newContent);
        page.setModified(Calendar.getInstance());
        page.setModifier(wikiUserName);

        page = service.storePage(token, page);
        setZfinAntibodyPageMetaData(page);
        return page;
    }

    private RemotePage createPageForAntibody(Antibody antibody) throws Exception {
        String pageTitle = getWikiTitleFromAntibody(antibody);

        RemotePage page = new RemotePage();
        page.setContent(createWikiPageContentForAntibodyFromTemplate(antibody));
        page.setCreated(Calendar.getInstance());
        page.setCreator(wikiUserName);
        page.setHomePage(false);
        page.setModified(Calendar.getInstance());
        page.setModifier(wikiUserName);
        page.setParentId(PARENT_PAGE_ID);
        page.setSpace(Space.ANTIBODY.getValue());
        page.setTitle(pageTitle);
        page.setVersion(1);

        service.storePage(token, page);

        try {
            page = service.getPage(token, Space.ANTIBODY.getValue(), pageTitle);
        } catch (java.rmi.RemoteException e) {
            logger.error("failed to create page:" + pageTitle);
            throw new FailedToCreatePageException(pageTitle);
        }

        setZfinAntibodyPageMetaData(page);

        return page;
    }

    /**
     * This method validates that we have the same number of antibodies in zfin than pages labeled zfin_antibody.
     * If we have more wiki pages than antibodes then we drop wiki pages.
     * If we have more antibodies then wiki pages, there must have been a problem creating wiki pages and we report that error.
     *
     * @param zfinAntibodyHashMap       The cached list of antiboidies processed.
     * @param wikiSynchronizationReport The Page report statistics.
     * @return number of dropped antibodies.
     * @throws Exception Failure to process or drop antibodies.
     */

    public WikiSynchronizationReport validateAntibodiesOnWikiWithZFIN(HashMap<String, Antibody> zfinAntibodyHashMap,
                                                                      WikiSynchronizationReport wikiSynchronizationReport) {

        int numAntibodies = zfinAntibodyHashMap.values().size();
        // get all pages for the zfin_antibody label
        RemoteSearchResult[] remoteSearchResults = new RemoteSearchResult[0];
        try {
            remoteSearchResults = service.getLabelContentByName(token, Label.ZFIN_ANTIBODY_LABEL.getValue());
        } catch (Exception e) {
            logger.error("Failed to drop pages because of error", e);
            return wikiSynchronizationReport;
        }

        if (remoteSearchResults.length < numAntibodies) {
            logger.error("More Antibodies in ZFIN[" + numAntibodies + "] than in the wiki [" + remoteSearchResults.length + "]");
        } else if (remoteSearchResults.length > numAntibodies) {
            // drop antibodies
            logger.warn("Less Antibodies in ZFIN[" + numAntibodies + "] than in the wiki [" + remoteSearchResults.length + "]: DROPPING ANTIBODIES");
            for (RemoteSearchResult remoteSearchResult : remoteSearchResults) {
                if (false == zfinAntibodyHashMap.containsKey(remoteSearchResult.getTitle())) {
                    try {
                        wikiSynchronizationReport = dropPage(remoteSearchResult, wikiSynchronizationReport);
                    } catch (Exception e) {
                        logger.error("failed to drop page: " + remoteSearchResult.getTitle(), e);
                        wikiSynchronizationReport.addErrorPage(remoteSearchResult.getTitle());
                    }
                }
            }

        } else {
            logger.info(numAntibodies + " antibodies updated or created from ZFIN ");
        }
        return wikiSynchronizationReport;
    }

    public void dropPageIndividually(String antibodyAbbreviation) throws Exception{
        RemoteSearchResult[] searchResults = service.search(token,antibodyAbbreviation,2);
        if(searchResults.length!=1){
            logger.error("wrong number of search results for["+antibodyAbbreviation+"]: "+ searchResults.length);
            return ; 
        }
        service.removePage(token,searchResults[1].getId()) ;

    }

    private WikiSynchronizationReport dropPage(RemoteSearchResult remoteSearchResult, WikiSynchronizationReport wikiSynchronizationReport) throws Exception {
        logger.warn("zfin_antibody wiki page not a ZFIN Antibody, DROPPING: " + remoteSearchResult.getTitle());
        // if page has comments, create error
        RemoteComment[] remoteComments = service.getComments(token, remoteSearchResult.getId());
        if (remoteComments.length == 0) {
            service.removePage(token, remoteSearchResult.getId());
            wikiSynchronizationReport.addDroppedPage(remoteSearchResult.getTitle());
        }
        // if comments exist
        else {
            logger.warn("zfin_antibody wiki page has comment, can not drop, must move manually: " + remoteSearchResult.getTitle());
            wikiSynchronizationReport.addErrorPage(remoteSearchResult.getTitle() + " has " + remoteComments.length + " comments, must move manually");
        }

        return wikiSynchronizationReport;
    }

    /**
     * Validate that the page in question has the corresponding label attached.
     *
     * @param page  Page to validate.
     * @param label Label name to validate against.
     * @return Returns true if the page has at least this label.
     * @throws java.rmi.RemoteException
     */
    public boolean pageHasLabel(RemotePage page, String label) throws java.rmi.RemoteException {
        boolean isZfinAntibodyLabelledPage = false;
        RemoteLabel[] remoteLabels = service.getLabelsById(token, page.getId());
        for (RemoteLabel remoteLabel : remoteLabels) {
            if (remoteLabel.getName().equals(label)) {
                isZfinAntibodyLabelledPage = true;
            }
        }
        return isZfinAntibodyLabelledPage;
    }

    /**
     * Generated a link to the wiki's antibody page for a given antibody.
     *
     * @param name Name of antibody
     * @return If true, a wiki page exists for that antibody that is labeled zfin_antibody.
     */
    public String getWikiLink(String name) {
        try {
            // get a page with the same title, if so, than set the same Id, as we will be replacing this page
            RemotePage page;

            String pageTitle = getWikiTitleFromAntibodyName(name);

            page = service.getPage(token, Space.ANTIBODY.getValue(), pageTitle);
            if (page == null) {
                return null;
            } else {
                if (pageHasLabel(page, Label.ZFIN_ANTIBODY_LABEL.getValue())) {
                    return page.getUrl();
                } else {
                    return null;
                }
            }
        }
        catch (Exception e) {
            logger.warn("Problem getting wiki link for ["+name+"]",e);
            return null;
        }
    }


}
