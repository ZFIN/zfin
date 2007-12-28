package org.zfin.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for anatomy search results.
 */
public class UrlCreatorTest {

    @Test
    public void noParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getFullURL();
        Assert.assertEquals("No request params added. URL encoded", "http%3A%2F%2Fzfin.org%2F", fullUrl);
    }

    @Test
    public void oneParameter() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        String fullUrl = url.getFullURL();
        Assert.assertEquals("No request params added", "http%3A%2F%2Fzfin.org%2F%3Fname%3Dvalue", fullUrl);
    }

    @Test
    public void twoParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        String name2 = "name2";
        String value2 = "value2";
        url.addNamevaluePair(name2, value2);
        String fullUrl = url.getFullURL();
        Assert.assertEquals("No request params added", "http%3A%2F%2Fzfin.org%2F%3Fname%3Dvalue%26name2%3Dvalue2", fullUrl);
    }

    @Test
    public void twoIdenticalParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        url.addNamevaluePair(name, value);
        String fullUrl = url.getFullURL();
        Assert.assertEquals("No request params added", "http%3A%2F%2Fzfin.org%2F%3Fname%3Dvalue", fullUrl);
    }

}
