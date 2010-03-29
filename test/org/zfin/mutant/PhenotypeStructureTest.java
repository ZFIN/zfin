package org.zfin.mutant;

import org.junit.Test;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

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
        Term supertermThree = new GenericTerm();
        String trunk = "trunk";
        supertermThree.setTermName(trunk);
        structureThree.setSuperterm(supertermThree);

        PhenotypeStructure structureTwo = new PhenotypeStructure();
        Term supertermTwo = new GenericTerm();
        String brain = "brain";
        supertermTwo.setTermName(brain);
        structureTwo.setSuperterm(supertermTwo);

        PhenotypeStructure structureOne = new PhenotypeStructure();
        Term superterm = new GenericTerm();
        String evl = "EVL";
        superterm.setTermName(evl);
        structureOne.setSuperterm(superterm);

        List<PhenotypeStructure> list = new ArrayList<PhenotypeStructure>(3);
        list.add(structureThree);
        list.add(structureOne);
        list.add(structureTwo);

        Collections.sort(list);

        assertTrue(list.size() == 3);
        assertEquals(brain, list.get(0).getSuperterm().getTermName());
        assertEquals(evl, list.get(1).getSuperterm().getTermName());
        assertEquals(trunk, list.get(2).getSuperterm().getTermName());

    }
}