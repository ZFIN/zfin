package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.GregorianCalendar;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicExpressionDTO {


    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned = new GregorianCalendar();

    @JsonIgnore
    private String expressionResultId;
    private String geneId;
    private PublicationAgrDTO evidence;
    private CrossReferenceDTO crossReference;
    private String assay;
    private DataProviderDTO dataProvider;
    private ExpressionStageIdentifiersDTO whenExpressed;
    private ExpressionTermIdentifiersDTO whereExpressed;

}
