# Building Energy Rating

## Certificate Number Scraper
This application queries the Sustainable Energy Authority of Ireland, SEAI, to identify numbers that point to certificates.

```sh
sbt certificateNumberScraper/run
```

## Certificate Scraper seai.ie HTML
The application, using identified numbers, queries the SEAI for HTML certificates using their website.

```sh
sbt certificateScraperSeaiIeHtml/run
```

## Certificate Scraper seai.ie PDF
The application, using identified numbers, queries the SEAI for PDF certificates using their file storage.

```sh
sbt certificateScraperSeaiIePDF/run
```

## API
The application returns certificates gathered from the SEAI.

```sh
sbt api/run
```
