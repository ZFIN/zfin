package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.zfin.construct.ConstructComponent;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;


import java.util.HashSet;
import java.util.Set;

import static org.zfin.framework.HibernateUtil.currentSession;

@Service
public class ConstructComponentService {
    private static ConstructRepository cr = RepositoryFactory.getConstructRepository();
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static String openP="(";

    private static String cassetteSeparator="Cassette";
    private static String storedComponentSeparator="#";
    private static String componentSeparator=":";


    public static String[] getCassettes(String constructStoredName) {
        if (constructStoredName == null)
            return null;


        String[] array1;
        array1 = constructStoredName.split(cassetteSeparator);
        for (int i = 0; i < array1.length; i++) {
            array1[i] = array1[i].trim();


        }
        return array1;
    }

//making assumption that "(" is the construct wrapper

    //TODO convert category to Enum

    public  static  void setConstructWrapperComponents(String constructType,String constructPrefix, String constructId,int cassetteNumber) {
        int numberOfWrapperComponents=3; //count of type, prefix and "("))
        if (constructPrefix.equals("")){
            numberOfWrapperComponents=2;
        }


        mr.addConstructComponent(cassetteNumber,1,constructId,constructType,ConstructComponent.Type.TEXT_COMPONENT,"construct wrapper component",null);
        if (numberOfWrapperComponents==2){

            mr.addConstructComponent(cassetteNumber,2,constructId,openP,ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT,"construct wrapper component",ir.getCVZdbIDByTerm(openP).getZdbID());
        }

            if (numberOfWrapperComponents==3){
                mr.addConstructComponent(cassetteNumber,2,constructId,constructPrefix,ConstructComponent.Type.TEXT_COMPONENT,"construct wrapper component",null);
                mr.addConstructComponent(cassetteNumber,3,constructId,openP,ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT,"construct wrapper component",ir.getCVZdbIDByTerm(openP).getZdbID());

            }


    }
    public static void getPromotersAndCoding(String cassetteName,int cassetteNumber,String zdbID,String newPub) {


        String promString;
        String codingString;
        String[] promoterArray;
        String[] codingArray;
        Set<Marker> promoterMarker = new HashSet<>();
        Set<Marker> codingMarker = new HashSet<>();


        promString=StringUtils.substringBefore(cassetteName,componentSeparator);
        codingString=StringUtils.substringAfter(cassetteName,componentSeparator);

        promoterArray = promString.split(storedComponentSeparator);
        codingArray = codingString.split(storedComponentSeparator);
        for (int i = 0; i < promoterArray.length; i++) {
            int lastComp=getComponentCount(zdbID);
            createComponentRecords(StringUtils.trim(promoterArray[i]),"promoter component", promoterMarker,ConstructComponent.Type.PROMOTER_OF,cassetteNumber,lastComp,newPub,zdbID);
        }
        if (codingArray.length!=0) {
            int lastComponent=getComponentCount(zdbID);
            createComponentRecords(componentSeparator, "promoter component", promoterMarker, ConstructComponent.Type.PROMOTER_OF, cassetteNumber, lastComponent, newPub, zdbID);

        }
            for (int i = 0; i < codingArray.length; i++) {
               int lastComponent=getComponentCount(zdbID);
                createComponentRecords(StringUtils.trim(codingArray[i]), "coding component", codingMarker, ConstructComponent.Type.CODING_SEQUENCE_OF, cassetteNumber, lastComponent, newPub, zdbID);
            }

        if (!promoterMarker.isEmpty() || !codingMarker.isEmpty()) {
            cr.addConstructRelationships(promoterMarker, codingMarker, cr.getConstructByID(zdbID), newPub);
        }

    }

     public static int getComponentCount(String zdbID){
        String sqlCount = " select MAX(cc_order) from construct_component where cc_construct_zdb_id=:zdbID ";
        Query query = currentSession().createSQLQuery(sqlCount);
        query.setString("zdbID", zdbID);
        Session session = HibernateUtil.currentSession();
        int lastComp = (Integer) query.uniqueResult() + 1;
        return lastComp;
    }

    private static void createComponentRecords(String componentString,String componentCategory,Set<Marker> componentArray,ConstructComponent.Type type,int cassetteNumber,int componentOrder,String constructPub,String constructID) {
        String componentZdbID = null;
        if (!(componentString.isEmpty())) {
            Marker mrkr = mr.getMarkerByAbbreviationAndAttribution(componentString, constructPub);
            if (mrkr != null) {

                componentZdbID = mrkr.getZdbID();

                componentArray.add(mrkr);

            } else {
                ControlledVocab cVocab = ir.getCVZdbIDByTerm(componentString);
                if (cVocab != null) {
                    componentZdbID = cVocab.getZdbID();
                    type = ConstructComponent.Type.CONTROLLED_VOCAB_COMPONENT;
                } else {

                    type = ConstructComponent.Type.TEXT_COMPONENT;
                }
            }
            mr.addConstructComponent(cassetteNumber, componentOrder, constructID, componentString, type, componentCategory, componentZdbID);
        }
    }
}

