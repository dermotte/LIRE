package liredemo.flickr;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 * Date: 10.06.2008
 * Time: 09:36:39
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FlickrPhoto {
    String title, url, photourl;
    BufferedImage img = null;
    List<String> tags;

    public FlickrPhoto(String title, String url, String photourl, List<String> tags) {
        this.title = title;
        this.url = url;
        this.photourl = photourl;
        this.tags = new LinkedList<String>(tags);
    }

    public String toString() {
        return title + ": " + url + " (" + photourl + ")";
    }
}
