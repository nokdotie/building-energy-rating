### Examples

* http://localhost:8080/api/v1/ber/100105170
* add `"X-API-Key"` header with valid token (see `UserTokenInMemoryStore`)


* example using curl: 
* `curl --header "X-API-Key: wqerasdffv123fv342rfsd" http://localhost:8080/api/v1/ber/100105170`

Expected response:
``` json
{
  "number": 100105170,
  "rating": "C1",
  "issuedOn": "2016-05-20",
  "validUntil": "2026-05-20",
  "address": "95 GRATTAN PARK\nSALTHILL\nCO. GALWAY\nGALWAY CITY",
  "energyRatingInKilowattHourPerSquareMetrePerYear": 168.89,
  "carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear": 41.51
}
```

