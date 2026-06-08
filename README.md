1.Run project locally:
#start db:
cd docker
docker compose --env-file ../.env up -d


#You cannot run IT using Intellij, because is not compatible with JUnit6, run them from terminal
mvn test -Dtest=BikeXpertScraperServiceTest#shouldScrapeOneProductWithDetails
