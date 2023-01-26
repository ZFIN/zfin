package org.zfin.marker.agr;

import org.alliancegenome.curation_api.model.ingest.dto.NameSlotAnnotationDTO;
import org.zfin.infrastructure.EntityZdbID;

public class LinkMLInfoService {

	public static NameSlotAnnotationDTO getNameSlotAnnotationDTO(EntityZdbID entity) {
		NameSlotAnnotationDTO geneNameDTO = new NameSlotAnnotationDTO();
		geneNameDTO.setDisplayText(entity.getEntityName());
		geneNameDTO.setFormatText(entity.getEntityName());
		geneNameDTO.setNameTypeName("full_name");
		return geneNameDTO;
	}

	public static NameSlotAnnotationDTO getSymbolSlotAnnotationDTO(EntityZdbID marker) {
		NameSlotAnnotationDTO symbolDTO = new NameSlotAnnotationDTO();
		symbolDTO.setDisplayText(marker.getAbbreviation());
		symbolDTO.setFormatText(marker.getAbbreviation());
		symbolDTO.setNameTypeName("nomenclature_symbol");
		symbolDTO.setSynonymScopeName("exact");
		return symbolDTO;
	}

	public static NameSlotAnnotationDTO getSymbolSlotAnnotationDTO(EntityZdbID marker) {
		NameSlotAnnotationDTO symbolDTO = new NameSlotAnnotationDTO();
		symbolDTO.setDisplayText(marker.getAbbreviation());
		symbolDTO.setFormatText(marker.getAbbreviation());
		symbolDTO.setNameTypeName("nomenclature_symbol");
		symbolDTO.setSynonymScopeName("exact");
		return symbolDTO;
	}


}
