package org.zfin.curation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.zfin.curation.dto.ExpressionFigureStageDTO;
import org.zfin.curation.dto.StageRangeIntersection;
import org.zfin.curation.dto.WidgetUtil;
import org.zfin.curation.client.CurationEntryPoint;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.junit.client.GWTTestCase;

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