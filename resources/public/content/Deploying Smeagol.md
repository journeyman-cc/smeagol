## Deploying as a stand-alone application
To deploy Smeagol as a stand-alone application, either download the jar file for the release you want to deploy, or clone the source and compile it with:

    lein bower install
    lein ring uberjar

This will create a jar file in the `target` directory, named `smeagol-`*VERSION*`-standalone.jar`.

Smeagol cannot access either its configuration or its content from the jar file, as otherwise they would not be editable. There are three solutions to this:

### Custom configuration file
You can copy the standard [[Configuration]] file `resources/config.edn` to somewhere outside the jar file, edit it to suit your installation, and set up a single environment variable, `SMEAGOL_CONFIG`, whose value is the path to your new configuration file.

### Environment variables
Alternatively, you can configure everything through [[Environment Variables]].

### Hybrid strategy
You can have both a configuration file and environment variables. If you do this, the environment variables override the values in the configuration file.

### Necessary content
**NOTE** that `SMEAGOL_CONTENT_DIR` must contain at least the following files:

1. `_edit-side-bar.md` - the side-bar that should be displayed when editing pages;
2. `_header.md` - the header to be displayed on all pages;
3. `_side-bar.md` - the side-bar that should be displayed when not editing pages.

Standard versions of these files can be found in the [source repository](https://github.com/journeyman-cc/smeagol/tree/master/resources/public/content). All these files should be in markdown format - see [[Extensible Markup]].

You can run the jar file with:

    java -jar smeagol-VERSION-standalone.jar

## Deploying within a servlet container
To deploy Smeagol within a servlet container, either download the jar file for the release you want to deploy, or clone the source and compile it with:

    lein bower install
    lein ring uberwar

This will create a war file in the `target` directory, named `smeagol-`*VERSION*`-standalone.war`.  Deploy this to your servlet container in the normal way; details will depend on your container. Instructions for Tomcat are [here](https://tomcat.apache.org/tomcat-8.0-doc/deployer-howto.html).

The problem with this is that unless the environment variables (see above) were already set up in the environment of the servlet container at the time when the servlet container were launched, Smeagol will run with its built-in defaults. This will run perfectly satisfactorily provided your servlet container is configured to unpack war files, which most are.

## Experimental Docker image

You can now run Smeagol as a [Docker](http://www.docker.com) image. Read more about using the [[Docker Image]].
