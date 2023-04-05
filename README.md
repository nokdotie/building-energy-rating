# Building Energy Rating

## Scraper
This application queries the Sustainable Energy Authority of Ireland, SEAI, to identify existing certificates, parse them, and save the extracted information.

```sh
sbt scraper/run
```

## API
The application returns certificates gathered from the SEAI.

```sh
sbt api/run
```
