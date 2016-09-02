package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.curation.repository.CurationRepository;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.ComparatorCreator;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.repository.PublicationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/publication")
public class PublicationDashboardController {

    private final static Logger LOG = Logger.getLogger(PublicationDashboardController.class);

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    CurationRepository curationRepository;

    @RequestMapping("/bins")
    public String showPublicationBins(Model model) {
        model.addAttribute("currentUser", ProfileService.getCurrentSecurityUser());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Bins");
        return "publication/publication-bins.page";
    }

    @ResponseBody
    @RequestMapping(value = "/search-status", method = RequestMethod.GET)
    public List<DashboardPublicationBean> getListOfPubsInBin(@RequestParam(required = false) Long status,
                                                             @RequestParam(required = false) Long location,
                                                             @RequestParam(required = false) String owner,
                                                             @RequestParam(required = false) String sort) {
        List<PublicationTrackingHistory> histories = publicationRepository.getPublicationsByStatus(status, location, owner);
        Comparator cmp = Order.getComparator(sort);
        if (cmp != null) {
            Collections.sort(histories, cmp);
        }

        List<DashboardPublicationBean> beans = new ArrayList<>(histories.size());
        for (PublicationTrackingHistory history : histories) {
            DashboardPublicationBean bean = new DashboardPublicationBean();
            bean.setZdbId(history.getPublication().getZdbID());
            bean.setTitle(history.getPublication().getTitle());
            bean.setCitation(history.getPublication().getJournalAndPages());
            bean.setAuthors(history.getPublication().getAuthors());
            bean.setAbstractText(history.getPublication().getAbstractText());
            bean.setLastUpdate(history.getDate());
            bean.setPdfPath(history.getPublication().getFileName());
            List<DashboardImageBean> images = new ArrayList<>();
            for (Figure figure : history.getPublication().getFigures()) {
                for (Image image : figure.getImages()) {
                    DashboardImageBean imageBean = new DashboardImageBean();
                    imageBean.setLabel(figure.getLabel());
                    imageBean.setFullPath(image.getUrl());
                    imageBean.setMediumPath(image.getMediumUrl());
                    images.add(imageBean);
                }
            }
            bean.setImages(images);
            beans.add(bean);
        }
        return beans;
    }

    private enum Order {
        UPDATE("date"),
        PUB_DATE("publication.publicationDate");

        private String fieldName;

        Order(String fieldName) {
            this.fieldName = fieldName;
        }

        public static Comparator getComparator(String value) {
            if (value == null) {
                return null;
            }

            String[] parts = value.split(",");
            Order order = valueOf(parts[0].toUpperCase());
            if (order == null) {
                return null;
            }

            Comparator cmp = ComparatorCreator.orderBy(order.fieldName);
            if (parts.length > 1 && parts[1].equals("desc")) {
                cmp = Collections.reverseOrder(cmp);
            }
            return cmp;
        }
    }

}
