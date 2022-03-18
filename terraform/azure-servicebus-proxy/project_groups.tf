resource "octopusdeploy_project_group" "appbuilder_github_oauth_project_group" {
  description  = "The GitHub Actions Azure Service Bus Proxy."
  name         = "GitHub Actions Azure Service Bus Proxy"
}

output "appbuilder_github_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
}