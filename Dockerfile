FROM java:8-alpine
MAINTAINER Isuru Weerarathna <uisurumadushanka89@gmail.com>


# create a directory for nyserver in root
RUN mkdir /nyserver

# copy tar file to app folder
COPY ./server/target/nyserver.tar.gz /nyserver

# update the system
RUN apk upgrade --update

# move to the app folder and extract
RUN cd /nyserver; tar -xzvf nyserver.tar.gz

WORKDIR /nyserver

# default nyql port is 9009
EXPOSE 9009

CMD ["java", "-Dcom.virtusa.gto.nyql.mode=server", "-classpath", ".:lib/*", "com.virtusa.gto.nyql.server.NyServer"]

