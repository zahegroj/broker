FROM centos:centos7

RUN export MQ_PACKAGES="MQSeries*.rpm" \
  && mkdir -p /tmp/mq \
  && mkdir -p /tmp/iib

ADD iib-10.0.0.3.tar.gz /tmp/iib
ADD WS_MQ_LINUX_ON_X86_64_V8.0_IMG.tar.gz /tmp/mq

RUN cd /tmp/mq \
  && groupadd --gid 1000 mqm \
  && useradd --uid 1000 --gid mqm --home-dir /var/mqm mqm \
  && usermod -G mqm root \
  && yum install redhat-lsb -y \
  && yum install openssh-server -y \
  && echo "fs.file-max = 524288" > /etc/sysctl.conf

RUN cd /tmp/mq \
  && ./mqlicense.sh -accept \
  && rpm -ivh MQSeries*.rpm \ 
  && /opt/mqm/bin/setmqinst -p /opt/mqm -i \
  && rm -rf /tmp/mq

ENV MQ_QMGR_CMDLEVEL=802

VOLUME /var/mqm

RUN mv /tmp/iib/iib-10.0.0.3 /opt/ \
  && cd /opt/iib-10.0.0.3 \
  && ./iib make registry global accept license silently

EXPOSE 1414
EXPOSE 4414
EXPOSE 7800
EXPOSE 9991
