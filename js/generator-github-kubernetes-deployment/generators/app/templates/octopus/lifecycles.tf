data "octopusdeploy_lifecycles" "application_lifecycle" {
  partial_name = "Application"
  skip         = 5
  take         = 100
}

data "octopusdeploy_lifecycles" "infrastructure_lifecycle" {
  partial_name = "Infrastructure"
  skip         = 5
  take         = 100
}

data "octopusdeploy_lifecycles" "administration_lifecycle" {
  partial_name = "Administration"
  skip         = 5
  take         = 100
}