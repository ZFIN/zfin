package org.zfin.sequence.reno.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Accession;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.LinkageGroup;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.presentation.CandidateBean;
import org.zfin.sequence.reno.repository.RenoRepository;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getOrthologyRepository;

/**
 * Common reno services.
 */
@Service
public class RenoService {

    private final Logger logger = Logger.getLogger(RenoService.class);

    private Map<Marker, Set<LinkageGroup>> cachedLinkageGroupMap = new HashMap<Marker, Set<LinkageGroup>>();

    public List<Marker> checkForExistingRelationships(CandidateBean candidateBean, RunCandidate rc) {
        List<Marker> associatedMarkers = getAllSingleAssociatedGenesFromQueriesForRunCandidate(rc);
        List<Marker> identifiedMarkers = rc.getIdentifiedMarkers();
        List<Marker> smallSegments = getRelatedMarkers(identifiedMarkers);
        candidateBean.setSmallSegments(smallSegments);

        if (associatedMarkers != null) {
            for (Marker associatedMarker : associatedMarkers) {
                MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
                for (Marker smallSegment : smallSegments) {
                    boolean hasRelationship = markerRepository.hasSmallSegmentRelationship(associatedMarker, smallSegment);
                    if (hasRelationship) {
                        candidateBean.addMessage("This candidate already has a small-segment relationship to " +
                                associatedMarker.getAbbreviation());
                    }
                }
            }
        }
        return associatedMarkers;
    }

    public List<Marker> getRelatedMarkers(List<Marker> identifiedMarkers) {
        List<Marker> segments = getSmallSegementClones(identifiedMarkers);
        if (CollectionUtils.isEmpty(segments)) {
            segments = getTranscriptProducts(identifiedMarkers);
        }
        return segments;
    }

    public List<Marker> getSmallSegementClones(List<Marker> markers) {
        List<Marker> segments = new ArrayList<Marker>();

        //pull the ESTs from the candidate
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)
                    && !segments.contains(m)) {
                segments.add(m);
            }
        }
        logger.debug("createRelationships segments.size(): " + segments.size());
        return segments;
    }


    public List<Marker> getTranscriptProducts(List<Marker> markers) {
        List<Marker> segments = new ArrayList<Marker>();

        //pull the ESTs from the candidate
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)
                    && !segments.contains(m)) {
                segments.add(m);
            }
        }
        logger.debug("createRelationships segments.size(): " + segments.size());
        return segments;
    }

    /**
     * Creates a set of Evidence Codes from a set of evidence code strings coming from the submission form.
     *
     * @param formEvidenceCodes Set of evidences
     * @param orthologyPub      publication
     * @param ortholog          Ortholog
     * @return set of OrthologEvidence codes
     */
    public Set<OrthologEvidence> createEvidenceCollection(Set<OrthologEvidence.Code> formEvidenceCodes, Publication orthologyPub, Ortholog ortholog) {
        HashSet<OrthologEvidence> orthoEvidences = new HashSet<>();
        if (formEvidenceCodes != null) {
            for (OrthologEvidence.Code orthoevidence : formEvidenceCodes) {
                OrthologEvidence oe = new OrthologEvidence();
                oe.setEvidenceCode(getOrthologyRepository().getEvidenceCode(orthoevidence.getString()));
                oe.setPublication(orthologyPub);
                oe.setOrtholog(ortholog);
                orthoEvidences.add(oe);
            }
        }
        ortholog.setEvidenceSet(orthoEvidences);
        return orthoEvidences;

    }

    /**
     * Retrieve the linkage groups for all hit-related marker or clones.
     * For hit get the ZFIN marker object and check for the linkage groups.
     * If there are none found and the marker is a clone check the associated gene
     * and its linkage groups.
     *
     * @param rc Runcandidate
     */
    public void populateLinkageGroups(RunCandidate rc) {
        if (rc == null)
            return;

        List<Query> queries = rc.getCandidateQueryList();
        logger.info("populateLinkageGroups rc: " + rc.getZdbID());
        logger.info("populateLinkageGroups queries.size: " + queries.size());
        for (Query query : queries) {
            Set<Hit> hits = query.getBlastHits();
            logger.info("populateLinkageGroups hits.size: " + hits.size());
            for (Hit hit : hits) {
                logger.debug("populateLinkageGroups hit: " + hit.getZdbID());
                logger.debug("populateLinkageGroups hit.getTargetAccession: " + hit.getTargetAccession().getID());
                Set<MarkerDBLink> markerDBLinks = hit.getTargetAccession().getBlastableMarkerDBLinks();
                logger.debug("populateLinkageGroups markerDBLinks.size: " + markerDBLinks.size());

                Set<LinkageGroup> hitLinkageGroup = new TreeSet<LinkageGroup>();
                for (MarkerDBLink markerDBLink : markerDBLinks) {
                    logger.debug("populateLinkageGroups markerDBLink: " + markerDBLink.getZdbID());
                    Marker marker = markerDBLink.getMarker();
                    if (false == cachedLinkageGroupMap.containsKey(marker)) {
                        cachedLinkageGroupMap.put(marker, MarkerService.getLinkageGroups(marker));
                    }
                    hitLinkageGroup.addAll(cachedLinkageGroupMap.get(marker));
//                    hit.getTargetAccession().setLinkageGroups(MarkerService.getLinkageGroups(markerDBLink.getMarker()));
                }
                hit.getTargetAccession().setLinkageGroups(hitLinkageGroup);
            }
        }
    }

    public void renameGene(Marker gene, String attributionZdbID) {
        Publication pub = new Publication();
        pub.setZdbID(attributionZdbID);
        //logger.info("geneToRename in renameGene method: " + gene.getAbbreviation().toString());

        RepositoryFactory.getMarkerRepository().renameMarker(gene, pub, MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES);
        RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(gene, "data_alias", "", "", "");

    }

    /**
     * Remove note from Candidate, make it a new datanote on a gene.
     *
     * @param rc   a RunCandidate that will lose it's note
     * @param gene a Marker that will gain a note.
     */
    public void moveNoteToGene(RunCandidate rc, Marker gene) {

        logger.info("enter moveNoteToGene");
        logger.info("existingGene abbrev: " + gene.getAbbreviation() + " " + gene.getZdbID() + " rc:" + rc.getZdbID());
        if (!StringUtils.isEmpty(rc.getCandidate().getNote())) {
            logger.debug("attach a data note to the gene");
            RepositoryFactory.getMarkerRepository().addMarkerDataNote(gene, rc.getCandidate().getNote());
            rc.getCandidate().setNote(null);
        }
        logger.info("exit moveNoteToGene");
    }

    /**
     * Handle a lock or unlock request.
     * Obtain a lock if:
     * 1) action code is lock
     * 2) if runcandidate is not already locked
     * Unlock if action code is unlock
     *
     * @param candidateBean Candidate Bean
     */
    public void handleLock(CandidateBean candidateBean) {

        RenoRepository rr = RepositoryFactory.getRenoRepository();
        Person currentUser = ProfileService.getCurrentSecurityUser();
        RunCandidate rc = candidateBean.getRunCandidate();

        if (StringUtils.equals(candidateBean.getAction(), CandidateBean.LOCK_RECORD)) {
            boolean success = rr.lock(currentUser, rc);
            if (success)
                logger.info(currentUser.getZdbID() + " is locking " + rc.getZdbID());
            else
                logger.error("couldn't get lock for " + currentUser.getUsername());

        } else if (StringUtils.equals(candidateBean.getAction(), CandidateBean.UNLOCK_RECORD)) {
            rr.unlock(currentUser, rc);
            logger.info(currentUser.getZdbID() + " is unlocking " + rc.getZdbID());
        }
    }

    /**
     * Finishes all in-queue runs.
     *
     * @param run Run to finish.
     */
    public void finishRemainderRedundancy(Run run) {
        List<RunCandidate> runCandidates = RepositoryFactory.getRenoRepository().getSangerRunCandidatesInQueue(run);
        RenoRepository rr = RepositoryFactory.getRenoRepository();
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        Person currentUser = ProfileService.getCurrentSecurityUser();


        // these are all unlocked, so must lock all of them first.
        for (RunCandidate runCandidate : runCandidates) {
            rr.lock(currentUser, runCandidate);
        }

        // now handle them
        for (RunCandidate runCandidate : runCandidates) {

            if (mr.getMarkerByName(runCandidate.getCandidate().getSuggestedName()) == null &&
                    mr.getMarkerByAbbreviation(runCandidate.getCandidate().getSuggestedName().toLowerCase()) == null) {
                handleRedundancyNovelGene(runCandidate);
                runCandidate.setDone(true);
                runCandidate.getCandidate().setLastFinishedDate(new Date());

            } else {
                logger.warn("marker exists, can not assign name to it: " + runCandidate.getCandidate().getSuggestedName());
            }
        }
    }


    public void handleRedundancyNovelGene(RunCandidate runCandidate) {
        logger.info("enter handleNovelGene");

        if (!runCandidate.getRun().isRedundancy()) {
            logger.info("run should be redundancy");
            return;
        }

        //Create a Marker object
        Marker novelGene = new Marker();

        novelGene.setName(runCandidate.getCandidate().getSuggestedName());
        novelGene.setOwner(runCandidate.getLockPerson());
        logger.info("novelGene is set: " + novelGene.getName());

        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        MarkerType mt = mr.getMarkerTypeByName(runCandidate.getCandidate().getMarkerType());
        if (mt == null) {
            String newline = System.getProperty("line.separator");
            String message = "No Marker Type with name " + runCandidate.getCandidate().getMarkerType() + " found for " +
                    " Candidate: " + newline + runCandidate.getCandidate();
            throw new NullPointerException(message);
        }
        // if a new gene is created make sure the abbreviation is lower case according to
        // nomenclature conventions.
        String suggestedAbbreviation = runCandidate.getCandidate().getSuggestedName();
        if (mt.getType() == Marker.Type.GENE) {
            suggestedAbbreviation = suggestedAbbreviation.toLowerCase();
        }
        novelGene.setAbbreviation(suggestedAbbreviation);
        novelGene.setMarkerType(mt);
        mr.createMarker(novelGene, ((RedundancyRun) runCandidate.getRun()).getRelationPublication(), false);
        logger.info("novelGene zdb_id: " + novelGene.getZdbID());
        //update marker history reason
        MarkerHistory mhist = mr.getLastMarkerHistory(novelGene, MarkerHistory.Event.ASSIGNED);

        if (mhist == null) {
            String errorMessage = "No Marker History found. Trigger did not run! ";
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);

        }
        // change the reason for creating the marker in the Marker History
        mhist.setReason(MarkerHistory.Reason.NOT_SPECIFIED);

        createRedundancyRelationships(runCandidate, novelGene);

        //create data note, copy curator note to data note, set curator note to null
        moveNoteToGene(runCandidate, novelGene);
    }


    /**
     * Creates marker relationships (or DBLinks) to connect markers (or accessions)
     * <p/>
     * the basic case is, for one est, or more than one est, we just make a marker relationship.
     * <p/>
     * if there are genes and ests both, it's a cleanup issue.  that's not handled yet
     * <p/>
     * if there is only a gene, then there's no new relationships to add, because that gene
     * came in because it already had a link to the query accession
     * <p/>
     * if there are no markers at all associated with the query accessions, then we link
     * them to the gene that the curators chose.
     *
     * @param rc   the RunCandidate
     * @param gene the gene chosen by the curators (could be newly created)
     */
    public void createRedundancyRelationships(RunCandidate rc, Marker gene) {
        logger.info("createRelationships gene: " + gene);
        logger.info("createRelationships runCanZdbID: " + rc.getZdbID());

        if (!rc.getRun().isRedundancy()) {
            logger.info("run should be redundancy");
            return;
        }
        String attributionZdbID = ((RedundancyRun) rc.getRun()).getRelationPublication().getZdbID();

        // thing to associate with
        List<Marker> markers = rc.getIdentifiedMarkers();
        logger.debug("createRelationships markers.size(): " + markers.size());
        List<Marker> relatedMarkers = getRelatedMarkers(markers);

        //pull the single gene from the collection, if there is one.
        Marker candidateMarker = null;
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                logger.debug("createRelationships marker type: " + m.getMarkerType().getType());
                logger.debug("createRelationships is in type group genedom");
                candidateMarker = m;
                break;
            } else if (m.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
                logger.debug("createRelationships marker type: " + m.getMarkerType().getType());
                logger.debug("createRelationships is in type group transcript");
                candidateMarker = m;
                break;
            } else {
                logger.debug("createRelationships NOT in type group genedom or transcript");

            }
        }
        logger.debug("createRelationships rc.getCandidateQueries().size(): " + rc.getCandidateQueries().size());
        //pull the query accessions from the runCandidate
        Set<Accession> accessions = new HashSet<Accession>();
        for (Query q : rc.getCandidateQueries()) {
            accessions.add(q.getAccession());
            logger.debug("adding accessions");
        }

        //if there are segments, we associate them with the gene,
        //if there are not, we make dblinks connecting the query
        //accessions to the gene.
        //if there is a gene associated with the query accessions, ignore it,
        //because the association we would make already exists
        if (!relatedMarkers.isEmpty()) {
            logger.debug("createRelationships segments are not empty");
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            for (Marker segment : relatedMarkers) {
                logger.info("adding small segment to gene: " + segment);
                MarkerRelationship mrel = new MarkerRelationship();
                if (segment.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
                    mrel.setType(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
                } else {
                    mrel.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
                }
                mrel.setFirstMarker(gene);
                mrel.setSecondMarker(segment);
                if (mr.getMarkerRelationship(gene, segment, mrel.getType()) == null) {
                    mr.addMarkerRelationship(mrel, attributionZdbID);
                } else {
                    logger.info("marker relationship alredy exists for:\n" + mrel + " for run candidate:\n" + rc);
                }
            }
            //our query accession(s) was(were) linked to one or more segments, we just made
            //relationships from those segments to the gene that the curator chose
            //now, if there is a dblink from the accession directly to that gene,
            //we delete it.  (unless it's directly attributed to a journal article)
            MarkerService.removeRedundantDBLinks(gene, accessions);

        } else if (candidateMarker == null) {
            //no segments & no genes means that we have an accession that
            //has yet to be linked to any marker at all, so we link it to whatever
            //gene the curators chose.
            createRedundancyDBLinks(rc, gene);
        }
    }

    /**
     * Associate the query accessions directly to the gene, because we don't
     * have an EST to put in between them
     *
     * @param rc   Runcandidate
     * @param gene Gene object
     */
    private void createRedundancyDBLinks(RunCandidate rc, Marker gene) {
        logger.info("creating DBLinks");

        if (!rc.getRun().isRedundancy()) {
            logger.info("run should be redundancy");
            return;
        }
        //create DBLinks for all queries
        String attributionZdbID = ((RedundancyRun) rc.getRun()).getRelationPublication().getZdbID();
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        for (Query q : rc.getCandidateQueries()) {
            mr.addDBLink(gene, q.getAccession().getNumber(),
                    q.getAccession().getReferenceDatabase(), attributionZdbID);
        }
    }

    public List<Marker> getMarkersForAccession(Accession accession) {
        List<Marker> markers = new ArrayList<>();

        List<DBLink> links = RepositoryFactory.getSequenceRepository().getDBLinksForAccession(accession);

        for (DBLink link : links) {

            if (link instanceof MarkerDBLink) {
                MarkerDBLink markerLink = (MarkerDBLink) link;
                markers.add(markerLink.getMarker());
            }
        }
        return markers;
    }


    /**
     * Return all genes found in list of blasthits
     *
     * @return Marker objects for genes found in list of blast hits
     */
    public List<Marker> getAllSingleAssociatedGenesFromQueriesForRunCandidate(RunCandidate rc) {
        List<Marker> genes = new ArrayList<Marker>();

        logger.debug("enter getAllAssociatedGenesFromQueries: " + rc.getCandidateQueries().size());

        for (Query q : rc.getCandidateQueries()) {
            logger.debug("I've got a query: " + q.getAccession().getNumber() + " num hits: " + q.getBlastHits().size());
            for (Hit h : q.getBlastHits()) {
                logger.debug("I've got a hit: " + h.getZdbID());
                logger.debug("I've got a hit accession: " + h.getTargetAccession().getNumber());
                Accession a = h.getTargetAccession();
                List<Marker> genesToAdd = new ArrayList<Marker>();
                logger.debug("number of genes for hit: " + genes.size());
                logger.debug("accession_id: " + a.getID());

                for (Marker m : getMarkersForAccession(a)) {
                    logger.debug("I've got a Marker: " + m.getAbbreviation() + " of type: " + m.getMarkerType().getType());
                    logger.debug("genes.contains(m): " + genes.contains(m));
                    logger.debug("is in type group genedom: " + m.isInTypeGroup(Marker.TypeGroup.GENEDOM));
                    // if the hit is a gene, then add directly
                    if ((m.isInTypeGroup(Marker.TypeGroup.GENEDOM))
                            && (!genes.contains(m))) {
                        logger.debug("ADDING genedom gene: " + m.getAbbreviation());
                        genesToAdd.add(m);
//                        genes.add(m);
                    }
                    // if the hit is not a gene, then add any genes that encode it
                    else {
                        Set<MarkerRelationship> secondMarkerRelationships = m.getSecondMarkerRelationships();
                        logger.debug(m.getAbbreviation() + (secondMarkerRelationships != null ? " number of second marker relationships: " + secondMarkerRelationships.size() : "null"));
                        for (MarkerRelationship rel : m.getSecondMarkerRelationships()) {
                            Marker gene = rel.getFirstMarker();
                            logger.debug("gene: " + (gene == null ? "null" : gene.getAbbreviation()));
                            logger.debug("encoding gene is in type group genedom: " + gene.isInTypeGroup(Marker.TypeGroup.GENEDOM));
                            logger.debug("genes to add size: " + genesToAdd.size());
                            logger.debug("genes to add contains: " + genesToAdd.contains(gene));

                            if (gene.isInTypeGroup(Marker.TypeGroup.GENEDOM) && !genesToAdd.contains(gene) && rel.getType().equals(
                                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT)) {
                                logger.debug("ADDING encoding gene: " + gene.getAbbreviation());
                                genesToAdd.add(gene);
                            } else {
                                logger.debug("NOT adding encoding gene: " + gene.getAbbreviation());
                            }
                            if (gene.isInTypeGroup(Marker.TypeGroup.GENEDOM) && !genesToAdd.contains(gene) && rel.getType().equals(
                                    MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT)) {
                                logger.debug("ADDING encoding gene: " + gene.getAbbreviation());
                                genesToAdd.add(gene);
                            } else {
                                logger.debug("NOT adding encoding gene: " + gene.getAbbreviation());
                            }
                        }
                        // only add if a single encoded relationship
                    }
                }
                logger.debug("genes to add " + genesToAdd.size() + " for hit accession " + a.getNumber());
                if (genesToAdd.size() == 1) {
                    logger.debug("adding one gene: " + genesToAdd.get(0).getAbbreviation());
                    Marker geneToAdd = genesToAdd.get(0);
                    if (!genes.contains(geneToAdd)) {
                        genes.add(genesToAdd.get(0));
                    }
                }
            }
        }
        return genes;
    }


}
