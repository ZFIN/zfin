package org.zfin.people.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.people.AccountInfo;
import org.zfin.people.Person;
import org.zfin.people.UserService;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that serves the people pages.
 */
public class AddressController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(AddressController.class);
    private ProfileRepository profileRep = RepositoryFactory.getProfileRepository();

    public AddressController() {
        setCommandClass(ProfileBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
                                  BindException errors) throws Exception {

        ProfileBean profileForm = (ProfileBean) command;
        Person person = profileForm.getPerson();
        AddressController.LOG.info("Start Action Class");
        person = profileRep.getPerson(person.getZdbID());
        profileForm.setPerson(person);

        AccountInfo accountInfo = null;
        if (UserService.isRootUser() || UserService.isOwner(person.getZdbID(), Person.class)) {
            Person pers = profileRep.getPerson(person.getZdbID());
            accountInfo = pers.getAccountInfo();
            profileForm.setAccountInfo(accountInfo);
        }


        return new ModelAndView("view-person-detail", "profileForm", profileForm);
    }

}
