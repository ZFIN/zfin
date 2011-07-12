package org.zfin.datatransfer.webservice;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Will allow access to quartz jobs, but right now, without spring 3, have to run them this way.
 */

@Controller
@RequestMapping("/quartz")
public class QuartzController {

    private Logger logger = Logger.getLogger(QuartzController.class);

    // e.g.,
    // /webapp/quartz/run/MicroarrayWebserviceJob
    // blocking version
    @RequestMapping(value = "run/{job}", method = RequestMethod.GET)
    public
    @ResponseBody
    int runSynchronous(@PathVariable("job") String jobString) {

        // TODO: should allow for any class
        // TODO: should load via spring bean (once we are spring 3)
        // TODO: can allow asynchronous version with status updates (ie, processing)
        // TODO: change context to /quartz
        if (jobString != null && false==jobString.equals("org.zfin.datatransfer.microarray.MicroarrayWebserviceJob")) {
            try {
                logger.info("Attempting to run job ["+jobString+"]");
                Job job = (Job) Class.forName(jobString).newInstance();
                logger.info("Finished loading job["+jobString+"] executing");
                job.execute(null);
                logger.info("Executing job["+jobString+"]");
                return HttpStatus.SC_OK;
            } catch (ClassNotFoundException cnfe) {
                logger.error("Unable to find job class for job ["+jobString+"]",cnfe);
                return HttpStatus.SC_EXPECTATION_FAILED;
            } catch (ClassCastException cce) {
                logger.error("Unable to cast class to org.quartz.Job ["+jobString+"]",cce);
                return HttpStatus.SC_EXPECTATION_FAILED;
            } catch (Exception e) {
                logger.error("Error instantiating job",e);
                return HttpStatus.SC_EXPECTATION_FAILED;
            }
        }
        else{
            logger.error("no job string");
            return HttpStatus.SC_EXPECTATION_FAILED;
        }

    }


}
