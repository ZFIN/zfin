package org.zfin.infrastructure;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 */
public class TrieTest {

    @Test
    public void dumpEdges(){
        PatriciaTrie<String,String> pTree = new PatriciaTrie<String,String>(new StringKeyAnalyzer()) ;
        pTree.put("brain","dog") ;
        pTree.put("brain","cat") ;

        Map.Entry<String,String> entry = pTree.select("brain") ;
        System.out.println(entry.getKey()) ;
        System.out.println(entry.getValue()) ; 


//        Trie trie = new Trie();
////        trie.add("brain") ;
////        trie.add("bran") ;
////        trie.add("nab") ;
//        trie.add("googol") ;
//        for(String edge: trie.dumpEdges()){
//            System.out.println(edge) ;
//        }
//        System.out.println(trie.size()) ;
//        assertEquals(trie.dumpEdges().length,trie.size()) ;
    }
}
