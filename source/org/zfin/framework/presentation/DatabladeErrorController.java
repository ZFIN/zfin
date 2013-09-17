package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.DatabladeErrorException;

@Controller
public class DatabladeErrorController {



    @RequestMapping(value = "/datablade-error")
    public void handleDatabadeError(@RequestParam String url,
                                      @RequestParam String message) {
        String error = System.getProperty("line.separator")
                + url
                + System.getProperty("line.separator")
                + message;


        throw new DatabladeErrorException(error);
    }


    public static final Logger logger = Logger.getLogger(DatabladeErrorController.class);
}
