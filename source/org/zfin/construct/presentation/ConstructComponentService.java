package org.zfin.construct.presentation;

import com.zerog.common.java.lang.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.zfin.construct.ConstructComponent;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;


import java.util.HashSet;
import java.util.Set;

@Service
public class ConstructComponentService {
    private static ConstructRepository cr = RepositoryFactory.getConstructRepository();
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();



    private static String[] getNumCassettes(String constructStoredName) {
        if (constructStoredName == null)
            return null;


        String[] array1;
        array1 = constructStoredName.split("%");
        for (int i = 0; i < array1.length; i++) {
            array1[i] = array1[i].trim();


        }
        return array1;
    }


    private  static String[] getComponentsFromName(String constructStoredName) {
        if (constructStoredName == null)
            return null;


        String[] array2;
        array2 = constructStoredName.split("#");
        /*String[] array1;
        array1 = constructStoredName.split("%");*/
        /*for (int i = 0; i < array1.length; i++) {
            array1[i] = array1[i].trim();*/

        for (int j = 0; j < array2.length; j++) {
            if(StringUtils.isNotEmpty(array2[j])) {
                array2[j] = array2[j].trim();
            }
        }

        return array2;
    }



    public static void setConstructComponents(String constructStoredName, String newPub, String constructId) {

        int constructLength=constructStoredName.length();
        String[] cassetteArray = getNumCassettes(constructStoredName);
        Set<Marker> promoterMarker = new HashSet<>();
        Set<Marker> codingMarker = new HashSet<>();

        //int lengthOfConstruct = cpt.length;
        int numCassettes=cassetteArray.length;


         int componentOrder=1;
        for (int k = 0; k < numCassettes; k++) {
            String[] cassetteComponent= getComponentsFromName(cassetteArray[k]);
            int lengthOfCassComponent = cassetteComponent.length;
            for (int i = 0; i < lengthOfCassComponent; i++) {
                    ConstructComponent ccs = new ConstructComponent();
                if (k>0) {
                    if (cassetteComponent[i].startsWith("Cassette")) {
                        ccs.setComponentCategory("cassette delimiter");
                        ccs.setType(ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT);

                        String consctCassette = ",";
                        ccs.setComponentZdbID(ir.getCVZdbIDByTerm(consctCassette).getZdbID());
                        ccs.setComponentValue(consctCassette);
                    }
                }
                if (cassetteComponent[i].startsWith("Prefix")) {
                    ccs.setComponentCategory("prefix component");
                    ccs.setType(ConstructComponent.Type.TEXT_COMPONENT);
                    String consctPrefix = StringUtils.substringAfter(cassetteComponent[i], "Prefix");
                    ccs.setComponentValue(consctPrefix);
                }
                if (cassetteComponent[i].startsWith("Prom")) {
                    ccs.setComponentCategory("promoter component");

                    ccs.setType(ConstructComponent.Type.PROMOTER_OF);
                    String consctComponent = StringUtils.substringAfter(cassetteComponent[i], "Prom");
                    ccs.setComponentValue(consctComponent);
                    //Marker mrkr = mr.getMarkerByAbbreviation(consctComponent);
                    Marker mrkr=mr.getMarkerByAbbreviationAndAttribution(consctComponent,newPub);
                    if (mrkr != null) {

                        ccs.setComponentZdbID(mrkr.getZdbID());
                        promoterMarker.add(mrkr);


                    } else {
                        ControlledVocab cVocab=ir.getCVZdbIDByTerm(consctComponent);
                        if (cVocab!=null) {
                            String cvID = ir.getCVZdbIDByTerm(consctComponent).getZdbID();
                            if (cvID != null) {
                                ccs.setComponentZdbID(cvID);
                                ccs.setType(ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT);
                            } else {
                                ccs.setType(ConstructComponent.Type.TEXT_COMPONENT);

                            }
                        }
                        else{
                            ccs.setComponentValue(consctComponent);
                            ccs.setType(ConstructComponent.Type.TEXT_COMPONENT);
                        }
                    }
                } else {
                    if (cassetteComponent[i].startsWith("Cassette")) {
                        ccs.setComponentValue(",");
                    }
                    else{
                        if (cassetteComponent[i].startsWith("Prefix")) {
                            ccs.setComponentValue(StringUtils.substringAfter(cassetteComponent[i], "Prefix"));
                        }
                        else{
                            ccs.setComponentValue(cassetteComponent[i]);
                        }
                    }

                }
                if (cassetteComponent[i].startsWith("Coding")) {
                    ccs.setType(ConstructComponent.Type.CODING_SEQUENCE_OF);
                    ccs.setComponentCategory("coding sequence component");
                    String consctComponent = StringUtils.substringAfter(cassetteComponent[i], "Coding");
                    ccs.setComponentValue(consctComponent);
                    Marker mrkr = mr.getMarkerByAbbreviation(consctComponent);
                    if (mrkr != null) {
                        ccs.setComponentZdbID(mrkr.getZdbID());
                        codingMarker.add(mrkr);

                    } else {

                        ControlledVocab cVocab=ir.getCVZdbIDByTerm(consctComponent);
                        if (cVocab!=null) {
                            String cvID = ir.getCVZdbIDByTerm(consctComponent).getZdbID();
                            if (cvID != null) {
                                ccs.setComponentZdbID(cvID);
                                ccs.setType(ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT);
                            } else {
                                ccs.setType(ConstructComponent.Type.TEXT_COMPONENT);
                            }
                        }
                        else{
                            ccs.setComponentValue(consctComponent);
                            ccs.setType(ConstructComponent.Type.TEXT_COMPONENT);
                        }
                    }

                }


                ccs.setComponentCassetteNum(k+1);
                ccs.setComponentOrder(componentOrder);

                ccs.setConstructZdbID(constructId);
                //cns.setCnsConstructID(1);

               mr.addConstructComponent(ccs);
                componentOrder++;

            }
        }
      //  String promString= org.springframework.util.StringUtils.collectionToDelimitedString(promoterMarker. ",");
        if (!promoterMarker.isEmpty() || !codingMarker.isEmpty()) {
            cr.addConstructRelationships(promoterMarker, codingMarker, cr.getConstructByID(constructId), newPub);
        }

        }



}

