package org.zfin.framework;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * This interface marks a class as serializable according to java.io
 * and serializable according to GWT, i.e. it can be used as an object in
 * client side Java Script
 */
public interface ZfinSerializable extends Serializable, IsSerializable {
}
