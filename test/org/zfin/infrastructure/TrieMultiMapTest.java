package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.ontology.Term;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Need to make sure that it is able to register with all of the proper interface
 * methods for map.
 * WORKS
 * - clear
 * - addItem
 * - isEmpty
 * - size
 * - get
 * - remove (string key)
 * - addItemAll
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
public class TrieMultiMapTest {

    private final static Logger logger = Logger.getLogger(TrieMultiMapTest.class) ;

    private final static String key1 = "key1" ;
    private final static Object a = new String("key1value") ;
    private final static Object b = new String("key2value") ;
    private final static Object c = new String("key3value") ;
    private final static String key2 = "key2" ;


    @Test
    public void addItemAll(){
        TrieMultiMap trieMap = new TrieMultiMap() ;
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        trieMap.putAll(createMap());
        assertFalse(trieMap.isEmpty());
        assertEquals(2,trieMap.size());
        assertEquals(3,trieMap.values().size());
    }

    @Test
    public void testIsEmpty(){
        TrieMultiMap trieMap = new TrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;
        trieMap.put("key",new Object()) ;
        assertFalse(trieMap.isEmpty())  ;
        assertEquals(1,trieMap.size())  ;
    }

    @Test
    public void getPut(){
        TrieMultiMap trieMap = new TrieMultiMap()  ;

        //empt set
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        // add 1
        trieMap.put(key1,a) ;
        assertFalse(trieMap.isEmpty());
        assertEquals(1,trieMap.size())  ;
        Collection testA = trieMap.get(key1) ;
        assertNotNull(testA);
        assertEquals(a,testA.iterator().next());
        assertNull(trieMap.get(key2)) ;
        assertEquals(1,testA.size()) ;

        // add 2
        trieMap.put(key1,b) ;
        assertEquals(1,trieMap.size())  ;
        testA = trieMap.get(key1) ;
        assertNotNull(testA);
        assertEquals(2,testA.size()) ;
        Iterator iterator = testA.iterator() ;
        assertEquals(a,iterator.next());
        assertEquals(b,iterator.next());
        Collection testB = trieMap.get(key2) ;
        assertNull(testB);


        // remove a
        trieMap.remove(key1) ;

        // test for b and no A
        assertEquals(0,trieMap.size())  ;
        testA = trieMap.get(key1) ;
        assertNull(testA);
        testB = trieMap.get(key2) ;
        assertNull(testB);
//        assertEquals(b,testB.toString());
    }

    @Test
    public void values(){
        TrieMultiMap trieMap1 = new TrieMultiMap() ;
        trieMap1.putAll(createMap());
        Collection collection = trieMap1.values() ;
        assertNotNull(collection);
        assertEquals(3,collection.size()) ;
        assertTrue(collection.iterator().hasNext());
    }

    @Test
    public void hashCodeEquals(){
        TrieMultiMap trieMap1 = new TrieMultiMap() ;
        trieMap1.putAll(createMap());
        TrieMultiMap trieMap2 = new TrieMultiMap() ;
        trieMap2.putAll(createMap());

        assertEquals(2,trieMap1.size());
        assertEquals(2,trieMap2.size());
        logger.debug(trieMap1.hashCode()+"-"+trieMap2.hashCode());
    }

    @Test
    public void containsKey(){
        TrieMultiMap trieMap = new TrieMultiMap()  ;

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
        trieMap.put(key1,b) ;
        assertEquals(1,trieMap.size())  ;
        assertTrue(trieMap.containsKey(key1));
        assertFalse(trieMap.containsKey(key2));
        assertTrue(trieMap.containsValue(a));
        assertTrue(trieMap.containsValue(b));



        // remove a
        trieMap.remove(key1) ;
    }


    @Test
    public void entrySet(){
        TrieMultiMap trieMap = new TrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        Set entrySet = trieMap.entrySet() ;
        assertTrue(entrySet.isEmpty());
        trieMap.put("key",new Object()) ;
        entrySet = trieMap.entrySet() ;
        assertFalse(entrySet.isEmpty());
    }

    @Test
    public void testClear(){
        TrieMultiMap trieMap = new TrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        trieMap.put("key",new Object()) ;
        assertFalse(trieMap.isEmpty())  ;
        trieMap.clear();
        assertTrue(trieMap.isEmpty())  ;
    }

    @Test
    public void multiTest(){
        TrieMultiMap trieMap = new TrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        trieMap.put("key","value1") ;
        trieMap.put("key","value2") ;
        String[] results = trieMap.suggest("key") ;
        assertEquals(1,results.length);
        assertEquals(2,trieMap.get("key").size()) ; 
    }


    private Map createMap(){
        TrieMultiMap entryMap = new TrieMultiMap() ;
        entryMap.put(key1,a) ;
        entryMap.put(key1,b) ;
        entryMap.put(key2,c) ;
        return entryMap ;
    }

}