package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.*;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Provides a handle to the SMP BlastQueryThread.
 * Doing this also allows custom heuristics, as well.
 */
public class SMPBlastController extends XMLBlastController {

    @Override
    protected void scheduleBlast(XMLBlastBean blastBean){
        blastBean.setNumChunks(4);
        BlastQueryJob blastSingleTicketQueryThread = new SMPBlastQueryThread(blastBean,SMPWublastService.getInstance());
        BlastQueryThreadCollection.getInstance().addJobAndStart(blastSingleTicketQueryThread);
    }


}