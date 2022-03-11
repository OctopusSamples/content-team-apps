resource "octopusdeploy_project_group" "appbuilder_octopus_oauth_project_group" {
  description  = "The App Builder Octopus OAuth Proxy."
  name         = "App Builder Octopus OAuth Proxy"
}

output "appbuilder_octopus_oauth_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_octopus_oauth_project_group.id
}