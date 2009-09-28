package org.zfin.sequence.blast.presentation;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.marker.presentation.BlastBean;
import org.zfin.marker.Transcript;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.WebHostWublastBlastService;
import org.zfin.sequence.blast.MountedWublastBlastService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import java.util.List;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.OutputStreamWriter;

/**
 * Allows downloading of blast sequence.
 */
public class DownloadBlastSequenceController extends AbstractController {

    final int BUFSIZE = 256 ;

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {

        String accession = httpServletRequest.getParameter(LookupStrings.ACCESSION) ;
        List<Sequence> sequences = MountedWublastBlastService.getInstance().
                getSequencesForAccessionAndDisplayGroup(
                        accession, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE,
                        DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE) ;
        logger.info("transcriptviewcontroller # of seq: " + sequences.size());

        String sequenceData = sequences.get(0).getFormattedData() ;
        String fileName = accession+".fa" ;

        ServletOutputStream outputStream = httpServletResponse.getOutputStream() ;
        ServletContext context = getServletContext() ;

        String mimeType = context.getMimeType(fileName) ;
        httpServletResponse.setContentType( (( mimeType!=null ) ? mimeType : "application/octet-stream"));
        httpServletResponse.setContentLength(sequenceData.length());
        httpServletResponse.setHeader("Content-Disposition","attachment; filename=\""+fileName+"\"");
        
        //
        //  Stream to the requester.
        //
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream) ;
        outputStreamWriter.write(sequenceData);

        outputStreamWriter.flush();
        outputStreamWriter.close();

        return null ;
    }
}
