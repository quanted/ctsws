# ###########################################
# # Original working method 
# ###########################################
# FROM tomcat:7.0-jre8
FROM tomcat:10-jdk17-openjdk-buster

RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && \
	tar -xvf apache-maven-3.6.3-bin.tar.gz && \
	rm apache-maven-3.6.3-bin.tar.gz && \
	mv apache-maven-3.6.3 /opt/

ENV M2_HOME='/opt/apache-maven-3.6.3' 

ENV PATH="$M2_HOME/bin:$PATH" 
