package org.zfin.wiki.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.presentation.AntibodyPresentation;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.presentation.SourcePresentation;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FileUtil;
import org.zfin.wiki.*;

import java.io.*;
import java.util.*;

import static org.zfin.properties.ZfinProperties.isPushToWiki;

/**
 */
public class AntibodyWikiWebService extends WikiWebService {

    public static final Logger logger = Logger.getLogger(AntibodyWikiWebService.class);

    // Antibody Homepage page ID
    private final long PARENT_PAGE_ID = 131090;

    // from the "antibody" template in the antibody space
    // todo: move to a file
    private static File ANTIBODY_TEMPLATE_FILE;

    private String antibodyTemplateData = null;


    enum ReturnStatus {
        ERROR, CREATE, UPDATE, DROP, NOCHANGE
    }


    private static AntibodyWikiWebService instance = null;

    private AntibodyWikiWebService() {
    }

    public static AntibodyWikiWebService getInstance() throws WikiLoginException {
        if (instance == null) {
            instance = new AntibodyWikiWebService();
            ANTIBODY_TEMPLATE_FILE = FileUtil.createFileFromStrings(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value(), "WEB-INF", "conf", "antibody.template");
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
            if (isZfinAntibodyLabeledPage) {
                logger.debug("is ZFIN Antibody Page: " + pageTitle);
            } else {
                logger.error("Non-ZFIN Antibody Page (missing zfin_antibody label): " + pageTitle);
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
        return name.replaceAll("/", "-");
    }

    protected String getWikiTitleFromAntibody(Antibody antibody) {
        return getWikiTitleFromAntibodyName(antibody.getName());
    }


    public void clearAntibodyTemplate() {
        antibodyTemplateData = null;
    }

    public String getAntibodyTemplate() throws IOException {
        File file = null;
        try {
            if (antibodyTemplateData == null) {
                file = ANTIBODY_TEMPLATE_FILE;
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuffer = new StringBuilder();
                String buffer;
                while ((buffer = bufferedReader.readLine()) != null) {
                    stringBuffer.append(buffer).append(FileUtil.LINE_SEPARATOR);
                }
                antibodyTemplateData = stringBuffer.toString();
            }
        } catch (IOException e) {
            String errorString = "Failed to read template file" + FileUtil.LINE_SEPARATOR;
            if (file != null) {
                errorString += "exists: " + file.exists() + FileUtil.LINE_SEPARATOR;
                errorString += "absolute path: " + file.getAbsolutePath() + FileUtil.LINE_SEPARATOR;
                errorString += "canonical path: " + file.getCanonicalPath() + FileUtil.LINE_SEPARATOR;
                errorString += "path: " + file.getPath() + FileUtil.LINE_SEPARATOR;
                errorString += "name: " + file.getName() + FileUtil.LINE_SEPARATOR;
                errorString += "is directory: " + file.isDirectory() + FileUtil.LINE_SEPARATOR;
                errorString += "is file: " + file.isFile() + FileUtil.LINE_SEPARATOR;
                errorString += "is hidden: " + file.isHidden() + FileUtil.LINE_SEPARATOR;
                errorString += "is absolute: " + file.isAbsolute() + FileUtil.LINE_SEPARATOR;
                errorString += "parent: " + file.getParent() + FileUtil.LINE_SEPARATOR;
                errorString += "can read: " + file.canRead() + FileUtil.LINE_SEPARATOR;
                errorString += "can write: " + file.canWrite() + FileUtil.LINE_SEPARATOR;
            }
            logger.error(errorString, e);
            throw e;
        }
        return antibodyTemplateData;
    }

    /**
     * @param antibody Antibody to mangle name of.
     * @return Returns the case-preserved suffix of an antibody if they start with ab- or anti-
     */
    public String getUserFriendlyAntibodyName(Antibody antibody) {
        String antibodyName = antibody.getName();
        if (antibodyName.toLowerCase().startsWith("ab")
                ||
                antibodyName.toLowerCase().startsWith("anti")
                ) {
            return antibodyName.substring(antibodyName.indexOf("-") + 1);
        }
        return null;
    }

    /**
     * @param antibody Antibody source.
     * @return The page content as a String
     */
    public String createWikiPageContentForAntibodyFromTemplate(Antibody antibody) throws IOException {
        String content;
        content = getAntibodyTemplate();

        AntibodyService antibodyService = new AntibodyService(antibody);

        StringBuilder antibodyNameString = new StringBuilder();
        antibodyNameString.append(getHyperlink(antibody.getName(), "http://zfin.org/" + antibody.getZdbID()));
        antibodyNameString.append(" from the ");
        antibodyNameString.append(getHyperlink("ZFIN antibody database", "http://zfin.org/action/antibody/search") + ".");
        content = content.replace("{text-data:AntibodyName}{page-info:title}{text-data}", antibodyNameString.toString());

        // aliases
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
        String userFriendlyAntibodyName = getUserFriendlyAntibodyName(antibody);
        if (userFriendlyAntibodyName != null) {
            if (aliasStringBuilder.length() > 0) {
                aliasStringBuilder.append(" , ");
            }
            aliasStringBuilder.append(userFriendlyAntibodyName);
            aliasStringBuilder.append(" ");
        }
        content = content.replace("{text-data:OtherInfo}{text-data}", getEncodedString(aliasStringBuilder.toString()));

        // always works on zebrafish
        content = content.replace("{list-data:WorksOnZebrafish}{list-option}yes{list-option}{list-option}no{list-option}{list-data}", "yes");

        // host species
        content = content.replace("{text-data:HostOrganism}{text-data}", (antibody.getHostSpecies() != null ? antibody.getHostSpecies() : ""));

        // Immunogen species
        content = content.replace("{text-data:ImmunogenOrganism}{text-data}", (antibody.getImmunogenSpecies() != null ? antibody.getImmunogenSpecies() : ""));

        // antibody isotope
        String isotypeReplaceString = "";
        if (antibody.getHeavyChainIsotype() != null) {
            isotypeReplaceString += antibody.getHeavyChainIsotype();
        }
        if (antibody.getLightChainIsotype() != null) {
            if (antibody.getHeavyChainIsotype() != null) {
                isotypeReplaceString += " , ";
            }
            isotypeReplaceString += antibody.getLightChainIsotype();
        }
        content = content.replace("{text-data:AntibodyIsotype}{text-data}", isotypeReplaceString);

        // antibody type
        String clonalType = antibody.getClonalType();
        if (StringUtils.isEmpty(clonalType)) {
            clonalType = "";
        }
        content = content.replace("{text-data:AntibodyType}{text-data}", clonalType);

        // anatomical structures 
        StringBuilder anatomyStringBuilder = new StringBuilder();
        List<AnatomyLabel> anatomyLabelList = antibodyService.getAntibodyLabelings();
        Set<AnatomyLabel> anatomyLabelSet = new TreeSet<>(new Comparator<AnatomyLabel>() {
            @Override
            public int compare(AnatomyLabel o1, AnatomyLabel o2) {
                return o1.compareTo(o2);
            }
        });
        anatomyLabelSet.addAll(anatomyLabelList);
        for (AnatomyLabel anatomyLabel : anatomyLabelSet) {
            if (anatomyLabel.getSuperterm() != null && TermPresentation.getWikiLink(anatomyLabel.getSuperterm()) != null) {

                anatomyStringBuilder.append(TermPresentation.getWikiLink(anatomyLabel.getSuperterm()));
                if (anatomyLabel.getSubterm() != null) {
                    anatomyStringBuilder.append(" ");
                    anatomyStringBuilder.append(TermPresentation.getWikiLink(anatomyLabel.getSubterm()));
                }
                anatomyStringBuilder.append("; ");
            }
        }
        String anatomyLink = "";
        if (anatomyStringBuilder.length() > 1) {
            anatomyLink = anatomyStringBuilder.substring(0, anatomyStringBuilder.length() - 2);
        }
        content = content.replace("{text-data:AnatomicalStructuresRecognized}{text-data}", anatomyLink);

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
        if (CollectionUtils.isNotEmpty(antibody.getSuppliers())) {
            int count = antibody.getSuppliers().size();
            int index = 0;
            List<MarkerSupplier> suppliers = new ArrayList<>(antibody.getSuppliers());
            Collections.sort(suppliers);
            for (MarkerSupplier supplier : suppliers) {
                String wikiLink = SourcePresentation.getWikiLink(supplier.getOrganization());
                supplierStringBuilder.append(wikiLink);
                if (++index < count)
                    supplierStringBuilder.append(" &nbsp;");
            }
        }
        content = content.replace("{text-data:Suppliers}{text-data}", supplierStringBuilder);

        // public comments
        StringBuilder publicCommentsStringBuilder = new StringBuilder();
        publicCommentsStringBuilder.append(" <ul> ");
        publicCommentsStringBuilder.append(" <li> ");
        publicCommentsStringBuilder.append("Imported from ZFIN Antibody page " + AntibodyPresentation.getWikiLink(antibody));
        publicCommentsStringBuilder.append(" </li> ");
        publicCommentsStringBuilder.append(FileUtil.LINE_SEPARATOR);
        // create a sorted list of these
        if (CollectionUtils.isNotEmpty(antibody.getExternalNotes())) {
            List<AntibodyExternalNote> externalNotes = new ArrayList<>();
            externalNotes.addAll(antibody.getExternalNotes());
            Collections.sort(externalNotes);
            for (AntibodyExternalNote externalNote : externalNotes) {
                publicCommentsStringBuilder.append(" <li> ");
                publicCommentsStringBuilder.append(getEncodedString(externalNote.getNote()));
                publicCommentsStringBuilder.append(" (");
                if (externalNote.getSinglePubAttribution() != null) {
                    String wikiLink = PublicationPresentation.getWikiLink(externalNote.getSinglePubAttribution().getPublication());
                    publicCommentsStringBuilder.append(wikiLink);
                }
                publicCommentsStringBuilder.append(")");
                publicCommentsStringBuilder.append(" </li> ");
                publicCommentsStringBuilder.append(FileUtil.LINE_SEPARATOR);
            }
        } else {
            publicCommentsStringBuilder.append("No notes imported.").append(FileUtil.LINE_SEPARATOR);
        }
        publicCommentsStringBuilder.append(" <li> ");
        publicCommentsStringBuilder.append(getHyperlink("Citations for " + antibody.getName() +
                " at ZFIN ", "http://zfin.org/action/antibody/antibody-publication-list?orderBy=author&amp;antibodyID=" + antibody.getZdbID()));
        publicCommentsStringBuilder.append(" </li> ");
        publicCommentsStringBuilder.append(FileUtil.LINE_SEPARATOR);
        publicCommentsStringBuilder.append("</ul>");
        content = content.replace("{text-data:Comments|type=area|width=400px|height=150px}{text-data}", publicCommentsStringBuilder);


        StringBuilder assaysTestedStringBuilder = new StringBuilder();
        for (String assayName : antibodyService.getDistinctAssayNames()) {
            assaysTestedStringBuilder.append(" <tr><td><p> ");
            assaysTestedStringBuilder.append(assayName.toLowerCase());
            assaysTestedStringBuilder.append(" </p></td><td> ");
            assaysTestedStringBuilder.append(" </td><td> ");
            assaysTestedStringBuilder.append("yes");
            assaysTestedStringBuilder.append(" </td><td><p> ");
            assaysTestedStringBuilder.append(" from ");
            assaysTestedStringBuilder.append(getHyperlink("ZFIN curation", "http://zfin.org/" + antibody.getZdbID()));
            assaysTestedStringBuilder.append(" </p></td></tr> ");
            assaysTestedStringBuilder.append(FileUtil.LINE_SEPARATOR);
        }
        content = content.replace("{table:Assays Tested}", assaysTestedStringBuilder.toString());


        return content;
    }

    public static String getEncodedString(String value) {
        if (value == null)
            return null;
        value = value.replace("ü", "&uuml;");
        value = value.replace("ä", "&auml;");
        value = value.replace("ö", "&ouml;");
        value = value.replace("ó", "&oacute;");
        value = value.replace("õ", "&otilde;");
        value = value.replace("ñ", "&ntilde;");
        value = value.replace("é", "&eacute;");
        value = value.replace("á", "&aacute;");
        value = value.replace("à", "&agrave;");
        value = value.replace("î", "&icirc;");
        value = value.replace("í", "&iacute;");
        value = value.replace("è", "&egrave;");
        value = value.replace("ø", "&oslash;");
        value = value.replace("&#945;", "&alpha;");
        value = value.replace("&#946;", "&beta;");
        value = value.replace("&#947;", "&gamma;");
        value = value.replace("&#8211;", "&ndash;");
        value = value.replace("&#8220;", "&rdquo;");
        value = value.replace("®", "&reg;");
        value = value.replaceAll("R&D", "R&amp;D");
        value = value.replaceAll("\"", "&quot;");
        value = value.replaceAll(">", "&gt;");
        value = value.replaceAll("<", "&lt;");
        value = value.replaceAll(" \\& ", " &amp; ");
        //value = value.replaceAll("\\&&&[^\\&alpha;]", "&amp;");
        return value;
    }

    private StringBuilder getHyperlink(String name, String url) {
        StringBuilder builder = new StringBuilder("<a href=\"");
        builder.append(url);
        builder.append("\">");
        builder.append(name);
        builder.append("</a>");
        return builder;
    }

    public ReturnStatus synchronizeAntibodyWithWiki(Antibody antibody) throws FileNotFoundException {
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
                if (newContent.equals(page.getContent())) {
                    return ReturnStatus.NOCHANGE;
                } else {
                    StringBuilder builder = new StringBuilder("Difference for: " + antibody.getName() + "\n");
                    builder.append(StringUtils.difference(newContent, page.getContent()).substring(0, 50));
                    builder.append("\n");
                    builder.append(StringUtils.difference(page.getContent(), newContent).substring(0, 50));
                    logger.info(builder);
                    updatePageForAntibody(newContent, page);
                    return ReturnStatus.UPDATE;
                }
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Could not synchronize the Antibody with the wiki for antibody: " + antibody, e);
            return ReturnStatus.ERROR;
        }
    }


    /**
     * Synchronizes the antibody wiki and the antibodies in ZFIN, adding, updating, and dropping where appropriate.
     * <p/>
     * 1. Loads all antibodies from ZFIN.
     * 2. Loads template file.
     * 3. From each antibody, renders the wiki page and evaluates if there are any changes (new or update) and
     * updates/creates if necessary.
     * 4. Counts the file set of antibodies and determines if any pages need to be removed
     * (will need to be manually removed if comments).
     *
     * @throws FileNotFoundException Thrown if unable to find the template file.
     */
    public WikiSynchronizationReport synchronizeAntibodiesOnWikiWithZFIN() throws FileNotFoundException {
        WikiSynchronizationReport wikiSynchronizationReport = new WikiSynchronizationReport(true);
        if (!isPushToWiki()) {
            logger.info("not authorized to push antibodies to wiki");
            return wikiSynchronizationReport;
        }
        List<Antibody> antibodies = RepositoryFactory.getAntibodyRepository().getAllAntibodies();
        if (CollectionUtils.isEmpty(antibodies)) {
            logger.error("no antibodies returned");
            return null;
        }
        logger.info("Checking " + antibodies.size() + " antibodies in ZFIN");
        Map<String, Antibody> zfinAntibodyHashMap = new HashMap<>();
        String pageTitle;
        clearAntibodyTemplate();
        for (Antibody antibody : antibodies) {
            // containers
            pageTitle = getWikiTitleFromAntibody(antibody);
            zfinAntibodyHashMap.put(pageTitle.toUpperCase(), antibody);
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
        }
        try {
            return validateAntibodiesOnWikiWithZFIN(zfinAntibodyHashMap, wikiSynchronizationReport);
        } catch (Exception e) {
            logger.error("Error validating antibodies", e);
        }
        return null;
    }

    public RemotePage mergeAntibody(Antibody antibodyToMergeInto, Antibody antibodyToDelete) throws Exception {
        if (!isPushToWiki()) {
            logger.info("not authorized to push to wiki by ZfinProperties");
            return null;
        }

        // get page to merge into
        String titleToMergeInto = getWikiTitleFromAntibody(antibodyToMergeInto);
        RemotePage pageToMergeInto = getPageForAntibodyName(titleToMergeInto);

        // get page to delete
        String titleToDelete = getWikiTitleFromAntibody(antibodyToDelete);
        RemotePage pageToDelete = getPageForAntibodyName(titleToDelete);


        // recreate new page from old
        RemotePage newPage = updatePageForAntibody(antibodyToMergeInto, pageToMergeInto.getTitle());

        // move comments over getTitle() to new antibody
        moveComments(pageToDelete, pageToMergeInto);

        return newPage;
    }

    public void moveComments(Antibody antibodyToDelete, Antibody antibodyToMergeInto) throws Exception {
        // get page to merge into
        String titleToMergeInto = getWikiTitleFromAntibody(antibodyToMergeInto);
        RemotePage pageToMergeInto = getPageForAntibodyName(titleToMergeInto);

        // get page to delete
        String titleToDelete = getWikiTitleFromAntibody(antibodyToDelete);
        RemotePage pageToDelete = getPageForAntibodyName(titleToDelete);

        moveComments(pageToDelete, pageToMergeInto);
    }

    /**
     * change ownership of comment from page to delete to page to merge into
     *
     * @param pageToDelete    page
     * @param pageToMergeInto page
     * @throws Exception
     */
    private void moveComments(RemotePage pageToDelete, RemotePage pageToMergeInto) throws Exception {
        for (RemoteComment comment : service.getComments(token, pageToDelete.getId())) {
            service.removeComment(token, comment.getId());
            comment.setPageId(pageToMergeInto.getId());
            service.addComment(token, comment);
        }
    }

    public RemotePage updatePageForAntibody(Antibody antibody, String oldName) throws Exception {
        if (!isPushToWiki()) {
            logger.info("not authorized to push to wiki by ZfinProperties");
            return null;
        }

        String newTitle = getWikiTitleFromAntibody(antibody);
        String oldTitle = getWikiTitleFromAntibodyName(oldName);
        RemotePage page = getPageForAntibodyName(oldTitle);

        if (page == null) {
            logger.warn("page not found for name: " + oldTitle + " for antibody: " + antibody);
            return null;
        }

        if (!newTitle.equals(oldTitle)) {
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

    public RemotePage createPageForAntibody(Antibody antibody) throws Exception {
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
     * If we have more wiki pages than antibodies then we drop wiki pages.
     * If we have more antibodies then wiki pages, there must have been a problem creating wiki pages and we report that error.
     *
     * @param zfinAntibodyHashMap       The cached list of antibodies processed.
     * @param wikiSynchronizationReport The Page report statistics.
     * @return number of dropped antibodies.
     */

    public WikiSynchronizationReport validateAntibodiesOnWikiWithZFIN(Map<String, Antibody> zfinAntibodyHashMap,
                                                                      WikiSynchronizationReport wikiSynchronizationReport) {

        int numAntibodies = zfinAntibodyHashMap.values().size();
        // get all pages for the zfin_antibody label
        RemotePageSummary[] remotePageSummaries;
        try {
            remotePageSummaries = service.getPages(token, "AB");
        } catch (Exception e) {
            logger.error("Failed to drop pages because of error", e);
            return wikiSynchronizationReport;
        }

        if (remotePageSummaries.length < numAntibodies) {
            logger.error("More Antibodies in ZFIN[" + numAntibodies + "] than in the wiki [" + remotePageSummaries.length + "]");
        } else if (remotePageSummaries.length > numAntibodies) {
            // drop antibodies
            logger.warn("Fewer Antibodies in ZFIN[" + numAntibodies + "] than in the wiki [" + remotePageSummaries.length + "]: DROPPING ANTIBODIES");
            List<RemotePageSummary> communityAntibodies = new ArrayList<>();
            for (RemotePageSummary remoteSearchResult : remotePageSummaries) {
                if (!zfinAntibodyHashMap.containsKey(remoteSearchResult.getTitle().toUpperCase())) {
                    try {
                        RemoteLabel[] labelsByIds = service.getLabelsById(token, remoteSearchResult.getId());
                        if (labelsByIds != null) {
                            boolean isZfinAntibody = false;
                            // check if the antibody is an old / stale zfin antibody
                            for (RemoteLabel label : labelsByIds) {
                                if (label.getName().equalsIgnoreCase(Label.ZFIN_ANTIBODY_LABEL.getValue())) {
                                    isZfinAntibody = true;
                                    break;
                                }
                            }
                            // if it is an old ZFIN antibody then drop it otherwise keep it.
                            if (isZfinAntibody) {
                                logger.info("trying to drop!: " + remoteSearchResult.getTitle());
                                wikiSynchronizationReport = dropPage(remoteSearchResult, wikiSynchronizationReport);
                            } else
                                communityAntibodies.add(remoteSearchResult);
                        }
                    } catch (Exception e) {
                        logger.error("failed to drop page: " + remoteSearchResult.getTitle(), e);
                        wikiSynchronizationReport.addErrorPage(remoteSearchResult.getTitle());
                    }
                }
            }
            logger.info("There are " + communityAntibodies.size() + " antibodies created by the community.");

        } else {
            logger.info(numAntibodies + " antibodies updated or created from ZFIN ");
        }
        return wikiSynchronizationReport;
    }

    public void dropPageIndividually(String antibodyName) throws Exception {
        RemoteSearchResult[] searchResults = service.search(token, antibodyName, 2);
        if (searchResults.length != 1) {
            logger.error("wrong number of search results for[" + antibodyName + "]: " + searchResults.length);
            return;
        }
        service.removePage(token, searchResults[0].getId());

    }

    private WikiSynchronizationReport dropPage(RemotePageSummary remoteSearchResult, WikiSynchronizationReport wikiSynchronizationReport) throws Exception {
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
     * Generated a link to the Wiki's antibody page for a given antibody.
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
                    //hardcode devwiki in case antibody is created while testing.
                    if (instance.wikiHost.contains("devwiki")) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("https://");
                        // sb.append(AntibodyWikiWebService.getInstance().getDomainName());
                        sb.append(AntibodyWikiWebService.getInstance().getWikiHost());
                        sb.append("/display/AB/");
                        sb.append(name);
                        return sb.toString();
                    } else
                        return page.getUrl();

                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            logger.warn("Problem getting wiki link for [" + name + "]", e);
            return null;
        }
    }


}
