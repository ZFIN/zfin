package org.zfin.properties.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class ZfinPropertiesController extends AbstractCommandController{

    private final Logger logger = Logger.getLogger(ZfinPropertiesController.class) ;

    private String view ;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        String paramterValue ;
        for(ZfinPropertiesEnum zfinPropertiesEnum : ZfinPropertiesEnum.values()){
            paramterValue =  request.getParameter(zfinPropertiesEnum.name()) ;
            if(paramterValue!=null && false==paramterValue.equals(zfinPropertiesEnum.value())){
                zfinPropertiesEnum.setValue(request.getParameter(zfinPropertiesEnum.name())) ;
            }
        }
        return new ModelAndView(view) ;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
