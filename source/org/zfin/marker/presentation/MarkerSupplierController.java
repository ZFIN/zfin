package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Organization;
import org.zfin.profile.presentation.SupplierBean;
import org.zfin.profile.presentation.SupplierBeanValidator;
import org.zfin.profile.repository.ProfileRepository;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/marker")
public class MarkerSupplierController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @InitBinder("supplierBean")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new SupplierBeanValidator());
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/suppliers", method = RequestMethod.GET)
    public List<SupplierBean> getSuppliersForMarker(@PathVariable String zdbID) {
        List<MarkerSupplier> suppliers = markerRepository.getSuppliersForMarker(zdbID);
        List<SupplierBean> beans = new ArrayList<>(suppliers.size());
        for (MarkerSupplier supplier : suppliers) {
            beans.add(SupplierBean.convert(supplier.getOrganization()));
        }
        return beans;
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/suppliers", method = RequestMethod.POST)
    public SupplierBean addSupplierForMarker(@PathVariable String zdbID,
                                             @Valid @RequestBody SupplierBean supplierBean,
                                             BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid supplier", errors);
        }

        Marker marker = markerRepository.getMarkerByID(zdbID);
        Organization supplier = profileRepository.getOrganizationByName(supplierBean.getName());

        if (MarkerService.markerHasSupplier(marker, supplier)) {
            errors.rejectValue("name", "marker.supplier.inuse");
            throw new InvalidWebRequestException("Invalid supplier", errors);
        }

        HibernateUtil.createTransaction();
        profileRepository.addSupplier(supplier, marker);
        HibernateUtil.flushAndCommitCurrentSession();

        return SupplierBean.convert(supplier);
    }

    @ResponseBody
    @RequestMapping(value = "/{markerID}/suppliers/{orgID}", method = RequestMethod.DELETE, produces = "text/plain")
    public String removeSupplier(@PathVariable String markerID,
                                 @PathVariable String orgID) {
        Marker marker = markerRepository.getMarkerByID(markerID);
        Organization organization = profileRepository.getOrganizationByZdbID(orgID);

        HibernateUtil.createTransaction();
        profileRepository.removeSupplier(organization, marker);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

}
