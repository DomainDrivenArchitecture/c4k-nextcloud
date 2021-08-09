FROM meissa-cloud-app

RUN apt update
RUN mkdir /usr/share/man/man1/
RUN apt -yqq install --no-install-recommends --yes curl default-jre-headless

RUN curl -L -o /tmp/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/2.0.0/dda-serverspec-standalone.jar

COPY serverspec.edn /tmp/serverspec.edn

RUN java -jar /tmp/serverspec.jar /tmp/serverspec.edn -v
