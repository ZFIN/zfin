package org.zfin.datatransfer.go.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.go.*;
import org.zfin.gwt.root.dto.GoDefaultPublication;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceQualifier;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.gwt.root.ui.GoEvidenceValidator;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.marker.Marker;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.service.SequenceService;
import org.zfin.util.FileUtil;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Gene Association File Service
 */
//@Service
public class GafService {

    protected Logger logger = Logger.getLogger(GafService.class);
    protected static final String PUBMED_PREFIX = "PMID:";

    protected SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    protected PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    protected MarkerGoTermEvidenceRepository markerGoTermEvidenceRepository = RepositoryFactory.getMarkerGoTermEvidenceRepository();
    protected OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    protected MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    protected ReferenceDatabase uniprot;
    protected GafOrganization.OrganizationEnum organizationEnum;


    protected DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    protected Map<String, Publication> goRefPubMap = new HashMap<String, Publication>();
    protected Pattern patternPipe = Pattern.compile("ZFIN:(.*)\\|.*");

    protected Map<String, String> replacedGeneAndMOZDBIds = new HashMap<String, String>();

    public GafService(GafOrganization.OrganizationEnum organizationEnum) {
        this.organizationEnum = organizationEnum;
        for (GoDefaultPublication goDefaultPublication : GoDefaultPublication.getGoRefPubs()) {
            goRefPubMap.put(goDefaultPublication.title(), publicationRepository.getPublication(goDefaultPublication.zdbID()));
        }
    }


    /**
     * This method takes the parsed data and uses the database to properly create records.
     * If not, it reports an error.
     *
     * @param gafEntries Parsed GafEntry objects.  Should be pre-filtered for obvious exclusion.
     * @param gafJobData Report object.
     */
    public void processGoaGafEntries(List<GafEntry> gafEntries, GafJobData gafJobData) {

        // get uniprot ReferenceDatabase
        uniprot = sequenceRepository.getZebrafishSequenceReferenceDatabase(
                ForeignDB.AvailableName.UNIPROTKB
                , ForeignDBDataType.DataType.POLYPEPTIDE
        );
    }

    public void processEntries(List<GafEntry> gafEntries, GafJobData gafJobData) {

        GafOrganization gafOrganization =
                markerGoTermEvidenceRepository.getGafOrganization(organizationEnum);

        logger.info("processing " + gafEntries.size() + " entries");
        if (gafEntries.size() == 0) {
            logger.error("no entries to process!");
            return;
        }
        gafJobData.markStartTime();

        int count = 0;
        for (GafEntry gafEntry : gafEntries) {
            ////FB case 8432 prevent GO annotation to GO:0005623 from FP-Inf. GAF load
            if (!gafEntry.isCell()) {
                // find genes based on uniprot ID via marker relations
                try {
                    Collection<Marker> genes = getGenes(gafEntry.getEntryId());
                    // for each gene, create an entry
                    // if no gene is then report error
                    if (CollectionUtils.isNotEmpty(genes)) {
                        for (Marker gene : genes) {
                            MarkerGoTermEvidence annotationToAdd = generateAnnotation(gafEntry, gene, gafOrganization);
                            if (annotationToAdd != null) {
                                if (gafJobData.getNewEntries().contains(annotationToAdd)) {
                                    throw new GafValidationError("A duplicate entry is being added:" +
                                            FileUtil.LINE_SEPARATOR + annotationToAdd + " from:" +
                                            FileUtil.LINE_SEPARATOR + gafEntry);
                                }
                                gafJobData.addNewEntry(annotationToAdd);
                            } else {
                                throw new GafValidationError("Annotation to add is null for some reason for gene " + gafEntry, gafEntry);
                            }
                        } // end of gene loop
                    } else {
                        throw new GafValidationError("Unable to find genes associated with ID[" + gafEntry.getEntryId() + "]", gafEntry);
                    }
                } catch (GafAnnotationExistsError gafAnnotationExistsError) {
                    logger.debug("Existing annotation: " + gafAnnotationExistsError.getMessage() + " for " + gafEntry);
                    gafJobData.addExistingEntry(gafAnnotationExistsError.getAnnotation());
                } catch (GafValidationError gafValidationError) {
                    logger.debug("Validation error: " + gafValidationError.getMessage() + " for " + gafEntry);
                    gafJobData.addError(gafValidationError);
                }

                ++count;
                if (count % 200 == 0) {
                    logger.info("at " + count + " of " + gafEntries.size() + " done "
                            + ((float) count / (float) gafEntries.size()) * 100f + "%");
                }
            }
        } // end of gaf entry for loop


        gafJobData.markStopTime();
    }

    protected ReferenceDatabase getUniprot() {
        if (uniprot == null) {
            // get uniprot ReferenceDatabase
            uniprot = sequenceRepository.getZebrafishSequenceReferenceDatabase(
                    ForeignDB.AvailableName.UNIPROTKB
                    , ForeignDBDataType.DataType.POLYPEPTIDE
            );
        }

        return uniprot;
    }

    protected Collection<Marker> getGenes(String entryId) throws GafValidationError {
        Set<Marker> returnGenes = new HashSet<Marker>();
        if (entryId.startsWith("ZDB-GENE-")) {
            Marker gene = markerRepository.getGeneByID(entryId);
            if (gene == null) {
                throw new GafValidationError("No gene found for ID: " + entryId);
            }
            returnGenes.add(gene);
        } else {
            List<MarkerDBLink> markerDBLinks = sequenceRepository.getMarkerDBLinksForAccession(entryId, getUniprot());
            for (MarkerDBLink markerDBLink : markerDBLinks) {
                Marker gene = markerDBLink.getMarker();
                if (gene.getZdbID().startsWith("ZDB-GENE-")) {
                    returnGenes.add(gene);
                } else {
                    logger.debug("no gene associated with dblink: " + markerDBLink);
                }
            }
        }

        return returnGenes;
    }


    public void generateRemovedEntriesReport(GafJobData gafJobData, Collection<String> zdbIdsToDrop) {
        if (CollectionUtils.isNotEmpty(zdbIdsToDrop)) {
            for (String zdbIdToDrop : zdbIdsToDrop) {
                MarkerGoTermEvidence markerGoTermEvidence = markerGoTermEvidenceRepository.getMarkerGoTermEvidenceByZdbID(zdbIdToDrop);
                gafJobData.addRemoved(markerGoTermEvidence);
            }
        }
    }

    /**
     * Find entries in the database that are not in the incoming file for a particular organization.
     *
     * @param gafJobData
     * @param gafOrganization
     * @return Collection of zdbids to be removed.
     */
    public Collection<String> findOutdatedEntries(GafJobData gafJobData, GafOrganization gafOrganization) {
        Set<String> existingZfinZdbIDs = new TreeSet<String>(markerGoTermEvidenceRepository.getEvidencesForGafOrganization(gafOrganization));
        Set<String> newGafEntryZdbIds = new TreeSet<String>();

        for (GafJobEntry gafJobEntry : gafJobData.getExistingEntries()) {
            newGafEntryZdbIds.add(gafJobEntry.getZdbID());
        }
        for (MarkerGoTermEvidence gafReportAnnotation : gafJobData.getNewEntries()) {
            newGafEntryZdbIds.add(gafReportAnnotation.getZdbID());
        }

        Collection<String> zdbIdsToDrop = CollectionUtils.subtract(existingZfinZdbIDs, newGafEntryZdbIds);
        return zdbIdsToDrop;
    }

    public void generateRemovedEntries(GafJobData gafJobData, GafOrganization gafOrganization) {
        Collection<String> zdbIdsOutdated = findOutdatedEntries(gafJobData, gafOrganization);
        generateRemovedEntriesReport(gafJobData, zdbIdsOutdated);
    }

    public void removeEntries(GafJobData gafJobData) {
        for (GafJobEntry gafJobEntry : gafJobData.getRemovedEntries()) {
            RepositoryFactory.getInfrastructureRepository().deleteActiveDataByZdbID(gafJobEntry.getZdbID());
        }
    }

    public MarkerGoTermEvidence generateAnnotation(GafEntry gafEntry, Marker gene, GafOrganization gafOrganization)
            throws GafValidationError {
        // lookup GO annotation
        GenericTerm goTerm = getGoTerm(gafEntry);

        // find pubmed ID
        Publication publication = getPublication(gafEntry);
        // set new object to create

        MarkerGoTermEvidence markerGoTermEvidenceToAdd = new MarkerGoTermEvidence();
        markerGoTermEvidenceToAdd.setMarker(gene);
        markerGoTermEvidenceToAdd.setGoTerm(goTerm);
        markerGoTermEvidenceToAdd.setSource(publication);
        markerGoTermEvidenceToAdd.setExternalLoadDate(new Date());

        // validate qualifier
        GoEvidenceQualifier goEvidenceQualifier = getQualifier(gafEntry, goTerm);
        markerGoTermEvidenceToAdd.setFlag(goEvidenceQualifier);
        // validate evidence code
        GoEvidenceCode goEvidenceCode = markerGoTermEvidenceRepository.getGoEvidenceCode(gafEntry.getEvidenceCode());
        if (goEvidenceCode == null) {
            throw new GafValidationError("invalid evidence code: " + gafEntry.getEvidenceCode(), gafEntry);
        }
//        GoEvidenceCodeEnum goEvidenceCodeEnum = GoEvidenceCodeEnum.getType(goEvidenceCode.getCode());
        markerGoTermEvidenceToAdd.setEvidenceCode(goEvidenceCode);

        // validate created date
        try {
            markerGoTermEvidenceToAdd.setCreatedWhen(dateFormat.parse(gafEntry.getCreatedDate()));
            markerGoTermEvidenceToAdd.setModifiedWhen(dateFormat.parse(gafEntry.getCreatedDate()));
        } catch (ParseException e) {
            throw new GafValidationError("Failed to parse marker go term evidence", gafEntry);
        }
        // validate created by
        markerGoTermEvidenceToAdd.setOrganizationCreatedBy(gafEntry.getCreatedBy());
        markerGoTermEvidenceToAdd.setGafOrganization(gafOrganization);


        // validate inferences
        handleInferences(gafEntry, markerGoTermEvidenceToAdd);


        List<MarkerGoTermEvidence> existingEvidenceList = markerGoTermEvidenceRepository.getLikeMarkerGoTermEvidencesButGo(markerGoTermEvidenceToAdd);
        /**
         * If there is an existing annotation with the same or more specific go term
         * with either more or the same inferences then do not add.
         */
        for (MarkerGoTermEvidence existingMarkerGoTermEvidence : existingEvidenceList) {
            if (isMoreSpecificAnnotation(existingMarkerGoTermEvidence, markerGoTermEvidenceToAdd)) {
                markerGoTermEvidenceToAdd.setZdbID(existingMarkerGoTermEvidence.getZdbID());
                throw new GafAnnotationExistsError(gafEntry, markerGoTermEvidenceToAdd);
            }
        }
        return markerGoTermEvidenceToAdd;
    }

    protected void handleInferences(GafEntry gafEntry, MarkerGoTermEvidence markerGoTermEvidence)
            throws GafValidationError {
        String inferenceEntry = gafEntry.getInferences();
        if (StringUtils.isNotEmpty(inferenceEntry)) {
            GoEvidenceCodeEnum goEvidenceCodeEnum = GoEvidenceCodeEnum.getType(markerGoTermEvidence.getEvidenceCode().getCode());
            String publicationZdbId = markerGoTermEvidence.getSource().getZdbID();
            Set<InferenceGroupMember> inferredFrom = new HashSet<InferenceGroupMember>();
            Set<String> inferenceSet = new HashSet<String>();
            inferenceSet.addAll(Arrays.asList(inferenceEntry.split("\\|")));
            if (!GoEvidenceValidator.isValidCardinality(goEvidenceCodeEnum, inferenceSet)) {
                throw new GafValidationError(GoEvidenceValidator.generateErrorString(goEvidenceCodeEnum, publicationZdbId), gafEntry);
            }


            for (String inference : inferenceSet) {
                if (goEvidenceCodeEnum == GoEvidenceCodeEnum.IGI) {
                    if (InferenceCategory.UNIPROTKB.isType(inference)) {
                        List<MarkerDBLink> markerDBLinks = sequenceRepository.getMarkerDBLinksForAccession(
                                inference.substring(InferenceCategory.UNIPROTKB.prefix().length()), SequenceService.getUniprotRefDB());
                        // if it is one gene, then set that as the prefix
                        if (markerDBLinks != null && markerDBLinks.size() == 1 && markerDBLinks.get(0).getMarker().getZdbID().startsWith("ZDB-GENE")) {
                            inference = InferenceCategory.ZFIN_GENE.prefix() + markerDBLinks.get(0).getMarker().getZdbID();
                        }
                    }
                }
                if (inference == null) {
                    throw new GafValidationError("inference is null " +
                            " for code " + goEvidenceCodeEnum.name() +
                            " and pub " + publicationZdbId + " "
                            , gafEntry);
                }

                if (!GoEvidenceValidator.isInferenceValid(inference, goEvidenceCodeEnum, publicationZdbId)) {
                    throw new GafValidationError("Invalid inference code[" + inference + "] " +
                            " for code " + goEvidenceCodeEnum.name() +
                            " and pub " + publicationZdbId + " "
                            , gafEntry);
                }
                InferenceGroupMember inferenceGroupMember = new InferenceGroupMember();
                inferenceGroupMember.setInferredFrom(inference);
                inferredFrom.add(inferenceGroupMember);
            }
            markerGoTermEvidence.setInferredFrom(inferredFrom);


            try {
                GoEvidenceValidator.validateEvidenceVsPub(goEvidenceCodeEnum, publicationZdbId, inferenceSet.iterator().next());
                GoEvidenceValidator.validateProteinBinding(goEvidenceCodeEnum, inferenceSet,
                        markerGoTermEvidence.getGoTerm().getOboID(),
                        markerGoTermEvidence.getGoTerm().getOntology().getOntologyName(),
                        markerGoTermEvidence.getFlag());
            } catch (ValidationException e) {
                throw new GafValidationError(e.getMessage(), gafEntry);
            }
        }
    }

    protected GoEvidenceQualifier getQualifier(GafEntry gafEntry, GenericTerm goTerm) throws GafValidationError {
        if (!gafEntry.getQualifier().isEmpty()) {
            // they use "contributes_to" and "NOT"
            if (gafEntry.getQualifier().equals("NOT")) {
                return GoEvidenceQualifier.NOT;
            } else if (gafEntry.getQualifier().equals("contributes_to")) {
                return GoEvidenceQualifier.CONTRIBUTES_TO;
            } else if (gafEntry.getQualifier().equals("colocalizes_with")) {
                if (goTerm.getOntology() != Ontology.GO_CC) {
                    throw new GafValidationError(GoEvidenceQualifier.COLOCALIZES_WITH.toString() +
                            " may only be used with " + Ontology.GO_CC.getCommonName());
                }
                return GoEvidenceQualifier.COLOCALIZES_WITH;
            } else {
                throw new GafValidationError("unable to identify GoFlag [" + gafEntry.getQualifier() + "]", gafEntry);
            }
        } else {
            return null;
        }
    }

    protected Publication getPublication(GafEntry gafEntry) throws GafValidationError {
        String pubMedID = gafEntry.getPubmedId();
        Publication publication = null;
        if (StringUtils.isEmpty(pubMedID)) {
            throw new GafValidationError("Must have a Pubmed ID", gafEntry);
        }
        if (pubMedID.startsWith("ZFIN:") && getZfinPubId(pubMedID) != null) {
            publication = RepositoryFactory.getPublicationRepository().getPublication(getZfinPubId(pubMedID));
            if (publication == null) {
                throw new GafValidationError("No pub found for zdbID : " + getZfinPubId(pubMedID), gafEntry);
            }
        } else if (getPubMedId(pubMedID) != null) {
            List<Publication> publications = RepositoryFactory.getPublicationRepository().getPublicationByPmid(getPubMedId(pubMedID));
            if (publications == null || publications.size() == 0) {
                throw new GafValidationError("No pub found for pmid: " + pubMedID);
            } else if (publications.size() == 1) {
                publication = publications.get(0);
            } else if (publications.size() > 1) {
                throw new GafValidationError("Multiple pubs found for pmid: " + pubMedID);
            }
        }
        // if special go_ref, than use the alternate pubmed id
        else if (pubMedID.startsWith(FpInferenceGafParser.GOREF_PREFIX)) {
            if (goRefPubMap.containsKey(pubMedID)) {
                publication = goRefPubMap.get(pubMedID);
            } else {
                // if no pub is found locally, report error (can use citexplore to see if its a proper pub, though)
                throw new GafValidationError("Goref ID is not known or loaded[" + pubMedID + "]", gafEntry);
            }
        }

        if (publication == null) {
            throw new GafValidationError("Unable to find pubmed ID[" + pubMedID + "]", gafEntry);
        }

        // if pub is closed and has an existing ZFIN GO annotation, then report
        return publication;

    }

    /**
     * @param pubMedID String of form: ZFIN:ZDB-PUB-000111-5|PMID:10611375
     * @return Return pub zdbID.
     */
    protected String getZfinPubId(String pubMedID) {
        if (pubMedID.contains("|")) {
            return patternPipe.matcher(pubMedID).replaceAll("$1");
        } else {
            return pubMedID.split(":")[1];
        }
    }

    protected GenericTerm getGoTerm(GafEntry gafEntry) throws GafValidationError {
        String inferences = gafEntry.getInferences();
        if (inferences != null && inferences.startsWith("GO:")) {
            GenericTerm goTermInference = ontologyRepository.getTermByOboID(inferences);
            validateGoTerm(goTermInference.getOboID(), gafEntry);
        }
        GenericTerm goTerm = ontologyRepository.getTermByOboID(gafEntry.getGoTermId());
        validateGoTerm(goTerm.getOboID(), gafEntry);
        return goTerm;
    }

    protected GenericTerm validateGoTerm(String goTermID, GafEntry gafEntry) throws GafValidationError {
        GenericTerm goTerm = ontologyRepository.getTermByOboID(goTermID);
        if (goTerm == null) {
            throw new GafValidationError("Unable to find GO Term for:" + FileUtil.LINE_SEPARATOR + goTermID,
                    gafEntry);
        }
        if (goTerm.isObsolete()) {
            throw new GafValidationError("Go term must not be obsolete:" + FileUtil.LINE_SEPARATOR + goTerm, gafEntry);
        }
        if (goTerm.isSecondary()) {
            throw new GafValidationError("Go term must not be secondary:" + FileUtil.LINE_SEPARATOR + goTerm, gafEntry);
        }
        return goTerm;
    }

    protected String getPubMedId(String pubMedId) {
        if (pubMedId.startsWith(PUBMED_PREFIX)) {
            return pubMedId.substring(PUBMED_PREFIX.length());
        }
        return null;
    }

    protected boolean isMoreSpecificAnnotation(MarkerGoTermEvidence existingMarkerGoTermEvidence, MarkerGoTermEvidence markerGoTermEvidenceToAdd)
            throws GafValidationError {
        if (existingMarkerGoTermEvidence.isSameButGo(markerGoTermEvidenceToAdd)) {
            return ontologyRepository.isParentChildRelationshipExist(markerGoTermEvidenceToAdd.getGoTerm(), existingMarkerGoTermEvidence.getGoTerm());
        }
        return false;
    }

    public void addAnnotation(MarkerGoTermEvidence markerGoTermEvidenceToAdd, GafJobData gafJobData)
            throws GafValidationError {
        // everything seems to be fine, lets try adding some things!
        MarkerGoTermEvidence markerGoTermEvidenceToRemove;
        try {
            markerGoTermEvidenceToRemove = markerGoTermEvidenceRepository.getNdExistsForGoGeneEvidenceCode(markerGoTermEvidenceToAdd);
            if (markerGoTermEvidenceToRemove != null) {
                logger.debug("removing existing with Nd evidence: " + markerGoTermEvidenceToRemove);
                markerGoTermEvidenceRepository.removeEvidence(markerGoTermEvidenceToRemove);
                gafJobData.addRemoved(markerGoTermEvidenceToRemove);
                logger.debug("removed entry " + markerGoTermEvidenceToRemove.getZdbID() + " replaced by " + markerGoTermEvidenceToAdd);
            }

            logger.debug("adding " + markerGoTermEvidenceToAdd);

            markerGoTermEvidenceRepository.addEvidence(markerGoTermEvidenceToAdd);
            RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(markerGoTermEvidenceToAdd.getZdbID(), "MarkerGoTermEvidence", markerGoTermEvidenceToAdd.toString(), "Created new MarkerGoTermEvidence record from GafService load");

            logger.debug("added " + markerGoTermEvidenceToAdd);
        } catch (Exception e) {
            throw new GafValidationError("Failed to commit gaf entry: " + e.toString(), e);
        }
    }

    public void addAnnotationsBatch(List<MarkerGoTermEvidence> batchToAdd, GafJobData gafJobData) throws GafValidationError {

        for (MarkerGoTermEvidence markerGoTermEvidence : batchToAdd) {
            addAnnotation(markerGoTermEvidence, gafJobData);
        }
    }

    public void addAnnotations(GafJobData gafJobData) throws GafValidationError {

        for (MarkerGoTermEvidence markerGoTermEvidence : gafJobData.getNewEntries()) {
            addAnnotation(markerGoTermEvidence, gafJobData);
        }
    }

    public int removeEntriesBatch(List<GafJobEntry> batchToRemove, GafJobData gafJobData) {
        List<String> zdbIDs = new ArrayList<String>();
        for (GafJobEntry gafJobEntry : batchToRemove) {
            zdbIDs.add(gafJobEntry.getZdbID());
        }

        // delete record attributions and then evidences directly
        RepositoryFactory.getInfrastructureRepository().deleteRecordAttributionByDataZdbIDs(zdbIDs);

        return markerGoTermEvidenceRepository.deleteMarkerGoTermEvidenceByZdbIDs(zdbIDs);
    }

    /**
     * Loop over all entries and replace marker IDs if they are merged into new zdb IDs
     *
     * @param gafEntries entries to be modified
     */
    public void replaceMergedZDBIds(List<GafEntry> gafEntries) {
        Map<String, String> oldNewZDBIds = getReplacedDataMapFromEntities(ActiveData.Type.GENE, ActiveData.Type.MRPHLNO);
        for (GafEntry gafEntry : gafEntries) {

            // replace the ZDB Id with replaced one for column 2, object id
            if (StringUtils.startsWith(gafEntry.getEntryId(), "ZDB-")) {
                replaceAttributeOnGafEntry(gafEntry, "entryId", oldNewZDBIds);
            }

            // FB case 7957 "GAF load should handle merged markers"
            // with field
            // examples:
            // ZFIN:ZDB-GENE-030721-3|ZFIN:ZDB-MRPHLNO-070906-3
            // InterPro:IPR000536|InterPro:IPR001628|InterPro:IPR008946
            String[] withFieldPieces = gafEntry.getInferences().split("\\|");

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i <= withFieldPieces.length - 1; i++) {
                String currentWithField = withFieldPieces[i];
                if (StringUtils.startsWith(currentWithField, "ZFIN:")) {
                    String[] withFieldsZFIN = currentWithField.split(":");
                    String withFieldZDBId = withFieldsZFIN[1];
                    if (StringUtils.startsWith(withFieldZDBId, "ZDB-")) {
                        sb.append(withFieldsZFIN[0]);     // "ZFIN"
                        sb.append(":");
                        if (oldNewZDBIds.containsKey(withFieldZDBId)) {
                            String replacedWithFieldZDBId = oldNewZDBIds.get(withFieldZDBId);
                            sb.append(replacedWithFieldZDBId);
                        } else {
                            sb.append(withFieldZDBId);
                        }
                    }
                } else {
                    sb.append(withFieldPieces[i]);
                }
                if (i < withFieldPieces.length - 1) {
                    sb.append("|");
                }
            }
            gafEntry.setInferences(sb.toString());

        }
    }

    public void replaceAttributeOnGafEntry(GafEntry gafEntry, String fieldName, Map<String, String> translationMap) {
        if (gafEntry == null || fieldName == null || translationMap == null)
            return;
        try {
            String oldFieldValue = (String) PropertyUtils.getProperty(gafEntry, fieldName);
            if (translationMap.containsKey(oldFieldValue))
                PropertyUtils.setProperty(gafEntry, fieldName, translationMap.get(oldFieldValue));
        } catch (IllegalAccessException e) {
            logger.error(e);
        } catch (InvocationTargetException e) {
            logger.error(e);
        } catch (NoSuchMethodException e) {
            logger.error(e);
        }
    }

    public Map<String, String> getReplacedDataMapFromEntities(ActiveData.Type... types) {
        if (types == null)
            return null;
        Map<String, String> oldNewZDBIds = new HashMap<String, String>();
        for (ActiveData.Type type : types) {
            List<ReplacementZdbID> replacedEntityIds = RepositoryFactory.getInfrastructureRepository().getReplacedZdbIDsByType(type);
            for (ReplacementZdbID replacementZdbID : replacedEntityIds) {
                oldNewZDBIds.put(replacementZdbID.getOldZdbID(), replacementZdbID.getReplacementZdbID());
            }
        }
        return oldNewZDBIds;
    }
}
