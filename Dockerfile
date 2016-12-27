FROM tomcat:alpine
COPY target/smeagol-*-standalone.war $CATALINA_HOME/webapps/smeagol.war

