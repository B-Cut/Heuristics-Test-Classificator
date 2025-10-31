# Cloning a repo now happens outside container
# While in development, this script still doesn't run the application itself

FROM ubuntu

ENV USE_GRADLE="0"

# Add maven and some common dependencies
RUN apt-get update
RUN apt-get install -y jq git openjdk-21-jre openjdk-21-jdk gradle maven

COPY build.sh build.sh

ENTRYPOINT ["/build.sh"]


