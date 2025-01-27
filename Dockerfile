FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=1234 \
    MYSQL_DATABASE=whatsapp \
    LANG=C.UTF-8

COPY script.sql /docker-entrypoint-initdb.d/

EXPOSE 3306

CMD ["mysqld"]