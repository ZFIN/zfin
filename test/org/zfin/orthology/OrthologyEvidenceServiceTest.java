package org.zfin.orthology;

import org.junit.Test;
import org.zfin.publication.Publication;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.hibernate.validator.util.Contracts.assertNotNull;

public class OrthologyEvidenceServiceTest {

    @Test
    public void testSorting() {
        Orthology orthology = new Orthology();
        orthology.setEvidenceCode(getEvidenceCode("AA", 1));
        orthology.setPublication(getPublication("Woods"));

        Orthology orthology1 = new Orthology();
        orthology1.setEvidenceCode(getEvidenceCode("CL", 3));
        orthology1.setPublication(getPublication("Zygote"));

        Orthology orthology11 = new Orthology();
        orthology11.setEvidenceCode(getEvidenceCode("CL", 3));
        orthology11.setPublication(getPublication("Avaron"));

        Orthology orthology12 = new Orthology();
        orthology12.setEvidenceCode(getEvidenceCode("CL", 3));
        orthology12.setPublication(getPublication("Good"));

        Orthology orthology2 = new Orthology();
        orthology2.setEvidenceCode(getEvidenceCode("NT", 2));
        orthology2.setPublication(getPublication("Woods"));

        List<Orthology> list = new ArrayList<>(3);
        list.add(orthology1);
        list.add(orthology2);
        list.add(orthology11);
        list.add(orthology12);
        list.add(orthology);

        assertEquals("CL", list.get(0).getEvidenceCode().getCode());
        List<Orthology> sortedList = OrthologyEvidenceService.getEvidenceCenteredList(list);
        assertNotNull(sortedList);
        assertEquals("AA", sortedList.get(0).getEvidenceCode().getCode());
        assertEquals("NT", sortedList.get(1).getEvidenceCode().getCode());
        assertEquals("CL", sortedList.get(2).getEvidenceCode().getCode());
        assertEquals("Avaron", sortedList.get(2).getPublication().getShortAuthorList());
        assertEquals("Good", sortedList.get(3).getPublication().getShortAuthorList());
        assertEquals("Zygote", sortedList.get(4).getPublication().getShortAuthorList());

    }

    private Publication getPublication(String woods) {
        Publication publication = new Publication();
        publication.setShortAuthorList(woods);
        return publication;
    }

    private EvidenceCode getEvidenceCode(String value, int order) {
        EvidenceCode code = new EvidenceCode();
        code.setCode(value);
        code.setOrder(order);
        return code;
    }
}
