FROM alpine:latest
RUN echo http://dl-cdn.alpinelinux.org/alpine/edge/main >> /etc/apk/repositories
RUN echo http://dl-cdn.alpinelinux.org/alpine/edge/testing >> /etc/apk/repositories
RUN echo http://dl-cdn.alpinelinux.org/alpine/edge/community >> /etc/apk/repositories
RUN apk update
RUN apk --update add bash tar openjdk8-jre stunnel
# About nsswitch.conf - see https://registry.hub.docker.com/u/frolvlad/alpine-oraclejdk8/dockerfile/
COPY rootfs /
# S6 is a process manager (helps with DNS hacks)
ADD https://github.com/just-containers/s6-overlay/releases/download/v1.13.0.0/s6-overlay-amd64.tar.gz /
RUN apk -U add dnsmasq jq curl && \
  chmod 755 /bin/*.sh /etc/services.d/dns/run /etc/services.d/dns-hack/run /etc/services.d/syslog/run && \
  mkdir /app && \
  echo 'hosts: files mdns4_minimal [NOTFOUND=return] dns mdns4' >> /etc/nsswitch.conf && \
  addgroup -g 998 app && \
  adduser -D  -G app -s /bin/false -u 999 app
RUN rm -rf /var/cache/apk/*
COPY build/install/EmbsServer /opt/rapture/EmbsServer
COPY docker/config /opt/rapture/EmbsServer/etc/rapture/config
COPY docker/stunnel /opt/stunnel
COPY docker/run.sh /tmp/run.sh
ENV RAPTURE_CONFIG_HOME /opt/rapture/EmbsServer/etc/rapture/config
VOLUME ["/opt/rapture/EmbsServer/etc/rapture/config"]
WORKDIR /opt/rapture/EmbsServer/bin
ENTRYPOINT [ "/init" ]
CMD [ "/tmp/run.sh" ]
