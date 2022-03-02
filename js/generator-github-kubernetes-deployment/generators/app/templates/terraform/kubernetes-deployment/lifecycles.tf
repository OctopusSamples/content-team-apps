data "octopusdeploy_lifecycles" "application_lifecycle" {
  ids          = []
  partial_name = "Application"
  skip         = 5
  take         = 100
}