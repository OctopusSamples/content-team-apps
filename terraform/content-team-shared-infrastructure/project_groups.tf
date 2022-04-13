resource "octopusdeploy_project_group" "project_group" {
  description  = "The Content Team Shared Infrastructure."
  name         = "Content Team Shared Infrastructure"
}

output "appbuilder_frontend_project_group_id" {
  value = octopusdeploy_project_group.project_group.id
}