package org.zfin.search.presentation;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.util.CollectionUtils;
import org.zfin.expression.Figure;
import org.zfin.fish.FeatureGene;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.search.Category;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
* This should match the fl parameter set as default in solrconfig
* */
public class SearchResult implements ProvidesLink {

    //fields mapped to the Solr index
    @Field
    String id;
    @Field
    String name;
    @Field("full_name")
    String fullName;
    @Field("category")
    List<String> categories;
    @Field
    String type;



    @Field

    String url;
    @Field
    String screen;
    @Field("image")
    List<String> images;
    @Field("thumbnail")
    List<String> thumbnails;
    @Field("img_zdb_id")
    List<String> imageZdbIds;
    @Field
    String snapshot;
    @Field
    Float score;
    @Field
    Date date;
    @Field("attribution_count")
    Integer attributionCount;

    @Field("has_orthology")
    String hasOrthology;

    //these two are used to join to ExpressionDetailsGenerated without using the oft-regenerating primary key
    @Field("xpat_zdb_id")
    String xpatZdbId;
    @Field("fig_zdb_id")
    String figZdbId;
    @Field
    String pgcmid;


    @Field("[explain]")
    String explain;

    Object entity;
    Figure figure;

    //fields that need to be injected after
    String matchingText;

    /* this is only used when the search result is an autocomplete response */
    String autocompleteLabel;

    String displayedID;
    List<String> relatedLinks;
    Map attributes;

    //maybe this belongs directly on the fish?
    private List<FeatureGene> featureGenes;



    private Logger logger = Logger.getLogger(SearchResult.class);

    public String getLink() {
        String cssClass = "";
        if (id.startsWith("ZDB-GENE"))
            cssClass = " class = \"genedom\" ";
        if (id.contains("CONSTRCT"))
            cssClass = " class = \"genedom\" ";
        return "<a " + cssClass + " href=\"" + url + "\">" +  name +  "</a>";
    }

    public String getPgcmid() {
        return pgcmid;
    }

    public void setPgcmid(String pgcmid) {
        this.pgcmid = pgcmid;
    }
    public String getLinkWithAttribution() {
        return getLink();
    }

    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }

    public String getId() {
        return id;
    }

    public String getDivID() {
        return id.replace("-", "");
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getMatchingText() {
        return matchingText;
    }

    public void setMatchingText(String matchingText) {
        this.matchingText = matchingText;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public List<String> getImageZdbIds() {
        return imageZdbIds;
    }

    public void setImageZdbIds(List<String> imageZdbIds) {
        this.imageZdbIds = imageZdbIds;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getAutocompleteLabel() {
        return autocompleteLabel;
    }

    public void setAutocompleteLabel(String autocompleteLabel) {
        this.autocompleteLabel = autocompleteLabel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getAttributionCount() {
        return attributionCount;
    }

    public void setAttribution_count(Integer attributionCount) {
        this.attributionCount = attributionCount;
    }

    public void setAttributionCount(Integer attributionCount) {
        this.attributionCount = attributionCount;
    }

    public List<String> getRelatedLinks() {
        return relatedLinks;
    }

    public void setRelatedLinks(List<String> relatedLinks) {
        this.relatedLinks = relatedLinks;
    }

    public String getDisplayedID() {
        return displayedID;
    }

    public void setDisplayedID(String displayedID) {
        this.displayedID = displayedID;
    }

    public Map getAttributes() {
        return attributes;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getXpatZdbId() { return xpatZdbId; }

    public void setXpatZdbId(String xpatZdbId) { this.xpatZdbId = xpatZdbId; }

    public String getFigZdbId() { return figZdbId; }

    public void setFigZdbId(String figZdbId) { this.figZdbId = figZdbId; }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public void addAttribute(String label, String value) {
        if (attributes == null)
            attributes = new LinkedHashMap();
        attributes.put(label, value);
    }

    public List<FeatureGene> getFeatureGenes() {
        return featureGenes;
    }

    public void setFeatureGenes(List<FeatureGene> featureGenes) {
        this.featureGenes = featureGenes;
    }

    public String getHasOrthology() {
        return hasOrthology;
    }

    public void setHasOrthology(String hasOrthology) {
        this.hasOrthology = hasOrthology;
    }

    public Object getEntity() { return entity; }

    public void setEntity(Object entity) { this.entity = entity; }

    public Figure getFigure() { return figure; }

    public void setFigure(Figure figure) { this.figure = figure; }

    /* just grab an arbitrary first one for now.. */
    public String getThumbnail() {
        if (CollectionUtils.isEmpty(thumbnails))
            return null;
        return thumbnails.get(0);
    }

    public String getImage() {
        if (CollectionUtils.isEmpty(images))
            return null;
        return images.get(0);
    }

    public String getCategory() {
        if (categories == null || categories.size() == 0) { return null; }
        else if (categories.size() == 1) { return categories.get(0); }
        else if (categories.size() == 2 && categories.contains(Category.REPORTER_LINE.getName())) {
            return Category.REPORTER_LINE.getName();
        } else {
            return categories.get(0);
        }

    }


}
