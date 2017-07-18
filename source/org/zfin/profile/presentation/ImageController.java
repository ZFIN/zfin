package org.zfin.profile.presentation;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class ImageController {

    // Maximum of 10 MB images we will handle.
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    // make images fit into this size of square.
    public static final int MAX_IMAGES_SQUARE_SIZE_PIXELS = 500;

    private static final File IMAGE_LOADUP_DIR = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString(), ZfinPropertiesEnum.IMAGE_LOAD.toString());
    private static final String PROFILE_IMAGE_DIR = "profile";

    private Logger logger = Logger.getLogger(ImageController.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PersonController personController;

    @Autowired
    private CompanyController companyController;

    @Autowired
    private LabController labController;

    @RequestMapping(value = "/image/delete/{zdbID}", method = RequestMethod.POST)
    public String deletePicture(@PathVariable final String zdbID, Model model) throws Exception {
        Person person = getPersonAndSetTab(zdbID, model);
        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(person);
        if (securityPersonZdbId == null) {
            model.addAttribute("errors", "User may not edit this user.");
            return "profile/profile-edit.page";
        }

        HibernateUtil.createTransaction();

        profileService.deleteImage(zdbID, securityPersonZdbId);

        HibernateUtil.currentSession().flush();
        HibernateUtil.currentSession().getTransaction().commit();

        return getProfileEditView(zdbID, model);
    }

    @RequestMapping(value = "/image/edit/{zdbID}", method = RequestMethod.POST)
    public String updatePicture(@PathVariable final String zdbID,
                                @RequestParam("file") final MultipartFile file,
                                Model model)
            throws Exception {
        if (!file.isEmpty()) {
            Person person = getPersonAndSetTab(zdbID, model);

            final String securityPersonZdbId = profileService.isEditableBySecurityPerson(person);
            if (securityPersonZdbId == null) {
                model.addAttribute("errors", "User may not edit this user.");
                return "profile/profile-edit.page";
            }

            long size = file.getSize();
            if (size > MAX_IMAGE_SIZE) {
                model.addAttribute("imageError", "File too large. Please upload an image smaller than 10 MB");
                return getProfileEditView(zdbID, model);
            }
            String fileType = file.getContentType();
            if (!isSupportedImageType(fileType)) {
                model.addAttribute("imageError", "File is not a supported image.  Please submit a jpg, gif or png less than 10MB in size.");
                return getProfileEditView(zdbID, model);
            }

            HibernateUtil.createTransaction();

            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String destinationName = PROFILE_IMAGE_DIR + File.separator +
                    zdbID + "-" + Instant.now().toEpochMilli() + // timestamped to break browser cache on upload
                    FilenameUtils.EXTENSION_SEPARATOR + extension;
            File destinationFile = new File(IMAGE_LOADUP_DIR, destinationName);
            Files.copy(file.getInputStream(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            profileService.updateImage(zdbID, securityPersonZdbId, destinationName);

            HibernateUtil.currentSession().flush();
            HibernateUtil.currentSession().getTransaction().commit();
        }
        return getProfileEditView(zdbID, model);
    }

    private boolean isSupportedImageType(String contentType) {
        return getFileType(contentType) != null;
    }

    private String getFileType(String contentType) {
        if (contentType == null)
            return null;
        String contentTypeLower = contentType.toLowerCase();
        String[] tokens = contentTypeLower.split("/");
        if (tokens == null || tokens.length != 2)
            return null;
        if (!tokens[0].equalsIgnoreCase("image"))
            return null;
        List<String> types = Arrays.asList(fileTypes);
        return types.contains(tokens[1]) ? tokens[1] : null;

    }

    private static String[] fileTypes = {"gif", "jpg", "jpeg", "png"};

    private byte[] getScaledImageBytes(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        logger.debug("Original image Size (height,width): " + originalImage.getHeight() + ", " + originalImage.getWidth());
        BufferedImage scaledImage;
        // do not re-scale if the image is smaller than the max size
        if (originalImage.getHeight() < MAX_IMAGES_SQUARE_SIZE_PIXELS && originalImage.getWidth() < MAX_IMAGES_SQUARE_SIZE_PIXELS)
            scaledImage = originalImage;
        else
            scaledImage = Scalr.resize(originalImage, MAX_IMAGES_SQUARE_SIZE_PIXELS);
        // make original image available to GC
        originalImage.flush();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(scaledImage, getFileType(file.getContentType()), baos);
        return baos.toByteArray();
    }

    public void setProfileRepository(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    private Person getPersonAndSetTab(String zdbID, Model model) {
        Person person = null;
        ActiveSource.Type type = ActiveSource.validateID(zdbID);
        if (type == ActiveSource.Type.PERS) {
            person = profileRepository.getPerson(zdbID);
            model.addAttribute(LookupStrings.SELECTED_TAB, PersonController.TAB_INDEX.PICTURE.getLabel());
        } else if (type == ActiveSource.Type.COMPANY) {
            Company company = profileRepository.getCompanyById(zdbID);
            person = company.getContactPerson();
            model.addAttribute(LookupStrings.SELECTED_TAB, CompanyController.TAB_INDEX.PICTURE.getLabel());
        } else if (type == ActiveSource.Type.LAB) {
            Lab lab = profileRepository.getLabById(zdbID);
            person = lab.getContactPerson();
            model.addAttribute(LookupStrings.SELECTED_TAB, LabController.TAB_INDEX.PICTURE.getLabel());
        }
        return person;
    }

    private String getProfileEditView(String zdbID, Model model) {
        ActiveSource.Type type = ActiveSource.validateID(zdbID);
        if (type == ActiveSource.Type.PERS) {
            return personController.editView(zdbID, model);
        } else if (type == ActiveSource.Type.COMPANY) {
            return companyController.editView(zdbID, model);
        } else if (type == ActiveSource.Type.LAB) {
            return labController.editView(zdbID, model);
        } else {
            return "redirect:/action/profile/edit/" + zdbID;
        }
    }
}
