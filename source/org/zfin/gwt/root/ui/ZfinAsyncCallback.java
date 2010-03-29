package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.util.WidgetUtil;

/**
 * Basic Callback class that implements the onFailure() method that
 * can be inherited in most cases.
 * The constructor expects an error message in case somethings goes wrong and
 * second the GWT label that is used to show error messages.
 */
public class ZfinAsyncCallback<T> implements AsyncCallback<T> {

    private String message;
    private ErrorHandler errorHandler;
    private RootPanel loadingPanel;

    private static final String LOGIN_REQUIRED = "login required";

    public ZfinAsyncCallback(String errorMessage, ErrorHandler errorLabel, String loadingImageDivName) {
        this.message = errorMessage;
        this.errorHandler = errorLabel;
        loadingPanel = RootPanel.get(loadingImageDivName);
        if (loadingPanel != null) {
            Widget widget = getImageWidget();
            Image loadingImage;
            if (widget == null) {
                loadingImage = new Image();
                loadingPanel.add(loadingImage);
                loadingImage.setUrl(WidgetUtil.AJAX_LOADER_GIF);
            } else {
                loadingImage = (Image) widget;
            }
            loadingImage.setVisible(true);
        }
    }

    private Widget getImageWidget() {
        int numOfWidgets = loadingPanel.getWidgetCount();
        if (numOfWidgets == 0)
            return null;
        return loadingPanel.getWidget(0);
    }

    public ZfinAsyncCallback(String errorMessage, ErrorHandler errorLabel) {
        this.message = errorMessage;
        this.errorHandler = errorLabel;
    }

    public void onFailure(Throwable throwable) {
        onFailureCleanup();
        if (handleConnectionError(throwable)) return;
        if (handleDuplicateRecords(throwable)) return;
        if (checkLogin(throwable)) return;
        displayMessage(message + throwable);
        if (loadingPanel != null)
            getImageWidget().setVisible(false);
    }

    public void onSuccess(T t) {
        if (loadingPanel != null)
            getImageWidget().setVisible(false);
    }

    private boolean handleDuplicateRecords(Throwable throwable) {
        String errorMessage = throwable.getMessage();
        if (throwable instanceof Exception && errorMessage != null && !errorMessage.equals(LOGIN_REQUIRED)) {
            displayMessage(throwable.getMessage());
            return true;
        }
        return false;
    }

    private boolean handleConnectionError(Throwable t) {
        return (t instanceof RuntimeException
                &&
                t.getMessage().startsWith("Unable to read XmlHttpRequest.status; " +
                        "likely causes are a networking error or bad cross-domain request.")
        );
    }

    public void displayMessage(String s) {
        if (errorHandler != null) {
            errorHandler.setError(s);
        } else {
            Window.alert(s);
        }
    }

    private boolean checkLogin(Throwable t) {
        String message = t.getMessage();
        if (message != null && message.indexOf(LOGIN_REQUIRED) > -1) {
            Window.open("/action/login", "_blank", "status=1,toolbar=1,menubar=1,location=1,resizable=0,height=400,width=600");
            return true;
        }
        return false;
    }

    protected void onFailureCleanup() {

    }
}
