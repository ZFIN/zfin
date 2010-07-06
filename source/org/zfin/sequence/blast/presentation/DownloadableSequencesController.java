package org.zfin.sequence.blast.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.sequence.blast.BlastDownloadService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class DownloadableSequencesController extends AbstractCommandController{

    private final Logger logger = Logger.getLogger(DownloadableSequencesController.class) ;

    private String view ;

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        BlastDownloadBean blastDownloadBean = (BlastDownloadBean) command ;

        logger.info("action: "+ blastDownloadBean.getAction());

        if(blastDownloadBean.getAction()!=null){
            switch (blastDownloadBean.getAction()) {
                case MORPHOLINO:
                    return download("zfin_mrph.fa",BlastDownloadService.getMorpholinoDownload(),response) ;
                case GENBANK_ALL:
                    return download("zfin_genbank_acc.unl",BlastDownloadService.getGenbankAllDownload(),response) ;
                case GENBANK_CDNA:
                    return download("zfin_genbank_cdna_acc.unl",BlastDownloadService.getGenbankCdnaDownload(),response) ;
                case GENBANK_XPAT_CDNA:
                    return download("zfin_genbank_xpat_cdna_acc.unl",BlastDownloadService.getGenbankXpatCdnaDownload(),response) ;
                case GENOMIC_REFSEQ:
                    return download("zfin_genomic_refseq_acc.unl",BlastDownloadService.getGenomicRefseqDownload(),response) ;
                case GENOMIC_GENBANK:
                    return download("zfin_genomic_genbank_acc.unl",BlastDownloadService.getGenomicGenbankDownload(),response) ;
                default:
                    throw new RuntimeException("Action not found: "+blastDownloadBean.getAction()) ;
            }
        }

        return new ModelAndView(view);
    }

    private ModelAndView download(String name,String data, HttpServletResponse response) throws Exception {
        byte[] bytes = data.getBytes();

        response.setBufferSize(bytes.length);
        response.setContentLength(bytes.length);
        // reference: http://onjava.com/pub/a/onjava/excerpt/jebp_3/index3.html
        response.setContentType("application/x-download");
        response.setHeader("Content-Disposition","attachment; filename="+name);


        ServletOutputStream servletOutputStream = response.getOutputStream();

        servletOutputStream.write(bytes,0,bytes.length);
        servletOutputStream.flush();
        servletOutputStream.close();
        return null ;
    }

}
