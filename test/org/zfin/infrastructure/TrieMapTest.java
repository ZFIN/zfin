package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Need to make sure that it is able to register with all of the proper interface
 * methods for map.
 * WORKS
 * - clear
 * - put
 * - isEmpty
 * - size
 * - get
 * - remove (string key)
 * - putAll
 * - entrySet (hmm . . . . , and important one)
 * - values
 * - containsKey
 * - containsValue
 * - keySet
 * - remove (Object, from AbstractMap)
 * - hashCode (hmmm / / / )
 * - toString()
 * - equals (hmm . . . )
 *
 */
public class TrieMapTest {

    private final static Logger logger = Logger.getLogger(TrieMapTest.class) ;

    private final static String key1 = "key1" ;
    private final static Object a = new String("key1value") ;
    private final static Object b = new String("key2value") ;
    private final static String key2 = "key2" ;


    @Test
    public void putAll(){
        TrieMap trieMap = new TrieMap() ;
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        trieMap.putAll(getMap()) ;
        assertFalse(trieMap.isEmpty());
        assertEquals(2,trieMap.size());
    }

    @Test
    public void testIsEmpty(){
        TrieMap trieMap = new TrieMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;
        trieMap.put("key",new Object()) ;
        assertFalse(trieMap.isEmpty())  ;
        assertEquals(1,trieMap.size())  ;
    }

    @Test
    public void getPut(){
        TrieMap trieMap = new TrieMap()  ;

        //empt set
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        // add 1
        trieMap.put(key1,a) ;
        assertFalse(trieMap.isEmpty());
        assertEquals(1,trieMap.size())  ;
        Object testA = trieMap.get(key1) ;
        assertNotNull(testA);
        assertEquals(a,testA.toString());
        assertNull(trieMap.get(key2)) ;

        // add 2
        trieMap.put(key2,b) ;
        assertEquals(2,trieMap.size())  ;
        testA = trieMap.get(key1) ;
        assertNotNull(testA);
        assertEquals(a,testA.toString());
        Object testB = trieMap.get(key2) ;
        assertNotNull(testB);
        assertEquals(b,testB.toString());


        // remove a
        trieMap.remove(key1) ;

        // test for b and no A
        assertEquals(1,trieMap.size())  ;
        testA = trieMap.get(key1) ;
        assertNull(testA);
        testB = trieMap.get(key2) ;
        assertNotNull(testB);
        assertEquals(b,testB.toString());
    }

    @Test
    public void values(){
        TrieMap trieMap1 = new TrieMap() ;
        trieMap1.putAll(getMap()) ;
        Collection collection = trieMap1.values() ;
        assertNotNull(collection);
        assertEquals(2,collection.size()) ;
        assertTrue(collection.iterator().hasNext());
    }

    @Test
    public void hashCodeEquals(){
        TrieMap trieMap1 = new TrieMap() ;
        trieMap1.putAll(getMap()) ;
        TrieMap trieMap2 = new TrieMap() ;
        trieMap2.putAll(getMap()) ;

        assertEquals(2,trieMap1.size());
        assertEquals(2,trieMap2.size());
//        assertEquals(trieMap1,trieMap2);
        logger.debug(trieMap1.hashCode()+"-"+trieMap2.hashCode());
    }

    @Test
    public void containsKey(){
        TrieMap trieMap = new TrieMap()  ;

        Set keySet = trieMap.keySet()  ;
        assertNotNull(keySet) ;
        assertFalse(keySet.iterator().hasNext()) ;

        //empt set
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        assertFalse(trieMap.containsKey(key1));
        assertFalse(trieMap.containsKey(key2));
        assertFalse(trieMap.containsValue(a));
        assertFalse(trieMap.containsValue(b));

        // add 1
        trieMap.put(key1,a) ;
        assertFalse(trieMap.isEmpty());
        assertEquals(1,trieMap.size())  ;
        assertTrue(trieMap.containsKey(key1));
        assertFalse(trieMap.containsKey(key2));
        assertTrue(trieMap.containsValue(a));
        assertFalse(trieMap.containsValue(b));

        // add 2
        trieMap.put(key2,b) ;
        assertEquals(2,trieMap.size())  ;
        assertTrue(trieMap.containsKey(key1));
        assertTrue(trieMap.containsKey(key2));
        assertTrue(trieMap.containsValue(a));
        assertTrue(trieMap.containsValue(b));



        // remove a
        trieMap.remove(key1) ;
    }


    @Test
    public void entrySet(){
        TrieMap trieMap = new TrieMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        Set entrySet = trieMap.entrySet() ;
        assertTrue(entrySet.isEmpty());
        trieMap.put("key",new Object()) ;
        entrySet = trieMap.entrySet() ;
        assertFalse(entrySet.isEmpty());
    }

    @Test
    public void testClear(){
        TrieMap trieMap = new TrieMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        trieMap.put("key",new Object()) ;
        assertFalse(trieMap.isEmpty())  ;
        trieMap.clear();
        assertTrue(trieMap.isEmpty())  ;
    }

    private Map getMap(){
        HashMap entryMap = new HashMap(2) ;
        entryMap.put(key1,a) ;
        entryMap.put(key2,b) ;
        return entryMap ;
    }

    @Test
    public void multiTest(){
        TrieMap trieMap = new TrieMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        trieMap.put("key","value1") ;
        trieMap.put("key","value2") ;
        String[] results = trieMap.suggest("key") ;
        assertFalse(trieMap.isEmpty());  
        assertEquals(1,results.length);
    }

}
