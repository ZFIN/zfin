package org.zfin.gwt.root.server;

import java.util.regex.Pattern;

/**
 */
public class Highlighter {

    private String prefix = "<strong>" ;
    private String suffix= "</strong>" ;
    private String match ;
    private String[] matches ;
    private String matchPattern ;
    protected static final String illegalCharacters = "[\\\\,(,)]";
    private Pattern contiguousPattern;

    public Highlighter(){ }

    protected Highlighter(String match){
        setMatch(match);
    }

    public void setMatch(String match){
        this.match = match.replaceAll(illegalCharacters,"\\\\$0") ;
//        this.match = match ;
        if(this.match.trim().contains(" ")){
            matches = this.match.split("\\s+") ;
            matchPattern = "" ;
            for(int i = 0 ; i < matches.length ; i++){
                matchPattern += matches[i] ;
                if(i!=matches.length-1){
                    matchPattern += "|" ;
                }
            }
            contiguousPattern = Pattern.compile("("+matchPattern+")",Pattern.CASE_INSENSITIVE);
        }
        else{
            contiguousPattern = Pattern.compile("("+this.match+")",Pattern.CASE_INSENSITIVE);
        }
    }

    protected boolean contains(String s) {
        return contiguousPattern.matcher(s).find();
    }

    public String highlight(String s) {
        return  contiguousPattern.matcher(s).replaceAll(prefix+"$1"+suffix) ;
    }
}
