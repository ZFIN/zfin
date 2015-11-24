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
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.sequence.STRMarkerSequence;

import javax.validation.Valid;

@Controller
@RequestMapping("/str")
public class StrDetailsController {

    @Autowired
    MarkerRepository markerRepository;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new StrDetailsValidator());
    }


    @ResponseBody
    @RequestMapping(value = "/{zdbID}/details", method = RequestMethod.GET)
    public StrDetailsBean getStrDetails(@PathVariable String zdbID) {
        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(zdbID);
        STRMarkerSequence sequence = str.getSequence();
        StrDetailsBean bean = new StrDetailsBean();
        bean.setZdbID(str.getZdbID());
        bean.setName(str.getAbbreviation());
        bean.setType(str.getType().toString());
        bean.setSequence1(sequence.getSequence());
        bean.setSequence2(sequence.getSecondSequence());
        return bean;
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
        try {
            str.setName(bean.getName());
            str.setAbbreviation(bean.getName());
            sequence.setSequence(bean.getSequence1());
            sequence.setSecondSequence(bean.getSequence2());

            session.save(str);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        return bean;
    }

}
