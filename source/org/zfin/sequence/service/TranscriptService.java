package org.zfin.sequence.service;

import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.gbrowse.GBrowseService;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.orthology.Species;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.util.*;

/**
 * This class
 */
public class TranscriptService {

    private final static Logger logger = Logger.getLogger(TranscriptService.class);

    public static Transcript convertMarkerToTranscript(Marker marker) {
        return RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(marker.getZdbID());
    }


    public static Set<RelatedMarker> getRelatedGenes(Transcript transcript) {
        Set<RelatedMarker> relatedMarkers;
        relatedMarkers = MarkerService.getRelatedMarkers(transcript, MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        return relatedMarkers;
    }

    public static Set<Marker> getRelatedGenesFromTranscript(Marker marker) {
        Set<RelatedMarker> relatedMarkers = MarkerService.getRelatedMarkers(marker, MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        Set<Marker> genes = new TreeSet<Marker>();
        for (RelatedMarker relatedMarker : relatedMarkers) {
            genes.add(relatedMarker.getMarkerRelationship().getFirstMarker());
        }
        return genes;
    }

    public static Set<RelatedMarker> getRelatedTranscripts(Marker gene) {
        Set<RelatedMarker> relatedMarkers;
        relatedMarkers = MarkerService.getRelatedMarkers(gene, MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        return relatedMarkers;
    }

    public static Set<RelatedMarker> getTargetGenes(Transcript transcript) {
        Set<RelatedMarker> relatedMarkers;
        relatedMarkers = MarkerService.getRelatedMarkers(transcript, MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE);
        return relatedMarkers;
    }

    public static Set<RelatedMarker> getRelatedTranscriptsForTranscript(Transcript transcript) {
        Set<RelatedMarker> relatedTranscripts = new TreeSet<RelatedMarker>();

        Set<RelatedMarker> relatedGenes = getRelatedGenes(transcript);
        for (RelatedMarker relatedGene : relatedGenes) {
            Marker gene = relatedGene.getMarker();
            for (RelatedMarker relatedTranscript : TranscriptService.getRelatedTranscripts(gene)) {
                //don't add this transcript to it's own related transcript list
                if (!relatedTranscript.getMarker().equals(transcript))
                    relatedTranscripts.add(relatedTranscript);
            }
        }

        return relatedTranscripts;
    }


    public static RelatedTranscriptDisplay getRelatedTranscriptsForGene(Marker gene) {
        return getRelatedTranscriptsForGene(gene, null);
    }

    public static RelatedTranscriptDisplay getRelatedTranscriptsForGene(Marker gene, Transcript highlightedTranscript) {
        return getRelatedTranscriptsForGene(gene, highlightedTranscript, true);
    }

    public static RelatedTranscriptDisplay getRelatedTranscriptsForGene(Marker gene, Transcript highlightedTranscript, boolean displayGBrowseImage) {
        RelatedTranscriptDisplay rtd = new RelatedTranscriptDisplay();
        rtd.setGene(gene);
        Set<RelatedMarker> relatedTranscripts = getRelatedTranscripts(gene);

        for (RelatedMarker rm : relatedTranscripts) {
            Transcript transcript = convertMarkerToTranscript(rm.getMarker());
            rm.setMarker(transcript);
            rm.setDisplayedSequenceDBLinks(TranscriptService.getDBLinksForDisplayGroup(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE));
            rtd.add(rm);
        }

        if (displayGBrowseImage ) {
            try {
                logger.debug("attempting to get GBrowseImage list");
                rtd.setGbrowseImages(GBrowseService.getGBrowseTranscriptImages(gene, highlightedTranscript));
            } catch (Exception e) {
                logger.error("Couldn't get GBrowse Feature " + e.getMessage());
            }
        } else {
            logger.debug("not even trying showing GBrowse image, probably because the indexer is asking for the page");
        }
        return rtd;
    }


    /**
     * Excludes gene relationships
     *
     * @param transcript to build display object for
     * @return display map
     */
    public static RelatedMarkerDisplay getRelatedMarkerDisplay(Transcript transcript) {
        RelatedMarkerDisplay rmd = new RelatedMarkerDisplay();

        //this could go either way, there are presently three relationships that can exist
        //for transcripts, so we can include only one or exclude only the other.  The
        //pattern in the apg code is to exclude, which favors that when things are
        //added to the database, the software will display them without new changes

        for (MarkerRelationship mrel : transcript.getFirstMarkerRelationships()) {
            if (!mrel.getType().equals(MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE))
                rmd.addRelatedMarker(new RelatedMarker(transcript, mrel));
        }
        for (MarkerRelationship mrel : transcript.getSecondMarkerRelationships()) {
            if (!mrel.getType().equals(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT))
                rmd.addRelatedMarker(new RelatedMarker(transcript, mrel));
        }

        return rmd;

    }


    /**
     * @param transcript The transcript to get supporting sequence info for.
     * @return Supporting sequences.
     */
    public static List<DBLink> getSupportingDBLinks(Transcript transcript) {
        List<DBLink> dbLinks = new ArrayList<DBLink>();

        for (TranscriptDBLink dblink : transcript.getTranscriptDBLinks()) {
            if (dblink.isInDisplayGroup(DisplayGroup.GroupName.TRANSCRIPT_LINKED_SEQUENCE))
                dbLinks.add(dblink);
        }

        logger.debug(dbLinks.size() + " marker linked sequence dblinks");

        return dbLinks;

    }

    /**
     * @param transcript The transcript to get supporting sequence info for.
     * @return Supporting sequences.
     */
    public static SequenceInfo getSupportingSequenceInfo(Transcript transcript) {
        SequenceInfo sequenceInfo = new SequenceInfo();
        List<DBLink> dbLinks = getSupportingDBLinks(transcript);
        sequenceInfo.addDBLinks(dbLinks);

        logger.debug(sequenceInfo.size() + " marker linked sequence dblinks");

        return sequenceInfo;

    }

    public static SummaryDBLinkDisplay getSummaryPages(Transcript transcript) {
        SummaryDBLinkDisplay sp = new SummaryDBLinkDisplay();

        TreeSet<TranscriptDBLink> sortedDBLinks = new TreeSet<TranscriptDBLink>();
        sortedDBLinks.addAll(transcript.getTranscriptDBLinks());

        for (DBLink dblink : sortedDBLinks) {
            if (dblink.isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE))
                sp.addDBLink(dblink);
        }

        return sp;
    }

    public static SummaryDBLinkDisplay getProteinProductDBLinks(Transcript transcript) {
        SummaryDBLinkDisplay sp = new SummaryDBLinkDisplay();
        for (DBLink dblink : transcript.getTranscriptDBLinks()) {
            if (dblink.isInDisplayGroup(DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE))
                sp.addDBLink(dblink);
        }

        return sp;
    }

    /**
     * Build the TranscriptTargets presentation object for a given transcript
     *
     * @param transcript transcript to build object for
     * @return Presentation object containing a single dblink for the predicted
     *         targets and a collection of RelatedMarkers for published targets
     */
    public static TranscriptTargets getTranscriptTargets(Transcript transcript) {
        TranscriptTargets targets = new TranscriptTargets();

        //todo: replace with database methods when there's support
        //TranscriptDBLink dblink = new TranscriptDBLink();
        //dblink.setTranscript(transcript);
        //if (transcript.getTranscriptDBLinks().size() > 0)
        //    targets.setPredictedTarget(transcript.getTranscriptDBLinks().iterator().next());
        //targets.setPublishedTargets(TranscriptService.getTargetGenes(transcript));

        for (TranscriptDBLink dblink : transcript.getTranscriptDBLinks()) {
            if (dblink.isInDisplayGroup(DisplayGroup.GroupName.MICRORNA_TARGETS))
                targets.setPredictedTarget(dblink);
        }
        targets.setPublishedTargets(TranscriptService.getTargetGenes(transcript));


        return targets;
    }


    /**
     * @param transcriptAddBean Creates transcript add bean
     * @return Created transcript for bean
     */
    public static Transcript createTranscript(TranscriptAddBean transcriptAddBean) {

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

        Transcript transcript = new Transcript();
        transcript.setName(transcriptAddBean.getName());
        transcript.setAbbreviation(transcriptAddBean.getName().toLowerCase());
        transcript.setTranscriptType(markerRepository.getTranscriptTypeForName(transcriptAddBean.getChosenType()));
        transcript.setStatus(markerRepository.getTranscriptStatusForName(transcriptAddBean.getChosenStatus()));


        // set type
        MarkerType realMarkerType = new MarkerType();
        realMarkerType.setName(Marker.Type.TSCRIPT.toString());
        realMarkerType.setType(Marker.Type.getType(Marker.Type.TSCRIPT.toString()));
        Set<Marker.TypeGroup> typeGroup = new HashSet<Marker.TypeGroup>();
        typeGroup.add(Marker.TypeGroup.getType(Marker.TypeGroup.TRANSCRIPT.toString()));
        realMarkerType.setTypeGroups(typeGroup);
        transcript.setMarkerType(realMarkerType);

        // set owner
        Person owner = RepositoryFactory.getProfileRepository().getPerson(transcriptAddBean.getOwnerZdbID());
        transcript.setOwner(owner);

        HibernateUtil.currentSession().save(transcript);
        HibernateUtil.currentSession().flush();
        //finally, run the regen names script
        markerRepository.runMarkerNameFastSearchUpdate(transcript);

        return transcript;
    }


    public static List<MarkerDBLink> getProteinMarkerDBLinksForAccessionForRefDBName(String accessionString, String dbName) {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.getType(dbName),
                ForeignDBDataType.DataType.POLYPEPTIDE, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
        return RepositoryFactory.getSequenceRepository().getMarkerDBLinksForAccession(accessionString, referenceDatabase);
    }

    public static List<TranscriptDBLink> getProteinTranscriptDBLinksForAccessionForRefDBName(String accessionString, String dbName) {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.getType(dbName),
                ForeignDBDataType.DataType.POLYPEPTIDE, ForeignDBDataType.SuperType.SEQUENCE, Species.ZEBRAFISH);
        return RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(accessionString, referenceDatabase);
    }


    public static SortedSet<Genotype> getNonReferenceStrainsForTranscript(Transcript transcript) {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

        SortedSet<Genotype> genotypes = new TreeSet<Genotype>();

        Genotype referenceStrain = getVegaReferenceStrain();

        Set<RelatedMarker> relatedClones = MarkerService.getRelatedMarkers(transcript, MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT);

        for (RelatedMarker relatedClone : relatedClones) {
            //first convert the marker object into a clone object - without this step it
            //will just be a marker and won't cast down properly to clone.
            Clone clone = markerRepository.getCloneById(relatedClone.getMarker().getZdbID());
            ProbeLibrary probeLib = clone.getProbeLibrary();
            Genotype strain = (probeLib == null) ? null : probeLib.getStrain();

            if (strain != null) {
                logger.debug(clone.getName() + " has strain " + strain.getHandle());
                boolean isSameBackground = strain.equals(referenceStrain);
                logger.debug(strain.getHandle() + ".equals(" + referenceStrain + "): " + isSameBackground);
            }


            if (strain != null && !strain.equals(referenceStrain)) {
                logger.debug("Adding " + strain.getHandle() + " to NonReferenceStrains for " + transcript.getAbbreviation());
                genotypes.add(strain);
            }
        }
        return genotypes;
    }

    public static Genotype getVegaReferenceStrain() {
        return RepositoryFactory.getMutantRepository().getGenotypeByHandle(Genotype.Wildtype.TU.toString());
    }

    public static List<TranscriptDBLink> getDBLinksForDisplayGroup(Transcript transcript, DisplayGroup.GroupName groupName) {
        List<TranscriptDBLink> links = new ArrayList<TranscriptDBLink>();
        for (TranscriptDBLink transcriptDBLink : transcript.getTranscriptDBLinks()) {
            if (transcriptDBLink.getReferenceDatabase().isInDisplayGroup(groupName)) {
                links.add(transcriptDBLink);
            }
        }
        return links;
    }

    public static Integer getTranscriptLength(Transcript transcript, DisplayGroup.GroupName displayGroup) {
        Integer length = 0;
        for (TranscriptDBLink link : getDBLinksForDisplayGroup(transcript, displayGroup)) {
            if ((link.getLength() != null)
                    && (link.getLength() > length))
                length = link.getLength();
        }
        if (length == 0) return null;
        else return length;
    }


    /**
     * Count the number of total attributions to for a Transcript object including
     * attributions to:
     * aliases
     * dblinks
     * marker relationships
     *
     * @param transcript Transcript to get citations for
     * @return number of unique publications associated with this Transcript or it's components
     */
    public static Set<Publication> getAllAttributionPublications(Transcript transcript) {
        Set<Publication> publications = new HashSet<Publication>();

        // add alias associated publictions
        for (Publication pub : MarkerService.getAliasAttributions(transcript)) {
            publications.add(pub);
        }

        //add attributions from marker relationships
        for (Publication pub : MarkerService.getMarkerRelationshipAttributions(transcript)) {
            publications.add(pub);
        }

        //add attributions from dblinks
        for (Publication pub : MarkerService.getDBLinkPublicaions(transcript)) {
            publications.add(pub);
        }

        return publications;
    }


    public static List<TranscriptTypeStatusDefinition> getAllTranscriptTypeStatusDefinitions() {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        return markerRepository.getAllTranscriptTypeStatusDefinitions();
    }

    public static List<TranscriptType> getAllTranscriptTypes() {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        return markerRepository.getAllTranscriptTypes();
    }

    public static boolean isSupportingSequence(TranscriptDBLink transcriptDBLink){
        return transcriptDBLink.isInDisplayGroup(DisplayGroup.GroupName.TRANSCRIPT_LINKED_SEQUENCE);
    }
}
