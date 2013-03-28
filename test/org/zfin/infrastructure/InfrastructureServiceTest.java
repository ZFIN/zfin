package org.zfin.infrastructure;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class InfrastructureServiceTest extends AbstractDatabaseTest {


    @Test
    public void getPublicationById() {
        String pubID = "ZDB-PUB-050328-4";
        Object o = InfrastructureService.getEntityById(pubID);
        assertNotNull(o);
        assertTrue("Instance not a Publication", o instanceof Publication);
        Publication pub = (Publication) o;
        assertEquals("Publication Title does not match", "Cilia-driven fluid flow in the zebrafish pronephros, brain and Kupffer's vesicle is required for normal organogenesis", pub.getTitle());
    }

    @Test
    public void getPersonById() {
        String pubID = "ZDB-PERS-000208-2";
        Object o = InfrastructureService.getEntityById(pubID);
        assertNotNull(o);
        assertTrue("Instance not a Person", o instanceof Person);
        Person person = (Person) o;
        assertEquals("Person name does not match", "Herzog, Wiebke", person.getFullName());
    }
}
