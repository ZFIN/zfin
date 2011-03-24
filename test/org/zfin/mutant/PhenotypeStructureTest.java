package org.zfin.mutant;

import org.junit.Test;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings({"FeatureEnvy", "SuppressionAnnotation"})
public class PhenotypeStructureTest {

    @Test
    public void caseInsensitiveSorting(){
        PhenotypeStructure structureThree = new PhenotypeStructure();
        GenericTerm supertermThree = new GenericTerm();
        String trunk = "trunk";
        supertermThree.setTermName(trunk);
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(supertermThree);
        structureThree.setEntity(entity);
        GenericTerm qualityThree = new GenericTerm();
        qualityThree.setTermName("quality");
        structureThree.setQualityTerm(qualityThree);
        structureThree.setTag(PhenotypeStatement.Tag.ABNORMAL);


        PhenotypeStructure structureTwo = new PhenotypeStructure();
        GenericTerm supertermTwo = new GenericTerm();
        String brain = "brain";
        supertermTwo.setTermName(brain);
        PostComposedEntity entityTwo = new PostComposedEntity();
        entityTwo.setSuperterm(supertermTwo);
        structureTwo.setEntity(entityTwo);
        GenericTerm qualityTwo = new GenericTerm();
        qualityTwo.setTermName("quality");
        structureTwo.setQualityTerm(qualityTwo);
        structureTwo.setTag(PhenotypeStatement.Tag.ABNORMAL);

        PhenotypeStructure structureOne = new PhenotypeStructure();
        GenericTerm superterm = new GenericTerm();
        String evl = "EVL";
        superterm.setTermName(evl);
        PostComposedEntity entityOne = new PostComposedEntity();
        entityOne.setSuperterm(superterm);
        structureOne.setEntity(entityOne);
        GenericTerm qualityOne = new GenericTerm();
        qualityOne.setTermName("quality");
        structureOne.setQualityTerm(qualityOne);
        structureOne.setTag(PhenotypeStatement.Tag.ABNORMAL);

        List<PhenotypeStructure> list = new ArrayList<PhenotypeStructure>(3);
        list.add(structureThree);
        list.add(structureOne);
        list.add(structureTwo);

        Collections.sort(list);

        assertTrue(list.size() == 3);
        assertEquals(brain, list.get(0).getEntity().getSuperterm().getTermName());
        assertEquals(evl, list.get(1).getEntity().getSuperterm().getTermName());
        assertEquals(trunk, list.get(2).getEntity().getSuperterm().getTermName());

    }
}