NOW=$(date +%s)
CREATED=$${NOW}
FEATUREBRANCH="#{FixedFeatureBranch}"

# Test if the resource group exists
EXISTING_RG=$(az group list --query "[?name=='$${FEATUREBRANCH}']")
# jq -e returns a non-zero exit code if the last output value was false or null
echo $${EXISTING_RG} | jq -e 'select(.[] | length > 0)' > /dev/null

if [[ $? != "0" ]]
then
	echo "Creating new resource group"
	az group create -l westus -n "$${FEATUREBRANCH}" --tags LifeTimeInDays=7 Created=$${NOW}
else
	echo "Resource group already exists"
	# TAGS=$(az tag list --resource-id "/subscriptions/#{Octopus.Action.Azure.SubscriptionId}/resourcegroups/$${FEATUREBRANCH}")
fi

EXISTING_SP=$(az appservice plan list --resource-group "$${FEATUREBRANCH}")
echo "$${EXISTING_SP}" | jq -e 'select(.[] | length > 0)' > /dev/null
if [[ $? != "0" ]]
then
	echo "Creating new service plan"
	az appservice plan create \
      --sku B1 \
      --name "$${FEATUREBRANCH}sp" \
      --resource-group "$${FEATUREBRANCH}" \
      --is-linux
else
	echo "Service plan already exists"
fi

EXISTING_WA=$(az webapp list --resource-group "$${FEATUREBRANCH}")
echo "$${EXISTING_WA}" | jq -e 'select(.[] | length > 0)' > /dev/null
if [[ $? != "0" ]]
then
	echo "Creating new web app"
	az webapp create \
      --resource-group "$${FEATUREBRANCH}" \
      --plan "$${FEATUREBRANCH}sp" \
      --name "$${FEATUREBRANCH}wa" \
      --deployment-container-image-name "#{Octopus.Action[Deploy WebApp].Package.PackageId}:#{Octopus.Action[Deploy WebApp].Package.PackageVersion}" \
      --tags \
      	octopus-environment="#{Octopus.Environment.Name}" \
        octopus-role=Octofront \
        octopus-space="#{Octopus.Space.Name}" \
        octopus-project="#{Octopus.Project.Name}" \
        octopus-feature-branch="$${FEATUREBRANCH}"
else
	echo "Web App already exists"
fi

