resource "octopusdeploy_project_group" "backend_project_group" {
  description  = "The backend service."
  name         = "Kubernetes Backend"
}

resource "octopusdeploy_project_group" "frontend_project_group" {
  description  = "The frontend UI."
  name         = "Kubernetes Frontend"
}