package org.zfin.gwt.root.server;

import org.zfin.ontology.OntologyTokenizer;

import java.util.regex.Pattern;

/**
* Created by IntelliJ IDEA.
* User: nathandunn
* Date: Jun 22, 2010
* Time: 11:21:49 AM
* To change this template use File | Settings | File Templates.
*/
public class Highlighter {

    private String prefix = "<strong>" ;
    private String suffix= "</strong>" ;
    private String match ;
    private Pattern contiguousPattern;

    private OntologyTokenizer tokenizer = new OntologyTokenizer();

    public Highlighter(){ }

    public Highlighter(String match){
        setMatch(match);
    }

    public void setMatch(String match){
        this.match = match ;
        contiguousPattern = Pattern.compile("("+match+")",Pattern.CASE_INSENSITIVE);
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
