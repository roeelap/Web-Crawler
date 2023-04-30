package org.example;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private final String startUrl; // The starting URL to crawl from
    private final int maxUrls; // The maximum number of URLs to crawl to from a certain webpage
    private final int maxDepth; // The maximum depth the crawler should reach
    private final boolean uniqueFlag; // Whether a URL can be crawled in multiple depths
    private Map<Integer, Set<String>> uniqueUrls; // Used to keep track of unique URLs throughout the crawl process. Key - depth of the URL, Value - set of URLs at that depth.

    /**
     * Constructor for the CrawlerNode class.
     *
     * @param startUrl The URL to start crawling from.
     * @param maxUrls The maximum number of URLs to parse.
     * @param maxDepth The maximum depth of the crawler.
     * @param uniqueFlag Whether the crawler should only crawl each URL once.
     */
    public WebCrawler(String startUrl, int maxUrls, int maxDepth, boolean uniqueFlag) {
        this.startUrl = startUrl;
        this.maxUrls = maxUrls;
        this.maxDepth = maxDepth;
        this.uniqueFlag = uniqueFlag;
    }

    /**
     * Main method in order to start the crawling process.
     * Initializes a new hashmap to keep track of unique URLs and calls the recursive crawl method.
     */
    public void crawl() {
        // initialize the uniqueUrls map with the starting url at depth 0
        uniqueUrls = new HashMap<>();
        Set<String> urlsAtDepth0 = new HashSet<>();
        urlsAtDepth0.add(startUrl);
        uniqueUrls.put(0, urlsAtDepth0);

        // save the html content of the starting url and start crawling
        saveHTMLContent(0, startUrl);
        crawl(startUrl, 0);

        System.out.println("Crawling complete!");
        printStats();
    }

    /**
     * Performs a crawling process from a given URL.
     * Given a URL, this method will save the HTML content of the URL to a file, and then extract all URLs from the webpage.
     * It will then choose at most 'maxUrls' number of URLs to crawl while respecting the uniqueFlag.
     * It will then recursively call itself on each of the chosen URLs.
     * The recursion will stop when the maxDepth is reached.
     * @param startUrl The URL to start crawling from.
     * @param depth The current depth of the crawling process.
     */
    private void crawl(String startUrl, int depth) {
        // if we reached the max depth, stop crawling
        if (depth == maxDepth) return;

        // extract all URLs from the webpage
        Set<String> urls = extractUrls(startUrl);
        // choose maxUrls number of urls to crawl while respecting the uniqueFlag
        Set<String> newUrls = new HashSet<>();
        for (String url : urls) {
            // Check if maxUrls has been reached
            if (newUrls.size() >= maxUrls) break;

            if (uniqueUrls.containsKey(depth + 1)) {
                Set<String> urlsAtDepth = uniqueUrls.get(depth + 1);
                // check if the url has already been crawled at this depth
                if (urlsAtDepth.contains(url)) continue;
            } else {
                // if the depth has not been reached yet, add it to the map
                uniqueUrls.put(depth + 1, new HashSet<>());
            }

            // Check if uniqueFlag is true and if the link has already been crawled at a lower depth
            if (uniqueFlag && isUrlVisited(url, depth)) continue;

            // save the html content of the url to a file
            boolean isSaved = saveHTMLContent(depth + 1, url);
            // if the html content could not be saved, skip this url
            if (!isSaved) continue;

            // if we reached this point, we can add the url to the set of new urls to crawl,
            // and to the set of unique urls at depth + 1.
            newUrls.add(url);
            uniqueUrls.get(depth + 1).add(url);
        }

        // recursively crawl the new urls
        for (String newUrl : newUrls) {
            crawl(newUrl, depth + 1);
        }
    }

    /**
     * Given a startURL, extracts all URLs that can be reached from the startURLs webpage.
     * @param startUrl The URL to check.
     * @return the set of URLs that can be reached from the startURLs webpage.
     */
    private Set<String> extractUrls(String startUrl) {
        Set<String> urls = new HashSet<>();
        try {
            Document doc = Jsoup.connect(startUrl).userAgent("Mozilla").get();
            // extract all a tags with href attribute
            Elements elements = doc.select("a[href]");
            for (Element element : elements) {
                String url = element.attr("abs:href");
                // check if the url is valid
                if (isValidUrl(url)) {
                    urls.add(element.attr("abs:href"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting urls from url: " + startUrl);
            System.out.println(e.getMessage());
            return new HashSet<>();
        }
        return urls;
    }

    /**
     * Checks if a string is a valid URL.
     * @param url The URL string to check.
     * @return true if the string is a valid URL, false otherwise.
     */
    public boolean isValidUrl(String url) {
        if (url == null || !url.startsWith("http")) {
            return false;
        }
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    /**
     * Writes the html content of a webpage to a file.
     * The file path is in the format of ‘<depth>/<url>.html’
     * where <depth> is the depth of the url, and <url> is the url of the webpage
     * @return true if the html content was successfully saved to a file, false otherwise.
     */
    private boolean saveHTMLContent(int depth, String url) {
        // replace all characters that are not suitable for a file name with '_'
        String fileName = url.replaceAll("[^a-zA-Z0-9.-]", "_") + ".html";
        try {
            // create directory for depth if it doesn't exist
            String dirName = Integer.toString(depth);
            Path dirPath = Paths.get(dirName);
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }

            // create file
            Path filePath = dirPath.resolve(fileName);

            // connect to url and get html content
            Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
            Files.writeString(filePath, doc.outerHtml());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a URL has been visited at a lower depth.
     * @param url The URL to check.
     * @param maxSearchDepth The maximum depth to search.
     * @return true if the URL has been visited at a lower depth, false otherwise.
     */
    private boolean isUrlVisited(String url, int maxSearchDepth) {
        for (int i = 0; i <= maxSearchDepth; i++) {
            Set<String> urlsAtDepth = uniqueUrls.get(i);
            if (urlsAtDepth.contains(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints the stats of the web crawler.
     * The stats include the total number of unique URLs crawled to, and the number of unique URLs at each depth.
     */
    public void printStats() {
        System.out.println("========================================");
        System.out.println("Web Crawler Stats:");
        System.out.println("Total Number of unique URLs crawled to: " + getTotalNumberOfUniqueURLsCrawledTo());
        for (int i = 0; i <= maxDepth; i++) {
            System.out.println("Depth " + i + ": " + uniqueUrls.get(i).size() + " URLs");
        }
        System.out.println("========================================");
    }

    /**
     * Gets the total number of unique URLs crawled to.
     */
    public int getTotalNumberOfUniqueURLsCrawledTo() {
        int total = 0;
        for (int i = 0; i <= maxDepth; i++) {
            total += uniqueUrls.get(i).size();
        }
        return total;
    }
}
