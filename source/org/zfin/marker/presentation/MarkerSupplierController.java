package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Organization;
import org.zfin.profile.presentation.SupplierBean;
import org.zfin.profile.presentation.SupplierBeanValidator;
import org.zfin.profile.repository.ProfileRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/marker")
public class MarkerSupplierController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new SupplierBeanValidator());
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/suppliers", method = RequestMethod.GET)
    public List<SupplierBean> getSuppliersForMarker(@PathVariable String zdbID) {
        List<MarkerSupplier> suppliers = markerRepository.getSuppliersForMarker(zdbID);
        List<SupplierBean> beans = new ArrayList<>(suppliers.size());
        for (MarkerSupplier supplier : suppliers) {
            SupplierBean bean = new SupplierBean();
            bean.setZdbID(supplier.getOrganization().getZdbID());
            bean.setName(supplier.getOrganization().getName());
            beans.add(bean);
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

        Collection<MarkerSupplier> suppliers = marker.getSuppliers();
        if (CollectionUtils.isNotEmpty(suppliers)) {
            for (MarkerSupplier markerSupplier : marker.getSuppliers()) {
                if (markerSupplier.getOrganization().equals(supplier)) {
                    errors.rejectValue("name", "marker.supplier.inuse");
                    throw new InvalidWebRequestException("Invalid supplier", errors);
                }
            }
        }

        HibernateUtil.createTransaction();
        profileRepository.addSupplier(supplier, marker);
        HibernateUtil.flushAndCommitCurrentSession();

        SupplierBean newBean = new SupplierBean();
        newBean.setZdbID(supplier.getZdbID());
        newBean.setName(supplier.getName());
        return newBean;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerID}/suppliers/{orgID}", method = RequestMethod.DELETE)
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
