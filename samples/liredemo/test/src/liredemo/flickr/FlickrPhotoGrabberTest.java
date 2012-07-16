package liredemo.flickr;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * ...
 * Date: 03.07.2008
 * Time: 13:19:59
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FlickrPhotoGrabberTest {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, InterruptedException {
        HashSet<String> photoids = new HashSet<String>();
        int[] tagNumHist = new int[25];
        for (int i = 0; i < tagNumHist.length; i++) {
            tagNumHist[i] = 0;
        }
        int countAll = 0;
        while (photoids.size() < 10000) {
            List<FlickrPhoto> photos = FlickrPhotoGrabber.getRecentPhotos();
            for (FlickrPhoto photo : photos) {
                if (!photoids.contains(photo.url)) {
                    photoids.add(photo.url);
                    countAll++;
                    tagNumHist[photo.tags.size() < 25 ? photo.tags.size() : 24]++;
                    if (photoids.size() % 100 == 0) System.out.print(".");
                    if (photoids.size() % 1000 == 0) System.out.println("");
                }
            }
            Thread.sleep(500);
        }
        System.out.println("countAll = " + countAll);
        System.out.println("countWithTags = " + (countAll - tagNumHist[0]));
        for (int aTagNumHist : tagNumHist) {
            System.out.print(aTagNumHist + ";");
        }
    }
}
