FROM tomcat:10-jdk17-openjdk-buster

# Installs Maven:
RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && \
	tar -xvf apache-maven-3.6.3-bin.tar.gz && \
	rm apache-maven-3.6.3-bin.tar.gz && \
	mv apache-maven-3.6.3 /opt/

ENV M2_HOME='/opt/apache-maven-3.6.3' 

ENV PATH="$M2_HOME/bin:$PATH"

COPY ctsws/ /ctsws

COPY ctsws/repository/ /root/.m2/repository

WORKDIR /ctsws

# Builds ctsws:
RUN mvn package && \
	cp target/ctsws-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ctsws.war
