package org.zfin.mutant;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.expression.ConditionDataType;

import java.util.List;

import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Smoke tests for mutant entity Hibernate mappings converted from mutant.hbm.xml to JPA annotations.
 * Each test loads a single row to verify the mapping works end-to-end.
 */
public class MutantEntityMappingTest extends AbstractDatabaseTest {

    @Test
    public void loadGoFlag() {
        GoFlag flag = (GoFlag) currentSession()
            .createQuery("from GoFlag").setMaxResults(1).uniqueResult();
        assertNotNull("GoFlag should load from DB", flag);
        assertNotNull(flag.getName());
    }

    @Test
    public void loadMarkerGoTermEvidenceCreatedBySource() {
        MarkerGoTermEvidenceCreatedBySource src = (MarkerGoTermEvidenceCreatedBySource) currentSession()
            .createQuery("from MarkerGoTermEvidenceCreatedBySource").setMaxResults(1).uniqueResult();
        assertNotNull("MarkerGoTermEvidenceCreatedBySource should load from DB", src);
        assertNotNull(src.getName());
    }

    @Test
    public void loadGafOrganization() {
        GafOrganization org = (GafOrganization) currentSession()
            .createQuery("from GafOrganization").setMaxResults(1).uniqueResult();
        assertNotNull("GafOrganization should load from DB", org);
        assertNotNull(org.getOrganization());
    }

    @Test
    public void loadGenotypeFeature() {
        GenotypeFeature gf = (GenotypeFeature) currentSession()
            .createQuery("from GenotypeFeature").setMaxResults(1).uniqueResult();
        assertNotNull("GenotypeFeature should load from DB", gf);
        assertNotNull(gf.getZdbID());
        assertNotNull(gf.getGenotype());
        assertNotNull(gf.getFeature());
    }

    @Test
    public void loadConditionDataType() {
        ConditionDataType cdt = (ConditionDataType) currentSession()
            .createQuery("from ConditionDataType").setMaxResults(1).uniqueResult();
        assertNotNull("ConditionDataType should load from DB", cdt);
        assertNotNull(cdt.getZdbID());
    }

    @Test
    public void loadMarkerGoTermAnnotationExtnGroup() {
        MarkerGoTermAnnotationExtnGroup group = (MarkerGoTermAnnotationExtnGroup) currentSession()
            .createQuery("from MarkerGoTermAnnotationExtnGroup").setMaxResults(1).uniqueResult();
        assertNotNull("MarkerGoTermAnnotationExtnGroup should load from DB", group);
        assertNotNull(group.getMgtaegMarkerGoEvidence());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadFishWithMultipleStrs() {
        List<Fish> fishList = currentSession()
            .createQuery("from Fish f where size(f.strList) > 1")
            .setMaxResults(1)
            .list();
        assertFalse("Should find a Fish with multiple STRs", fishList.isEmpty());
        Fish fish = fishList.get(0);
        assertNotNull(fish.getZdbID());
        assertTrue("Fish should have more than one STR", fish.getStrList().size() > 1);
    }

    @Test
    public void loadMarkerGoTermAnnotationExtn() {
        MarkerGoTermAnnotationExtn extn = (MarkerGoTermAnnotationExtn) currentSession()
            .createQuery("from MarkerGoTermAnnotationExtn").setMaxResults(1).uniqueResult();
        assertNotNull("MarkerGoTermAnnotationExtn should load from DB", extn);
        assertNotNull(extn.getRelationshipTerm());
    }
}
