FROM maven:3.8.7

#ARG MAVEN_VERSION=3.8.8
#ARG USER_HOME_DIR="/root"
#ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

# Install Java.
#RUN apk --update --no-cache add openjdk8-jre curl
#RUN sudo apt-get install openjdk-8-jdk

#RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
# && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
# && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
# && rm -f /tmp/apache-maven.tar.gz \
# && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

#ENV MAVEN_HOME /usr/share/maven
#ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Define working directory.
WORKDIR /data
COPY src /data/src
COPY pom.xml /data/pom.xml 

# Define commonly used JAVA_HOME variable
#ENV JAVA_HOME /usr/lib/jvm/default-jvm/

# Define default command.
#CMD ["mvn", "--version"]
CMD ["mvn", "clean", "compile", "test"]