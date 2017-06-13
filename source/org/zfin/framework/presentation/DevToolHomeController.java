package org.zfin.framework.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.filter.UpdatesCheckFilter;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.presentation.JSONStatusResponse;
import org.zfin.profile.AccountInfo;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Simple controller that serves the developer tools home page.
 */
@Controller
public class DevToolHomeController {

    @RequestMapping("devtool/home")
    public String homePage() throws Exception {
        return "dev-tools/home.page";
    }

    @RequestMapping("devtool/system-status")
    @ResponseBody
    public SystemStatus getSystemStatus() throws Exception {
        ZdbFlag systemUpdatesDisabled = getInfrastructureRepository().getUpdatesFlag();
        SystemStatus status = new SystemStatus();
        status.setReadonly(systemUpdatesDisabled.isSystemUpdateDisabled());
        return status;
    }

    @RequestMapping(value = "devtool/system-status", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<JSONStatusResponse> setSystemStatus(@RequestParam("readonlyMode") boolean readonlyMode) throws Exception {
        HibernateUtil.createTransaction();
        SystemStatus status = new SystemStatus();
        try {
            ZdbFlag systemUpdatesFlag = getInfrastructureRepository().getUpdatesFlag();
            systemUpdatesFlag.setSystemUpdateDisabled(readonlyMode);
            status.setReadonly(systemUpdatesFlag.isSystemUpdateDisabled());
            HibernateUtil.flushAndCommitCurrentSession();
            UpdatesCheckFilter.setReadOnlyMode(readonlyMode);
        } catch (Exception e) {
            return new ResponseEntity<>(new JSONStatusResponse("Error", "Could not save update"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(new JSONStatusResponse("OK", ""), HttpStatus.OK);
    }

    @RequestMapping(value = "login-status")
    @ResponseBody
    public AccountInfo checkLoginStatus() throws Exception {
        Person person = ProfileService.getCurrentSecurityUser();
        return person == null ? null : person.getAccountInfo();
    }



}
