package org.zfin.alliancegenome;

import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "diseaseAnnotation")
public class AllianceDiseaseAnnotation {

    ObjectResponse<DiseaseAnnotation> entity;
}

