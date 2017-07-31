FROM alpine:latest
MAINTAINER Avinash Palicharla <avinash.palicharla@incapturetechnologies.com>
RUN apk --update add bash tar openjdk8-jre
COPY build/install/FileProcessingConfigPlugin /opt/rapture/FileProcessingConfigPlugin
WORKDIR /opt/rapture/FileProcessingConfigPlugin/bin
COPY docker/feat.sh /tmp/feat.sh
ENTRYPOINT [ "/tmp/feat.sh" ]
