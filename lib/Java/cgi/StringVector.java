package cgi;

import java.util.*;

// This appears in Core Web Programming from
// Prentice Hall Publishers, and may be freely used
// or adapted. 1997 Marty Hall, hall@apl.jhu.edu.

/** Works just like a Vector, but all elements
 *  must be Strings, and elements don't need to
 *  be cast to a String. Works identically in
 *  Java 1.02 and 1.1.1.
 */

public class StringVector implements Cloneable {
  protected Vector vector;

  protected StringVector(Vector v) {
    vector = v;
  }
  
  public StringVector() {
    vector = new Vector();
  }

  public StringVector(int initialCapacity) {
    vector = new Vector(initialCapacity);
  }

  public StringVector(int initialCapacity,
                      int capacityIncrement) {
    vector = new Vector(initialCapacity,
                        capacityIncrement);
  }

  public void addElement(String string) {
    vector.addElement(string);
  }

  public int capacity() {
    return(vector.capacity());
  }

  public Object clone() {
    System.out.println("Orig capacity: " +
                       vector.capacity());
    Vector newVector = (Vector)vector.clone();
    System.out.println("New capacity: " +
                       newVector.capacity());
    StringVector newStringVector = new StringVector();
    newStringVector.vector = newVector;
    return(newStringVector);
  }
  
  public boolean contains(String string) {
    return(vector.contains(string));
  }

  public void copyInto(String[] strings) {
    vector.copyInto(strings);
  }

  public String elementAt(int index) {
    return((String)vector.elementAt(index));
  }

  public Enumeration elements() {
    return(vector.elements());
  }

  public void ensureCapacity(int minCapacity) {
    vector.ensureCapacity(minCapacity);
  }

  public String firstElement() {
    return((String)vector.firstElement());
  }

  public int indexOf(String string) {
    return(vector.indexOf(string));
  }

  public int indexOf(String string, int startIndex) {
    return(vector.indexOf(string, startIndex));
  }

  public void insertElementAt(String string, int index) {
    vector.insertElementAt(string, index);
  }

  public boolean isEmpty() {
    return(vector.isEmpty());
  }

  public String lastElement() {
    return((String)vector.lastElement());
  }

  public int lastIndexOf(String string) {
    return(vector.lastIndexOf(string));
  }

  public int lastIndexOf(String string, int endIndex) {
    return(vector.lastIndexOf(string, endIndex));
  }

  public void removeAllElements() {
    vector.removeAllElements();
  }

  public boolean removeElement(String string) {
    return(vector.removeElement(string));
  }

  public void removeElementAt(int index) {
    vector.removeElementAt(index);
  }

  public void setElementAt(String string, int index) {
    vector.setElementAt(string, index);
  }

  public void setSize(int size) {
    vector.setSize(size);
  }

  public int size() {
    return(vector.size());
  }

  public String toString() {
    return(vector.toString());
  }

  public void trimToSize() {
    vector.trimToSize();
  }
}
