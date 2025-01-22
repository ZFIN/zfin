package org.zfin.uniprot.secondary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.dto.MarkerGoTermEvidenceSlimDTO;

import java.util.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SecondaryTermLoadAction implements Comparable<SecondaryTermLoadAction> {

	public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES}

	public enum SubType {
		MARKER_GO_TERM_EVIDENCE("Marker Go Term Evidence", 1),
		EXTERNAL_NOTE("External Note", 2),
		DB_LINK("DB Link", 3),
		PROTEIN_DOMAIN("Protein Domain", 4),
		PROTEIN("Protein", 5),
		INTERPRO_MARKER_TO_PROTEIN("Interpro Marker To Protein", 6),
		PROTEIN_TO_INTERPRO("Protein To Interpro", 7),
		PDB("PDB", 8),
		;

		private final String value;
		private final int processActionOrder;

		SubType(String s, int o) {
			this.value = s;
			this.processActionOrder = o;
		}

		@JsonValue
		public String getValue() {
			return value;
		}

		public int getProcessActionOrder() {
			return processActionOrder;
		}
	}

	private Type type;
	private SubType subType;
	private ForeignDB.AvailableName dbName;
	private String accession;
	private String geneZdbID;
	private String relatedEntityID;
	private String details;
	private int length;
	@JsonIgnore
	private Class handlerClass;
	private Map<String, String> relatedEntityFields;
	private Set<UniProtLoadLink> links;

	@JsonIgnore
	public String getPrefixedAccession() {
		return switch (dbName) {
			case INTERPRO -> "InterPro:" + accession;
			case UNIPROTKB -> "UniProtKB-KW:" + accession;
			case EC -> "EC:" + accession;
			default -> dbName.toString() + ":" + accession;
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SecondaryTermLoadAction that)) return false;
		return compareTo(that) == 0;
	}

	@Override
	public int compareTo(SecondaryTermLoadAction o) {
		Comparator<SecondaryTermLoadAction> comparator = Comparator.comparing(
				(SecondaryTermLoadAction obj) -> obj.accession, ObjectUtils::compare)
			.thenComparing(obj -> obj.type, ObjectUtils::compare)
			.thenComparing(obj -> obj.subType, ObjectUtils::compare)
			.thenComparing(obj -> obj.geneZdbID, ObjectUtils::compare)
			.thenComparing(obj -> obj.details, ObjectUtils::compare);
		return comparator.compare(this, o);
	}

	public String toString() {
		return "InterproLoadAction: " + " action=" + type +
			" subtype=" + subType +
			" accession=" + accession +
			" geneZdbID=" + geneZdbID +
			" relatedEntityID=" + relatedEntityID +
			" details=" + details +
			" length=" + length +
			" handlerClass=" + handlerClass +
			" relatedEntityFields=" + relatedEntityFieldsToString() +
			" links=" + links;
	}

	public String markerGoTermEvidenceRepresentation() {
		return geneZdbID + "," + dbName + ":" + this.accession;
	}

	public String getMd5() {
		return new MessageDigestPasswordEncoder("MD5").encode(toString());
	}

	public void setMd5(String md5) {
		//purposefully empty for deserialization
	}

	@JsonIgnore
	public String relatedEntityFieldsToString() {
		if (relatedEntityFields == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder("{");
		for (Map.Entry<String, String> entry : relatedEntityFields.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
		}
		sb.append("}");
		return sb.toString();
	}

	public List<UniProtLoadLink> getDynamicLinks() {
		List<UniProtLoadLink> dynamicLinks = new ArrayList<>();
		if (accession != null) {
			if (dbName != null && dbName.equals(ForeignDB.AvailableName.INTERPRO)) {
				dynamicLinks.add(UniProtLoadLink.create(dbName, accession));
			}
		}
		if (geneZdbID != null) {
			dynamicLinks.add(UniProtLoadLink.create(ForeignDB.AvailableName.ZFIN, geneZdbID));
		}
		return dynamicLinks;
	}

	@JsonIgnore
	public String getGoTermZdbID() {
		MarkerGoTermEvidenceSlimDTO markerGoTermEvidence = MarkerGoTermEvidenceSlimDTO.fromMap(this.getRelatedEntityFields());
		return markerGoTermEvidence.getGoTermZdbID();
	}

	@JsonIgnore
	public String getGoID() {
		MarkerGoTermEvidenceSlimDTO markerGoTermEvidence = MarkerGoTermEvidenceSlimDTO.fromMap(this.getRelatedEntityFields());
		return markerGoTermEvidence.getGoID();
	}

}
