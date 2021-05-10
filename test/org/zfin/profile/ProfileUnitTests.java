package org.zfin.profile;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 */
public class ProfileUnitTests {

    @Test
    public void addressIsEmpty(){
        Address address = new Address();
        assertTrue(address.isEmptyAddress());
        assertTrue(address.isInvalidAddress());
        address.setCity("Eugene");
        assertFalse(address.isEmptyAddress());
        assertTrue(address.isInvalidAddress());
    }

    @Test
    public void generateNameVariations(){
        Person p = new Person();

        p.setFirstName("Lenny");
        p.setLastName("Bruce");
        p.generateNameVariations();
        assertEquals("Bruce-L.",p.getShortName());
        assertEquals("Bruce, Lenny",p.getFullName());


        p.setFirstName("Lenny Alvin");
        p.generateNameVariations();
        assertEquals("Bruce-L.A.",p.getShortName());
        assertEquals("Bruce, Lenny Alvin",p.getFullName());


        p.setFirstName("L. A.");
        p.generateNameVariations();
        assertEquals("Bruce-L.A.",p.getShortName());
        assertEquals("Bruce, L. A.",p.getFullName());
    }
}
