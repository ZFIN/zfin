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
        String fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/", fullUrl);
    }

    @Test
    public void oneParameter() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        String fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/?name=value", fullUrl);
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
        String fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/?name=value&name2=value2", fullUrl);
    }

    @Test
    public void twoIdenticalParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        url.addNamevaluePair(name, value);
        String fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/?name=value", fullUrl);
    }

    @Test
    public void removeFirstParamAndValue() {

        String url1 = "http://zfin.org/?name=value&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL(true);
        Assert.assertEquals(url1, fullUrl);

        url.removeNamevaluePair("name");
        fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/?name2=value2", fullUrl);

    }

    @Test
    public void removeLastParamAndValue() {

        String url1 = "http://zfin.org/?name=value&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL(true);
        Assert.assertEquals(url1, fullUrl);

        url.removeNamevaluePair("name2");
        fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/?name=value", fullUrl);

    }

    @Test
    public void removeMiddleParamAndValue() {

        String url1 = "http://zfin.org/aber_warum?name=value&name1=value1&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL(true);
        Assert.assertEquals(url1, fullUrl);

        url.removeNamevaluePair("name1");
        fullUrl = url.getURL(true);
        Assert.assertEquals("http://zfin.org/aber_warum?name=value&name2=value2", fullUrl);

    }

    @Test
    public void removeMiddleParamAndValueQueryStringOnly() {

        String url1 = "http://zfin.org/aber_warum?name=value&name1=value1&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL(false);
        Assert.assertEquals("name=value&name1=value1&name2=value2", fullUrl);

        url.removeNamevaluePair("name1");
        fullUrl = url.getURL(false);
        Assert.assertEquals("name=value&name2=value2", fullUrl);

    }

}
