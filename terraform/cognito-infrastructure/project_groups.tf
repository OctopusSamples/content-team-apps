resource "octopusdeploy_project_group" "project_group" {
  description  = "The Cognito Infrastructure."
  name         = "Cognito Infrastructure"
}

output "appbuilder_frontend_project_group_id" {
  value = octopusdeploy_project_group.project_group.id
}