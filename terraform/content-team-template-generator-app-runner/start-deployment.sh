echo "Downloading Docker images"

echo "##octopus[stdout-verbose]"

docker pull amazon/aws-cli 2>&1

# Alias the docker run commands
shopt -s expand_aliases
alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

echo "##octopus[stdout-default]"

aws apprunner start-deployment --service-arn "#{Octopus.Action[Deploy App Runner Instance].Output.AwsOutputs[ServiceArn]}"

for i in {1..60}
do
  STATUS=$(aws apprunner describe-service --service-arn "#{Octopus.Action[Deploy App Runner Instance].Output.AwsOutputs[ServiceArn]}" | jq -r '.Service.Status')
  echo "App Runner status is $${STATUS}"
  if [[ $${STATUS} == "RUNNING" ]]; then
    break
  fi

  echo "Sleeping for 10 seconds"
  sleep 10
done