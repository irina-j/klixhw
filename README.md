# Klix HW

## Requirements
1. Install java 17 and make sure version is correct java -version
1. Install maven and check it works correctly that mvn -v

It should be enough for run application

## Build:
- To build application use ```mvn clean install```

## Running
- To start application use ```mvn spring-boot:run```
- To start tests only use ```mvn test```
- UI is available on ```localhost:8081```

## Calls for Application Test
1. Happy Path Scenario ```curl --location --request POST 'http://localhost:8081/v1/offer' \
   --header 'Content-Type: application/json' \
   --data-raw '{
   "phone": "+37176021755",
   "email": "john.doe@klix.app",
   "monthlyIncome": 150.0,
   "monthlyExpenses": 50.0,
   "dependents": 0,
   "maritalStatus": "SINGLE",
   "agreeToBeScored": true,
   "amount": 200
   }'```
2. Fast Bank Validation Fail ```curl --location 'http://localhost:8081/v1/offer' \
   --header 'Content-Type: application/json' \
   --data-raw '{
   "phone": "+37245678930",
   "email": "jane.doe@klix.com",
   "monthlyIncome": 1500.75,
   "monthlyExpenses": 300.5,
   "maritalStatus": "SINGLE",
   "agreeToBeScored": true,
   "amount": 200.0
   }'```
3. Klix Application Validation Errors ```curl --location 'http://localhost:8081/v1/offer' \
   --header 'Content-Type: application/json' \
   --data '{
   "phone": "+371123serg2",
   "email": "jane.doeklix.com",
   "monthlyIncome": 1500.75,
   "monthlyExpenses": 300.5,
   "maritalStatus": "SINGLE",
   "agreeToBeScored": true,
   "amount": 200.0
   }'```
