1.Run project locally with prod data:
#start db:
run build-prod.sh

##run on prod env based on docker container..
set -a
source .env.prod
set +a
docker run -d --name scraper-app -p8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e DB_URL="${DB_URL}" -e DB_USERNAME="${DB_USERNAME}" -e DB_PASSWORD="${DB_PASSWORD}" -e DATASOURCE_POSTGRESQL_SCHEMA="${DATASOURCE_POSTGRESQL_SCHEMA}" -e API_KEY="${API_KEY}" scraper-app



#You cannot run IT using Intellij, because is not compatible with JUnit6, run them from terminal
./mvnw test -Dtest=BikeXpertScraperServiceTest#shouldScrapeAndSaveOneProduct  

1. corectez sa preia Biciclete(categorie) si Mountain Bike(subcategorie) 
2. fac Scheduler ul pentru a porni scrapingul
3. Dockerfile ul
4. Deploy in aws k8s
5. fac un nou scraper de biciclete, pt alt site
6. fac un scraper pe accesorii de biciclete
7. fac un scraper pe ski uri
