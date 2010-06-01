package org.zfin.framework;

import org.biojava.bio.Annotation;
import org.biojava.bio.symbol.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.fail;

/**
 */
public class SuffixTreeTest {

    @Test
    public void treeTest(){
        try {
            SimpleAlphabet simpleAlphabet = new SimpleAlphabet() ;
            Set<Symbol> symbols = new HashSet<Symbol>() ;
            for(char c = 48 ; c < 128 ; c++){
                symbols.add(AlphabetManager.createSymbol(String.valueOf(c), Annotation.EMPTY_ANNOTATION)) ;
//                System.out.println("adding: "+c) ;
            }
            symbols.add(AlphabetManager.createSymbol("d", Annotation.EMPTY_ANNOTATION)) ;
            FiniteAlphabet alphanumeric = new SimpleAlphabet(symbols,"AlphaNumeric") ;
            AlphabetManager.registerAlphabet(alphanumeric.getName(),alphanumeric);
            SimpleAlphabet alpha1 = (SimpleAlphabet) AlphabetManager.alphabetForName(alphanumeric.getName());

            System.out.println(simpleAlphabet.size());
//            for(Iterator iterator = simpleAlphabet.iterator() ; iterator.hasNext() ; ){
//                System.out.println(iterator.next().toString()) ;
//            }
            SuffixTree suffixTree = new SuffixTree(alpha1)  ;
            System.out.println(suffixTree.getRoot().isTerminal()) ; 
//            List<Symbol> list = new ArrayList<Symbol>() ;
//            list.add(AlphabetManager.createSymbol("d",Annotation.EMPTY_ANNOTATION)) ;
//            list.add(AlphabetManager.createSymbol("o",Annotation.EMPTY_ANNOTATION)) ;
//            list.add(AlphabetManager.createSymbol("g",Annotation.EMPTY_ANNOTATION)) ;
//            list.add(AlphabetManager.createSymbol("g",Annotation.EMPTY_ANNOTATION)) ;
//            list.add(AlphabetManager.createSymbol("y",Annotation.EMPTY_ANNOTATION)) ;
//            SymbolList symbolList = new SimpleSymbolList(alpha1,list) ;
//            suffixTree.addSymbols(symbolList,0);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString()) ;
        }

    }
}
