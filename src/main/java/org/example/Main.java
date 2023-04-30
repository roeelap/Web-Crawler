package org.example;

/**
 * This Java program downloads a source URLs html and the html of URLs appearing in the resulting pages.
 * It takes in 4 arguments:
 * 1. The starting URL to the process with (startUrl).
 * 2. The maximal amount of different URLs to extract from the page (maxUrls).
 * 3. How deep the process should run (maxDepth).
 * 4. A Boolean flag indicating cross-level uniqueness (uniqueFlag).
 * For every webpage the program reaches, it will save the HTML content of it to a file.
 * The naming convention for the files is: '<depth>/<url>.html' - where depth is the depth of the page in the process,
 * and url is the URL of the page.
 * All the directories and files will be created inside this project's main directory.
 * The program uses an external library called 'Jsoup' to fetch and parse the HTML content of the webpages, and uses the
 * 'java.nio' library to save the files.
 */
public class Main {
    public static void main(String[] args) throws IllegalArgumentException {
        String url;
        int maxUrls, maxDepth;
        boolean uniqueFlag;

        // input validation
        if (args.length != 4) {
            throw new IllegalArgumentException("Usage: java Main <url> <maxUrls> <maxDepth> <uniqueFlag>\n" +
                    "expected 4 arguments, got " + args.length);
        }

        // maxUrls and maxDepth must be integers
        try{
            maxUrls = Integer.parseInt(args[1]);
            maxDepth = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Usage: java Main <url> <maxUrls> <maxDepth> <uniqueFlag>\n" +
                    "maxUrls and maxDepth must be non-negative integers");
        }

        // doesn't make sense to have negative maxUrls or maxDepth
        if (maxUrls < 0 || maxDepth < 0) {
            throw new IllegalArgumentException("Usage: java Main <url> <maxUrls> <maxDepth> <uniqueFlag>\n" +
                    "maxUrls and maxDepth must be non-negative integers");
        }

        // maxUrls cannot be 0 if maxDepth is greater than 0
        if (maxUrls == 0 && maxDepth > 0) {
            throw new IllegalArgumentException("Usage: java Main <url> <maxUrls> <maxDepth> <uniqueFlag>\n" +
                    "maxUrls cannot be 0 if maxDepth is greater than 0");
        }

        // uniqueFlag must be either true or false
        if (!args[3].equals("true") && !args[3].equals("false")) {
            throw new IllegalArgumentException("Usage: java Main <url> <maxUrls> <maxDepth> <uniqueFlag>\n" +
                    "uniqueFlag must be either true or false");
        }

        // Parse arguments
        url = args[0];
        uniqueFlag = args[3].equals("true");

        WebCrawler webCrawler = new WebCrawler(url, maxUrls, maxDepth, uniqueFlag);

        // starting url must be a valid url
        if (!webCrawler.isValidUrl(url)) {
            throw new IllegalArgumentException("Usage: java Main <url> <maxUrls> <maxDepth> <uniqueFlag>\n" +
                    url + " is not a valid url");
        }

        System.out.println("Starting to crawl from " + url + ", maxUrls = " + maxUrls + ", maxDepth = " + maxDepth + ", uniqueFlag = " + uniqueFlag);
        webCrawler.crawl();
    }
}