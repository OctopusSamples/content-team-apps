resource "octopusdeploy_project_group" "backend_project_group" {
  description  = "The backend service."
  name         = "Kubernetes Backend"
}

output "backend_project_group_id" {
  value = octopusdeploy_project_group.backend_project_group.id
}

resource "octopusdeploy_project_group" "frontend_project_group" {
  description  = "The frontend UI."
  name         = "Kubernetes Frontend"
}

output "frontend_project_group_id" {
  value = octopusdeploy_project_group.frontend_project_group.id
}