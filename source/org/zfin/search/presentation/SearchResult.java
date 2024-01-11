package org.zfin.search.presentation;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.util.CollectionUtils;
import org.zfin.expression.Figure;
import org.zfin.fish.FeatureGene;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.search.Category;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;

/*
 * This should match the fl parameter set as default in solrconfig
 * */
@Getter
@Setter
public class SearchResult implements ProvidesLink {
    private static final String SOLR_EMAIL_FIELD = "Email Address";
    //fields mapped to the Solr index
    private @Field String id;
    private @Field String name;
    private @Field("full_name") String fullName;
    private @Field("category") List<String> categories;
    private @Field String type;
    private @Field String url;
    private @Field String screen;
    private @Field("image") List<String> images;
    private @Field("thumbnail") List<String> thumbnails;
    private @Field("img_zdb_id") List<String> imageZdbIds;
    private @Field("profile_image") String profileImage;
    private @Field Float score;
    private @Field Date date;
    private @Field("attribution_count") Integer attributionCount;
    private @Field("has_orthology") String hasOrthology;

    //these two are used to join to ExpressionDetailsGenerated without using the oft-regenerating primary key
    private @Field("xpat_zdb_id") String xpatZdbId;
    private @Field("fig_zdb_id") String figZdbId;

    private @Field String pgcmid;
    private @Field("[explain]") String explain;
    private @Field("is_curatable") boolean curatable;

    private Object entity;
    private Figure figure;

    //fields that need to be injected after
    private String matchingText;
    private Map<String, List<String>> highlights;

    /* this is only used when the search result is an autocomplete response */
    private String autocompleteLabel;

    private String displayedID;
    private List<String> relatedLinks;
    private Map attributes;

    //maybe this belongs directly on the fish?
    private List<FeatureGene> featureGenes;

    private Logger logger = LogManager.getLogger(SearchResult.class);

    public String getLink() {
        String cssClass = "";
        if (id.startsWith("GENE") || id.contains("RNAG"))
            cssClass = " class = \"genedom\" ";
        if (id.contains("CONSTRCT"))
            cssClass = " class = \"genedom\" ";
        return "<a " + cssClass + " href=\"" + url + "\">" + name + "</a>";
    }

    public String getLinkWithAttribution() {
        return getLink();
    }

    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }

    public String getDivID() {
        return id.replace("-", "");
    }


    public void setAttribution_count(Integer attributionCount) {
        this.attributionCount = attributionCount;
    }

    public void addAttribute(String label, String value) {
        if (attributes == null)
            attributes = new LinkedHashMap();
        attributes.put(label, value);
    }

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
        if (categories == null || categories.size() == 0) {
            return null;
        } else if (categories.size() == 1) {
            return categories.get(0);
        } else if (categories.size() == 2 && categories.contains(Category.REPORTER_LINE.getName())) {
            return Category.REPORTER_LINE.getName();
        } else {
            return categories.get(0);
        }
    }

    /**
     * This is similar to setHighlights, except it first checks if there are any that should be hidden
     * due to privacy concernts.
     *
     * @param highlights the highlights that should be set on the search result
     */
    public void setHighlightsPreservingPrivacy(Map<String, List<String>> highlights) {

        // if this is a person, we need to filter out any highlights that are not public
        boolean isPerson = StringUtils.equals(getCategory(), Category.COMMUNITY.getName()) && StringUtils.equals(getType(), "Person");
        boolean isLab = StringUtils.equals(getCategory(), Category.COMMUNITY.getName()) && StringUtils.equals(getType(), "Lab");

        if (!isPerson && !isLab) {
            this.highlights = highlights;
            return;
        }

        Map<String, List<String>> filteredHighlights = filterHighlightsRemovePrivateInformation(highlights);
        this.highlights = filteredHighlights;
    }

    /**
     * Input is list of highlights from solr. Output is the same list with some potentially removed for privacy
     * @param highlights the highlights from solr
     * @return the filtered highlights
     */
    private Map<String, List<String>> filterHighlightsRemovePrivateInformation(Map<String, List<String>> highlights) {
        return highlights.entrySet().stream()
                .filter(entry -> shouldFieldBeDisplayed(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns true if a field should be hidden. Currently only email addresses are considered (for person or lab).
     * @param field the field name from solr highlights map
     * @return true if we should hide the highlight, otherwise false
     */
    private boolean shouldFieldBeHidden(String field) {
        if (SOLR_EMAIL_FIELD.equals(field) && getType().equals("Person")) {
            Person person = getProfileRepository().getPerson(this.getId());
            if (person.getEmailPrivacyPreference().shouldHide()) {
                return true;
            }
        }
        if (SOLR_EMAIL_FIELD.equals(field) && getType().equals("Lab")) {
            Lab lab = getProfileRepository().getLabById(this.getId());
            if (lab.getEmailPrivacyPreference().shouldHide()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inverse of shouldFieldBeHidden
     */
    private boolean shouldFieldBeDisplayed(String field) {
        return !shouldFieldBeHidden(field);
    }

}
