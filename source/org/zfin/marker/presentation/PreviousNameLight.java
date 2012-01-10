package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.publication.presentation.PublicationPresentation;

import java.util.regex.Pattern;

/**
 */
public class PreviousNameLight implements ProvidesLink , Comparable<PreviousNameLight>{

    private String markerZdbID;
    private String aliasZdbID;
    private String alias;
    private String publicationZdbID;
    private String realName ;

    //Alias to pub is a one to many relationship, but when it's many, we don't need the whole list, just the count.
    private int publicationCount;
    
    /**
     * From https://wiki.zfin.org/display/doc/Placeholder+Gene+Designations
     */
    static Pattern uninformativePrefixPattern = Pattern.compile("^[ch,df,hm,ik,mg,mp,ns,sr,ig,wu,xx,id,sb,im,sc,cssl,gb]");
    static Pattern transgenicPrefixPattern = Pattern.compile("^[Tg(,Gt(,Et(,Pt(,]");


    public PreviousNameLight(String realName){
        this.realName = realName ;
    }

    public String getMarkerZdbID() {
        return markerZdbID;
    }

    public void setMarkerZdbID(String markerZdbID) {
        this.markerZdbID = markerZdbID;
    }

    public String getAliasZdbID() {
        return aliasZdbID;
    }

    public void setAliasZdbID(String aliasZdbID) {
        this.aliasZdbID = aliasZdbID;
    }



    @Override
    public String getLink() {
        return alias ;
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLinkWithAttribution();
    }

    @Override
    public String getLinkWithAttribution() {
        if (publicationZdbID!=null){
            return alias + MarkerPresentation.getAttributionLink(this);        }
        else{
            return getLink();
        }
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPublicationZdbID() {
        return publicationZdbID;
    }

    public void setPublicationZdbID(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public boolean isUninformative(){
        if(isUninformative(alias)){
            return true;
        }
        else{
            return getDistance(alias,realName) >= realName.length();
        }
    }

    public int getDistance(String s1, String s2){
        return StringUtils.getLevenshteinDistance(s1.replaceAll("\\p{Punct}+","").toLowerCase(),s2.replaceAll("\\p{Punct}+","").toLowerCase());
    }

    public int getPublicationCount() {
        return publicationCount;
    }

    public void setPublicationCount(int publicationCount) {
        this.publicationCount = publicationCount;
    }

    /**
     * From case 6959
     * @param previousNameLight
     * @return
     */
    @Override
    public int compareTo(PreviousNameLight previousNameLight) {

        if(isUninformative() && false==previousNameLight.isUninformative()){
            return 1;
        }
        if(previousNameLight.isUninformative() && false==isUninformative()){
            return -1;
        }


        return  alias.compareToIgnoreCase(previousNameLight.getAlias());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PreviousNameLight");
        sb.append("{alias='").append(alias).append('\'');
        sb.append(", publicationZdbID='").append(publicationZdbID).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static boolean isUninformative(String name){
        if(uninformativePrefixPattern.matcher(name).matches()) return true ;
        if(name.contains(":") && false==transgenicPrefixPattern.matcher(name).matches()) return true ;
        return false ;
    }
}
