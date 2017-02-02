FROM centos:6.8
MAINTAINER "nabedge"
ENV container jooq-demo2
USER root
WORKDIR /tmp/docker-work

# OS時刻
RUN \
    cp /usr/share/zoneinfo/Japan /etc/localtime \
    && echo "ZONE=\"Asia/Tokyo\"" > /etc/sysconfig/clock

RUN \
    yum -y install yum-utils \
    && yum-config-manager --enable epel/x86_64 \
    && yum -y install http://rpms.famillecollet.com/enterprise/remi-release-6.rpm \
    && yum -y groupinstall "Development Tools"

RUN yum -y install \
    openssl-devel \
    readline-devel \
    libyaml-devel \
    libffi-devel \
    sqlite-devel \
    openssh-server \
    ntpd \
    supervisor \
    dovecot \
    telnet \
    wget \
    curl \
    mutt \
    lsof \
    sudo

# postgresql
RUN \
    wget -q https://ftp.postgresql.org/pub/source/v9.3.4/postgresql-9.3.4.tar.bz2 \
    && tar xf ./postgresql-9.3.4.tar.bz2 \
    && cd ./postgresql-9.3.4 \
    && ./configure --prefix=/usr/local/pgsql \
    && make \
    && make install \
    && adduser postgres \
    && mkdir /usr/local/pgsql/data \
    && chown postgres /usr/local/pgsql/data \
    && /bin/bash -l -c "sudo -u postgres /usr/local/pgsql/bin/initdb --locale=C --auth-host=trust --auth-local=trust -D /usr/local/pgsql/data" \
    && echo 'host all all 0.0.0.0/0 trust' >> /usr/local/pgsql/data/pg_hba.conf \
    && sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /usr/local/pgsql/data/postgresql.conf \
    && sed -i "s/#synchronous_commit = on/synchronous_commit = off/" /usr/local/pgsql/data/postgresql.conf \
    && cp contrib/start-scripts/linux /etc/init.d/postgresql \
    && chown root /etc/init.d/postgresql \
    && chmod 755 /etc/init.d/postgresql \
    && chkconfig --add postgresql

## supervisord config
RUN \
    echo '[supervisord]' > /etc/supervisord.conf \
    && echo 'nodaemon=true' >> /etc/supervisord.conf

# service start
COPY entrypoint.sh /
ENTRYPOINT ["/bin/sh", "/entrypoint.sh"]
