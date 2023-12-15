package org.zfin.uniprot.dto;

public record UniProtLoadSummaryItemDTO(String description, Long beforeLoadCount, Long afterLoadCount) {
}
