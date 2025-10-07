package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenomeLocationDTO {

    private String assembly;
    private String chromosome;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer startPosition;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer endPosition;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String strand;

    public GenomeLocationDTO(String assemblyName, String chromosome) {
        this.assembly = assemblyName;
        this.chromosome = chromosome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenomeLocationDTO that = (GenomeLocationDTO) o;

        if (!assembly.equals(that.assembly)) return false;
        if (!chromosome.equals(that.chromosome)) return false;
        if (startPosition != null ? !startPosition.equals(that.startPosition) : that.startPosition != null)
            return false;
        return endPosition != null ? endPosition.equals(that.endPosition) : that.endPosition == null;
    }

    @Override
    public int hashCode() {
        int result = assembly.hashCode();
        result = 31 * result + chromosome.hashCode();
        result = 31 * result + (startPosition != null ? startPosition.hashCode() : 0);
        result = 31 * result + (endPosition != null ? endPosition.hashCode() : 0);
        return result;
    }
}
