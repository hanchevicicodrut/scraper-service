1.Run project locally with prod data:
#start db:
run build-prod.sh

#Deploy on container locally
#build image
docker build -t scraper-app .

##run on default (dev) env
docker run -d --name scraper-app-dev -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/scraper_db \
  scraper-app

##run on prod env
set -a
source .env.prod
set +a
docker run -d --name scraper-app-prod -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="${DB_URL}" \
  -e DB_USERNAME="${DB_USERNAME}" \
  -e DB_PASSWORD="${DB_PASSWORD}" \
  -e DATASOURCE_POSTGRESQL_SCHEMA="${DATASOURCE_POSTGRESQL_SCHEMA}" \
  -e API_KEY="${API_KEY}" \
  scraper-app

##rebuild after code changes
docker build -t scraper-app .
docker rm -f scraper-app-dev
docker run -d --name scraper-app-dev ...  # same run command as above

#You cannot run IT using Intellij, because is not compatible with JUnit6, run them from terminal
./mvnw test -Dtest=BikeXpertScraperServiceTest#shouldScrapeAndSaveOneProduct  

1. corectez sa preia Biciclete(categorie) si Mountain Bike(subcategorie) 
2. fac Scheduler ul pentru a porni scrapingul
3. Dockerfile ul
4. Deploy in aws k8s
5. fac un nou scraper de biciclete, pt alt site
6. fac un scraper pe accesorii de biciclete
7. fac un scraper pe ski uri
