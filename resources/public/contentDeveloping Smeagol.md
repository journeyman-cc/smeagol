## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

You will need [node](https://nodejs.org/en/) and [bower](https://bower.io/) installed.

## Running

To start a web server for the application during development, run:

    lein bower install
    lein ring server
		
To deploy Smeagol as a stand-alone application, compile it with:

    lein bower install
    lein uberjar
		
This will create a jar file in the `target` directory, named `smeagol-`*VERSION*`-standalone.jar`. You can run this file with:

    java -jar smeagol-VERSION-standalone.jar
		
Test modification?

Alternatively, if you want to deploy to a servlet container (which I would strongly recommend), the simplest thing is to run:

    lein bower install
    lein ring uberwar

(a command which I'm sure Smeagol would entirely appreciate) and deploy the resulting war file.

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
