# FROM tomcat:10-jdk17-openjdk-buster
FROM tomcat:10.1.12-jdk17

# Installs Maven:
# RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && \
# 	tar -xvf apache-maven-3.6.3-bin.tar.gz && \
# 	rm apache-maven-3.6.3-bin.tar.gz && \
# 	mv apache-maven-3.6.3 /opt/ && \
# 	mkdir -p /root/.chemaxon/licenses
RUN wget https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz && \
	tar -xvf apache-maven-3.8.8-bin.tar.gz && \
	rm apache-maven-3.8.8-bin.tar.gz && \
	mv apache-maven-3.8.8 /opt/ && \
	mkdir -p /root/.chemaxon/licenses

ENV M2_HOME='/opt/apache-maven-3.8.8' 

ENV PATH="$M2_HOME/bin:$PATH"

COPY ctsws/ /ctsws

COPY ctsws/repository/ /root/.m2/repository

COPY ctsws/settings.xml /root/.m2/settings.xml

WORKDIR /ctsws

# Builds ctsws:
RUN mvn package && \
	cp target/ctsws-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ctsws.war
