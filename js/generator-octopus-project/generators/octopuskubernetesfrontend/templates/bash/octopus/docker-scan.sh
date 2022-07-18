# Manual docker scan plugin install instructions
# https://github.com/docker/scan-cli-plugin#on-linux
mkdir -p ~/.docker/cli-plugins
curl https://github.com/docker/scan-cli-plugin/releases/latest/download/docker-scan_linux_amd64 -L -s -S -o ~/.docker/cli-plugins/docker-scan
chmod +x ~/.docker/cli-plugins/docker-scan

echo "#{DockerHub.Password}" | docker login --username "#{DockerHub.Username}" --password-stdin 2>&1

docker scan --accept-license ${image}:#{Octopus.Action[Frontend WebApp].Package[${frontend}].PackageVersion}