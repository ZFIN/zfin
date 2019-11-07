package org.zfin.agr;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.marker.agr.AllExpressionDTO;
import org.zfin.marker.agr.BasicExpressionInfo;

@RestController
@RequestMapping("/api")
public class DataController {

    @RequestMapping(value = "/expression")
    public AllExpressionDTO getFirstExpression() {
        BasicExpressionInfo info = new BasicExpressionInfo(5);
        AllExpressionDTO basicExpressionDTO = info.getBasicExpressionInfo(5);
        return basicExpressionDTO;
    }



}
