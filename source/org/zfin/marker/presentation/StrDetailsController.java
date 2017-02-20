package org.zfin.marker.presentation;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.sequence.STRMarkerSequence;

import javax.validation.Valid;
import java.util.Objects;

@Controller
@RequestMapping("/str")
public class StrDetailsController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new StrDetailsValidator());
    }


    @ResponseBody
    @RequestMapping(value = "/{zdbID}/details", method = RequestMethod.GET)
    public StrDetailsBean getStrDetails(@PathVariable String zdbID) {
        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(zdbID);
        return StrDetailsBean.convert(str);
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/details", method = RequestMethod.POST)
    public StrDetailsBean updateStrDetails(@PathVariable String zdbID,
                                           @Valid @RequestBody StrDetailsBean bean,
                                           BindingResult result) {
        if (result.hasErrors()) {
            throw new InvalidWebRequestException("Invalid STR details", result);
        }

        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(zdbID);
        STRMarkerSequence sequence = str.getSequence();

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();

        if (!Objects.equals(bean.getName(), str.getName())) {
            infrastructureRepository.insertUpdatesTable(str.getZdbID(), "name", str.getName(), bean.getName(), "");
            str.setName(bean.getName());
            str.setAbbreviation(bean.getName());
            markerRepository.addMarkerAlias(str,str.getName(),null);
        }

        if (!Objects.equals(bean.getSequence1(), sequence.getSequence())) {
            infrastructureRepository.insertUpdatesTable(str.getZdbID(), "sequence", sequence.getSequence(), bean.getSequence1(), "");
            sequence.setSequence(bean.getSequence1());
        }

        // use Objects.equals here because sequence2 may be null
        if (!Objects.equals(bean.getSequence2(), sequence.getSecondSequence())) {
            infrastructureRepository.insertUpdatesTable(str.getZdbID(), "second sequence", sequence.getSecondSequence(), bean.getSequence2(), "");
            sequence.setSecondSequence(bean.getSequence2());
        }

        if (bean.getReversed1() || bean.getComplemented1()) {
            String note = MarkerService.getSTRModificationNote(bean.getReportedSequence1(), bean.getReversed1(), bean.getComplemented1());
            markerRepository.addMarkerDataNote(str, note);
        }

        if (bean.getReversed2() || bean.getComplemented2()) {
            String note = MarkerService.getSTRModificationNote(bean.getReportedSequence2(), bean.getReversed2(), bean.getComplemented2());
            markerRepository.addMarkerDataNote(str, note);
        }

        session.save(str);
        tx.commit();

        return StrDetailsBean.convert(str);
    }

}
