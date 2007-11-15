package org.zfin.framework.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.properties.ZfinProperties;
import org.zfin.util.FileWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Controller that obtains the meta data for the database.
 */
public class FileContentController extends AbstractCommandController {

    public FileContentController(){
        setCommandClass(FileContentBean.class);
    }
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        FileContentBean form = (FileContentBean) command;
        File file = form.getFile();
        FileWrapper wrapper = new FileWrapper(file, "ZFIN Properties");
        return new ModelAndView("file-content", "fileWrapper", wrapper);
    }
}
