package org.zfin.wiki.presentation;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public
class WikiPage {
    private long id;
    private String title;
    private String url;
    private Date created;
    private List<String> labels;
}