package org.zfin.marker.presentation;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Clone;
import org.zfin.marker.ProbeLibrary;
import org.zfin.marker.Vector;
import org.zfin.marker.repository.MarkerRepository;

@Controller
@RequestMapping("/api/clone")
@Log4j2
public class CloneDataController {

    @Autowired
    private MarkerRepository markerRepository;

    @InitBinder("linkDisplay")
    public void initLinkBinder(WebDataBinder binder) {
        binder.setValidator(new LinkDisplayValidator());
    }

    @InitBinder("markerReferenceBean")
    public void initReferenceBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerReferenceBeanValidator());
    }


    @ResponseBody
    @RequestMapping("/{cloneID}/data")
    public CloneDTO getCloneData(@PathVariable String cloneID) {
        Clone clone = markerRepository.getCloneById(cloneID);
        return DTOConversionService.convertToCloneDTO(clone);
    }

    @ResponseBody
    @RequestMapping(value = "/{cloneID}", method = RequestMethod.POST)
    public CloneDTO saveCloneData(@PathVariable String cloneID,
                                  @RequestBody CloneDTO cloneDTO) {
        Clone clone = markerRepository.getCloneById(cloneDTO.getZdbID());
        log.info("got clone: " + clone.getZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();
        try {
            if (clone.getProblem() == null) {
                DTOMarkerService.insertMarkerUpdate(clone, "problemType", "", cloneDTO.getProblemType());
            } else {
                DTOMarkerService.insertMarkerUpdate(clone, "problemType", clone.getProblem().name(), cloneDTO.getProblemType());
            }
            if (StringUtils.isEmpty(cloneDTO.getProblemType())) {
                clone.setProblem(null);
            } else {
                clone.setProblem(Clone.ProblemType.getProblemType(cloneDTO.getProblemType()));
            }

            DTOMarkerService.insertMarkerUpdate(clone, "digest", clone.getDigest(), cloneDTO.getDigest());
            clone.setDigest(cloneDTO.getDigest());

            DTOMarkerService.insertMarkerUpdate(clone, "PCR Amplification", clone.getPcrAmplification(), cloneDTO.getPcrAmplification());
            clone.setPcrAmplification(cloneDTO.getPcrAmplification());

            DTOMarkerService.insertMarkerUpdate(clone, "Polymerase Name", clone.getPolymeraseName(), cloneDTO.getPolymerase());
            clone.setPolymeraseName(cloneDTO.getPolymerase());

            DTOMarkerService.insertMarkerUpdate(clone, "Insert Size", clone.getInsertSize(), cloneDTO.getInsertSize());
            clone.setInsertSize(cloneDTO.getInsertSize());

            DTOMarkerService.insertMarkerUpdate(clone, "Clone Comments", clone.getCloneComments(), cloneDTO.getCloneComments());
            clone.setCloneComments(cloneDTO.getCloneComments());

            DTOMarkerService.insertMarkerUpdate(clone, "Rating", clone.getRating(), cloneDTO.getRating());
            clone.setRating(cloneDTO.getRating());

            DTOMarkerService.insertMarkerUpdate(clone, "Cloning Site", clone.getCloningSite(), cloneDTO.getCloningSite());
            clone.setCloningSite(cloneDTO.getCloningSite());

            // set vector
            String cloneVectorName = (clone.getVector() == null ? null : clone.getVector().getName());
            DTOMarkerService.insertMarkerUpdate(clone, "Vector Name", cloneVectorName, cloneDTO.getVectorName());
            if (cloneDTO.getVectorName() != null) {
                Vector vector = session.get(Vector.class, cloneDTO.getVectorName());
                clone.setVector(vector);
            } else {
                clone.setVector(null);
            }

            String cloneProbeLibraryName = (clone.getProbeLibrary() == null ? null : clone.getProbeLibrary().getName());
            DTOMarkerService.insertMarkerUpdate(clone, "Probe Library", cloneProbeLibraryName, cloneDTO.getProbeLibraryName());
            Query<ProbeLibrary> query = session.createQuery("from ProbeLibrary where name = :name", ProbeLibrary.class);
            query.setParameter("name", cloneDTO.getProbeLibraryName());
            ProbeLibrary probeLibrary = query.uniqueResult();
            clone.setProbeLibrary(probeLibrary);


            session.update(clone);
            session.flush();
            log.info("updated clone: " + clone);

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            log.error(e);
        }

        return cloneDTO;
    }


}
