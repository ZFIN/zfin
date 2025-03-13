package org.zfin.datatransfer.ctd;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.services.VocabularyService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.zfin.datatransfer.ctd.LoadCtdData.getMeshChebiMapping;

public class LoadCtdDataTest extends AbstractDatabaseTest {


    @Test
    public void testFetchingVocabularyFromDatabase() {
       Set<MeshCasChebiRelation> relations = getRelations();
       VocabularyService service = new VocabularyService();
       List<MeshChebiMapping> newRecords = relations.stream().map( r -> getMeshChebiMapping(r, service)).toList();
       assertTrue(newRecords.size() > 1);
    }

    private Set<MeshCasChebiRelation> getRelations() {
        Set set = new HashSet();
        set.add(new MeshCasChebiRelation("MESH:C028143", "homocitric acid", "CAS:3562-74-1", "CHEBI:17852", "homocitric acid"));
        set.add(new MeshCasChebiRelation("MESH:C028143", "homocitric acid", "CAS:3562-74-1", "CHEBI:17852", "homocitric acid"));
        set.add(new MeshCasChebiRelation("MESH:C015823", "17, 21-dihydroxypregnenolone", "CAS:1167-48-2", "CHEBI:27832", "17alpha,21-dihydroxypregnenolone"));
        set.add(new MeshCasChebiRelation("MESH:C093369", "furospongin 1", "CAS:35075-74-2", "CHEBI:67726", "Furospongin-1"));
        set.add(new MeshCasChebiRelation("MESH:D013805", "Theobromine", "CAS:83-67-0", "CHEBI:28946", "theobromine"));
        set.add(new MeshCasChebiRelation("MESH:C032318", "alpha-hydroxyalprazolam", "CAS:37115-43-8", "CHEBI:192657", "alpha-Hydroxyalprazolam"));
        return set;
    }

}
