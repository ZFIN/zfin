package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermInfo;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main composite for holding the Term Info.
 */
public class TermInfoComposite extends FlexTable implements ValueChangeHandler<String> {

    private boolean usedHyperlinkClickListener;
    private Map<String, TermInfo> historyMap = new HashMap<String, TermInfo>(5);
    private TermInfo currentTermInfo;
    private ErrorHandler errorElement = new SimpleErrorElement();
    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    private final String DEFAULT_DIVIDER = "&nbsp;&bull;&nbsp;";
    private String divider = DEFAULT_DIVIDER;
    private boolean noWrap = true;

    public TermInfoComposite() {
        super();
        setDefaultTermInfo();
        setHandler();
    }

    public TermInfoComposite(boolean showDefaultRootOntologyTerm) {
        super();
        if (showDefaultRootOntologyTerm)
            setDefaultTermInfo();
        setHandler();
    }

    public TermInfoComposite(boolean showDefaultRootOntologyTerm, String divider, boolean noWrap) {
        this(showDefaultRootOntologyTerm);
        this.divider = divider;
        this.noWrap = noWrap;
    }

    private void setHandler() {
        History.addValueChangeHandler(this);
    }

    private void setDefaultTermInfo() {
        String defaultAOTermID = "ZFA:0000037";
        lookupRPC.getTermInfo(OntologyDTO.ANATOMY, defaultAOTermID, new TermInfoCallBack(this, defaultAOTermID));
    }

    public void setToDefault() {
        setDefaultTermInfo();
    }

    public void updateTermInfo(TermInfo termInfo, String historyToken) {
        historyMap.put(historyToken, termInfo);
        updateTermInfo(termInfo);
    }

    private void updateTermInfo(TermInfo termInfo) {
        clear();
        currentTermInfo = termInfo;
        int rowIndex = 0;
        int headerColumn = 0;
        int dataColumn = 1;
        addHeaderEntry(TerminfoTableHeader.TERM.getName(), rowIndex);
        setWidget(rowIndex, dataColumn, new Label(termInfo.getName()));
        getCellFormatter().addStyleName(rowIndex++, headerColumn, WidgetUtil.BOLD);

        addHeaderEntry(TerminfoTableHeader.ID.getName(), rowIndex);
        setWidget(rowIndex++, dataColumn, new Label(termInfo.getID()));

        if (termInfo.getSynonyms() != null && termInfo.getSynonyms().size() > 0) {
            addHeaderEntry(TerminfoTableHeader.SYNONYMS.getName(), rowIndex);
            StringBuilder builder = new StringBuilder();
            List<String> synonyms = termInfo.getSynonyms();
            if (synonyms != null) {
                for (int i = 0; i < synonyms.size(); i++) {
                    builder.append(synonyms.get(i));
                    if (i < synonyms.size() - 1) {
                        builder.append(divider);
                    }
                }
            }
            setWidget(rowIndex++, 1, new HTML(builder.toString()));
        }

        if (StringUtils.isNotEmpty(termInfo.getDefinition())) {
            addHeaderEntry(TerminfoTableHeader.DEFINITION.getName(), rowIndex);
            setWidget(rowIndex++, 1, new HTML(termInfo.getDefinition()));
        }

        Map<String, List<TermInfo>> relatedTermsMap = termInfo.getRelatedTermInfos();
        if (relatedTermsMap != null) {
            for (String type : relatedTermsMap.keySet()) {
                FlowPanel panel = new FlowPanel();
                addHeaderEntry(type, rowIndex);
                List<TermInfo> relatedTerms = relatedTermsMap.get(type);
                for (int i = 0; i < relatedTerms.size(); i++) {
                    panel.add(createHyperlink(relatedTerms.get(i)));
                    if (i < relatedTerms.size() - 1) {
                        panel.add(new HTML(divider));
                    }
                }
                setWidget(rowIndex++, 1, panel);
            }
        }
        // comments
        addHeaderEntry(TerminfoTableHeader.COMMENT.getName(), rowIndex);
        if (noWrap) {
            getCellFormatter().addStyleName(rowIndex, headerColumn, WidgetUtil.NO_WRAP);
        }
        setWidget(rowIndex++, 1, new Label(termInfo.getComment()));
        // Obsolete
        if (termInfo.isObsolete()) {
            addHeaderEntry(TerminfoTableHeader.OBSOLETE.getName(), rowIndex);
            if (noWrap) {
                getCellFormatter().addStyleName(rowIndex, headerColumn, WidgetUtil.NO_WRAP);
            }
            Label label = new Label("True");
            label.setStyleName(WidgetUtil.RED);
            setWidget(rowIndex++, 1, label);
        }
    }

    private Hyperlink createHyperlink(TermInfo info) {
        Hyperlink link = new Hyperlink(info.getName(), info.getID());
        if (noWrap) {
            link.addStyleName(WidgetUtil.NO_WRAP);
        }
        link.addClickHandler(new TermInfoClickListener(info, this));
        return link;
    }

    private void addHeaderEntry(String name, int rowIndex) {
        setWidget(rowIndex, 0, new Label(name));
        getCellFormatter().addStyleName(rowIndex, 0, "bold left-top-aligned nowrap");
    }

    public void setUsedHyperlinkClickListener(boolean usedHyperlinkClickListener) {
        this.usedHyperlinkClickListener = usedHyperlinkClickListener;
    }

    public void onValueChange(ValueChangeEvent event) {
        String historyToken = (String) event.getValue();
        //Window.alert("value "+historyToken);
        TermInfo info = historyMap.get(historyToken);
        if (info == null)
            return;
        if (!usedHyperlinkClickListener)
            updateTermInfo(info);
        usedHyperlinkClickListener = false;
    }

    public TermInfo getCurrentTermInfo() {
        return currentTermInfo;
    }

    public void addErrorHandler(ErrorHandler errorHandler) {
        errorElement.addErrorHandler(errorHandler);
    }

    // ***** Click Handler

    private class TermInfoClickListener implements ClickHandler {

        private TermInfo termInfo;
        private TermInfoComposite termInfoComposite;

        private TermInfoClickListener(TermInfo info, TermInfoComposite termInfoComposite) {
            this.termInfo = info;
            this.termInfoComposite = termInfoComposite;
        }

        public void onClick(ClickEvent event) {
            lookupRPC.getTermInfo(termInfo.getOntology(), termInfo.getID(), new TermInfoCallBack(termInfoComposite, termInfo.getID()));
            setUsedHyperlinkClickListener(true);
            errorElement.clearAllErrors();
        }

    }


    public enum TerminfoTableHeader implements TableHeader {
        TERM(0, "TERM:"),
        ID(1, "ID:"),
        SYNONYMS(2, "Synonyms:"),
        DEFINITION(3, "Definition:"),
        PARENTS(4, "PARENTS:"),
        CHILDREN(5, "CHILDREN:"),
        COMMENT(8, "COMMENTS:"),
        OBSOLETE(9, "Obsolete:");

        private int index;
        private String value;

        private TerminfoTableHeader(int index, String value) {
            this.index = index;
            this.value = value;
        }

        public String getName() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public static TerminfoTableHeader getHeaderName(String name) {
            for (TerminfoTableHeader info : TerminfoTableHeader.values()) {
                if (info.getName().equals(name))
                    return info;
            }
            return null;
        }

        public static TerminfoTableHeader[] getHeaderNames() {
            return TerminfoTableHeader.values();
        }

    }
}
