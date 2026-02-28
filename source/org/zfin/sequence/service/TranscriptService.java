package org.zfin.sequence.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.Species;
import org.zfin.framework.HibernateUtil;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.jbrowse.presentation.GenomeBrowserImageBuilder;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Genotype;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zfin.Species.Type.ZEBRAFISH;
import static org.zfin.genomebrowser.GenomeBrowserBuild.GRCZ11;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.ForeignDB.AvailableName.RNA_CENTRAL;
import static org.zfin.sequence.ForeignDBDataType.DataType.OTHER;
import static org.zfin.sequence.ForeignDBDataType.SuperType.SUMMARY_PAGE;

public class TranscriptService {

    private final static Logger logger = LogManager.getLogger(TranscriptService.class);

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

    public static List<DBLink> getRelatedRNACentralIDs(Marker gene) {
        ReferenceDatabase rnaCentralReferenceDatabase = getSequenceRepository().getReferenceDatabase(RNA_CENTRAL, OTHER, SUMMARY_PAGE, ZEBRAFISH);
        Set<RelatedMarker> transcripts = getRelatedTranscripts(gene);
        List<DBLink> links = new ArrayList<>();

        for (RelatedMarker relatedMarker : transcripts) {
            Transcript transcript = getMarkerRepository().getTranscriptByZdbID(relatedMarker.getMarker().getZdbID());
            List<TranscriptDBLink> dblinks = getSequenceRepository().getTranscriptDBLinksForTranscript(transcript, rnaCentralReferenceDatabase);
            for (TranscriptDBLink link : dblinks) {
                links.add(link);
            }
        }
        Collections.sort(links, Comparator.comparing(DBLink::getAccessionNumber));
        return links;
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
            List<TranscriptDBLink> transcriptDisplayGroups = RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForMarkerAndDisplayGroup(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
            if (CollectionUtils.isNotEmpty(transcriptDisplayGroups) && transcriptDisplayGroups.size() == 1) {
                rm.setDisplayedSequenceDBLinks(transcriptDisplayGroups);
            } else {
                rm.setDisplayedSequenceDBLinks(transcriptDisplayGroups.stream().filter(transcriptDBLink -> transcriptDBLink.getReferenceDatabase().getForeignDB().getDbName().equals(ForeignDB.AvailableName.ENSEMBL_TRANS)).toList());
            }
            rm.setHasHavanna(transcriptDisplayGroups.stream().anyMatch(transcriptDBLink -> transcriptDBLink.getReferenceDatabase().getForeignDB().getDbName().equals(ForeignDB.AvailableName.VEGA_TRANS)));
            rtd.add(rm);
        }

        if (displayGBrowseImage
            && (getLinkageRepository().hasGenomeLocation(gene, MarkerGenomeLocation.Source.ZFIN)
                || getLinkageRepository().hasGenomeLocation(gene, MarkerGenomeLocation.Source.NCBI))) {
            GenomeLocation.Source locationSource = getLinkageRepository().hasGenomeLocation(gene, MarkerGenomeLocation.Source.ZFIN)
                ? GenomeLocation.Source.ZFIN
                : GenomeLocation.Source.NCBI;
            MarkerGenomeLocation landmark = getLinkageRepository().getGenomeLocation(gene, locationSource).get(0);
            int startPadding = (landmark.getEnd() - landmark.getStart()) / 10;
            int endPadding = (landmark.getEnd() - landmark.getStart()) / 20;
            GenomeBrowserImageBuilder imageBuilder = new GenomeBrowserImageBuilder()
                .setLandmarkByGenomeLocation(landmark)
                .genomeBuild(GRCZ11)
                // add 10% left padding and 5% right padding
                .withPadding(startPadding, endPadding)
                .tracks(GenomeBrowserTrack.Page.GENE_TRANSCRIPTS.getTracks());
            if (highlightedTranscript != null) {
                imageBuilder.highlight(highlightedTranscript.getAbbreviation());
            }
            rtd.setGbrowseImage(imageBuilder.build());
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
        List<DBLink> dbLinks = new ArrayList<>();

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
        Collection<DBLink> dbLinks = getSupportingDBLinks(transcript);
        sequenceInfo.addDBLinks(dbLinks);

        logger.debug((sequenceInfo.getDbLinks() == null ? "none " : sequenceInfo.getDbLinks().size()) + " marker linked sequence dblinks");

        return sequenceInfo;

    }

    public static SequenceInfo getAllSequenceInfo(Transcript transcript) {
        SequenceInfo sequenceInfo = new SequenceInfo();
        Collection<DBLink> dbLinks = getSupportingDBLinks(transcript);
        sequenceInfo.addDBLinks(dbLinks);

        logger.debug((sequenceInfo.getDbLinks() == null ? "none " : sequenceInfo.getDbLinks().size()) + " marker linked sequence dblinks");

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
     * targets and a collection of RelatedMarkers for published targets
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
        transcript.setOwner(ProfileService.getCurrentSecurityUser());

        HibernateUtil.currentSession().save(transcript);
        HibernateUtil.currentSession().flush();
        //finally, run the regen names script
        //  markerRepository.runMarkerNameFastSearchUpdate(transcript);

        return transcript;
    }


    public static List<MarkerDBLink> getProteinMarkerDBLinksForAccessionForRefDBName(String accessionString, String dbName) {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.getType(dbName),
            ForeignDBDataType.DataType.POLYPEPTIDE, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
        return RepositoryFactory.getSequenceRepository().getMarkerDBLinksForAccession(accessionString, referenceDatabase);
    }

    public static List<TranscriptDBLink> getProteinTranscriptDBLinksForAccessionForRefDBName(String accessionString, String dbName) {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(ForeignDB.AvailableName.getType(dbName),
            ForeignDBDataType.DataType.POLYPEPTIDE, ForeignDBDataType.SuperType.SEQUENCE, Species.Type.ZEBRAFISH);
        return RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForAccession(accessionString, referenceDatabase);
    }


    public static SortedSet<Genotype> getNonReferenceStrainsForTranscript(Transcript transcript) {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

        SortedSet<Genotype> genotypes = new TreeSet<>();

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
        List<TranscriptDBLink> links = new ArrayList<>();
        for (TranscriptDBLink transcriptDBLink : transcript.getTranscriptDBLinks()) {
            if (transcriptDBLink.getReferenceDatabase().isInDisplayGroup(groupName)) {
                links.add(transcriptDBLink);
            }
        }
        return links;
    }

    public static Integer getTranscriptLength(Transcript transcript, DisplayGroup.GroupName displayGroup) {
        Integer length = 0;
        for (DBLink link : RepositoryFactory.getSequenceRepository().getTranscriptDBLinksForMarkerAndDisplayGroup(transcript, displayGroup)) {
            if ((link.getLength() != null)
                && (link.getLength() > length))
                length = link.getLength();
        }
        if (length == 0) return null;
        else return length;
    }


    public static List<TranscriptTypeStatusDefinition> getAllTranscriptTypeStatusDefinitions() {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        return markerRepository.getAllTranscriptTypeStatusDefinitions();
    }

    public static List<TranscriptType> getAllTranscriptTypes() {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        return markerRepository.getAllTranscriptTypes();
    }

    public static boolean isSupportingSequence(TranscriptDBLink transcriptDBLink) {
        return transcriptDBLink.isInDisplayGroup(DisplayGroup.GroupName.TRANSCRIPT_LINKED_SEQUENCE);
    }

    public static List<RelatedMarker> getSortedTranscripts(List<RelatedMarker> transcripts, boolean withdrawn) {
        List<Transcript> transcriptlist = new ArrayList<>();
        List<RelatedMarker> transcriptsAsMRelatedMarker = new ArrayList<>();
        for (RelatedMarker marker : transcripts) {
            Transcript transcript = TranscriptService.convertMarkerToTranscript(marker.getMarker());
            if (withdrawn) {
                if (transcript.isWithdrawn()) {
                    transcriptsAsMRelatedMarker.add(marker);
                    transcriptlist.add(transcript);
                }
            } else {
                if (!transcript.isWithdrawn()) {
                    transcriptsAsMRelatedMarker.add(marker);
                    transcriptlist.add(transcript);
                }
            }
        }

        List<RelatedMarker> sortedTranscripts = new ArrayList<>();
        if (transcriptsAsMRelatedMarker.size() > 0) {
            transcriptlist.sort(
                Comparator.comparing(Transcript::getTranscriptType).thenComparing(Transcript::getAbbreviationOrder)
            );
            ;
            for (Transcript t : transcriptlist) {
                for (RelatedMarker m : transcriptsAsMRelatedMarker) {
                    if (t == TranscriptService.convertMarkerToTranscript(m.getMarker())) {
                        sortedTranscripts.add(m);
                    }
                }
            }
        }

        return sortedTranscripts;
    }

    // check if there are transcripts with names according to
    // <geneSymbol>-<index>
    public static Integer getLargestTxIndex(List<Transcript> links, String geneSymbol) {
        List<Integer> indexes = links.stream()
            .map(Marker::getAbbreviation)
            .map(txAbbrev -> {
                Pattern pattern = Pattern.compile("(" + geneSymbol + "-)(\\d{3})");
                Matcher matcher = pattern.matcher(txAbbrev);
                boolean matchFound = matcher.find();
                if (matchFound) {
                    return Integer.parseInt(matcher.group(2));
                } else {
                    return 0;
                }
            })
            .sorted()
            .toList();
        return indexes.get(indexes.size() - 1);
    }
}
