package org.zfin.profile.presentation;

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
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class ImageController {

    // Maximum of 10 MB images we will handle.
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    // make images fit into this size of square.
    public static final int MAX_IMAGES_SQUARE_SIZE_PIXELS = 500;
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



        Person person = null;
        if (zdbID.startsWith("ZDB-PERS")) {
            person = profileRepository.getPerson(zdbID);
            model.addAttribute(LookupStrings.SELECTED_TAB, PersonController.TAB_INDEX.PICTURE.getLabel());
        } else if (zdbID.startsWith("ZDB-COMPANY")) {
            Company company = profileRepository.getCompanyById(zdbID);
            person = company.getContactPerson();
            model.addAttribute(LookupStrings.SELECTED_TAB, CompanyController.TAB_INDEX.PICTURE.getLabel());

        } else if (zdbID.startsWith("ZDB-LAB")) {
            Lab lab = profileRepository.getLabById(zdbID);
            person = lab.getContactPerson();
            model.addAttribute(LookupStrings.SELECTED_TAB, LabController.TAB_INDEX.PICTURE.getLabel());
        }

        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(person);
        if (securityPersonZdbId != null) {
            HibernateUtil.createTransaction();


            profileService.deleteImage(zdbID, securityPersonZdbId);


            final ImageUploadMessageBean imageUploadMessageBean = new ImageUploadMessageBean();
            imageUploadMessageBean.setZdbID(zdbID);
            imageUploadMessageBean.setSecurityPersonZdbID(securityPersonZdbId);
            imageUploadMessageBean.setSnapshot(null);

            HibernateUtil.currentSession().flush();
            HibernateUtil.currentSession().getTransaction().commit();

        } else {
            model.addAttribute("errors", "User may not edit this user.");
            return "profile/profile-edit.page";
        }

        if (zdbID.startsWith("ZDB-PERS"))
            return personController.editView(zdbID, model);
        else if (zdbID.startsWith("ZDB-COMPANY"))
            return companyController.editView(zdbID, model);
        else if (zdbID.startsWith("ZDB-LAB"))
            return labController.editView(zdbID, model);
        else return "redirect:/action/profile/edit/" + zdbID;
    }

    @RequestMapping(value = "/image/edit/{zdbID}", method = RequestMethod.POST)
    public String updatePicture(@PathVariable final String zdbID,
                                @RequestParam("file") final MultipartFile file,
                                Model model)
            throws Exception {
        if (!file.isEmpty()) {

            Person person = null;
            if (zdbID.startsWith("ZDB-PERS")) {
                person = profileRepository.getPerson(zdbID);
                model.addAttribute(LookupStrings.SELECTED_TAB, PersonController.TAB_INDEX.PICTURE.getLabel());
            } else if (zdbID.startsWith("ZDB-COMPANY")) {
                Company company = profileRepository.getCompanyById(zdbID);
                person = company.getContactPerson();
                model.addAttribute(LookupStrings.SELECTED_TAB, CompanyController.TAB_INDEX.PICTURE.getLabel());
            } else if (zdbID.startsWith("ZDB-LAB")) {
                Lab lab = profileRepository.getLabById(zdbID);
                person = lab.getContactPerson();
                model.addAttribute(LookupStrings.SELECTED_TAB, LabController.TAB_INDEX.PICTURE.getLabel());
            }

            final String securityPersonZdbId = profileService.isEditableBySecurityPerson(person);
            if (securityPersonZdbId != null) {

                long size = file.getBytes().length;
                if (size > MAX_IMAGE_SIZE) {
                    model.addAttribute("imageError", "File too large. Please upload an image smaller than 10 MB");
                    if (zdbID.startsWith("ZDB-PERS"))
                        return personController.editView(zdbID, model);
                    else if (zdbID.startsWith("ZDB-COMPANY"))
                        return companyController.editView(zdbID, model);
                    else if (zdbID.startsWith("ZDB-LAB"))
                        return labController.editView(zdbID, model);
                    else return "redirect:/action/profile/edit/" + zdbID;
                }
                String fileType = file.getContentType();
                if (!isSupportedImageType(fileType)) {
                    model.addAttribute("imageError", "File is not a supported image.  Please submit a jpg, gif or png less than 10MB in size.");
                    if (zdbID.startsWith("ZDB-PERS"))
                        return personController.editView(zdbID, model);
                    else if (zdbID.startsWith("ZDB-COMPANY"))
                        return companyController.editView(zdbID, model);
                    else if (zdbID.startsWith("ZDB-LAB"))
                        return labController.editView(zdbID, model);
                    else return "redirect:/action/profile/edit/" + zdbID;
                }

                byte[] imageInByte = getScaledImageBytes(file);
                HibernateUtil.createTransaction();

                Blob snapshot = HibernateUtil.currentSession().getLobHelper().createBlob(imageInByte);

                profileService.updateImage(zdbID, securityPersonZdbId, snapshot);

                final ImageUploadMessageBean imageUploadMessageBean = new ImageUploadMessageBean();
                imageUploadMessageBean.setZdbID(zdbID);
                imageUploadMessageBean.setSecurityPersonZdbID(securityPersonZdbId);
                try {
                    imageUploadMessageBean.setSnapshot(file.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("failed to snapshot bytes", e);
                }

                HibernateUtil.currentSession().flush();
                HibernateUtil.currentSession().getTransaction().commit();

            } else {
                model.addAttribute("errors", "User may not edit this user.");
                return "profile/profile-edit.page";
            }
        }
        if (zdbID.startsWith("ZDB-PERS"))
            return personController.editView(zdbID, model);
        else if (zdbID.startsWith("ZDB-COMPANY"))
            return companyController.editView(zdbID, model);
        else if (zdbID.startsWith("ZDB-LAB"))
            return labController.editView(zdbID, model);
        else return "redirect:/action/profile/edit/" + zdbID;
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

    @RequestMapping(value = "/image/view/{zdbID}.jpg", method = RequestMethod.GET)
    public void viewSnapshot
            (@PathVariable String
                     zdbID, HttpServletResponse
                    response, OutputStream
                    outputStream) throws Exception {
        response.setContentType("image/jpeg");
        //response.setHeader("Cache-Control", "no-cache");
        Blob blob = null;
        if (zdbID.startsWith("ZDB-PERS")) {
            Person person = profileRepository.getPerson(zdbID);
            blob = person.getSnapshot();
        } else if (zdbID.startsWith("ZDB-COMPANY")) {
            Company company = profileRepository.getCompanyById(zdbID);
            blob = company.getSnapshot();
        } else if (zdbID.startsWith("ZDB-LAB")) {
            Lab lab = profileRepository.getLabById(zdbID);
            blob = lab.getSnapshot();
        }
        if (blob != null) {
            long length = blob.length();
            outputStream.write(blob.getBytes(1l, (int) length));
            outputStream.flush();
            outputStream.close();
        }
    }


    public void setProfileRepository(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
}
