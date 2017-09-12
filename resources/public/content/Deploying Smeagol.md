## Choosing a deployment mechanism
There are currently three ways you can deploy Smeagol: as an executable Jar file, as a Docker image, and as a web-app in a [Servlet container](https://en.wikipedia.org/wiki/Web_container). Each method has advantages and disadvantages.

The Jar file is extremely easy to deploy and to configure, but cannot currently serve [HTTPS](https://en.wikipedia.org/wiki/HTTPS), which, on the modern web, is a significant disadvantage. The Docker image is just a wrapper around the Jar file; it's particularly suitable for automated deployment. The web-app solution offloads responsibility for things like HTTPS to the Servlet container, and consequently can be much more secure; but it can really only be configured at compile time.

## Deploying as a stand-alone application
To deploy Smeagol as a stand-alone application, either download the jar file for the release you want to deploy, or clone the source and compile it with:

    lein bower install
    lein ring uberjar

This will create a jar file in the `target` directory, named `smeagol-`*VERSION*`-standalone.jar`.

Smeagol cannot access either its configuration or its content from the jar file, as otherwise they would not be editable. There are three solutions to this:

### Custom configuration file
You can copy the standard configuration file `resources/config.edn` to somewhere outside the jar file, edit it to suit your installation, and set up a single environment variable, `SMEAGOL_CONFIG`, whose value is the path to your new configuration file.

### Environment variables
Alternatively, you can configure everything through [[Environment Variables]].

### Hybrid strategy
You can have both a configuration file and environment variables. If you do this, the environment variables override the values in the configuration file.

### Necessary content

**NOTE** that the directory at `SMEAGOL_CONTENT_DIR` must contain at least the following files:

1. `_edit-side-bar.md` - the side-bar that should be displayed when editing pages;
2. `_header.md` - the header to be displayed on all pages;
3. `_side-bar.md` - the side-bar that should be displayed when not editing pages.

Standard versions of these files can be found in the [source repository](https://github.com/journeyman-cc/smeagol/tree/master/resources/public/content).

You can run the jar file with:

    java -jar smeagol-VERSION-standalone.jar

## Deploying within a servlet container
To deploy Smeagol within a servlet container, either download the jar file for the release you want to deploy, or clone the source and compile it with:

    lein bower install
    lein ring uberwar

This will create a war file in the `target` directory, named `smeagol-`*VERSION*`-standalone.war`.  Deploy this to your servlet container in the normal way; details will depend on your container. Instructions for Tomcat are [here](https://tomcat.apache.org/tomcat-8.0-doc/deployer-howto.html).

The problem with this is that unless the environment variables (see above) were already set up in the environment of the servlet container at the time when the servlet container were launched, Smeagol will run with its built-in defaults. If you want to change the defaults, you would have to edit the `resources/config.edn` file and recompile the war file. 

Smeagol will run as a web-app with the default configuration perfectly satisfactorily.

## Experimental Docker image

You can now run Smeagol as a [Docker](http://www.docker.com) image. Read more about [[Using the Docker Image]].

To run my Docker image, use

    docker run simonbrooke/smeagol

Smeagol will run, obviously, on the IP address of your Docker image, on port 8080. To find the IP address, start the image using the command above and then use

    docker inspect --format '{{ .NetworkSettings.IPAddress }}' $(docker ps -q)

Suppose this prints '10.10.10.10', then the URL to browse to will be http://10.10.10.10:8080/smeagol/

This image is _experimental_, but it does seem to work fairly well. What it does **not** yet do, however, is push the git repository to a remote location, so when you tear the Docker image down your edits will be lost. My next objective for this image is for it to have a cammand line parameter being the git address of a repository from which it can initialise the Wiki content, and to which it will periodically push local changes to the Wiki content.

To build your own Docker image, run:

    lein clean
    lein bower install
    lein ring uberwar
    lein docker build

This will build a new Docker image locally; you can, obviously, push it to your own Docker repository if you wish.
