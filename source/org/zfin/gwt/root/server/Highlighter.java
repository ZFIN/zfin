package org.zfin.gwt.root.server;

import org.zfin.ontology.OntologyTokenizer;

import java.util.regex.Pattern;

/**
 */
public class Highlighter {

    private String prefix = "<strong>" ;
    private String suffix= "</strong>" ;
    private String match ;
    private String[] matches ;
    private String matchPattern = "";
    private Pattern contiguousPattern;

    private OntologyTokenizer tokenizer = new OntologyTokenizer();

    public Highlighter(){ }

    public Highlighter(String match){
        setMatch(match);
    }

    public void setMatch(String match){
        this.match = match ;
        if(match.trim().contains(" ")){
            matches = match.split("\\s+") ;
            for(int i = 0 ; i < matches.length ; i++){
                matchPattern += matches[i] ;
                if(i!=matches.length-1){
                    matchPattern += "|" ;
                }
            }
            contiguousPattern = Pattern.compile("("+matchPattern+")",Pattern.CASE_INSENSITIVE);
        }
        else{
            contiguousPattern = Pattern.compile("("+match+")",Pattern.CASE_INSENSITIVE);
        }
    }


    public String highlight(String s) {
        String returnPattern = contiguousPattern.matcher(s).replaceAll(prefix+"$1"+suffix) ;

        // if there are no contiguous hits, assume that its split up, so split up
        if(returnPattern.equalsIgnoreCase(s)){
            Pattern splitPattern;
            for(String word: s.split("\\s+")){
                splitPattern = Pattern.compile("("+match+")",Pattern.CASE_INSENSITIVE);
            }
        }

        return returnPattern ;
    }
}
