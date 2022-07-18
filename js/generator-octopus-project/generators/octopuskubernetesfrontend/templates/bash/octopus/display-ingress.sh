# It can take a while for a load balancer to be provisioned
for i in {1..60}
do
    DNSNAME=$(kubectl get ingress ${frontend_ingress_name} -o json | jq -r '.status.loadBalancer.ingress[0].hostname')
    if [[ "$${DNSNAME}" != "null" ]]
    then
      break
    fi
    echo "Waiting for Ingress hostname"
    sleep 10
done
set_octopusvariable "DNSName" "$${DNSNAME}"

if [[ "$${DNSNAME}" != "null" ]]
then
  write_highlight "Open [http://$DNSNAME/index.html](http://$DNSNAME/index.html) to view the web app."
fi