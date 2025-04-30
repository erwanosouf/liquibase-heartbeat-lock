FROM postgres:16
ENV POSTGRES_PASSWORD=YOLO
COPY sql/bootstrap.sql /docker-entrypoint-initdb.d/bootstrap.sql