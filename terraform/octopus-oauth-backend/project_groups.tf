resource "octopusdeploy_project_group" "appbuilder_github_oauth_project_group" {
  description  = "The App Builder GitHub OAuth Proxy."
  name         = "App Builder GitHub OAuth Proxy"
}

output "appbuilder_github_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
}