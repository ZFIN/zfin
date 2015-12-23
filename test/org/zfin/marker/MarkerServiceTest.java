package org.zfin.marker;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.expression.service.ExpressionService;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerService;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;
import org.zfin.orthology.presentation.OrthologyPresentationRow;
import org.zfin.profile.Organization;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getProfileRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Tests for org.zfin.marker.service.MarkerService
 */
public class MarkerServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(MarkerServiceTest.class);

    @Test
    public void getRelatedMarkerDisplayTest() {
        //this method isn't in MarkerService, but it's used by MarkerService


        //piles of fake data
        MarkerType gene = new MarkerType();
        gene.setDisplayName("gene");
        HashSet<Marker.TypeGroup> geneGroups = new HashSet<>();
        geneGroups.add(Marker.TypeGroup.GENEDOM);
        gene.setTypeGroups(geneGroups);

        MarkerType clone = new MarkerType();
        HashSet<Marker.TypeGroup> cloneGroups = new HashSet<>();
        //todo: this will likely change to something like ALLCLONES
        cloneGroups.add(Marker.TypeGroup.CLONE);
        clone.setTypeGroups(cloneGroups);

        MarkerType transcript = new MarkerType();
        HashSet<Marker.TypeGroup> transcriptGroups = new HashSet<>();
        transcriptGroups.add(Marker.TypeGroup.GENEDOM);
        transcript.setTypeGroups(transcriptGroups);
        transcript.setDisplayName("transcript");

        Marker gene1 = new Marker();
        gene1.setName("gene");
        gene1.setMarkerType(gene);
        gene1.setZdbID("1");


        Clone BAC1 = new Clone();
        BAC1.setName("BAC1");
        BAC1.setMarkerType(clone);
        BAC1.setZdbID("2");

        Clone BAC2 = new Clone();
        BAC2.setName("BAC2");
        BAC2.setMarkerType(clone);
        BAC2.setZdbID("3");

        Transcript t1 = new Transcript();
        t1.setName("Transcript1");
        t1.setMarkerType(transcript);
        t1.setZdbID("4");


        Transcript t2 = new Transcript();
        t2.setName("Transcript2");
        t2.setMarkerType(transcript);
        t2.setZdbID("5");

        MarkerRelationshipType produces = new MarkerRelationshipType();
        produces.setFirstToSecondLabel("produces");
        produces.setSecondToFirstLabel("produced by");

        MarkerRelationship gene1ProducesT1 = new MarkerRelationship();
        gene1ProducesT1.setMarkerRelationshipType(produces);
        gene1ProducesT1.setFirstMarker(gene1);
        gene1ProducesT1.setSecondMarker(t1);

        RelatedMarker rm = new RelatedMarker(gene1, gene1ProducesT1);
        RelatedMarkerDisplay rmd = new RelatedMarkerDisplay();

        rmd.addRelatedMarker(rm);

        String firstLabel = rmd.keySet().iterator().next();
        TreeMap<MarkerType, TreeSet<RelatedMarker>> typeMap = rmd.get(firstLabel);
        MarkerType firstType = typeMap.keySet().iterator().next();

        assertTrue("RelatedMarkerDisplay should at least one key", rmd.keySet().size() > 0);
        assertTrue("typeMap should have at least one entry", typeMap.keySet().size() > 0);
        assertEquals("firstType should be transcript", transcript, firstType);


    }

    @Test
    public void mergeMarkerTest() {
        MergeMarkerValidator validator = new MergeMarkerValidator();
//        Antibody a = new Antibody();
//        a.setHeavyChainIsotype("a");
//
//        Antibody b = new Antibody();
//        b.setHeavyChainIsotype("b");

        assertFalse(validator.isEqualOrUnspecified("a", "b"));
        assertTrue(validator.isEqualOrUnspecified("a", "a"));
        assertTrue(validator.isEqualOrUnspecified("a", null));
        assertTrue(validator.isEqualOrUnspecified("a", ""));
        assertTrue(validator.isEqualOrUnspecified(null, null));
        assertTrue(validator.isEqualOrUnspecified("", ""));
        assertTrue(validator.isEqualOrUnspecified(null, "b"));
        assertTrue(validator.isEqualOrUnspecified("", "b"));
    }

    @Test
    public void relatedMarkerDisplayFast() {
        Marker m = getMarkerRepository().getGeneByAbbreviation("pax6a");
        List<MarkerRelationshipPresentation> rels;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false);
        assertNotNull(rels);
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true);
        assertTrue(rels.size() > 3);
        assertNotNull(rels);
    }

    @Test
    public void relatedMarkerDisplayFastMultipleSuppliers() {
        Marker m = getMarkerRepository().getGeneByID("ZDB-GENE-990415-72");
        List<MarkerRelationshipPresentation> rels;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false);
        assertNotNull(rels);
        assertEquals(1, rels.size());
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true);
        assertNotNull(rels);
        assertEquals(5, rels.size());

        // the est should have one, and the
        String link1 = rels.get(0).getLinkWithAttributionAndOrderThis();
        assertNotNull(link1);
        String link2 = rels.get(1).getLinkWithAttributionAndOrderThis();
        assertNotNull(link2);
    }

    @Test
    public void relatedMarkerDisplayFastMultipleAttributions() {
        Marker m = getMarkerRepository().getGeneByID("ZDB-GENE-010718-1");
        List<MarkerRelationshipPresentation> rels;
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, false);
        assertNotNull(rels);
        assertEquals(1, rels.size());
        rels = MarkerService.getRelatedMarkerDisplayExcludeType(m, true);
        assertNotNull(rels);
        assertEquals(2, rels.size());

        // the est should have 3 attributions and one orderThis
        MarkerRelationshipPresentation mrp = rels.get(0);
        assertEquals(1, mrp.getOrganizationLinks().size());
        assertEquals(3, mrp.getAttributionZdbIDs().size());

        String link1 = rels.get(0).getLinkWithAttributionAndOrderThis();
        assertNotNull(link1);
        String link2 = rels.get(1).getLinkWithAttributionAndOrderThis();
        assertNotNull(link2);
    }


    @Test
    public void geneViewController() throws Exception {
        GeneViewController geneViewController = new GeneViewController();
        geneViewController.setExpressionService(new ExpressionService());
        geneViewController.setMarkerRepository(getMarkerRepository());
        Model model = new ExtendedModelMap();
        geneViewController.getGeneView("ZDB-GENE-001103-1", model);
    }


    @Test
    public void getSequenceInfoSummary() {
        Marker m = getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfoSummary(m);
        assertThat(sequenceInfo.getNumberDBLinks(), greaterThan(11));
        assertThat(sequenceInfo.getNumberDBLinks(), lessThan(50)); // was 18
        assertEquals(4, sequenceInfo.getDbLinks().size());
    }

    @Test
    public void getSequenceInfoFull() {
        Marker m = getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        SequencePageInfoBean sequenceInfo = MarkerService.getSequenceInfoFull(m);
        assertThat(sequenceInfo.getDbLinks().size(), greaterThan(7));
        assertThat(sequenceInfo.getDbLinks().size(), lessThan(50)); // was 9
        Iterator<DBLink> iter = sequenceInfo.getDbLinks().iterator();
        DBLink dbLink;
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA, dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.REFSEQ, dbLink.getReferenceDatabase().getForeignDB().getDbName());
        dbLink = iter.next();
        assertEquals(ForeignDBDataType.DataType.RNA, dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK, dbLink.getReferenceDatabase().getForeignDB().getDbName());
//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.RNA,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.GENBANK,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.UNIPROTKB,dbLink.getReferenceDatabase().getForeignDB().getDbName());
//
//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.REFSEQ,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.POLYPEPTIDE,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.GENPEPT,dbLink.getReferenceDatabase().getForeignDB().getDbName());

//        dbLink = iter.next();
//        assertEquals(ForeignDBDataType.DataType.SEQUENCE_CLUSTERS,dbLink.getReferenceDatabase().getForeignDBDataType().getDataType());
//        assertEquals(ForeignDB.AvailableName.UNIGENE,dbLink.getReferenceDatabase().getForeignDB().getDbName());

        MarkerDBLink markerDBLink;

        TreeMap<String, TreeSet<MarkerDBLink>> related = sequenceInfo.getRelatedMarkerDBLinks();
        assertThat(related, hasKey("Encodes"));
        assertThat(related.get("Encodes"), hasSize(1));
        markerDBLink = related.get("Encodes").iterator().next();

        assertEquals(ForeignDBDataType.DataType.GENOMIC, markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK, markerDBLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals("eu736", markerDBLink.getMarker().getAbbreviation());

        assertThat(related, hasKey("Contained in"));
        assertThat(related.get("Contained in"), hasSize(3));
        Iterator<MarkerDBLink> secondIter = related.get("Contained in").iterator();
        markerDBLink = secondIter.next();

        assertEquals(ForeignDBDataType.DataType.GENOMIC, markerDBLink.getReferenceDatabase().getForeignDBDataType().getDataType());
        assertEquals(ForeignDB.AvailableName.GENBANK, markerDBLink.getReferenceDatabase().getForeignDB().getDbName());
        assertEquals(Marker.Type.BAC, markerDBLink.getMarker().getType());
        markerDBLink = secondIter.next();
        assertEquals(Marker.Type.BAC, markerDBLink.getMarker().getType());
        markerDBLink = secondIter.next();
        assertEquals(Marker.Type.FOSMID, markerDBLink.getMarker().getType());
    }

    @Test
    public void getSnpMappedMarkers() {
        Marker marker;
        MappedMarkerBean mappedMarkerBean;

        marker = getMarkerRepository().getMarkerByID("ZDB-SNP-060626-1008");
        assertNotNull(marker);
        mappedMarkerBean = MarkerService.getSnpMappedMarkers(marker);
        assertNotNull(mappedMarkerBean);
        List<String> unmappedMarkers = mappedMarkerBean.getUnMappedMarkers();
        assertEquals(1, unmappedMarkers.size());
        assertEquals("25", unmappedMarkers.iterator().next());

        // case 7593, should not be null even if null related markers
        marker = getMarkerRepository().getMarkerByID("ZDB-SNP-060626-97");
        assertNotNull(marker);
        mappedMarkerBean = MarkerService.getSnpMappedMarkers(marker);
        assertNotNull(mappedMarkerBean);
    }

    @Test
    public void getSequenceInfoFullNotDupe() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-GENE-030616-329");
        SequencePageInfoBean sequencePageInfoBean = MarkerService.getSequenceInfoFull(m);
        TreeMap<String, TreeSet<MarkerDBLink>> secMarkerDBLinks = sequencePageInfoBean.getRelatedMarkerDBLinks();
        Set<String> keys = secMarkerDBLinks.keySet();
        assertThat(keys, hasSize(1));
        assertThat(secMarkerDBLinks.get(keys.iterator().next()), hasSize(2));

    }

    @Test
    public void pullClonesOntoGeneFromTranscript() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-GENE-010606-1");
        GeneBean geneBean = new GeneBean();
        geneBean.setMarker(m);
        MarkerService.createDefaultViewForMarker(geneBean);
        assertEquals(1, geneBean.getMarkerRelationshipPresentationList().size());
        assertEquals("EST", geneBean.getMarkerRelationshipPresentationList().get(0).getMarkerType());
        MarkerService.pullClonesOntoGeneFromTranscript(geneBean);
        assertEquals(4, geneBean.getMarkerRelationshipPresentationList().size());

        assertEquals("BAC", geneBean.getMarkerRelationshipPresentationList().get(0).getMarkerType());
        assertEquals("BAC", geneBean.getMarkerRelationshipPresentationList().get(1).getMarkerType());
        assertEquals("Fosmid", geneBean.getMarkerRelationshipPresentationList().get(2).getMarkerType());
        assertEquals("EST", geneBean.getMarkerRelationshipPresentationList().get(3).getMarkerType());
    }

    @Test
    public void pullGeneOntoCloneFromTranscript() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-BAC-100127-974");
        CloneBean cloneBean = new CloneBean();
        cloneBean.setMarker(m);
        MarkerService.createDefaultViewForMarker(cloneBean);
        assertEquals(3, cloneBean.getMarkerRelationshipPresentationList().size());
        MarkerService.pullGeneOntoCloneFromTranscript(cloneBean);
        assertEquals(4, cloneBean.getMarkerRelationshipPresentationList().size());
        assertEquals("Gene", cloneBean.getMarkerRelationshipPresentationList().get(0).getMarkerType());
    }


    @Test
    public void pullGeneOntoCloneFromTranscript2() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-BAC-060503-669");
        CloneBean cloneBean = new CloneBean();
        cloneBean.setMarker(m);
        MarkerService.createDefaultViewForMarker(cloneBean);
        int count = 0;
        for (MarkerRelationshipPresentation markerRelationshipPresentation : cloneBean.getMarkerRelationshipPresentationList()) {
            if (markerRelationshipPresentation.getZdbId().startsWith("ZDB-GENE")) {
                ++count;
            }
        }
        assertEquals(9, count);
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), greaterThan(5));
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), lessThan(400));

        MarkerService.pullGeneOntoCloneFromTranscript(cloneBean);
        count = 0;
        for (MarkerRelationshipPresentation markerRelationshipPresentation : cloneBean.getMarkerRelationshipPresentationList()) {
            if (markerRelationshipPresentation.getZdbId().startsWith("ZDB-GENE")) {
                ++count;
            }
        }
        assertTrue(count > 10);
        assertThat(cloneBean.getMarkerRelationshipPresentationList().size(), greaterThan(30));
    }

    @Test
    public void getEnsemblAccessionId() {
        String geneAbbreviation = "pax2a";
        Marker marker = getMarkerRepository().getMarkerByAbbreviation(geneAbbreviation);
        String accession = MarkerService.getEnsemblAccessionId(marker);
        assertNotNull(accession);
        assertEquals("ENSDARG00000028148", accession);
    }

    @Test
    public void getGeneExpression() {
        String geneAbbreviation = "fus";
        Marker marker = getMarkerRepository().getMarkerByAbbreviation(geneAbbreviation);
        ExpressionService expressionService = new ExpressionService();
        MarkerExpression expressions = expressionService.getExpressionForGene(marker);
        assertNotNull(expressions);
    }

    @Test
    public void getConstructsForGene() {
        String geneAbbreviation = "EGFP";
        Marker efg = getMarkerRepository().getMarkerByAbbreviation(geneAbbreviation);
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION);
        Set<Marker> set = MarkerService.getRelatedMarker(efg, types);
        assertNotNull(set);
    }

    @Test
    public void getPhenotypeOnGene() {
        String geneAbbreviation = "ZDB-GENE-000627-2";
        Marker gene = getMarkerRepository().getMarkerByID(geneAbbreviation);
        PhenotypeOnMarkerBean bean = MarkerService.getPhenotypeOnGene(gene);
        assertNotNull(bean);
    }

    @Test
    public void getOrthologyForGene() {
        // pax2a
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-990415-8");

        OrthologyPresentationBean orthology = MarkerService.getOrthologyEvidence(m);
        List<OrthologyPresentationRow> orthologs = orthology.getOrthologs();
        assertThat("pax2a should have human and mouse orthologs", orthologs, hasSize(greaterThanOrEqualTo(2)));

        for (OrthologyPresentationRow ortholog : orthologs) {
            Collection<OrthologEvidencePresentation> evidenceCollection = ortholog.getEvidence();
            assertThat("each ortholog should have some evidence", evidenceCollection, not(empty()));
            for (OrthologEvidencePresentation evidence : evidenceCollection) {
                assertThat(evidence.getCode(), notNullValue());
                assertThat(evidence.getPublications(), not(empty()));
            }
        }
    }

    @Test
    public void getOrthologyForGeneAndPublication() {
        // pax2a
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-990415-8");
        // Pfeffer
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-980916-4");

        OrthologyPresentationBean orthology = MarkerService.getOrthologyEvidence(m, publication);
        List<OrthologyPresentationRow> orthologs = orthology.getOrthologs();
        assertThat("pax2a should only have mouse orthology in Pheffer et al., 1998", orthologs, hasSize(1));

        for (OrthologyPresentationRow ortholog : orthologs) {
            Collection<OrthologEvidencePresentation> evidenceCollection = ortholog.getEvidence();
            assertThat("each ortholog should have some evidence", evidenceCollection, not(empty()));
            for (OrthologEvidencePresentation evidence : evidenceCollection) {
                assertThat(evidence.getCode(), notNullValue());

                Set<Publication> publications = evidence.getPublications();
                assertThat(publications, hasSize(1));
                assertThat(publications.iterator().next(), is(publication));
            }
        }

    }

    @Test
    public void getOrthologyPresentationBeanNoOrthologs() {
        // SETUP -- simulate a gene with an orthology note but no orthologs (yes, this really happens)
        Marker m = new Marker();
        String noteText = "What a time to be alive";
        OrthologyNote note = new OrthologyNote();
        note.setMarker(m);
        note.setNote(noteText);
        m.setOrthologyNotes(new HashSet<>(Collections.singletonList(note)));
        Collection<Ortholog> orthologs = new ArrayList<>();

        // EXECUTE
        OrthologyPresentationBean bean = MarkerService.getOrthologyPresentationBean(orthologs, m, null);

        // VERIFY
        assertThat("OrthologyPresentationBean should not be null", bean, is(notNullValue()));
        assertThat("Note text should match", bean.getNote(), is(noteText));
        assertThat("Orthologs should be empty", bean.getOrthologs(), is(nullValue()));
    }

    @Test
    public void getSTRModificationNote() {
        String sequence = "AATTGGCCTTAAGG";
        String actual = MarkerService.getSTRModificationNote(sequence, true, false);
        assertThat(actual, is("Reported sequence " + sequence + " was reversed."));

        actual = MarkerService.getSTRModificationNote(sequence, true, true);
        assertThat(actual, is("Reported sequence " + sequence + " was reversed and complemented."));

        actual = MarkerService.getSTRModificationNote(sequence, false, true);
        assertThat(actual, is("Reported sequence " + sequence + " was complemented."));
    }

    @Test
    public void markerHasSupplier() {
        Marker marker = getMarkerRepository().getMarkerByID("ZDB-BAC-060503-214");
        Organization bprc = getProfileRepository().getLabById("ZDB-LAB-040701-1");
        Organization geneTools = getProfileRepository().getCompanyById("ZDB-COMPANY-000502-1");

        assertThat(bprc.getZdbID() + " should be a supplier for " + marker.getZdbID(),
                MarkerService.markerHasSupplier(marker, bprc), is(true));

        assertThat(geneTools.getZdbID() + " should not be a supplier for " + marker.getZdbID(),
                MarkerService.markerHasSupplier(marker, geneTools), is(false));
    }
}
