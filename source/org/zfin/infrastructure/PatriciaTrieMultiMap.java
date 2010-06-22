package org.zfin.infrastructure;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

import java.util.*;

/**
 */
public class PatriciaTrieMultiMap<V> extends PatriciaTrie<String, Set<V>> {

    private static final long serialVersionUID = 8774050446900697693L;

    public PatriciaTrieMultiMap() {
        super(new StringKeyAnalyzer());
    }

    public PatriciaTrieMultiMap(Map<String,Set<V>> map) {
        super(new StringKeyAnalyzer(),map);
    }

    /**
     * There is an issue reserializing, but rebuilding seems to work, so that is what we will do.
     */
    public void rebuild(){
        Map<String,Set<V>> map = new HashMap<String,Set<V>>(size()) ;
        for(String key: keySet()){
            map.put(key,get(key)) ;
        }
        clear();
        putAll(map);
        map = null ;
    }

    public V put(String s,V t){
        Set<V> values = get(s.toString()) ;
        if(values!=null){
            values.add(t) ;
        }
        else{
            Set set = new HashSet();
            set.add(t) ;
            put(s.toString(),set) ;
        }
        return t ;
    }

    /**
     *
     * @param prefix Query string.
     * @return The set of values that the prefix hit.
     */
    public Set<V> getSuggestedValues(String prefix){
        Set<V> terms  = new HashSet<V>() ;

        for(Set<V> termSet: getPrefixedBy(prefix).values() ){
            for(V term : termSet){
                terms.add(term) ;
            }
        }

        return terms ;
    }

    /**
     * Returns the flattened unique values in the map.
     * @return
     */
    public Set<V> getAllValues() {
        Set<V> valueSet = new HashSet<V>() ;
        for(Set<V> values : values()){
            valueSet.addAll(values) ;
        }
        return valueSet;
    }

    /**
     *
     * @param v
     * @return
     */
    public boolean containsValueInAllValues(V v) {
        for(V value : getAllValues()){
            if(v.equals(value)){
                return true ;
            }
        }
        return false ;
    }
}
