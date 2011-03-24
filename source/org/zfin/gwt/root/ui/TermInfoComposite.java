package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.*;

/**
 * Main composite for holding the Term Info.
 */
public class TermInfoComposite extends FlexTable implements ValueChangeHandler<String> {

    private boolean usedHyperlinkClickListener;
    private Map<String, TermDTO> historyMap = new HashMap<String, TermDTO>();
    private TermDTO currentTermInfoDTO;
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

    public void updateTermInfo(TermDTO termInfoDTO, String historyToken) {
        historyMap.put(historyToken, termInfoDTO);
        updateTermInfo(termInfoDTO);
    }

    private void updateTermInfo(TermDTO termInfoDTO) {
        clear();
        currentTermInfoDTO = termInfoDTO;
        int rowIndex = 0;
        int headerColumn = 0;
        int dataColumn = 1;
        addHeaderEntry(TerminfoTableHeader.TERM.getName(), rowIndex);
        setWidget(rowIndex, dataColumn, new Label(termInfoDTO.getName()));
        getCellFormatter().addStyleName(rowIndex++, headerColumn, WidgetUtil.BOLD);

        addHeaderEntry(TerminfoTableHeader.ID.getName(), rowIndex);
        String idDisplay = termInfoDTO.getOboID();
        idDisplay += " [";
        idDisplay += termInfoDTO.getZdbID();
        idDisplay += "]";
        setWidget(rowIndex++, dataColumn, new Label(idDisplay));

        if (termInfoDTO.getAliases() != null && termInfoDTO.getAliases().size() > 0) {
            addHeaderEntry(TerminfoTableHeader.SYNONYMS.getName(), rowIndex);
            StringBuilder builder = new StringBuilder();
            List<String> synonyms = new ArrayList<String>(termInfoDTO.getAliases());
            for (int i = 0; i < synonyms.size(); i++) {
                builder.append(synonyms.get(i));
                if (i < synonyms.size() - 1) {
                    builder.append(divider);
                }
            }
            setWidget(rowIndex++, 1, new HTML(builder.toString()));
        }

        if (StringUtils.isNotEmpty(termInfoDTO.getDefinition())) {
            addHeaderEntry(TerminfoTableHeader.DEFINITION.getName(), rowIndex);
            setWidget(rowIndex++, 1, new HTML(termInfoDTO.getDefinition()));
        }

        Map<String, Set<TermDTO>> relatedTermsMap = termInfoDTO.getAllRelatedTerms();
        if (relatedTermsMap != null) {
            for (String type : relatedTermsMap.keySet()) {
                if(false==RelationshipType.isStage(type)){
                    rowIndex = createTermEntry(type, rowIndex,relatedTermsMap);
                }
            }
            rowIndex = createTermEntry(RelationshipType.START_STAGE.getDisplay(),rowIndex,relatedTermsMap);
            rowIndex = createTermEntry(RelationshipType.END_STAGE.getDisplay(),rowIndex,relatedTermsMap);
        }
        // subsets
        if (currentTermInfoDTO.getSubsets() != null && currentTermInfoDTO.getSubsets().size() > 0) {
            addHeaderEntry(TerminfoTableHeader.SUBSETS.getName(), rowIndex);
            StringBuilder builder = new StringBuilder();
            Set<String> subsets = currentTermInfoDTO.getSubsets();
            Iterator<String> iterator = subsets.iterator();
            int size = 1;
            while (iterator.hasNext()) {
                builder.append(iterator.next());
                if (size < subsets.size()) {
                    builder.append(divider);
                }
                size++;
            }
            setWidget(rowIndex++, 1, new HTML(builder.toString()));
        }
        // comments
        addHeaderEntry(TerminfoTableHeader.COMMENT.getName(), rowIndex);
        if (noWrap) {
            getCellFormatter().addStyleName(rowIndex, headerColumn, WidgetUtil.NO_WRAP);
        }
        setWidget(rowIndex++, 1, new Label(termInfoDTO.getComment()));
        // Obsolete
        if (termInfoDTO.isObsolete()) {
            addHeaderEntry(TerminfoTableHeader.OBSOLETE.getName(), rowIndex);
            if (noWrap) {
                getCellFormatter().addStyleName(rowIndex, headerColumn, WidgetUtil.NO_WRAP);
            }
            Label label = new Label("True");
            label.setStyleName(WidgetUtil.RED);
            setWidget(rowIndex++, 1, label);
        }
    }

    private int createTermEntry(String type, int rowIndex, Map<String, Set<TermDTO>> relatedTermsMap){

        Set<TermDTO> relatedTermDTOs = relatedTermsMap.get(type);
        if(relatedTermDTOs==null) {
            return rowIndex ;
        }

        FlowPanel panel = new FlowPanel();
        addHeaderEntry(type, rowIndex);
//                Collections.sort(relatedTermDTOs);
//        for (int i = 0; i < relatedTermDTOs.size(); i++) {
//            panel.add(createHyperlink(relatedTermDTOs.get(i)));
//            if (i < relatedTermDTOs.size() - 1) {
//                panel.add(new HTML(divider));
//            }
//        }

        TermDTO termDTO ;
        for ( Iterator<TermDTO> iterator = relatedTermDTOs.iterator() ;
              iterator.hasNext() ;
                ) {
            termDTO = iterator.next();
            panel.add(createHyperlink(termDTO));
            if (iterator.hasNext()) {
                panel.add(new HTML(divider));
            }
        }
        setWidget(rowIndex, 1, panel);

        return rowIndex +1 ;
    }


    private Hyperlink createHyperlink(TermDTO infoDTO) {
        Hyperlink link = new Hyperlink(infoDTO.getName(), infoDTO.getZdbID());
        if (noWrap) {
            link.addStyleName(WidgetUtil.NO_WRAP);
        }
        link.addClickHandler(new TermInfoClickListener(infoDTO, this));
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
        TermDTO infoDTO = historyMap.get(historyToken);
        if (infoDTO == null)
            return;
        if (!usedHyperlinkClickListener)
            updateTermInfo(infoDTO);
        usedHyperlinkClickListener = false;
    }

    public TermDTO getCurrentTermInfoDTO() {
        return currentTermInfoDTO;
    }

    public void addErrorHandler(ErrorHandler errorHandler) {
        errorElement.addErrorHandler(errorHandler);
    }

    // ***** Click Handler

    private class TermInfoClickListener implements ClickHandler {

        private TermDTO termInfoDTO;
        private TermInfoComposite termInfoComposite;

        private TermInfoClickListener(TermDTO infoDTO, TermInfoComposite termInfoComposite) {
            this.termInfoDTO = infoDTO;
            this.termInfoComposite = termInfoComposite;
        }

        public void onClick(ClickEvent event) {
            lookupRPC.getTermInfo(termInfoDTO.getOntology(), termInfoDTO.getZdbID(), new TermInfoCallBack(termInfoComposite, termInfoDTO.getZdbID()));
            setUsedHyperlinkClickListener(true);
            errorElement.clearAllErrors();
        }

    }


    public enum TerminfoTableHeader implements TableHeader {
        TERM(0, "TERM:"),
        ID(1, "OBO ID [ID]:"),
        SYNONYMS(2, "Synonyms:"),
        DEFINITION(3, "Definition:"),
        PARENTS(4, "PARENTS:"),
        CHILDREN(5, "CHILDREN:"),
        SUBSETS(6, "Subsets:"),
        COMMENT(8, "Comments:"),
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
