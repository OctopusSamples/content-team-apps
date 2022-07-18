if [[ "#{Octopus.Action[Display the Ingress URL].Output.DNSName}" == "null" ]]
then
  echo "The previous step failed to find the ingress hostname. This means we are unable to test the service."
  exit 1
fi

# Load balancers can take a minute or so before their DNS is propagated.
# A status code of 000 means curl could not resolve the DNS name, so we wait for a bit until DNS is updated.
for i in {1..30}
do
    CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" "http://#{Octopus.Action[Display the Ingress URL].Output.DNSName}/index.html")
    if [[ "${CODE}" == "200" ]]
    then
      break
    fi
    echo "Waiting for DNS name to be resolvable and for service to respond"
    sleep 10
done

echo "response code: ${CODE}"
if [[ "${CODE}" == "200" ]]
then
  echo "success"
  exit 0
else
  echo "error"
  exit 1
fi