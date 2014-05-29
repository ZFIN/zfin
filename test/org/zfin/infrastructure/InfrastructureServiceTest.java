package org.zfin.infrastructure;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

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

    @Test
    public void getZdbDateString() {
        String zdbDate = InfrastructureService.getZdbDate(null);
        assertNull(zdbDate);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.MARCH, 12);
        zdbDate = InfrastructureService.getZdbDate(calendar);
        assertNotNull(zdbDate);
        assertEquals("130312", zdbDate);
    }
}
