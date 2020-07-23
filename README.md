# NeighborGood

Capstone project for STEP pod 46 in 2020.

This is not an official Google product.

## Configuration

A config file `/src/main/webapp/config.js` including a Google Maps API key should be included. This file should follow the below template:

```
var config = {
    MAPS_KEY: 'your-google-maps-api-key'
};
```

## Testing

All servlet unit tests can be run using the command `mvn test`

In order to run the IntegrationTest you must have Google Chrome installed and it must be accessible from where you are running the IntegrationTest.
To run the IntegrationTest first run `mvn clean`, then start the devServer with the command `mvn package appengine:start` and once the devServer is running use the command `mvn -Dtest=IntegrationTest test`.
Once you are done testing, stop the devServer with command `mvn package appengine:stop`