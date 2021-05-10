package org.zfin.gwt.root.ui;


/*******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A SuggestBox that uses REST and allows for multiple values, autocomplete and browsing
 *
 * @author Bess Siegal <bsiegal@novell.com>
 */
public class MultiValueSuggestBox extends Composite implements SelectionHandler<Suggestion>, Focusable, KeyUpHandler
{
    private SuggestBox m_field;
    private Map<String, String> m_valueMap;
    private int m_indexFrom = 0;
    private int m_indexTo = 0;
    private int m_findExactMatchesTotal = 0;
    private int m_findExactMatchesFound = 0;
    private ArrayList<String> m_findExactMatchesNot = new ArrayList<String>();

    private static final String DISPLAY_SEPARATOR = ", ";
    private static final String VALUE_DELIM = ";";
    private static final int PAGE_Size = 15;
    private static final int DELAY = 1000;
    private static final int FIND_EXACT_MATCH_QUERY_LIMIT = 20;

    private FormFeedback m_feedback;
    private boolean m_isMultivalued = false;
    private String m_restEndpointUrl;

    /**
     * Constructor.
     * @param restEndpointUrl the URL for the REST endpoint.  This URL should accept the parameters q (for query), indexFrom and indexTo
     * @param isMultivalued - true for allowing multiple values
     */
    public MultiValueSuggestBox(String restEndpointUrl, boolean isMultivalued)
    {
        m_restEndpointUrl = restEndpointUrl;
        m_isMultivalued = isMultivalued;

        FlowPanel panel = new FlowPanel();
        TextBoxBase textfield;
        if (isMultivalued) {
            panel.addStyleName("textarearow");
            textfield = new TextArea();
        } else {
            panel.addStyleName("textfieldrow");
            textfield = new TextBox();
        }

        //Create our own SuggestOracle that queries REST endpoint
        SuggestOracle oracle = new RestSuggestOracle();
        //intialize the SuggestBox
        m_field = new SuggestBox(oracle, textfield);
        if (isMultivalued) {
            //have to do this here b/c gwt suggest box wipes
            //style name if added in previous if
            textfield.addStyleName("multivalue");
        }
        m_field.addStyleName("wideTextField");
        m_field.addSelectionHandler(this);
        m_field.addKeyUpHandler(this);

        panel.add(m_field);
        m_feedback = new FormFeedback();
        panel.add(m_feedback);

        initWidget(panel);

        /*
         * Create a Map that holds the values that should be stored.
         * It will be keyed on "display value", so that any time a "display value" is added or removed
         * the valueMap can be updated.
         */
        m_valueMap = new HashMap<String, String>();

        resetPageIndices();
    }


    private void resetPageIndices()
    {
        m_indexFrom = 0;
        m_indexTo = m_indexFrom + PAGE_Size - 1;
    }

    /**
     * Convenience method to set the status and tooltip of the FormFeedback
     * @param status - a FormFeedback status
     * @param tooltip - a String tooltip
     */
    public void updateFormFeedback(int status, String tooltip)
    {
        m_feedback.setStatus(status);
        if (tooltip != null) m_feedback.setTitle(tooltip);

        TextBoxBase textBox = m_field.getTextBox();
        if (FormFeedback.LOADING == status) {
            textBox.setEnabled(false);
        } else {
            textBox.setEnabled(true);
            textBox.setFocus(false); //Blur then focus b/c of a strange problem with the cursor or selection highlights no longer visible within the textfield (this is a workaround)
            textBox.setFocus(true);
        }
    }

    private void putValue(String key, String value)
    {
        System.out.println("putting key = " + key + "; value = " + value);
        m_valueMap.put(key, value);
    }

    /**
     * Get the value(s) as a String.  If allowing multivalues, separated by the VALUE_DELIM
     * @return value(s) as a String
     */
    public String getValue()
    {
        //String together all the values in the valueMap
        //based on the display values shown in the field
        String text = m_field.getText();

        String values = "";
        String invalids = "";
        String newKeys = "";
        if (m_isMultivalued) {
            String[] keys = text.split(DISPLAY_SEPARATOR);
            for (String key : keys) {
                key = key.trim();
                if (!key.isEmpty()) {
                    String v = m_valueMap.get(key);
                    System.out.println("getValue for key = " + key + " is v = " + v);
                    if (null != v) {
                        values += v + VALUE_DELIM;
                        //rebuild newKeys removing invalids and dups
                        newKeys += key + DISPLAY_SEPARATOR;
                    } else {
                        invalids += key + DISPLAY_SEPARATOR;
                    }
                }
            }
            values = trimLastDelimiter(values, VALUE_DELIM);
            //set the new display values
            m_field.setText(newKeys);
        } else {
            values = m_valueMap.get(text);
        }

        //if there were any invalid show warning
        if (!invalids.isEmpty()) {
            //trim last separator
            invalids = trimLastDelimiter(invalids, DISPLAY_SEPARATOR);
            updateFormFeedback(FormFeedback.ERROR, "Invalids: " + invalids);
        }
        return values;
    }

    /**
     * Get the value map
     * @return value map
     */
    public Map<String, String> getValueMap()
    {
        return m_valueMap;
    }
    /**
     * If there is more than one key in the text field,
     * check that every key has a value in the map.
     * For any that do not, try to find its exact match.
     */
    private void findExactMatches()
    {
        String text = m_field.getText();
        String[] keys = text.split(DISPLAY_SEPARATOR.trim());
        int len = keys.length;
        if (len < 2) {
            //do not continue.  if there's 1, it is the last one, and getSuggestions can handle it
            return;
        }

        m_findExactMatchesTotal = 0;
        m_findExactMatchesFound = 0;
        m_findExactMatchesNot.clear();
        for (int pos = 0; pos < len; pos++) {
            String key = keys[pos].trim();

            if (!key.isEmpty()) {
                String v = m_valueMap.get(key);
                if (null == v) {
                    m_findExactMatchesTotal++;
                }
            }
        }
        //then loop through again and try to find them
        /*
         * We may have invalid values due to a multi-value copy-n-paste,
         * or going back and messing with a middle or first key;
         * so for each invalid value, try to find an exact match.                     *
         */
        for (int pos = 0; pos < len; pos++) {
            String key = keys[pos].trim();
            if (!key.isEmpty()) {
                String v = m_valueMap.get(key);
                if (null == v) {
                    findExactMatch(key, pos);
                }
            }
        }
    }

    private void findExactMatch(final String displayValue, final int position)
    {
        updateFormFeedback(FormFeedback.LOADING, null);

        queryOptions(
                displayValue,
                0,
                FIND_EXACT_MATCH_QUERY_LIMIT, //return a relatively small amount in case wanted "Red" and "Brick Red" is the first thing returned
                new OptionQueryCallback() {

                    @Override
                    public void error(Throwable exception)
                    {
                        // an exact match couldn't be found, just increment not found
                        m_findExactMatchesNot.add(displayValue);
                        finalizeFindExactMatches();
                    }

                    @Override
                    public void success(OptionResultSet optResults)
                    {
                        int totSize = optResults.getTotalSize();
                        if (totSize == 1) {
                            //an exact match was found, so place it in the value map
                            Option option = optResults.getOptions()[0];
                            extactMatchFound(position, option);
                        } else {
                            //try to find the exact matches within the results
                            boolean found = false;
                            for (Option option : optResults.getOptions()) {
                                if (displayValue.equalsIgnoreCase(option.getName())) {
                                    extactMatchFound(position, option);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                m_findExactMatchesNot.add(displayValue);
                                System.out.println("RestExactMatchCallback -- exact match not found for displ = " + displayValue);
                            }
                        }
                        finalizeFindExactMatches();
                    }

                    private void extactMatchFound(final int position, Option option)
                    {
                        putValue(option.getName(), option.getValue());
                        System.out.println("extactMatchFound ! exact match found for displ = " + displayValue);

                        //and replace the text
                        String text = m_field.getText();
                        String[] keys = text.split(DISPLAY_SEPARATOR.trim());
                        keys[position] = option.getName();
                        String join = "";
                        for (String n : keys) {
                            join += n.trim() + DISPLAY_SEPARATOR;
                        }
                        join = trimLastDelimiter(join, DISPLAY_SEPARATOR);
                        m_field.setText(join);

                        m_findExactMatchesFound++;
                    }

                    private void finalizeFindExactMatches()
                    {
                        if (m_findExactMatchesFound + m_findExactMatchesNot.size() == m_findExactMatchesTotal) {
                            //when the found + not = total, we're done
                            if (m_findExactMatchesNot.size() > 0) {
                                String join = "";
                                for (String val : m_findExactMatchesNot) {
                                    join += val.trim() + DISPLAY_SEPARATOR;
                                }
                                join = trimLastDelimiter(join, DISPLAY_SEPARATOR);
                                updateFormFeedback(FormFeedback.ERROR, "Invalid:" + join);
                            } else {
                                updateFormFeedback(FormFeedback.VALID, null);
                            }
                        }
                    }
                });
    }


    /**
     * Returns a String without the last delimiter
     * @param s - String to trim
     * @param delim - the delimiter
     * @return the String without the last delimter
     */
    private static String trimLastDelimiter(String s, String delim)
    {
        if (s.length() > 0) {
            s = s.substring(0, s.length() - delim.length());
        }
        return s;
    }


    @Override
    public void onSelection(SelectionEvent<Suggestion> event)
    {
        Suggestion suggestion = event.getSelectedItem();
        if (suggestion instanceof OptionSuggestion) {
            OptionSuggestion osugg = (OptionSuggestion) suggestion;
            //if NEXT or PREVIOUS were selected, requery but bypass the timer
            String value = osugg.getValue();
            if (OptionSuggestion.NEXT_VALUE.equals(value)) {
                m_indexFrom += PAGE_Size;
                m_indexTo += PAGE_Size;

                RestSuggestOracle oracle = (RestSuggestOracle) m_field.getSuggestOracle();
                oracle.getSuggestions();

            } else if (OptionSuggestion.PREVIOUS_VALUE.equals(value)) {
                m_indexFrom -= PAGE_Size;
                m_indexTo -= PAGE_Size;

                RestSuggestOracle oracle = (RestSuggestOracle) m_field.getSuggestOracle();
                oracle.getSuggestions();

            } else {
                //made a valid selection
                updateFormFeedback(FormFeedback.VALID, null);

                //add the option's value to the value map
                putValue(osugg.getName(), value);

                //put the focus back into the textfield so user
                //can enter more
                m_field.setFocus(true);
            }
        }
    }

    private String getFullReplaceText(String displ, String replacePre)
    {
        //replace the last bit after the last comma
        if (replacePre.lastIndexOf(DISPLAY_SEPARATOR) > 0) {
            replacePre = replacePre.substring(0, replacePre.lastIndexOf(DISPLAY_SEPARATOR)) + DISPLAY_SEPARATOR;
        } else {
            replacePre = "";
        }
        //then add a comma
        if (m_isMultivalued) {
            return replacePre + displ + DISPLAY_SEPARATOR;
        } else {
            return displ;
        }
    }


    @Override
    public int getTabIndex()
    {
        return m_field.getTabIndex();
    }


    @Override
    public void setAccessKey(char key)
    {
        m_field.setAccessKey(key);
    }


    @Override
    public void setFocus(boolean focused)
    {
        m_field.setFocus(focused);
    }


    @Override
    public void setTabIndex(int index)
    {
        m_field.setTabIndex(index);
    }

    @Override
    public void onKeyUp(KeyUpEvent event)
    {
        /*
         * Because SuggestOracle.requestSuggestions does not get called when the text field is empty
         * this key up handler is necessary for handling the case when there is an empty text field...
         * Here, the FormFeedback is reset.
         */
        updateFormFeedback(FormFeedback.NONE, null);
    }


    /**
     * Retrieve Options (name-value pairs) that are suggested from the REST endpoint
     * @param query - the String search term
     * @param from - the 0-based begin index int
     * @param to - the end index inclusive int
     * @param callback - the OptionQueryCallback to handle the response
     */
    private void queryOptions(final String query, final int from, final int to, final OptionQueryCallback callback)
    {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(m_restEndpointUrl + "?q=" + query + "&indexFrom=" + from + "&indexTo=" + to));

        // Set our headers
        builder.setHeader("Accept", "application/json");
        builder.setHeader("Accept-Charset", "UTF-8");

        builder.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(com.google.gwt.http.client.Request request, Response response)
            {
                JSONValue val = JSONParser.parse(response.getText());
                JSONObject obj = val.isObject();
                int totSize = (int) obj.get(OptionResultSet.TOTAL_SIZE).isNumber().doubleValue();
                OptionResultSet options = new OptionResultSet(totSize);
                JSONArray optionsArray = obj.get(OptionResultSet.OPTIONS).isArray();

                if (options.getTotalSize() > 0 && optionsArray != null) {

                    for (int i = 0; i < optionsArray.size(); i++) {
                        if (optionsArray.get(i) == null) {
                            /*
                             This happens when a JSON array has an invalid trailing comma
                             */
                            continue;
                        }

                        JSONObject jsonOpt = optionsArray.get(i).isObject();
                        Option option = new Option();
                        option.setName(jsonOpt.get(OptionResultSet.DISPLAY_NAME).isString().stringValue());
                        option.setValue(jsonOpt.get(OptionResultSet.VALUE).isString().stringValue());
                        options.addOption(option);
                    }
                }
                callback.success(options);
            }

            @Override
            public void onError(com.google.gwt.http.client.Request request, Throwable exception)
            {
                callback.error(exception);
            }
        });

        try {
            builder.send();
        } catch (RequestException e) {
            updateFormFeedback(FormFeedback.ERROR, "Error: " + e.getMessage());
        }
    }

    /*
     * Some custom inner classes for our SuggestOracle
     */
    /**
     * A custom Suggest Oracle
     */
    private class RestSuggestOracle extends SuggestOracle
    {
        private SuggestOracle.Request m_request;
        private SuggestOracle.Callback m_callback;
        private Timer m_timer;

        RestSuggestOracle()
        {
            m_timer = new Timer() {

                @Override
                public void run()
                {
                    /*
                     * The reason we check for empty string is found at
                     * http://development.lombardi.com/?p=39 --
                     * paraphrased, if you backspace quickly the contents of the field are emptied but a query for a single character is still executed.
                     * Workaround for this is to check for an empty string field here.
                     */

                    if (!m_field.getText().trim().isEmpty()) {
                        if (m_isMultivalued) {
                            //calling this here in case a user is trying to correct the "kev" value of Allison Andrews, Kev, Josh Nolan or pasted in multiple values
                            findExactMatches();
                        }
                        getSuggestions();
                    }
                }
            };
        }

        @Override
        public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback)
        {
            //This is the method that gets called by the SuggestBox whenever some types into the text field
            m_request = request;
            m_callback = callback;

            //reset the indexes (b/c NEXT and PREV call getSuggestions directly)
            resetPageIndices();

            //If the user keeps triggering this event (e.g., keeps typing), cancel and restart the timer
            m_timer.cancel();
            m_timer.schedule(DELAY);
        }

        private void getSuggestions()
        {
            String query = m_request.getQuery();

            //find the last thing entered up to the last separator
            //and use that as the query
            if (m_isMultivalued) {
                int sep = query.lastIndexOf(DISPLAY_SEPARATOR);
                if (sep > 0) {
                    query = query.substring(sep + DISPLAY_SEPARATOR.length());
                }
            }
            query = query.trim();

            //do not query if it's just an empty String
            //also do not get suggestions you've already got an exact match for this string in the m_valueMap
            if (query.length() > 0 && m_valueMap.get(query) == null) {
                //JSUtil.println("getting Suggestions for: " + query);
                updateFormFeedback(FormFeedback.LOADING, null);

                queryOptions(
                        query,
                        m_indexFrom,
                        m_indexTo,
                        new RestSuggestCallback(m_request, m_callback, query));
            }
        }


        @Override
        public boolean isDisplayStringHTML()
        {
            return true;
        }
    }

    /**
     * A custom callback that has the original SuggestOracle.Request and SuggestOracle.Callback
     */
    private class RestSuggestCallback extends OptionQueryCallback
    {
        private SuggestOracle.Request m_request;
        private SuggestOracle.Callback m_callback;
        private String m_query; //this may be different from m_request.getQuery when multivalued it's only the substring after the last delimiter

        RestSuggestCallback(Request request, Callback callback, String query)
        {
            m_request = request;
            m_callback = callback;
            m_query = query;
        }

        public void success(OptionResultSet optResults)
        {
            SuggestOracle.Response resp = new SuggestOracle.Response();
            List<OptionSuggestion> suggs = new ArrayList<OptionSuggestion>();
            int totSize = optResults.getTotalSize();

            if (totSize < 1) {
                //if there were no suggestions, then it's an invalid value
                updateFormFeedback(FormFeedback.ERROR, "Invalid: " + m_query);

            } else if (totSize == 1) {
                //it's an exact match, so do not bother with showing suggestions,
                Option o = optResults.getOptions()[0];
                String displ = o.getName();

                //remove the last bit up to separator
                m_field.setText(getFullReplaceText(displ, m_request.getQuery()));

                System.out.println("RestSuggestCallback.success! exact match found for displ = " + displ);

                //it's valid!
                updateFormFeedback(FormFeedback.VALID, null);

                //set the value into the valueMap
                putValue(displ, o.getValue());

            } else {
                //more than 1 so show the suggestions

                //if not at the first page, show PREVIOUS
                if (m_indexFrom > 0) {
                    OptionSuggestion prev = new OptionSuggestion(OptionSuggestion.PREVIOUS_VALUE, m_request.getQuery());
                    suggs.add(prev);
                }

                // show the suggestions
                for (Option o : optResults.getOptions()) {
                    OptionSuggestion sugg = new OptionSuggestion(o.getName(), o.getValue(), m_request.getQuery(), m_query);
                    suggs.add(sugg);
                }

                //if there are more pages, show NEXT
                if (m_indexTo < totSize) {
                    OptionSuggestion next = new OptionSuggestion(OptionSuggestion.NEXT_VALUE, m_request.getQuery());
                    suggs.add(next);
                }

                //nothing has been picked yet, so let the feedback show an error (unsaveable)
                updateFormFeedback(FormFeedback.ERROR, "Invalid: " + m_query);
            }

            //it's ok (and good) to pass an empty suggestion list back to the suggest box's callback method
            //the list is not shown at all if the list is empty.
            resp.setSuggestions(suggs);
            m_callback.onSuggestionsReady(m_request, resp);
        }

        @Override
        public void error(Throwable exception)
        {
            updateFormFeedback(FormFeedback.ERROR, "Invalid: " + m_query);
        }

    }

    /**
     * A bean to serve as a custom suggestion so that the value is available and the replace
     * will look like it is supporting multivalues
     */
    private class OptionSuggestion implements SuggestOracle.Suggestion
    {
        private String m_display;
        private String m_replace;
        private String m_value;
        private String m_name;

        static final String NEXT_VALUE = "NEXT";
        static final String PREVIOUS_VALUE = "PREVIOUS";

        /**
         * Constructor for navigation options
         * @param nav - next or previous value
         * @param currentTextValue - the current contents of the text box
         */
        OptionSuggestion(String nav, String currentTextValue)
        {
            if (NEXT_VALUE.equals(nav)) {
                m_display = "<div class=\"autocompleterNext\" title=\"Next\"></div>";
            } else {
                m_display = "<div class=\"autocompleterPrev\" title=\"Previous\"></div>";
            }
            m_replace = currentTextValue;
            m_value = nav;
        }

        /**
         * Constructor for regular options
         * @param displ - the name of the option
         * @param val - the value of the option
         * @param replacePre - the current contents of the text box
         * @param query - the query
         */
        OptionSuggestion(String displ, String val, String replacePre, String query)
        {
            m_name = displ;
            int begin = displ.toLowerCase().indexOf(query.toLowerCase());
            if (begin >= 0) {
                int end = begin + query.length();
                String match = displ.substring(begin, end);
                m_display = displ.replaceFirst(match, "<b>" + match + "</b>");
            } else {
                //may not necessarily be a part of the query, for example if "*" was typed.
                m_display = displ;
            }
            m_replace = getFullReplaceText(displ, replacePre);
            m_value = val;
        }

        @Override
        public String getDisplayString()
        {
            return m_display;
        }

        @Override
        public String getReplacementString()
        {
            return m_replace;
        }

        /**
         * Get the value of the option
         * @return value
         */
        public String getValue()
        {
            return m_value;
        }

        /**
         * Get the name of the option.
         * (when not multivalued, this will be the same as getReplacementString)
         * @return name
         */
        public String getName()
        {
            return m_name;
        }
    }

    /**
     * An abstract class that handles success and error conditions from the REST call
     */
    private abstract class OptionQueryCallback
    {
        abstract void success(OptionResultSet optResults);
        abstract void error(Throwable exception);
    }

    /**
     * Bean for name-value pairs
     */
    private class Option
    {

        private String m_name;
        private String m_value;

        /**
         * No argument constructor
         */
        public Option()
        {
        }
        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return m_name;
        }
        /**
         * @param name The name to set.
         */
        public void setName(String name)
        {
            m_name = name;
        }
        /**
         * @return Returns the value.
         */
        public String getValue()
        {
            return m_value;
        }
        /**
         * @param value The value to set.
         */
        public void setValue(String value)
        {
            m_value = value;
        }


    }

    /**
     * Bean for total size and options
     */
    private class OptionResultSet
    {
        /** JSON key for Options */
        public static final String OPTIONS = "Options";
        /** JSON key for DisplayName */
        public static final String DISPLAY_NAME = "DisplayName";
        /** JSON key for Value */
        public static final String VALUE = "Value";

        /** JSON key for the size of the Results */
        public static final String TOTAL_SIZE = "TotalSize";

        private final List<Option> m_options = new ArrayList<Option>();
        private int m_totalSize;


        /**
         * Constructor.  Must pass in the total size.
         * @param totalSize the total size of the template
         */
        public OptionResultSet(int totalSize)
        {
            setTotalSize(totalSize);
        }

        /**
         * Add an option
         * @param option - the Option to add
         */
        public void addOption(Option option)
        {
            m_options.add(option);
        }

        /**
         * @return an array of Options
         */
        public Option[] getOptions()
        {
            return m_options.toArray(new Option[m_options.size()]);
        }

        /**
         * @param totalSize The totalSize to set.
         */
        public void setTotalSize(int totalSize)
        {
            m_totalSize = totalSize;
        }

        /**
         * @return Returns the totalSize.
         */
        public int getTotalSize()
        {
            return m_totalSize;
        }
    }

}

