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
        Assert.assertEquals("No request params added. URL encoded", "http://zfin.org/", fullUrl);
    }

    @Test
    public void oneParameter() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        String fullUrl = url.getFullURL();
        Assert.assertEquals("No request params added", "http://zfin.org/?name=value", fullUrl);
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
        Assert.assertEquals("No request params added", "http://zfin.org/?name=value&name2=value2", fullUrl);
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
        Assert.assertEquals("No request params added", "http://zfin.org/?name=value", fullUrl);
    }

}
