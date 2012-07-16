package liredemo.flickr;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 * Date: 10.06.2008
 * Time: 09:35:51
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FlickrPhotoGrabber extends DefaultHandler {
    public static final String BASE_URL = "http://api.flickr.com/services/feeds/photos_public.gne?format=atom";

    // for sax parsing
    private LinkedList<FlickrPhoto> photos = new LinkedList<FlickrPhoto>();
    private boolean inEntry = false;
    private boolean inTitle = false;
    private String currentTitle = "", currentUrl = null, currentImage = null;
    private LinkedList<String> currentTags = new LinkedList<String>();

    public static List<FlickrPhoto> getRecentPhotos() throws IOException, SAXException, ParserConfigurationException {
        LinkedList<FlickrPhoto> photos = new LinkedList<FlickrPhoto>();
        URL u = new URL(BASE_URL);
        FlickrPhotoGrabber handler = new FlickrPhotoGrabber();
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(u.openStream(), handler);
        return handler.photos;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("entry")) inEntry = true;
        if (inEntry) {
            if (qName.equals("title")) inTitle = true;
            if (qName.equals("link")) {
                if (attributes.getValue("rel").equals("alternate")) currentUrl = attributes.getValue("href");
                if (attributes.getValue("rel").equals("enclosure")) currentImage = attributes.getValue("href");
            }
        }
        if (qName.equals("category")) currentTags.add(attributes.getValue("term"));
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("entry")) {
            inEntry = false;
            // add entry to list:
            photos.add(new FlickrPhoto(currentTitle.trim(), currentUrl.trim(), currentImage.trim(), currentTags));
            // clear:
            currentImage = null;
            currentTitle = "";
            currentUrl = null;
            currentTags.clear();
        }
        if (qName.equals("title")) inTitle = false;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (inTitle) currentTitle += new String(ch, start, length);
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        final List<FlickrPhoto> flickrPhotos = FlickrPhotoGrabber.getRecentPhotos();
        for (Iterator<FlickrPhoto> flickrPhotoIterator = flickrPhotos.iterator(); flickrPhotoIterator.hasNext(); ) {
            FlickrPhoto flickrPhoto = flickrPhotoIterator.next();
            System.out.println("flickrPhoto = " + flickrPhoto);
        }
    }


}
