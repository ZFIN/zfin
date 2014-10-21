package org.zfin.util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * Test class for anatomy search results.
 */
public class UrlCreatorTest {

    private Logger logger = Logger.getLogger(UrlCreatorTest.class);

    @Test
    public void noParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/", fullUrl);
    }

    @Test
    public void oneParameter() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNamevaluePair(name, value);
        String fullUrl = url.getURL();
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
        String fullUrl = url.getURL();
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
        String fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/?name=value", fullUrl);
    }

    @Test
    public void removeFirstParamAndValue() {

        String url1 = "http://zfin.org/?name=value&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL();
        Assert.assertEquals(url1, fullUrl);

        url.removeNameValuePair("name");
        fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/?name2=value2", fullUrl);

    }

    @Test
    public void removeLastParamAndValue() {

        String url1 = "http://zfin.org/?name=value&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL();
        Assert.assertEquals(url1, fullUrl);

        url.removeNameValuePair("name2");
        fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/?name=value", fullUrl);

    }

    @Test
    public void removeMiddleParamAndValue() {

        String url1 = "http://zfin.org/aber_warum?name=value&name1=value1&name2=value2";
        URLCreator url = new URLCreator(url1);
        String fullUrl = url.getURL();
        Assert.assertEquals(url1, fullUrl);

        url.removeNameValuePair("name1");
        fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/aber_warum?name=value&name2=value2", fullUrl);

    }

    @Test
    public void removeUnencodedParamAndValue() {
        String url = "/prototype?q=&fq=category%3A%22Mutant+%2F+Tg%22";
        String urlAfter = "/prototype?q=";
        URLCreator urlCreator = new URLCreator(url);
        urlCreator.removeNameValuePair("fq","category:\"Mutant / Tg\"");
        Assert.assertEquals(urlAfter,urlCreator.getURL());
    }



    @Test
    public void roundTripEncodingTest() {
        String initialUrl = "http://zfin.org/search?q=foo&fq=category:gene&fq=anatomy:brain";
        URLCreator urlCreator = new URLCreator(initialUrl);
        String outputUrl = urlCreator.getURL();
        try {
            outputUrl = URLDecoder.decode(outputUrl, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //this is maybe asking for too much, there may be no guarantee that order will be preserved
        Assert.assertEquals(initialUrl, outputUrl);
    }

    @Test
    public void removeSingleOfMultipleValues()  {

        String initialUrl = "http://zfin.org/search?q=foo&name=value1&name=value2";
        String urlAfterRemoval = "http://zfin.org/search?q=foo&name=value2";

        URLCreator urlCreator = new URLCreator(initialUrl);
        String outputUrl = urlCreator.getURL();

        Assert.assertEquals(initialUrl, outputUrl);

        urlCreator.removeNameValuePair("name", "value1");
        Assert.assertEquals(urlCreator.getURL(), urlAfterRemoval);

    }


    @Test
    public void getSingleNameValuePair() {
        String url = "http://zfin.org/search?q=foo&name=value";
        URLCreator urlCreator = new URLCreator(url);

        Assert.assertEquals("value", urlCreator.getFirstValue("name"));

    }

    @Test
    public void getMultipleNameValuePairs() {
        String url = "http://zfin.org/search?name=value3&name=value1&name=value2";
        URLCreator urlCreator = new URLCreator(url);

        List<NameValuePair> nameValuePairs = urlCreator.getNameValuePairs("name");
        Assert.assertTrue(nameValuePairs.contains(new BasicNameValuePair("name","value1")));
        Assert.assertTrue(nameValuePairs.contains(new BasicNameValuePair("name","value2")));
        Assert.assertTrue(nameValuePairs.contains(new BasicNameValuePair("name","value3")));

    }





}
