FROM cypress/included:9.6.1
RUN apt-get update; apt-get install -y libicu67 zip
RUN npm install -g inline-assets
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