package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 */
public class OntologyTokenizationTest {

    private Logger logger = Logger.getLogger(OntologyTokenizationTest.class) ;
    private OntologyTokenizer tokenizer = new OntologyTokenizer() ;

    @Test
    public void matchterTest(){
        Pattern p = Pattern.compile("(\\p{Alnum}{3,})",Pattern.MULTILINE);
        String matcherString = "3,10-dog,food(kittens)stuff" ;
        Matcher m = p.matcher(matcherString) ;
        int count = 0 ;
        logger.debug(m.groupCount());
        while(m.find()){
            switch(count){
                case 0: assertEquals(m.group(),"dog");
                    break ;
                case 1: assertEquals(m.group(),"food");
                    break ;
                case 2: assertEquals(m.group(),"kittens");
                    break ;
                case 3: assertEquals(m.group(),"stuff");
                    break ;
                default:
                    fail("should not be here") ;
            }
            ++count ;
            logger.debug(m.group()+ " " + m.start()+"-"+m.end());
        }
        assertEquals(4,count);
    }

    @Test
    public void shortTokenizer(){
        String matchString = "sensitivity of a process" ;
        Set<String> strings ;
        strings = tokenizer.tokenizeWords(matchString)  ;
        assertEquals(2,strings.size()) ;
        strings = tokenizer.tokenize(matchString)  ;
        assertEquals(2,strings.size());
        assertTrue(strings.contains("sensitivity")) ;
        assertTrue(strings.contains("process")) ;
    }

    @Test
    public void testBadParens(){
        String matchString = "aerobic (for occurrence)" ;
        Set<String> strings ;
        strings = tokenizer.tokenizeWords(matchString)  ;
        assertEquals(3,strings.size()) ;
        strings = tokenizer.tokenize(matchString)  ;
        assertEquals(3,strings.size());
        assertTrue(strings.contains("aerobic")) ;
        assertTrue(strings.contains("for")) ;
        assertTrue(strings.contains("occurrence")) ;
    }

    @Test
    public void matchterTest2(){
        String matcherString = "3,10-dog,food(kittens)stuff" ;
        Set<String> strings = tokenizer.tokenize(matcherString) ;
        assertEquals(5,strings.size());
        assertTrue(strings.contains(matcherString)) ;
        assertTrue(strings.contains("dog")) ;
        assertTrue(strings.contains("food")) ;
        assertTrue(strings.contains("kittens")) ;
        assertTrue(strings.contains("stuff")) ;
    }


    @Test
    public void tokenizeSimple(){
        Set<String> strings = tokenizer.tokenize("pelvic fin") ;
        Iterator<String> iter = strings.iterator();
        assertEquals(2,strings.size()) ;
        assertEquals("pelvic",iter.next()) ;
        assertEquals("fin",iter.next()) ;
        strings = tokenizer.tokenize("pelvic fin duct") ;
        assertEquals(3,strings.size()) ;
        assertTrue(strings.contains("pelvic"));
        assertTrue(strings.contains("pelvic")) ;
        assertTrue(strings.contains("fin")) ;
        assertTrue(strings.contains("duct")) ;
        strings = tokenizer.tokenize("num3-pelvic fin-thingy duct") ;
        assertEquals(7,strings.size()) ;
        assertTrue(strings.contains("num3")) ;
        assertTrue(strings.contains("pelvic")) ;
        assertTrue(strings.contains("fin")) ;
        assertTrue(strings.contains("thingy")) ;
        assertTrue(strings.contains("duct")) ;
        assertTrue(strings.contains("num3-pelvic")) ;
        assertTrue(strings.contains("fin-thingy")) ;
        strings = tokenizer.tokenize("num,3-pelvic fin-thingy duct") ;
        assertEquals(7,strings.size()) ;
        assertTrue(strings.contains("num")) ;
        assertTrue(strings.contains("pelvic")) ;
        assertTrue(strings.contains("fin")) ;
        assertTrue(strings.contains("thingy")) ;
        assertTrue(strings.contains("duct")) ;
        assertTrue(strings.contains("num,3-pelvic")) ;
        assertTrue(strings.contains("fin-thingy")) ;
        strings = tokenizer.tokenize("dog,food") ;
        assertEquals(3,strings.size()) ;
        assertTrue(strings.contains("dog")) ;
        assertTrue(strings.contains("food")) ;
        assertTrue(strings.contains("dog,food")) ;
        strings = tokenizer.tokenize("3,10-dog,food") ;
        assertEquals(3,strings.size()) ;
        assertTrue(strings.contains("dog")) ;
        assertTrue(strings.contains("food")) ;
        assertTrue(strings.contains("3,10-dog,food")) ;
        strings = tokenizer.tokenize("3,10-dog,food(kittens)stuff") ;
        assertEquals(5,strings.size()) ;
        assertTrue(strings.contains("dog")) ;
        assertTrue(strings.contains("food")) ;
        assertTrue(strings.contains("kittens")) ;
        assertTrue(strings.contains("stuff")) ;
        assertTrue(strings.contains("3,10-dog,food(kittens)stuff")) ;
        strings = tokenizer.tokenize("5'3,10-dog's,food(kitten's)stuff") ;
        assertEquals(5,strings.size()) ;
        assertTrue(strings.contains("dog")) ;
        assertTrue(strings.contains("food")) ;
        assertTrue(strings.contains("kitten")) ;
        assertTrue(strings.contains("stuff")) ;
        assertTrue(strings.contains("5'3,10-dog's,food(kitten's)stuff")) ;
    }

    @Test
    public void regexpTest(){
        Pattern p = Pattern.compile("(term)",Pattern.CASE_INSENSITIVE);
        assertTrue(p.matcher("Term").matches()) ;
        assertTrue(p.matcher("term").matches()) ;
        assertFalse(p.matcher("termD").matches()) ;
        String output = p.matcher("Term").replaceAll("<b>$1</b>") ;
        assertEquals("<b>Term</b>",output);
    }
}
