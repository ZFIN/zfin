package org.zfin.alliancegenome;

import lombok.Getter;
import lombok.Setter;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "diseaseAnnotation")
public class AllianceDiseaseAnnotation {

    ObjectResponse<DiseaseAnnotation> entity;
}

