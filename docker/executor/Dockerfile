FROM java:8-jre

ENV APP_HOME /opt/azkaban-executor

RUN echo "Asia/Seoul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN useradd -ms /bin/bash -d ${APP_HOME} executor

RUN set -ex \
    && AZKABAN_EXEC_NAME=azkaban-exec-server-3.90.0 \
    && fileId=15jllIx3eAmAb9d-GZ_KISWxnZuAJiP5r \
    && fileName=${AZKABAN_EXEC_NAME}.tar.gz \
    && curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null \
    && code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)" \
    && curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName} \
    && tar -zxf ${fileName} -C ${APP_HOME} \
    && cp -R ${APP_HOME}/azkaban-exec-server/* ${APP_HOME}/ \
    && rm -rf ${APP_HOME}/azkaban-exec-server \
    && rm -rf ${APP_HOME}/conf/azkaban.properties \
    && rm -rf ${fileName}

COPY activate-executor.sh ${APP_HOME}/bin/activate-executor.sh
COPY start-exec.sh ${APP_HOME}/bin/start-exec.sh
COPY start-and-activate-exec.sh ${APP_HOME}/bin/start-and-activate-exec.sh

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown executor: -R ${APP_HOME}

USER executor
WORKDIR ${APP_HOME}