# Web-Crawler

This Java program downloads a source URLs html and the html of URLs appearing in the resulting pages.

 It takes in 4 arguments:
 1. The starting URL to the process with (startUrl).
 2. The maximal amount of different URLs to extract from the page (maxUrls).
 3. How deep the process should run (maxDepth).
 4. A Boolean flag indicating cross-level uniqueness (uniqueFlag).

 For every webpage the program reaches, it will save the HTML content of it to a file.
 The naming convention for the files is: '<depth>/<url>.html' - where depth is the depth of the page in the process, and url is the URL of the page.
 All the directories and files will be created inside this project's main directory.

 The program uses an external library called 'Jsoup' to fetch and parse the HTML content of the webpages, and uses the 'java.nio' library to save the files.