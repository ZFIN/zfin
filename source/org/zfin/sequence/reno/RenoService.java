package org.zfin.sequence.reno;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerService;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.LinkageGroup;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.presentation.CandidateBean;

import java.util.*;

/**
 * Common reno services.
 */
public class RenoService {

    private static final Logger LOG = Logger.getLogger(RenoService.class);

    private static Map<Marker, Set<LinkageGroup>> cachedLinkageGroupMap = new HashMap<Marker, Set<LinkageGroup>>();

    public static List<Marker> checkForExistingRelationships(CandidateBean candidateBean, RunCandidate rc) {
        List<Marker> associatedMarkers = rc.getAllSingleAssociatedGenesFromQueries();
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

    public static List<Marker> getRelatedMarkers(List<Marker> identifiedMarkers) {
        List<Marker> segments = getSmallSegementClones(identifiedMarkers);
        if (CollectionUtils.isEmpty(segments)) {
            segments = getTranscriptProducts(identifiedMarkers);
        }
        return segments;
    }

    public static List<Marker> getSmallSegementClones(List<Marker> markers) {
        List<Marker> segments = new ArrayList<Marker>();

        //pull the ESTs from the candidate
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)
                    && !segments.contains(m)) {
                segments.add(m);
            }
        }
        LOG.debug("createRelationships segments.size(): " + segments.size());
        return segments;
    }


    public static List<Marker> getTranscriptProducts(List<Marker> markers) {
        List<Marker> segments = new ArrayList<Marker>();

        //pull the ESTs from the candidate
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)
                    && !segments.contains(m)) {
                segments.add(m);
            }
        }
        LOG.debug("createRelationships segments.size(): " + segments.size());
        return segments;
    }

    /**
     * Creates a set of Evidence Codes from a set of evidence code strings coming from the submission form.
     *
     * @param formEvidenceCodes Set of evidences
     * @param orthologyPub      publication
     * @return set of OrthoEvidence codes
     */
    public static Set<OrthoEvidence> createEvidenceCollection(Set<OrthoEvidence.Code> formEvidenceCodes, Publication orthologyPub) {
        HashSet<OrthoEvidence> OrthoEvidences = new HashSet<OrthoEvidence>();
        for (OrthoEvidence.Code orthoevidence : formEvidenceCodes) {
            OrthoEvidence oe = new OrthoEvidence();
            oe.setOrthologueEvidenceCode(orthoevidence);
            oe.setPublication(orthologyPub);
            OrthoEvidences.add(oe);
        }
        return OrthoEvidences;

    }

    /**
     * Retrieve the linkage groups for all hit-related marker or clones.
     * For hit get the ZFIN marker object and check for the linkage groups.
     * If there are none found and the marker is a clone check the associated gene
     * and its linkage groups.
     *
     * @param rc Runcandidate
     */
    public static void populateLinkageGroups(RunCandidate rc) {
        if (rc == null)
            return;

        List<Query> queries = rc.getCandidateQueryList();
        LOG.info("popularLinkageGroups rc: " + rc.getZdbID());
        LOG.info("popularLinkageGroups queries.size: " + queries.size());
        for (Query query : queries) {
            Set<Hit> hits = query.getBlastHits();
            LOG.info("popularLinkageGroups hits.size: " + hits.size());
            for (Hit hit : hits) {
                LOG.debug("popularLinkageGroups hit: " + hit.getZdbID());
                LOG.debug("popularLinkageGroups hit.getTargetAccession: " + hit.getTargetAccession().getID());
                Set<MarkerDBLink> markerDBLinks = hit.getTargetAccession().getBlastableMarkerDBLinks();
                LOG.debug("popularLinkageGroups markerDBLinks.size: " + markerDBLinks.size());

                Set<LinkageGroup> hitLinkageGroup = new TreeSet<LinkageGroup>();
                for (MarkerDBLink markerDBLink : markerDBLinks) {
                    LOG.debug("popularLinkageGroups markerDBLink: " + markerDBLink.getZdbID());
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

    public static void renameGene(Marker gene, String attributionZdbID) {
        Person currentUser = Person.getCurrentSecurityUser();
        Publication pub = new Publication();
        pub.setZdbID(attributionZdbID);
        RepositoryFactory.getMarkerRepository().renameMarker(gene, pub, MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES);
        RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(gene, "data_alias", "", currentUser, "", "");
//        ir.insertUpdatesTable(geneToRename.getZdbID(),"dalias_alias",geneToRename.getAbbreviation(),"",rc.getLockPerson().getZdbID(),rc.getLockPerson().getName());

    }

    /**
     * Remove note from Candidate, make it a new datanote on a gene.
     *
     * @param rc   a RunCandidate that will lose it's note
     * @param gene a Marker that will gain a note.
     */
    public static void moveNoteToGene(RunCandidate rc, Marker gene) {

        LOG.info("enter moveNoteToGene");
        LOG.info("existingGene abbrev: " + gene.getAbbreviation());
        if (!StringUtils.isEmpty(rc.getCandidate().getNote())) {
            LOG.debug("attach a data note to the gene");
            RepositoryFactory.getMarkerRepository().addMarkerDataNote(gene, rc.getCandidate().getNote(), rc.getLockPerson());
            rc.getCandidate().setNote(null);
        }
        LOG.info("exit moveNoteToGene");
    }
}
