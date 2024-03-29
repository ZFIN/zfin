package org.zfin.publication.presentation;

import lombok.Getter;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

@Setter
@Getter
public class PublicationSearchResultBean {

    @Field("id")
    private String zdbID;

    @Field("author_string")
    private String authors;

    @Field
    private String year;

    @Field("name")
    private String title;

    @Field
    private String journal;

    @Field
    private String pages;

    @Field
    private String volume;

    @Field("publication_status")
    private String status;

}
