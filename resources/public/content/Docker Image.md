Smeagol is available as a Docker image

To run my Docker image, use

    docker run -p 127.0.0.1:80:80 simonbrooke/smeagol

Where 127.0.0.1 is the IP address through which you want to forward port 80 (in real life it wouldn't be 127.0.0.1, but that's safe for testing).

You can then browse to Smeagol by pointing your browser at http://localhost/.

As of version 0.99.10, the Docker image is now based on the Jetty, rather than the Tomcat, deployment of Smeagol (that is to say, it runs the executable jar file). This makes for a lighter weight Docker image. All configuration can be overridden with [[Environment Variables]], which can be passed into the Docker container when the image is invoked, or from a [[Configuration]] file.

The `config.edn` and `passwd` files and the `content` directory are copied into `/usr/local/etc` in the Docker image, and the appropriate environment variables are set up to point to them:
```
COPY resources/passwd /usr/local/etc/passwd
COPY resources/config.edn /usr/local/etc/config.edn
COPY resources/public/content /usr/local/etc/content

ENV SMEAGOL_CONFIG=/usr/local/etc/config.edn
ENV SMEAGOL_CONTENT_DIR=/usr/local/etc/content
ENV SMEAGOL_PASSWD=/usr/local/etc/passwd
```
This works for play purposes. However, it means that any edits made to either the `passwd` file or the `content` directory will be lost when the Docker image is shut down. You really need to have these resources copied to a place in a real file system which is mounted by the image. While I intend that by the 1.1.0 release of Smeagol it will be possible to configure a remote origin repository to which changes are periodically pushed, which will backup and preserve the content, this won't save the `passwd` file, as this is deliberately not stored in the git repository for security reasons.

## Mounting real file systems

It's possible to mount external file systems, and to override environment variables, with arguments to Docker's extraordinarily complex [run command](https://docs.docker.com/engine/reference/commandline/run/).

I'm currently working with a recipe:

    docker run -p 127.0.0.1:80:80 -v ~/tmp/etc:/usr/local/etc simonbrooke/smeagol

Where:

1. `127.0.0.1` is the IP address on the real host on which you wish to serve;
2. `:80:80` maps port 80 on the image to port 80 on the specified IP address;
3. `~/tmp/etc` is the directory on the file system of the real host where files are stored;
4. `/usr/local/etc` is the directory within the image file system to which that will be mounted;

This works, and uses the default values of the environment variables which are set up in the Docker image. However, I'm very much prepared to believe there are better recipes.

## Status

This image is _experimental_, but it does seem to work fairly well. What it does **not** yet do, however, is push the git repository to a remote location, so unless you have mounted an external file store, when you tear the Docker image down your edits will be lost. My next objective for this image is for it to have a cammand line parameter being the git address of a repository from which it can initialise the Wiki content, and to which it will periodically push local changes to the Wiki content.

## Building the Docker image

To build your own Docker image, run:

    lein clean
    lein bower install
    lein ring uberjar
    lein docker build

This will build a new Docker image locally; you can, obviously, push it to your own Docker repository if you wish.
