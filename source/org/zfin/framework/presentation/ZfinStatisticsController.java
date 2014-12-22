package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.util.FileUtil;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping(value = "/devtool")
public class ZfinStatisticsController {

    @RequestMapping("/zfin-statistics")
    protected String showGlobalSession(@ModelAttribute("formBean") ZfinStatisticsBean form) throws Exception {

        List<File> apgFiles = FileUtil.countApgFiles();
        form.setApgFiles(apgFiles);
        List<File> jspFiles = FileUtil.countJspFiles();
        form.setJspFiles(jspFiles);
        List<File> classesFiles = FileUtil.countClassFiles();
        form.setClassesFiles(classesFiles);
        return "zfin-statistics-view";
    }

}
