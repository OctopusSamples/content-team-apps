# The image created by this Dockerfile can either be used as a worker image, or run directly as a Tentacle.
# When run as a worker image, the Tentacle agent is not used, as Octopus overrides the CMD and ENTRYPOINT.
# When run as a tentacle, the Tentacle agent is called to register a worker with an Octopus instance.

FROM postman/newman:5.2.0-ubuntu
RUN apt-get update; apt-get install -y libicu60 locales
RUN locale-gen en_US.UTF-8
RUN curl -L \
    https://download.octopusdeploy.com/linux-tentacle/tentacle-6.1.1403-linux_x64.tar.gz \
    --output tentacle-linux_x64.tar.gz
RUN mkdir /opt/octopus
RUN tar xvzf tentacle-linux_x64.tar.gz -C /opt/octopus
RUN echo '#!/bin/bash' > /opt/configure.sh
RUN echo '/opt/octopus/tentacle/Tentacle create-instance \
    --instance "Worker" \
    --config "/etc/octopus/Worker/tentacle-Worker.config"' \
>> /opt/configure.sh
RUN echo '/opt/octopus/tentacle/Tentacle new-certificate \
    --instance "Worker" \
    --if-blank' \
>> /opt/configure.sh
RUN echo '/opt/octopus/tentacle/Tentacle configure \
    --instance "Worker" \
    --app "/home/Octopus/Applications" \
    --noListen "True" --reset-trust' \
>> /opt/configure.sh
RUN echo '/opt/octopus/tentacle/Tentacle register-worker \
    --instance "Worker" \
    --force --server "${ServerUrl}" \
    --name `hostname` \
    --comms-style "TentacleActive" \
    --server-comms-port "10943" \
    --apiKey "${ServerApiKey}" \
    --space "${Space}" \
    --workerpool "${TargetWorkerPool}"' \
>> /opt/configure.sh
RUN echo '/opt/octopus/tentacle/Tentacle agent \
    --noninteractive \
    --instance "Worker"' >> /opt/configure.sh
RUN chmod +x /opt/configure.sh
ENTRYPOINT []
CMD ["/opt/configure.sh"]