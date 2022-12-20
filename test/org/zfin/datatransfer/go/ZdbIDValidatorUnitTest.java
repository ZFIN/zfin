package org.zfin.datatransfer.go;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.infrastructure.ReplacementZdbID;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class ZdbIDValidatorUnitTest extends AbstractDatabaseTest {

    @Test
    public void validateSimpleID() {

        //test that a known gene validates as existing
        boolean exists = ZdbIDValidator.validateExists("ZDB-GENE-110913-113");
        assertTrue(exists);

        //test that an invalid data type fails to validate
        exists = ZdbIDValidator.validateExists("ZDB-BOGUSTYPE-111111-11");
        assertFalse(exists);

        //test that a valid formatted id that does not exist returns false
        exists = ZdbIDValidator.validateExists("ZDB-GENE-650000-1");
        assertFalse(exists);

    }

    @Test
    public void validateListOfIDs() {

        List<String> ids = List.of(
                "ZDB-XPAT-040724-1381",
                "ZDB-TERMREL-190214-522",
                "ZDB-CUR-190826-593",
                "ZDB-FMREL-120806-15882",
                "ZDB-DALIAS-170508-7",
                "ZDB-FIG-100830-19",
                "ZDB-EXTNOTE-210715-2",
                "ZDB-DALIAS-160831-60188",
                "ZDB-DBLINK-220830-1784",
                "ZDB-DALIAS-091209-19932"
        );

        //test that a list of known entities validate as existing
        boolean exists = ZdbIDValidator.validateAllIDsExist(ids);
        assertTrue(exists);

        //test that a list with some valid and some invalid will fail:
        ids = List.of(
                "ZDB-GENE-650000-1",
                "ZDB-GENE-650000-2",
                "ZDB-GENE-650000-3",
                "ZDB-FMREL-120806-15882",
                "ZDB-DALIAS-170508-7",
                "ZDB-FIG-100830-19",
                "ZDB-EXTNOTE-210715-2",
                "ZDB-DALIAS-160831-60188",
                "ZDB-DBLINK-220830-1784",
                "ZDB-DALIAS-091209-19932"
        );
        exists = ZdbIDValidator.validateAllIDsExist(ids);
        assertFalse(exists);

    }

    @Test
    public void getInvalidIDsFromList() {

        //test that a list with some valid and some invalid will fail:
        List<String> ids = List.of(
                "ZDB-GENE-650000-1",
                "ZDB-GENE-650000-2",
                "ZDB-GENE-650000-3",
                "ZDB-FMREL-120806-15882",
                "ZDB-DALIAS-170508-7",
                "ZDB-FIG-100830-19",
                "ZDB-EXTNOTE-210715-2",
                "ZDB-DALIAS-160831-60188",
                "ZDB-DBLINK-220830-1784",
                "ZDB-DALIAS-091209-19932"
        );
        List<String> invalidIDs = ZdbIDValidator.getInvalidIDsFromSet(new HashSet<>(ids));
        assertEquals(3, invalidIDs.size());
        assertEquals("ZDB-GENE-650000-1", invalidIDs.get(0));
        assertEquals("ZDB-GENE-650000-2", invalidIDs.get(1));
        assertEquals("ZDB-GENE-650000-3", invalidIDs.get(2));

    }

    @Test
    public void getAllActiveDataResolveMerged() {
        //test that a list with some valid and some invalid will fail:
        List<String> ids = List.of(
                "ZDB-GENE-650000-1", //bogus ID
                "ZDB-GENE-030131-3937", //replaced to ZDB-GENE-000112-47 wu:fc59g04	22747
                "ZDB-GENE-000208-18" //just a regular gene (urod)
        );

        List<ReplacementZdbID> results = getInfrastructureRepository().getAllReplacementZdbIds(ids);
        assertEquals(1, results.size());
        assertEquals("ZDB-GENE-000112-47", results.get(0).getReplacementZdbID());

    }

    @Test
    public void getInvalidIDsFromSetResolvingMerged() {
        //test that a list with some valid and some invalid will fail:
        List<String> ids = List.of(
                "ZDB-GENE-650000-1", //bogus ID
                "ZDB-GENE-030131-3937", //replaced to ZDB-GENE-000112-47 wu:fc59g04	22747
                "ZDB-GENE-000208-18" //just a regular gene (urod)
        );

        List<String> invalidIDs = ZdbIDValidator.getInvalidIDsFromSetResolvingMerged(new HashSet<>(ids));
        assertEquals(1, invalidIDs.size());
        assertEquals("ZDB-GENE-650000-1", invalidIDs.get(0));

    }

}
