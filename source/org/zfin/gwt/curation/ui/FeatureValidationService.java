package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.util.StringUtils;

import java.util.Objects;

/**
 */
public class FeatureValidationService {

    public static final String UNSPECIFIED_FEATURE_NAME = "_unspecified";

    public static String isValidToSave(FeatureDTO featureDTO) {

        // should never get here

        if (!isFeatureSaveable(featureDTO))
            return "You must specify a lab prefix, feature type, and feature line number, assembly, start and end positions.";

        if (StringUtils.isNotEmpty(featureDTO.getFeatureChromosome())) {

            if (featureDTO.getFeatureStartLoc() != null && featureDTO.getFeatureEndLoc() != null) {
                if (featureDTO.getFeatureStartLoc() > featureDTO.getFeatureEndLoc()) {
                    return "Start location cannot be greater than end location";
                }
            }

            if (featureDTO.getFeatureStartLoc() != null || featureDTO.getFeatureEndLoc() != null) {
                if (featureDTO.getAssembly() == null) {
                    return "You must specify an assembly if you specify a location";
                }
            }

        } else if (featureDTO.getFeatureStartLoc() != null || featureDTO.getFeatureEndLoc() != null) {
            return "You must specify a chromosome if you specify a location";
        }

        if (StringUtils.isNotEmpty(featureDTO.getAssemblyInfoDate())) {
            if (featureDTO.getAssemblyInfoDate().length() != 8 || !(featureDTO.getAssemblyInfoDate().contains("/"))) {
                return "Please enter date in mm/dd/yy format";
            }
        }

        if (featureDTO.getPublicNote()!=null) {
            if (featureDTO.getPublicNote().getNoteTag()==null) {

                {
                    return "please enter note type";
                }
            }
        }

        FeatureTypeEnum featureTypeEnum = featureDTO.getFeatureType();
        if (Objects.requireNonNull(featureTypeEnum) == FeatureTypeEnum.COMPLEX_SUBSTITUTION) {
            if (featureDTO.getPublicNoteList() == null) {
                boolean yes = Window.confirm("Do you want to briefly summarize authors statement about " + featureTypeEnum.getDisplay() + "?");
                return (yes ? "Briefly summarize authors' statement about " + featureTypeEnum.getDisplay() + " in a public note." : null);
            }
        }

        return null;
    }

    public static boolean isFeatureSaveable(FeatureDTO dtoFromGUI) {
        if (dtoFromGUI.getFeatureType() == null) return false;


       /* if (StringUtils.isEmpty(dtoFromGUI.getFeatureChromosome())) {
            if (dtoFromGUI.getFeatureStartLoc()==null){
                Window.alert("You must specify a location if you specify a chromosome");
                return false;
            }
        }*/

        switch (dtoFromGUI.getFeatureType()) {
            case TRANSGENIC_INSERTION:
                boolean isKnownInSite = dtoFromGUI.getKnownInsertionSite();
                if (isKnownInSite) {
                    return StringUtils.isNotEmpty(dtoFromGUI.getLabPrefix()) &&
                            StringUtils.isNotEmpty(dtoFromGUI.getLineNumber()) &&
                            StringUtils.isNotEmpty(dtoFromGUI.getTransgenicSuffix());
                } else {
                    return StringUtils.isNotEmpty(dtoFromGUI.getLabPrefix()) &&
                            StringUtils.isNotEmpty(dtoFromGUI.getLineNumber()) &&
                            StringUtils.isNotEmpty(dtoFromGUI.getTransgenicSuffix());
                }

            case POINT_MUTATION:
            case DELETION:
            case INDEL:
            case MNV:
            case SEQUENCE_VARIANT:
            case INSERTION:
                return StringUtils.isNotEmpty(dtoFromGUI.getLabPrefix()) &&
                        StringUtils.isNotEmpty(dtoFromGUI.getLineNumber());
            case INVERSION:
            case TRANSLOC:
            case DEFICIENCY:
                // enable feature names
                return StringUtils.isNotEmpty(dtoFromGUI.getOptionalName()) &&
                        StringUtils.isNotEmpty(dtoFromGUI.getLabPrefix()) &&
                        StringUtils.isNotEmpty(dtoFromGUI.getLineNumber());
            case COMPLEX_SUBSTITUTION:
                // enable feature names
                return StringUtils.isNotEmpty(dtoFromGUI.getLabPrefix()) &&
                        StringUtils.isNotEmpty(dtoFromGUI.getLineNumber());
            case UNSPECIFIED:
                return StringUtils.isNotEmpty(dtoFromGUI.getOptionalName());
            default:
                Window.alert("Unknown feature type: " + dtoFromGUI.getFeatureType());
                return false;
        }

    }

    /**
     * Rules from here: <a href="http://zfinwinserver1.uoregon.edu/fogbugz/default.asp?pg=pgDownload&pgType=pgWikiAttachment&ixAttachment=1269&sFileName=Splitlabdesignations.20100809.pptx">...</a>
     *
     * @param dtoFromGUI The feaure DTO generated from the GUI (or another DTO)
     * @return Name to display for feature.
     * // if translocation, deficiency, inversion, or complex
     * //    (dominant) name + (unspecified ? "unspecified" : lab designation + line #)
     * // this is based on the spl: checkFeatureAbbrev
     * // unrecovered is only used in loads
     * // feature_df_transloc_complex_prefix is part of a trigger
     * define vFeatureLabPrefix like feature_prefix.fp_prefix;
     * <p/>
     * let vFeatureLabPrefix =
     * (select fp_prefix
     * from feature_prefix
     * where vFeatureLabPrefixId = fp_pk_id);
     * <p/>
     * if (vFeatureUnspecified ='t')
     * then
     * if (vFeatureAbbrev != vFeatureMrkrAbbrev||"_unspecified")
     * then raise exception -746,0,"FAIL!: unspecified allele must have abbrev like _unspecified. checkFeatureAbbrev.";
     * end if;
     * elif (vFeatureUnrecovered ='t')
     * then
     * if (vFeatureAbbrev != vFeatureMrkrAbbrev||"_unrecovered")
     * then raise exception -746,0,"FAIL!: unrecovered allele must have abbrev like _unrecovered. checkFeatureAbbrev.";
     * end if;
     * elif (vFeatureDominant = 't')
     * then
     * if (vFeatureAbbrev not like 'd%')
     * then raise exception -746,0,"FAIL!: dominant allele must have abbrev like d*. checkFeatureAbbrev.";
     * end if;
     * elif (vFeatureType = 'TRANSGENIC_INSERTION' and vFeatureKnownInsertionSite = 'f')
     * then
     * if (vFeatureAbbrev != vFeatureMrkrAbbrev||vFeatureLabPrefix||vFeatureLineNumber)
     * then raise exception -746,0,"FAIL!:tg insert not like construct||labPrefix||lineNumber. checkFeatureAbbrev.";
     * end if;
     * elif (vFeatureType = 'TRANSGENIC_INSERTION' and vFeatureKnownInsertionSite = 't')
     * then
     * if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber||vFeatureTgSuffix)
     * then raise exception -746,0,"FAIL!:tg known insert not like labPrefix||lineNumber||tgSuffix checkFeatureAbbrev.";
     * end if;
     * elif (vFeatureType in ('DEFICIENCY','COMPLEX','TRANSLOCATION'))
     * then
     * if (vFeatureAbbrev != vFeatureDfTranslocComplexPrefix||vFeatureLabPrefix||vFeatureLineNumber)
     * then raise exception -746,0,"FAIL!: complex, DF, T must have abbrev like tgprefix||labPrefix||lineNumber. checkFeatureAbbrev.";
     * end if;
     * else
     * if (vFeatureType != vFeatureLabPrefix||vFeatureLineNumber and vFeatureUnspecified = 'f' and vFeatureDominant = 'f' and vFeatureMrkrAbbrev is null and vFeatureKnownInsertionSite = 'f' and vFeatureDfTranslocComplexPrefix is null and vFeatureTgSuffix = 'f')
     * then raise exception -746,0,"FAIL!: feature_abbrev != fPrefix||fLineNumber. checkFeatureAbbrev.";
     * end if;
     * end if;
     */

    // can unspecefied be dominant?
    // can transgenics be dominant?
    public static String generateFeatureDisplayName(FeatureDTO dtoFromGUI) {
        FeatureTypeEnum featureType = dtoFromGUI.getFeatureType();
        if (featureType == null) {
            return null;
        }

        String returnString = (dtoFromGUI.getDominant() ? "d" : "");
        switch (featureType) {
            case TRANSGENIC_INSERTION:
                /*if(isKnownInSite){
                    returnString += featureDTO.getLabPrefix()
                            + featureDTO.getLineNumber()
                            + featureDTO.getTransgenicSuffix() ;
                }
                else{
                    returnString += featureDTO.getOptionalName()
                            + featureDTO.getLabPrefix()
                            + featureDTO.getLineNumber()  ;
                }*/
                returnString += dtoFromGUI.getLabPrefix()
                        + dtoFromGUI.getLineNumber()
                        + dtoFromGUI.getTransgenicSuffix();
                break;
            case POINT_MUTATION:
            case DELETION:
            case SEQUENCE_VARIANT:
            case INSERTION:
                returnString += dtoFromGUI.getLabPrefix()
                        + dtoFromGUI.getLineNumber()
                ;
                break;
            case INVERSION:
            case INDEL:
            case MNV:
            case TRANSLOC:
            case DEFICIENCY:
            case COMPLEX_SUBSTITUTION:
                // enable feature names
                returnString += dtoFromGUI.getOptionalName()
                        + dtoFromGUI.getLabPrefix()
                        + dtoFromGUI.getLineNumber()
                ;
                break;

            case UNSPECIFIED:
                returnString = dtoFromGUI.getOptionalName() + UNSPECIFIED_FEATURE_NAME;
                break;
            default:
                Window.alert("Unknown feature type: " + featureType);
                returnString = null;
        }

        return returnString.replaceAll("null", "");
    }

    public static String getAbbreviationFromName(FeatureDTO featureDTO) {
        String fullName = generateFeatureDisplayName(featureDTO);
        if (featureDTO.getFeatureType() == null || StringUtils.isEmptyTrim(fullName)) {
            return null;
        }

        String dominantString = (featureDTO.getDominant() ? "d" : "");

        switch (featureDTO.getFeatureType()) {
            case TRANSGENIC_INSERTION:
                return dominantString
                        + featureDTO.getLabPrefix()
                        + featureDTO.getLineNumber()
                        + featureDTO.getTransgenicSuffix();
                /*if(featureDTO.getKnownInsertionSite()){
                    return dominantString
                            +  featureDTO.getLabPrefix()
                            + featureDTO.getLineNumber()
                            + featureDTO.getTransgenicSuffix() ;
                }
                else{
                    return  dominantString
                            +  featureDTO.getLabPrefix()
                            + featureDTO.getLineNumber()  ;
                }*/
            case INVERSION:
            case INDEL:
            case TRANSLOC:
            case DEFICIENCY:
            case COMPLEX_SUBSTITUTION:
                return dominantString
                        + featureDTO.getLabPrefix()
                        + featureDTO.getLineNumber();
            case UNSPECIFIED:
            case POINT_MUTATION:
            case DELETION:
            case SEQUENCE_VARIANT:
            case MNV:
            case INSERTION:
                // just uses the defaults
                return fullName;
            default:
                return null;
        }
    }


    public static String getNameFromFullName(FeatureDTO featureDTO) {
        if (featureDTO.getFeatureType() == null || StringUtils.isEmptyTrim(featureDTO.getName())) {
            return null;
        }

        int dominantIndex = (featureDTO.getDominant() ? 1 : 0);

        String name = featureDTO.getName();
        switch (featureDTO.getFeatureType()) {
            case TRANSGENIC_INSERTION:
               /* if(featureDTO.getKnownInsertionSite()){
                    return null ;
                }
                else{*/
                return name.substring(0, name.indexOf(featureDTO.getLabPrefix() + featureDTO.getLineNumber())).substring(dominantIndex);
//                }
            case POINT_MUTATION:
            case DELETION:
            case SEQUENCE_VARIANT:
            case INDEL:
            case MNV:
            case INSERTION:
                // just uses the defaults
                return null;
            case INVERSION:
            case TRANSLOC:
            case DEFICIENCY:
            case COMPLEX_SUBSTITUTION:
                // enable feature names
                return name.substring(0, name.indexOf(featureDTO.getLabPrefix() + featureDTO.getLineNumber())).substring(dominantIndex);

            case UNSPECIFIED:
                return name.substring(0, name.indexOf(UNSPECIFIED_FEATURE_NAME));
            default:
                return null;
        }
    }

}
