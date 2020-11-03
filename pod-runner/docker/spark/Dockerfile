FROM mykidong/spark:v3.0.0

ENV APP_HOME /opt/spark

RUN echo "Asia/Seoul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN useradd -ms /bin/bash -d ${APP_HOME} spark

COPY enc ${APP_HOME}/
COPY pod-runner-1.0.0-*-fat.jar ${APP_HOME}/
COPY run.sh ${APP_HOME}/
RUN chmod a+x ${APP_HOME}/*.sh

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown spark: -R ${APP_HOME}

RUN echo "deb [check-valid-until=no] http://cdn-fastly.deb.debian.org/debian jessie main" > /etc/apt/sources.list.d/jessie.list
RUN echo "deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list
RUN sed -i '/deb http:\/\/deb.debian.org\/debian jessie-updates main/d' /etc/apt/sources.list
RUN apt-get -o Acquire::Check-Valid-Until=false update
RUN apt-get install -y openssh-client
RUN apt-get install -y curl
RUN apt-get install -y unzip
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && unzip awscliv2.zip && ./aws/install

RUN apt-get install -y apt-transport-https gnupg2
RUN curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -
RUN echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | tee -a /etc/apt/sources.list.d/kubernetes.list
RUN apt-get -o Acquire::Check-Valid-Until=false update
RUN apt-get install -y kubectl


USER spark
RUN export SPARK_HOME=${APP_HOME} && export PATH=$PATH:${SPARK_HOME}/bin
WORKDIR ${APP_HOME}