package org.zfin.framework.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.hibernate.stat.Statistics;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.util.FileUtil;
import org.geneontology.io.ExtensionFilenameFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Controller that obtains the meta data for the database.
 */
public class ZfinStatisticsController extends AbstractCommandController {

    public ZfinStatisticsController() {
        setCommandClass(ZfinStatisticsBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        ZfinStatisticsBean form = (ZfinStatisticsBean) command;
        List<File> apgFiles = FileUtil.countApgFiles();
        form.setApgFiles(apgFiles);
        List<File> jspFiles = FileUtil.countJspFiles();
        form.setJspFiles(jspFiles);
        List<File> classesFiles = FileUtil.countClassFiles();
        form.setClassesFiles(classesFiles);
        return new ModelAndView("zfin-statistics-view", LookupStrings.FORM_BEAN, form);
    }

}
