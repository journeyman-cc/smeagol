FROM alpine:3.6

MAINTAINER Simon Brooke <simon@journeyman.cc>

ENV JAVA_HOME=/usr/lib/jvm/default-jvm

RUN apk add --no-cache openjdk8 && \
    ln -sf "${JAVA_HOME}/bin/"* "/usr/bin/"

# ensure the directories I'm going to write to actually exist!
RUN mkdir -p /usr/local/bin
RUN mkdir -p /usr/local/etc

COPY target/smeagol-*-standalone.jar /usr/local/bin/smeagol.jar
COPY resources/passwd /usr/local/etc/passwd
COPY resources/config.edn /usr/local/etc/config.edn
COPY resources/public/content /usr/local/etc/content

ENV SMEAGOL_CONFIG=/usr/local/etc/config.edn
ENV SMEAGOL_CONTENT_DIR=/usr/local/etc/content
ENV SMEAGOL_PASSWD=/usr/local/etc/passwd
ENV TIMBRE_DEFAULT_STACKTRACE_FONTS="{}"
ENV TIMBRE_LEVEL=':info'
ENV PORT=80

EXPOSE 80

CMD java -jar /usr/local/bin/smeagol.jar

