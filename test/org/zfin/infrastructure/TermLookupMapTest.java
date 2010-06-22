package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
public class TermLookupMapTest {

    private final static Logger logger = Logger.getLogger(TermLookupMapTest.class) ;

    private final static String key1 = "key1" ;
    private final static Object a = new String("key1value") ;
    private final static Object b = new String("key2value") ;
    private final static Object c = new String("key3value") ;
    private final static String key2 = "key2" ;


    @Test
    public void addItemAll(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap() ;
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        trieMap.putAll(createMap());
        assertFalse(trieMap.isEmpty());
        assertEquals(2,trieMap.size());
        assertEquals(2,trieMap.values().size());
        assertEquals(3,trieMap.getAllValues().size());
    }

    @Test
    public void testIsEmpty(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;
        trieMap.put("key",new Object()) ;
        assertFalse(trieMap.isEmpty())  ;
        assertEquals(1,trieMap.size())  ;
    }

    @Test
    public void getPut(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap()  ;

        //empt set
        assertTrue(trieMap.isEmpty())  ;
        assertEquals(0,trieMap.size())  ;

        // add 1
        trieMap.put(key1,a) ;
        assertFalse(trieMap.isEmpty());
        assertEquals(1,trieMap.size())  ;
        Collection testA = (Collection) trieMap.get(key1) ;
        assertNotNull(testA);
        assertEquals(a,testA.iterator().next());
        assertNull(trieMap.get(key2)) ;
        assertEquals(1,testA.size()) ;

        // add 2
        trieMap.put(key1,b) ;
        assertEquals(1,trieMap.size())  ;
        testA = (Collection) trieMap.get(key1) ;
        assertNotNull(testA);
        assertEquals(2,testA.size()) ;
        Iterator iterator = testA.iterator() ;
        assertEquals(a,iterator.next());
        assertEquals(b,iterator.next());
        Collection testB = (Collection) trieMap.get(key2) ;
        assertNull(testB);


        // remove a
        trieMap.remove(key1) ;

        // test for b and no A
        assertEquals(0,trieMap.size())  ;
        testA = (Collection) trieMap.get(key1) ;
        assertNull(testA);
        testB = (Collection) trieMap.get(key2) ;
        assertNull(testB);
//        assertEquals(b,testB.toString());
    }

    @Test
    public void values(){
        PatriciaTrieMultiMap trieMap1 = new PatriciaTrieMultiMap() ;
        trieMap1.putAll(createMap());
        Collection collection = trieMap1.values() ;
        assertNotNull(collection);
        assertEquals(2,collection.size()) ;
        assertTrue(collection.iterator().hasNext());
        Set allValues = trieMap1.getAllValues() ;
        assertNotNull(allValues);
        assertEquals(3,allValues.size()) ;
        assertTrue(allValues.iterator().hasNext());
    }

    @Test
    public void hashCodeEquals(){
        PatriciaTrieMultiMap trieMap1 = new PatriciaTrieMultiMap() ;
        trieMap1.putAll(createMap());
        PatriciaTrieMultiMap trieMap2 = new PatriciaTrieMultiMap() ;
        trieMap2.putAll(createMap());

        assertEquals(2,trieMap1.size());
        assertEquals(2,trieMap2.size());
        logger.debug(trieMap1.hashCode()+"-"+trieMap2.hashCode());
    }

    @Test
    public void containsKey(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap()  ;

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
        assertTrue(trieMap.containsValueInAllValues(a));
        assertFalse(trieMap.containsValueInAllValues(b));

        // add 2
        trieMap.put(key1,b) ;
        assertEquals(1,trieMap.size())  ;
        assertTrue(trieMap.containsKey(key1));
        assertFalse(trieMap.containsKey(key2));
        assertTrue(trieMap.containsValueInAllValues(a));
        assertTrue(trieMap.containsValueInAllValues(b));



        // remove a
        trieMap.remove(key1) ;
    }


    @Test
    public void entrySet(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        Set entrySet = trieMap.entrySet() ;
        assertTrue(entrySet.isEmpty());
        trieMap.put("key",new Object()) ;
        entrySet = trieMap.entrySet() ;
        assertFalse(entrySet.isEmpty());
    }

    @Test
    public void testClear(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        trieMap.put("key",new Object()) ;
        assertFalse(trieMap.isEmpty())  ;
        trieMap.clear();
        assertTrue(trieMap.isEmpty())  ;
    }

    @Test
    public void multiTest(){
        PatriciaTrieMultiMap trieMap = new PatriciaTrieMultiMap()  ;
        assertTrue(trieMap.isEmpty())  ;
        trieMap.put("key","value1") ;
        trieMap.put("key","value2") ;
        Set<String> results = trieMap.getPrefixedBy("key").keySet() ;
        assertEquals(1,results.size());
        assertEquals(2, ((Set) trieMap.get("key")).size()) ; 
    }


    private Map createMap(){
        PatriciaTrieMultiMap entryMap = new PatriciaTrieMultiMap() ;
        entryMap.put(key1,a) ;
        entryMap.put(key1,b) ;
        entryMap.put(key2,c) ;
        return entryMap ;
    }

}