package org.zfin.sequence.blast.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.MountedWublastBlastService;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Allows downloading of blast sequence.
 */
@Controller
public class DownloadBlastSequenceController {

    @RequestMapping("/blast/download-sequence")
    protected String showBlastDefinitions(@RequestParam(required = false) String accession,
                                          HttpServletResponse response,
                                          HttpServletRequest request) throws Exception {

        List<Sequence> sequences = MountedWublastBlastService.getInstance().
                getSequencesForAccessionAndDisplayGroup(
                        accession, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE,
                        DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE);
        logger.info("transcriptviewcontroller # of seq: " + sequences.size());

        String sequenceData = sequences.get(0).getFormattedData();
        String fileName = accession + ".fa";

        ServletOutputStream outputStream = response.getOutputStream();
        ServletContext context = request.getSession().getServletContext();

        String mimeType = context.getMimeType(fileName);
        response.setContentType(((mimeType != null) ? mimeType : "application/octet-stream"));
        response.setContentLength(sequenceData.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        //
        //  Stream to the requester.
        //
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write(sequenceData);

        outputStreamWriter.flush();
        outputStreamWriter.close();

        return null;
    }

    private Logger logger = LogManager.getLogger(DownloadBlastSequenceController.class);
}
