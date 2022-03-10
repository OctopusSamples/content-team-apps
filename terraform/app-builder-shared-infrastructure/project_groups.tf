resource "octopusdeploy_project_group" "appbuilder_project_group" {
  description  = "The App Builder Shared Infrastructure."
  name         = "App Builder Shared Infrastructure"
}

output "appbuilder_frontend_project_group_id" {
  value = octopusdeploy_project_group.appbuilder_project_group.id
}