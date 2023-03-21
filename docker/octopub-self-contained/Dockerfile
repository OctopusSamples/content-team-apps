FROM openjdk:17-slim-buster
RUN apt-get update
RUN apt-get install nginx supervisor -y
COPY docker/octopub-self-contained/default /etc/nginx/sites-enabled/default
COPY js/generator-octopus-javascript-frontend/generators/app/templates/js/frontend/build /var/www/html/
COPY js/generator-octopus-java-microservice/generators/app/templates/java/products-microservice/target/products-microservice-runner.jar /opt/
COPY docker/octopub-self-contained/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
RUN mkdir -p /var/log/supervisord
EXPOSE 80
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
