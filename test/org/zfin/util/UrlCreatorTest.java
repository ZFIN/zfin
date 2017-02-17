package org.zfin.util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
        url.addNameValuePair(name, value);
        String fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/?name=value", fullUrl);
    }

    @Test
    public void twoParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNameValuePair(name, value);
        String name2 = "name2";
        String value2 = "value2";
        url.addNameValuePair(name2, value2);
        String fullUrl = url.getURL();
        Assert.assertEquals("http://zfin.org/?name=value&name2=value2", fullUrl);
    }

    @Test
    public void twoIdenticalParameters() {

        String url1 = "http://zfin.org/";
        URLCreator url = new URLCreator(url1);
        String name = "name";
        String value = "value";
        url.addNameValuePair(name, value);
        url.addNameValuePair(name, value);
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
        String url = "/prototype?q=&fq=category%3A%22Mutation+%2F+Tg%22";
        String urlAfter = "/prototype?q=";
        URLCreator urlCreator = new URLCreator(url);
        urlCreator.removeNameValuePair("fq","category:\"Mutation / Tg\"");
        Assert.assertEquals(urlAfter,urlCreator.getURL());
    }



    @Test
    public void roundTripEncodingTest() throws UnsupportedEncodingException {
        String initialUrl = "http://zfin.org/search?q=foo&fq=category:gene&fq=anatomy:brain";
        URLCreator urlCreator = new URLCreator(initialUrl);
        String outputUrl = urlCreator.getURL();
        outputUrl = URLDecoder.decode(outputUrl, HTTP.UTF_8);
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

    @Test
    public void parseParameterWithPipeCharacter() {
        // the | character used in some urls can be problematic with some URL-handling classes
        String url = "http://zfin.org/do-something?a=1&b=2|3&c=4";
        URLCreator urlCreator = new URLCreator(url);
        assertThat(urlCreator.getFirstValue("a"), is("1"));
        assertThat(urlCreator.getFirstValue("b"), is("2|3"));
        assertThat(urlCreator.getFirstValue("c"), is("4"));
    }

    @Test
    public void addParameterWithPipeCharacter() {
        String url = "http://zfin.org/do-something?a=1";
        URLCreator urlCreator = new URLCreator(url);
        urlCreator.addNameValuePair("b", "lorem|ipsum");
        assertThat(urlCreator.getURL(), is(url + "&b=lorem%7Cipsum"));
    }

    @Test
    public void replaceParameterWasBlank() {
        String url = "http://zfin.org/do-something?a=&b=2";
        URLCreator urlCreator = new URLCreator(url);
        urlCreator.replaceNameValuePair("a", "3");

        String actualUrl = urlCreator.getURL();
        String expectedUrl = "http://zfin.org/do-something?b=2&a=3";
        assertThat(actualUrl, is(expectedUrl));
    }

    @Test
    public void replaceParameterWasPopulated() {
        String url = "http://zfin.org/do-something?a=1&b=2";
        URLCreator urlCreator = new URLCreator(url);
        urlCreator.replaceNameValuePair("a", "4");

        String actualUrl = urlCreator.getURL();
        String expectedUrl = "http://zfin.org/do-something?b=2&a=4";
        assertThat(actualUrl, is(expectedUrl));
    }

}
