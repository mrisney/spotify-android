package org.risney.cache.android;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marcrisney on 6/18/16.
 */
public class GoogleImageSearch {

    public static final String TAG = GoogleImageSearch.class.getSimpleName();

    final static String GOOGLE_IMAGE_SEARCH_PREFIX = "https://www.google.com/search?tbm=isch&q=";
    final static String UA = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";

    public static List<String> findImages(String question, int searchResults) {
        List<String> imageLinks = new ArrayList<>();
        try {
            String googleUrl = GOOGLE_IMAGE_SEARCH_PREFIX + question.replace(",", "");
            Document doc = Jsoup.connect(googleUrl).userAgent(UA).timeout(10 * 1000).get();
            List<Element> imageLinkList = doc.select("[data-src]").subList(0, searchResults);
            for (Element link : imageLinkList) {
                imageLinks.add(link.attr("abs:data-src").toString());
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return imageLinks;
    }
}
