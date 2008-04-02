package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;

/**
 * The structure of this SuggestBox is used in order to capture the extra "Enter" event.
 * As this uses a "GET" encoding we can be a bit more
 */
public class AnatomyLookup extends ErrorAssociable {

    HorizontalPanel lookupPanel = new HorizontalPanel();
    ItemSuggestOracle oracle = new ItemSuggestOracle(this);
    TextBox textBox = new TextBox();
    SuggestBox suggestBox;
    SuggestOracle.Suggestion suggestion = null;
    Button submitButton = new Button("Search");
    FormPanel formPanel = new FormPanel();

    String currentText = null;


    public void onModuleLoad() {

        rootPanel.add(formPanel);
        formPanel.setWidget(lookupPanel);

        textBox.setName("searchTerm");
        textBox.setTitle("searchTerm");
        textBox.setVisibleLength(30);
        DOM.setElementAttribute(textBox.getElement(), "autocomplete", "off");
        suggestBox = new SuggestBox(oracle, textBox);
        suggestBox.setLimit(60);
        suggestBox.addEventHandler(new SuggestionHandler() {
            public void onSuggestionSelected(SuggestionEvent event) {
                suggestion = event.getSelectedSuggestion();
//                Window.alert("suggest selected" +
//                        " replace["+ suggestion.getReplacementString()+"] " +
//                        " display["+suggestion.getDisplayString()+"] " +
//                        " currentText["+currentText+"] " +
//                        " suggestBox["+suggestBox.getText()+"] " +
//                        " textBox["+textBox.getText()+"]") ;
                if (suggestion.getReplacementString() == null) {
                    suggestBox.setText(currentText);
                    submitSearch(currentText);
                } else if (suggestion.getReplacementString() != null) {
                    submitSearch(suggestion.getReplacementString());
                }
            }
        });



        suggestBox.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget w, char c, int i) {
                if (textBox.getText() != null & textBox.getText().length() > 0) {
                    currentText = textBox.getText()  ;
                    if( Character.isLetterOrDigit(c)){
                       currentText += c ;  
                    }
//                    Window.alert("keypress on w: "+ w.getTitle()+ " c: "+c+" " +
//                            "currentText["+currentText+"] " +
//                            "textBox["+textBox.getText()+"] " +
//                            "suggestBox["+suggestBox.getText()+"]") ;
                }
            }

        });


        formPanel.addFormHandler(new FormHandler() {

            public void onSubmitComplete(FormSubmitCompleteEvent event) {
//                Window.alert("error");
            }

            public void onSubmit(FormSubmitEvent event) {
//                Window.alert("onsubmit" +
//                        " replace["+ suggestion.getReplacementString()+"] " +
//                        " display["+suggestion.getDisplayString()+"] " +
//                        " currentText["+currentText+"] " +
//                        " suggestBox["+suggestBox.getText()+"] " +
//                        " textBox["+textBox.getText()+"]") ;
                if (suggestion.getReplacementString() != null) {
                    submitSearch(suggestion.getReplacementString());
                } else if (suggestion.getReplacementString() == null && currentText != null) {
                    submitSearch(currentText);
                }
            }
        });

        submitButton.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                submitSearch(textBox.getText());
            }
        });


        lookupPanel.add(suggestBox);
        lookupPanel.add(submitButton);
        loadErrorPanel();
        RootPanel.get("anatomyTerm").add(rootPanel);
        textBox.setFocus(true);
    }

    public void setText(String newText){
        textBox.setText(newText);
    }

    private void submitSearch(String text) {
        if (text != null) {
            Window.open("/action/anatomy/search?action=term-search&searchTerm=" + text.replaceAll(" ", "%20"), "_self",
                    "");
        }
    }


}
