## NyServer - NyQL as a Service

If you are into micro-service architecture or you want to invoke
NyQL from a different language (Javascript, Python etc), then the solution
is to use NyServer which is an independent running server which anyone
can call a script and get the result out of it.

NyServer exposes the script executions through an service API. And the results will
always be in json format.

NyServer uses [Spark Framework](http://sparkjava.com/) to implement the server, which is basically an embedded jetty
server. Also NyServer is available in Docker Hub under [uisuru89/nyserver](https://hub.docker.com/r/uisuru89/nyserver/) as well.


#### Building NyServer bundle from Source

**Prerequisites**:
 * Java 1.8+
 * Git (for building bundle)
 
 
 1. Clone the git project from github
 2. Run `mvn clean install` from root folder.
 3. Go to _server_ folder using `cd server` command.
 4. Run `mvn assembly:assembly` command to build bundle in zip and tar.gz formats.
 5. The bundle is created inside _target_ folder.
 6. Extract the appropriate bundle (zip if windows, tar.gz otherwise)
 7. Run `server.bat` or `sh server.sh` to start the server.
 
For server configurations see below.

#### Running using a Docker Image

It is very easier to run the service using docker image. Assuming you have installed Docker in your machine, run,
`docker pull uisuru89/nyserver` command to pull the _latest_ image.

Run the docker image using `docker run -p 9009:9009 uisuru89/nyserver`.


### Server Configurations

Server is configured via `server.json` file inside _/config_ folder in root.

 * __port__: server port to use (default is 9009)
 * __basePath__: base url path to add for all routes (default path is _/ny_)
 * __auth__: Authentication settings for the server. When enabled, every service call must have the secret token in its header.
    * __enabled__: boolean flag to indicate authentication is enabled or not.
    * __token__: token to be expected with each call when authentication is enabled.
 * __websocket__: boolean flag to indicate whether profiling information should be pumped through a web socket. (default is _false_)
 

In addition to that, server expects a `nyql.json` configuration file to connect with database and scripts.
Currently in sever bundle, it is in _/config_ folder.

### Server Configurations via Environment Variables

Sometimes it is safer to pass parameters via environment variables, specially in docker cloud environments.
In such cases, you can tweak many configurations via env variables as shown in below.

 * __LOG4J_CONFIG_FILE__: Path to log4j.properties file. NyServer by default bundles with log4j logging.
 * __NYSERVER_CONFIG_PATH__: Path to `server.json` config file. (default is _./config/server.json_)
 * __NYSERVER_NYJSON_PATH__: Path to `nyql.json` file. (Default is _./config/nyql.json_)
 * __NYSERVER_PORT__: Port for the server. (Default is _9009_)
 * __NYSERVER_AUTH_TOKEN__: Secret token to expect from all clients.
 