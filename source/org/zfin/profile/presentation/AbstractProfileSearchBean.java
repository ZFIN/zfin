package org.zfin.profile.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.util.URLCreator;

/**
 */
public abstract class AbstractProfileSearchBean extends PaginationBean {

    public static final String PRINT_VIEW = "printable";
    public static final String HTML_VIEW = "html";

    protected String name;
    protected String address;
    protected String contains;
    protected String containsType;
    protected String view = HTML_VIEW; // print vs html

    protected URLCreator creator ;

    @Override
    public abstract String getActionUrl() ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trim(name);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = StringUtils.trim(address);
    }

    public String getContains() {
        return contains;
    }

    public void setContains(String contains) {
        this.contains = StringUtils.trim(contains);
    }

    public String getContainsType() {
        return containsType;
    }

    public void setContainsType(String containsType) {
        this.containsType = containsType;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public boolean isEmpty() {
        return (StringUtils.isEmpty(name)
                && StringUtils.isEmpty(address)
                && StringUtils.isEmpty(contains)
        );

    }
}
