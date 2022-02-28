data "octopusdeploy_environments" "development_environment" {
  name         = "Development"
  partial_name = "Development"
  skip         = 5
  take         = 100
}

data "octopusdeploy_environments" "production_environment" {
  name         = "Production"
  partial_name = "Production"
  skip         = 5
  take         = 100
}

data "octopusdeploy_environments" "administration_environment" {
  name         = "Administration"
  partial_name = "Administration"
  skip         = 5
  take         = 100
}