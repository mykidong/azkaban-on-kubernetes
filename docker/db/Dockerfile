FROM java:8-jre

ENV APP_HOME /opt/azkaban-db

RUN echo "Asia/Seoul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN useradd -ms /bin/bash -d ${APP_HOME} db

RUN set -ex \
    && AZKABAN_DB_NAME=azkaban-db-3.90.0 \
    && fileId=1_oYPbDg3MKAu4RjL0P-_ZIl5ixlPgq04 \
    && fileName=${AZKABAN_DB_NAME}.tar.gz \
    && curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null \
    && code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)" \
    && curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName} \
    && tar -zxf ${fileName} -C ${APP_HOME} \
    && cp -R ${APP_HOME}/azkaban-db/* ${APP_HOME}/ \
    && rm -rf ${APP_HOME}/azkaban-db \
    && rm -rf ${fileName}


RUN chown db: -R ${APP_HOME}

RUN echo "deb [check-valid-until=no] http://cdn-fastly.deb.debian.org/debian jessie main" > /etc/apt/sources.list.d/jessie.list
RUN echo "deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list
RUN sed -i '/deb http:\/\/deb.debian.org\/debian jessie-updates main/d' /etc/apt/sources.list
RUN apt-get -o Acquire::Check-Valid-Until=false update

RUN apt-get -y -f install mysql-client

USER db