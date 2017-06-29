FROM alpine:latest
MAINTAINER Avinash Palicharla <avinash.palicharla@incapturetechnologies.com>
RUN apk --update add bash tar openjdk8-jre
COPY build/install/FileProcessingPlugin /opt/rapture/FileProcessingPlugin
WORKDIR /opt/rapture/FileProcessingPlugin/bin
COPY docker/feat.sh /tmp/feat.sh
ENTRYPOINT [ "/tmp/feat.sh" ]
