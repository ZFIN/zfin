package org.zfin.people;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class OrganizationUrlTest implements Serializable {

    private Lab labOne;
    private Lab labOneOne;
    private Lab labTwo;

    @Before
    public void setup(){
        labOne = new Lab();
        labOne.setAddress("address 1");
        labOne.setEmail("email 1");
        labOne.setFax("fax 1");
        labOne.setName("name 1");
        //labOne.setOwner("owner");

        labOneOne = new Lab();
        labOneOne.setAddress("address 1");
        labOneOne.setEmail("email 1");
        labOneOne.setFax("fax 1");
        labOneOne.setName("name 1");
        //labOneOne.setOwner("owner");

        labTwo = new Lab();
        labTwo.setAddress("address 2");
        labTwo.setEmail("email 2");
        labTwo.setFax("fax 2");
        labTwo.setName("name 2");
        //labTwo.setOwner("owner");

    }

    @Test
    public void hyperlinkgOnlyEquality(){
        SourceUrl hyperlinkOnlyOne;
        SourceUrl hyperlinkOnlyTwo;
        hyperlinkOnlyOne = new SourceUrl();
        hyperlinkOnlyOne.setHyperlinkName("hyperlinke");

        hyperlinkOnlyTwo = new SourceUrl();
        hyperlinkOnlyTwo.setHyperlinkName("hyperlinke");

        assertEquals("one = one", hyperlinkOnlyOne, hyperlinkOnlyOne);
        assertEquals("two = two", hyperlinkOnlyTwo, hyperlinkOnlyTwo);
        assertEquals("one = two", hyperlinkOnlyOne, hyperlinkOnlyTwo);
    }

    @Test
    public void prefixOnlyEquality(){
        SourceUrl urlPrefixOne;
        SourceUrl urlPrefixTwo;
        urlPrefixOne = new SourceUrl();
        urlPrefixOne.setUrlPrefix("urlPrefix");

        urlPrefixTwo = new SourceUrl();
        urlPrefixTwo.setUrlPrefix("urlPrefix");

        assertEquals("one = one", urlPrefixOne, urlPrefixOne);
        assertEquals("two = two", urlPrefixTwo, urlPrefixTwo);
        assertEquals("one = two", urlPrefixOne, urlPrefixTwo);
    }

    @Test
    public void businessPurposeOnlyEquality(){
        SourceUrl businessPurposeOne;
        SourceUrl businessPurposetwo;
        businessPurposeOne = new SourceUrl();
        businessPurposeOne.setBusinessPurpose("business Purpose");

        businessPurposetwo = new SourceUrl();
        businessPurposetwo.setBusinessPurpose("business Purpose");

        assertEquals("one = one", businessPurposeOne, businessPurposeOne);
        assertEquals("two = two", businessPurposetwo, businessPurposetwo);
        assertEquals("one = two", businessPurposeOne, businessPurposetwo);
    }

    @Test
    public void labOnlyEquality(){
        SourceUrl businessPurposeOne;
        SourceUrl businessPurposeTwo;
        SourceUrl businessPurposeThree;
        businessPurposeOne = new SourceUrl();
        businessPurposeOne.setOrganization(labOne);

        businessPurposeTwo = new SourceUrl();
        businessPurposeTwo.setOrganization(labOneOne);

        businessPurposeThree = new SourceUrl();
        businessPurposeThree.setOrganization(labTwo);

        assertEquals("one = one", businessPurposeOne, businessPurposeOne);
        assertEquals("two = two", businessPurposeTwo, businessPurposeTwo);
        assertEquals("one = two", businessPurposeOne, businessPurposeTwo);
        assertNotSame("one = two", businessPurposeOne, businessPurposeThree);
    }

    @Test
    public void OrgEquality(){
        SourceUrl businessPurposeOne;
        SourceUrl businessPurposeTwo;
        SourceUrl businessPurposeThree;
        businessPurposeOne = new SourceUrl();
        businessPurposeOne.setOrganization(labOne);
        businessPurposeOne.setHyperlinkName("hyperlink");
        businessPurposeOne.setBusinessPurpose("business Purpose");
        businessPurposeOne.setUrlPrefix("url prefox");

        businessPurposeTwo = new SourceUrl();
        businessPurposeTwo.setOrganization(labOneOne);
        businessPurposeTwo.setHyperlinkName("hyperlink");
        businessPurposeTwo.setBusinessPurpose("business Purpose");
        businessPurposeTwo.setUrlPrefix("url prefox");

        businessPurposeThree = new SourceUrl();
        businessPurposeThree.setOrganization(labTwo);

        assertEquals("one = one", businessPurposeOne, businessPurposeOne);
        assertEquals("two = two", businessPurposeTwo, businessPurposeTwo);
        assertEquals("one = two", businessPurposeOne, businessPurposeTwo);
        assertNotSame("one = two", businessPurposeOne, businessPurposeThree);
    }

}