# valr
This is the repository for an assignment to create an order book.
The technologies that were used are the following:
1. Spring Boot 3.x
2. Kotlin
3. Gradle
4. Test Containers
5. Jib (gradle plugin to build and publish docker images)
6. Ktlint (formatter for Kotlin)
7. Spring Security with JWT
8. GitHub actions to test and trigger pipelines
9. Swagger for REST endpoint documentation

The data structures that were used have been simplified, maybe a further optimisation would have been to have a map with a key of **PriceLevel** consisting of **Currency** and **Price** to get O(1) retrievals
