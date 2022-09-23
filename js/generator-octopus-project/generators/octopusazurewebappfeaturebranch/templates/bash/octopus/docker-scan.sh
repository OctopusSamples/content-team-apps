# Manual docker scan plugin install instructions
# https://github.com/docker/scan-cli-plugin#on-linux
mkdir -p ~/.docker/cli-plugins
curl https://github.com/docker/scan-cli-plugin/releases/latest/download/docker-scan_linux_amd64 -L -s -S -o ~/.docker/cli-plugins/docker-scan
chmod +x ~/.docker/cli-plugins/docker-scan

if [[ "#{DockerHub.Username}" == "##{DockerHub.Username}" ]];
then
  echo "The username variable is not defined, so no scan will be performed."
  exit 0
fi

if [[ -z "#{DockerHub.Username}" ]];
then
  echo "The username variable is empty, so no scan will be performed."
  exit 0
fi

if [[ "#{DockerHub.Password}" == "##{DockerHub.Password}" ]];
then
  echo "The password variable is not defined, so no scan will be performed."
  exit 0
fi

if [[ -z "#{DockerHub.Password}" ]];
then
  echo "The password variable is empty, so no scan will be performed."
  exit 0
fi

echo "#{DockerHub.Password}" | docker login --username "#{DockerHub.Username}" --password-stdin 2>&1

docker scan --accept-license ${docker_image}:#{Octopus.Action[Deploy WebApp].Package.PackageVersion}