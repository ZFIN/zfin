package org.zfin.sequence;

import org.zfin.sequence.blast.Database;

/**
 * This class refers to sequences in the blast database, which is references through xdget and xdformat.
 * 
 */
public class Sequence implements Comparable {
    private Defline defLine ;
    private String data ;
    private DBLink dbLink;

    private final int FORMATTED_SEQUENCE_LENGTH = 40 ;

    public String getFormattedData(){
        String returnString = getDefLine() + "\n" ;
        returnString += getFormattedSequence() + "\n" ;
        return returnString ;
    }


    public String getFormattedSequence(){
        String returnString = "";
        int dataLength = data.length() ;
        for(int i = 0 ; i < dataLength ; i+= FORMATTED_SEQUENCE_LENGTH){
            if(i+ FORMATTED_SEQUENCE_LENGTH >= dataLength){
                returnString += data.substring(i) ;
            }
            else{
                returnString += data.substring(i,i+ FORMATTED_SEQUENCE_LENGTH) ;
            }
            returnString += "\n" ;
        }

        return returnString ;
    }

    public Defline getDefLine() {
        return defLine;
    }

    public void setDefLine(Defline defLine) {
        this.defLine = defLine;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public DBLink getDbLink() {
        return dbLink;
    }

    public void setDbLink(DBLink dbLink) {
        this.dbLink = dbLink;
    }

    public String toString(){
        String returnString = "" ;
        returnString += "SEQUENCE: \n"  ;
        returnString += "defLine: " + defLine + "\n"  ;
        returnString += "data: " + data + "\n"  ;
        if(dbLink ==null){
            returnString += "dbLink: " + null + "\n"  ;
        }
        else{
            returnString += "dbLink: " + dbLink.getAccessionNumber()+ "\n"  ;
            returnString += "               " + dbLink.getLength()+ "\n"  ;
            returnString += "               " + dbLink.getZdbID()+ "\n"  ;
        }
        return returnString ;
    }

    public int compareTo(Object o) {
        Sequence otherSequence = (Sequence)o;
        if (otherSequence == null || dbLink == null || otherSequence.getDbLink() == null)
            return +1;
        return getDbLink().getAccessionNumber().compareTo(otherSequence.getDbLink().getAccessionNumber());
    }


    public static enum Type {
        NUCLEOTIDE("n"),
        POLYPEPTIDE("p");

        private final String value ;



        private Type(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No blast database of type " + type + " found.");
        }

        public static Type getType(Database.Type type) {
            if(type== Database.Type.NUCLEOTIDE){
                return Type.NUCLEOTIDE ;
            }
            else
            if(type== Database.Type.PROTEIN){
                return Type.POLYPEPTIDE ;
            }
            else{
                throw new RuntimeException("invalid type for conversion: "+ type.toString()) ;
            }
        }
    }
}
