
package org.zfin.uniquery.search;

import org.apache.lucene.document.Document;


public class Hit extends Object
    {
    private Document document;
    private float score;
    private String highlightedText;

    public Hit(Document document, float score, String highlightedText)
        {
        this.document = document;
        this.score = score;
        this.highlightedText = highlightedText;
        }

    public Document getDocument()
        {
        return document;
        }


    public float getScore()
        {
        return score;
        }


    public String getHighlightedText()
        {
        return highlightedText;
        }
    }
