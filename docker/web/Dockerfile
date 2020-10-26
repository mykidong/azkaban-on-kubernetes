FROM java:8-jre

ENV APP_HOME /opt/azkaban-web

RUN echo "Asia/Seoul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN useradd -ms /bin/bash -d ${APP_HOME} web

RUN set -ex \
    && AZKABAN_WEB_NAME=azkaban-web-server-3.90.0 \
    && fileId=1GzVG5_aKlG8Mb38M3a10jF8X-VYpSxJx \
    && fileName=${AZKABAN_WEB_NAME}.tar.gz \
    && curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null \
    && code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)" \
    && curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName} \
    && tar -zxf ${fileName} -C ${APP_HOME} \
    && cp -R ${APP_HOME}/azkaban-web-server/* ${APP_HOME}/ \
    && rm -rf ${APP_HOME}/azkaban-web-server \
    && rm -rf ${APP_HOME}/conf/azkaban.properties \
    && rm -rf ${fileName}

COPY start-web.sh ${APP_HOME}/bin/start-web.sh

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown web: -R ${APP_HOME}

EXPOSE 8081
USER web
WORKDIR ${APP_HOME}