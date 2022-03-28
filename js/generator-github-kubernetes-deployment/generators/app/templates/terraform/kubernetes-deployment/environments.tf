data "octopusdeploy_environments" "development_security" {
  name         = "Development (Security)"
  skip         = 0
  take         = 1
}

data "octopusdeploy_environments" "production_security" {
  name         = "Production (Security)"
  skip         = 0
  take         = 1
}