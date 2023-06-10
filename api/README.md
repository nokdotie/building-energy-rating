## Examples

* http://localhost:8080/v1/ber/100000066
* add `"X-API-Key"` header with valid ApiKey (see `UserApiKeyInMemoryStore` and `GoogleFirestoreUserApiKeyStore`)

### Local development using `UserApiKeyInMemoryStore`
* example using curl:
* `curl --header "X-API-Key: wqerasdffv123fv342rfsd" http://localhost:8080/v1/ber/100000066`

### Using `Admin` ApiKey from `GoogleFirestoreUserApiKeyStore`
* example using curl:
* `curl --header "X-API-Key: sKIBl1R4VUqIIv1HFMKE" http://localhost:8080/v1/ber/100000066`

### Using `User` ApiKey from `GoogleFirestoreUserApiKeyStore`
* example using curl:
* `$ curl --header "X-API-Key: L9dkeANFgX4jUgGNrBAA" http://localhost:8080/v1/ber/100000066`

Exemplary response:
``` json
{
  "number": 100000066,
  "rating": "B3",
  "ratingImageUrl": "https://ber.nok.ie/static/images/ber/B3.svg",
  "issuedOn": "2018-09-02",
  "validUntil": "2028-09-02",
  "property": {
    "address": "5 LAKESIDE\nOLDWOOD\nGOLFLINKS ROAD\nROSCOMMON\nCO. ROSCOMMON"
  },
  "assessor": {
    "number": 105285,
    "companyNumber": 105284
  },
  "domesticEnergyAssessmentProcedureVersion": "3.2.1",
  "energyRating": 139.01,
  "carbonDioxideEmissionsIndicator": 27.93
}
```
