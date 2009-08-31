package org.zfin.curation.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Hyperlink;

public class ZfinHyperlinkWithEventCancelBubble extends Hyperlink {

    private ClickListenerCollection clickListener = new ClickListenerCollection();

    public ZfinHyperlinkWithEventCancelBubble(String name, String say) {
        super(name, say);
        sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {
            case Event.ONCLICK:
                clickListener.fireClick(null);
                DOM.eventCancelBubble(event, true);
        }
    }

    public void addClickListener(ClickListener listener) {
        clickListener.add(listener);
    }
}
