package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that is called to produce Pagination = walking windows.
 */
@Controller
public class PaginationController {

    @RequestMapping("/pagination")
    protected String showLookupTestPage(@ModelAttribute("formBean") ApgPaginationBean bean) throws Exception {
        // apg call
        if (bean.getFirstPageRecord() > 0)
            if (bean.getPage() == null)
                bean.setPage("1");

        if (bean.getMaxDisplayRecordsInteger() > 1)
            bean.setPageInteger(bean.getFirstPageRecord() / bean.getMaxDisplayRecordsInteger() + 1);
        else
            bean.setPageInteger(bean.getFirstPageRecord());
        return "pagination.page";
    }
}
