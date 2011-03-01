package org.zfin.gwt;

import org.junit.Test;
import org.zfin.gwt.root.dto.GoDefaultPublication;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 */
public class InferenceUnitTest {

    @Test
    public void simpleMatch() {
        String testString1 = "GenBank:12345";
        assertTrue(testString1.matches("GenBank:.*"));
        String testString2 = "ZFIN:ZDB-MRPHLNO-12345";
        assertTrue(testString2.matches("ZFIN:ZDB-MRPHLNO-.*|asdf"));
        assertEquals("GenBank:.*", InferenceCategory.GENBANK.match());
        assertEquals("ZFIN:ZDB-MRPHLNO-.*|ZFIN:ZDB-GENO-.*", InferenceCategory.ZFIN_MRPH_GENO.match());
        assertEquals("ZFIN:ZDB-GENE-.*", InferenceCategory.ZFIN_GENE.match());
//        assertEquals("GenBank:.*",InferenceCategory.GENBANK.match());
//        "GenBank.*".
        assertEquals(InferenceCategory.GENBANK, InferenceCategory.getInferenceCategoryByValue(testString1));
        assertEquals(InferenceCategory.ZFIN_MRPH_GENO, InferenceCategory.getInferenceCategoryByValue(testString2));
        assertEquals(InferenceCategory.ZFIN_GENE, InferenceCategory.getInferenceCategoryByValue("ZFIN:ZDB-GENE-123213"));
        assertEquals(InferenceCategory.ZFIN_MRPH_GENO, InferenceCategory.getInferenceCategoryByValue("ZFIN:ZDB-GENO-1234"));
    }

    @Test
    public void testInferenceComparator() {
        Set<InferenceGroupMember> inferenceGroupMembers1 = null;
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();
        markerGoTermEvidence.setInferredFrom(inferenceGroupMembers1);

        Set<InferenceGroupMember> inferenceGroupMembers2 = null;
        assertTrue(markerGoTermEvidence.sameInferences(inferenceGroupMembers2));

        inferenceGroupMembers2 = new HashSet<InferenceGroupMember>();
        assertTrue(markerGoTermEvidence.sameInferences(inferenceGroupMembers2));

        inferenceGroupMembers1 = new HashSet<InferenceGroupMember>();
        assertTrue(markerGoTermEvidence.sameInferences(inferenceGroupMembers2));

        InferenceGroupMember inferenceGroupMemberA = new InferenceGroupMember();
        inferenceGroupMemberA.setInferredFrom("dog");
        inferenceGroupMembers1.add(inferenceGroupMemberA);
        markerGoTermEvidence.setInferredFrom(inferenceGroupMembers1);
        assertFalse(markerGoTermEvidence.sameInferences(inferenceGroupMembers2));


        InferenceGroupMember inferenceGroupMemberB = new InferenceGroupMember();
        inferenceGroupMemberB.setInferredFrom("cat");
        inferenceGroupMembers2.add(inferenceGroupMemberB);
        assertFalse(markerGoTermEvidence.sameInferences(inferenceGroupMembers2));

        inferenceGroupMemberB.setInferredFrom("dog");
        assertTrue(markerGoTermEvidence.sameInferences(inferenceGroupMembers2));
    }


    @Test
    public void testMarkerGoEvidenceEquals() {
        Set<InferenceGroupMember> inferenceGroupMembers1 = null;
        MarkerGoTermEvidence markerGoTermEvidence1 = new MarkerGoTermEvidence();
        markerGoTermEvidence1.setInferredFrom(inferenceGroupMembers1);


        MarkerGoTermEvidence markerGoTermEvidence2 = new MarkerGoTermEvidence();
        Set<InferenceGroupMember> inferenceGroupMembers2 = null;
        assertTrue(markerGoTermEvidence1.sameInferences(inferenceGroupMembers2));
        markerGoTermEvidence2.setInferredFrom(inferenceGroupMembers2);
        assertTrue(markerGoTermEvidence1.equals(markerGoTermEvidence2));


        inferenceGroupMembers2 = new HashSet<InferenceGroupMember>();
        assertTrue(markerGoTermEvidence1.sameInferences(inferenceGroupMembers2));

        inferenceGroupMembers1 = new HashSet<InferenceGroupMember>();
        assertTrue(markerGoTermEvidence1.sameInferences(inferenceGroupMembers2));

        InferenceGroupMember inferenceGroupMemberA = new InferenceGroupMember();
        inferenceGroupMemberA.setInferredFrom("dog");
        inferenceGroupMembers1.add(inferenceGroupMemberA);
        markerGoTermEvidence1.setInferredFrom(inferenceGroupMembers1);
        assertFalse(markerGoTermEvidence1.sameInferences(inferenceGroupMembers2));


        InferenceGroupMember inferenceGroupMemberB = new InferenceGroupMember();
        inferenceGroupMemberB.setInferredFrom("cat");
        inferenceGroupMembers2.add(inferenceGroupMemberB);
        assertFalse(markerGoTermEvidence1.sameInferences(inferenceGroupMembers2));

        inferenceGroupMemberB.setInferredFrom("dog");
        assertTrue(markerGoTermEvidence1.sameInferences(inferenceGroupMembers2));
    }


    @Test
    public void getOtherInferencesForPub() {

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.EC.zdbID()).length);
        assertEquals(InferenceCategory.EC, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.EC.zdbID())[0]);

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.SPKW.zdbID()).length);
        assertEquals(InferenceCategory.SP_KW, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.SPKW.zdbID())[0]);

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.INTERPRO.zdbID()).length);
        assertEquals(InferenceCategory.INTERPRO, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.INTERPRO.zdbID())[0]);

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_ENSEMBL.zdbID()).length);
        assertEquals(InferenceCategory.ENSEMBL, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_ENSEMBL.zdbID())[0]);

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_SP_SL.zdbID()).length);
        assertEquals(InferenceCategory.SP_SL, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_SP_SL.zdbID())[0]);

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_HAMAP.zdbID()).length);
        assertEquals(InferenceCategory.HAMAP, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_HAMAP.zdbID())[0]);

        assertEquals(1, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_UNIPROT.zdbID()).length);
        assertEquals(InferenceCategory.UNIPROTKB, GoEvidenceCodeEnum.IEA.getInferenceCategories(GoDefaultPublication.GOREF_UNIPROT.zdbID())[0]);
    }

}
