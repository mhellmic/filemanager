//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package net.spy.s3;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Returned by AWSAuthConnection.listAllMyBuckets().
 */
public class ListAllMyBucketsResponse extends Response {
    /**
     * A list of Bucket objects, one for each of this account's buckets.  Will be null if
     * the request fails.
     */
    public List<Bucket> entries;

    public ListAllMyBucketsResponse(HttpURLConnection connection) throws IOException {
        super(connection);
        if (connection.getResponseCode() < 400) {
            try {
                XMLReader xr = Utils.createXMLReader();
                ListAllMyBucketsHandler handler = new ListAllMyBucketsHandler();
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);

                xr.parse(new InputSource(connection.getInputStream()));
                this.entries = handler.getEntries();
            } catch (SAXException e) {
                throw new RuntimeException("Unexpected error parsing ListAllMyBuckets xml", e);
            }
        }
    }

    static class ListAllMyBucketsHandler extends DefaultHandler {

        private List<Bucket> entries = null;
        private Bucket currBucket = null;
        private StringBuffer currText = null;
        private SimpleDateFormat iso8601Parser = null;

        public ListAllMyBucketsHandler() {
            super();
            entries = new ArrayList<Bucket>();
            this.iso8601Parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            this.iso8601Parser.setTimeZone(new SimpleTimeZone(0, "GMT"));
            this.currText = new StringBuffer();
        }

        @Override
        public void startDocument() {
            // ignore
        }

        @Override
        public void endDocument() {
            // ignore
        }

        @Override
        public void startElement(String uri, String name, String qName, Attributes attrs) {
            if (name.equals("Bucket")) {
                this.currBucket = new Bucket();
            }
        }

        @Override
        public void endElement(String uri, String name, String qName) {
            if (name.equals("Bucket")) {
                this.entries.add(this.currBucket);
            } else if (name.equals("Name")) {
                this.currBucket.name = this.currText.toString();
            } else if (name.equals("CreationDate")) {
                try {
                    this.currBucket.creationDate = this.iso8601Parser.parse(this.currText.toString());
                } catch (ParseException e) {
                    throw new RuntimeException("Unexpected date format in list bucket output", e);
                }
            }
            this.currText = new StringBuffer();
        }

        @Override
        public void characters(char ch[], int start, int length) {
            this.currText.append(ch, start, length);
        }

        public List<Bucket> getEntries() {
            return this.entries;
        }
    }
}

