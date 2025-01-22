package org.zfin.framework.presentation.tags;


import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.expression.Experiment;
import org.zfin.expression.presentation.ExperimentPresentation;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;
import java.io.IOException;

public class CreateExperimentLinkTag extends BodyTagSupport{
    private final Logger logger = LogManager.getLogger(CreateExperimentLinkTag.class);

    private Experiment experiment;

    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        StringBuilder linkBuffer = new StringBuilder();

        if (experiment == null)
            return Tag.SKIP_BODY;


        linkBuffer.append(ExperimentPresentation.getLinkWithChemicalDetails(experiment));


        try {
            pageContext.getOut().print(linkBuffer.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.EVAL_PAGE;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }


}
