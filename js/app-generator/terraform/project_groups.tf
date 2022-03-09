resource "octopusdeploy_project_group" "appbuilder_frontend_project_group" {
  description  = "The App Builder Frontend."
  name         = "App Builder Frontend"
}

output "appbuilder_frontend_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_frontend_project_group.id
}