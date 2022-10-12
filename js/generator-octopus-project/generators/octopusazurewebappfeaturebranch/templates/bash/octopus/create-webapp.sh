NOW=$(date +%s)
CREATED=$${NOW}
FEATUREBRANCH="#{FixedFeatureBranch}"

# Test if the resource group exists
EXISTING_RG=$(az group list --query "[?name=='$${FEATUREBRANCH}']")
echo "##octopus[stdout-verbose]"
echo "$${EXISTING_RG}"
echo "##octopus[stdout-default]"
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

EXISTING_SP=$(az appservice plan list --resource-group "$${FEATUREBRANCH}" --query "[?name=='$${FEATUREBRANCH}sp']")
echo "##octopus[stdout-verbose]"
echo "$${EXISTING_SP}"
echo "##octopus[stdout-default]"
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

EXISTING_WA=$(az webapp list --resource-group "$${FEATUREBRANCH}" --query "[?name=='$${FEATUREBRANCH}wa']")
echo "##octopus[stdout-verbose]"
echo "$${EXISTING_WA}"
echo "##octopus[stdout-default]"
echo "$${EXISTING_WA}" | jq -e 'select(.[] | length > 0)' > /dev/null
if [[ $? != "0" ]]
then
	echo "Creating new web app"
	az webapp create \
      --resource-group "$${FEATUREBRANCH}" \
      --plan "$${FEATUREBRANCH}sp" \
      --name "$${FEATUREBRANCH}wa" \
      --deployment-container-image-name "nginx" \
      --tags \
      	octopus-environment="#{Octopus.Environment.Name}" \
        octopus-role="WebApp" \
        octopus-space="#{Octopus.Space.Name}" \
        octopus-project="#{Octopus.Project.Name}" \
        octopus-feature-branch="$${FEATUREBRANCH}"
else
	echo "Web App already exists"
fi

