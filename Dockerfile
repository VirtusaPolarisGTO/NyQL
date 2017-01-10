FROM java:8-alpine

# create a directory for nyserver in root
RUN mkdir /nyserver

# copy tar file to app folder
COPY ./server/target/nyserver.tar.gz /nyserver

# update the system
# RUN apt-get update

# move to the app folder and extract
RUN cd /nyserver; tar -xzvf nyserver.tar.gz

WORKDIR /nyserver

# default nyql port is 9009
EXPOSE 9009

CMD ["java", "-Dcom.virtusa.gto.nyql.mode=server", "-classpath", ".:lib/*", "com.virtusa.gto.nyql.server.NyServer"]

