package org.zfin.gwt.marker.ui;

public class SequenceValidator {

    public final static int NOT_FOUND = -1 ;

    public static int validateNucleotideSequence(String sequences){
        char[] chars = sequences.toCharArray() ;
        for(int i = 0 ; i < chars.length ; i++){
            switch (chars[i]){
                case 'A': case 'C': case 'T': case 'G': case 'U': case 'M': case 'K':
                case 'R': case 'Y': case 'V': case 'B': case 'H': case 'D': case 'W':
                case 'S': case 'N': case '-':
                    break ;
                default:
                    return i ;
            }
        }
        return NOT_FOUND ;
    }

    public static int validatePolypeptideSequence(String sequences){
        char[] chars = sequences.toCharArray() ;
        for(int i = 0 ; i < chars.length ; i++){
            switch (chars[i]){
                case 'L': case 'S': case 'E': case 'A': case 'V': case 'G':
                case 'K': case 'T': case 'R': case 'P': case 'D': case 'I':
                case 'Q': case 'N': case 'F': case 'Y': case 'H': case 'M':
                case 'C': case 'W': case '*': case '-': case 'B': case 'Z':
                case 'J': case 'X':
                    break ;
                default:
                    return i ;
            }
        }
        return NOT_FOUND ;
    }
}
