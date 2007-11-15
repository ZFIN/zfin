package org.zfin.framework.presentation;

import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.TilesUtilImpl;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class to display the current Tiles Configuration.
 */
public class TilesConfigurationController extends AbstractCommandController {

    public TilesConfigurationController() {
        setCommandClass(TilesConfigurationFormBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
                                  BindException errors) throws Exception {

        TilesConfigurationFormBean form = (TilesConfigurationFormBean) command;

        ServletContext servletContext = request.getSession().getServletContext();
        DefinitionsFactory factory = (DefinitionsFactory) servletContext.getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY);
        DefinitionsFactoryConfig config = factory.getConfig();
        form.setConfiguration(config);
        return new ModelAndView("tiles-configuration", "tilesForm", form);
    }

}
