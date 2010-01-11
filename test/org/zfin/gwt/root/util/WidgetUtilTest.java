package org.zfin.gwt.root.util;

import org.junit.Test;
import org.zfin.gwt.root.util.WidgetUtil;

import static junit.framework.Assert.assertEquals;
//import com.google.gwt.junit.client.GWTTestCase;

public class WidgetUtilTest {//extends GWTTestCase {

    @Test
    public void addCssClass() {
        String currentCss = "";
        String newClass = "bold";
        String cssClassString = WidgetUtil.addOrRemoveCssClass(newClass, true, currentCss);
        assertEquals(newClass, cssClassString);
    }

    @Test
    public void addCssClassToExisting() {
        String currentCss = "stripes";
        String newClass = "bold";
        String cssClassString = WidgetUtil.addOrRemoveCssClass(newClass, true, currentCss);
        assertEquals("stripes bold", cssClassString);
    }

    @Test
    public void removeCssClassWithExisting() {
        String currentCss = "stripes";
        String newClass = "bold";
        String cssClassString = WidgetUtil.addOrRemoveCssClass(newClass, false, currentCss);
        assertEquals("stripes", cssClassString);
    }

    @Test
    public void removeCssClassWithExistingSame() {
        String currentCss = "bold";
        String newClass = "bold";
        String cssClassString = WidgetUtil.addOrRemoveCssClass(newClass, false, currentCss);
        assertEquals("", cssClassString);
    }


    public String getModuleName() {
        return "org.zfin.curation.Curation";
    }
}