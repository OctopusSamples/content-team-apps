resource "octopusdeploy_lifecycle" "application_lifecycle" {
  description = "The application lifecycle. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name        = "Application"

  release_retention_policy {
    quantity_to_keep    = 1
    should_keep_forever = true
    unit                = "Days"
  }

  tentacle_retention_policy {
    quantity_to_keep    = 30
    should_keep_forever = false
    unit                = "Items"
  }

  phase {
    automatic_deployment_targets = []
    optional_deployment_targets  = [octopusdeploy_environment.development_environment.id]
    name                         = octopusdeploy_environment.development_environment.name

    release_retention_policy {
      quantity_to_keep    = 1
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }

  phase {
    automatic_deployment_targets = [octopusdeploy_environment.development_security_environment.id]
    name                         = octopusdeploy_environment.development_security_environment.name

    release_retention_policy {
      quantity_to_keep    = 1
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }

  phase {
    automatic_deployment_targets = []
    optional_deployment_targets  = [octopusdeploy_environment.production_environment.id]
    name                         = octopusdeploy_environment.production_environment.name

    release_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }

  phase {
    automatic_deployment_targets = [octopusdeploy_environment.production_security_environment.id]
    name                         = octopusdeploy_environment.production_security_environment.name

    release_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }
}

output "application_lifecycle_id" {
  value = octopusdeploy_lifecycle.application_lifecycle.id
}

resource "octopusdeploy_lifecycle" "infrastructure_lifecycle" {
  description = "The application lifecycle."
  name        = "Infrastructure"

  release_retention_policy {
    quantity_to_keep    = 1
    should_keep_forever = true
    unit                = "Days"
  }

  tentacle_retention_policy {
    quantity_to_keep    = 30
    should_keep_forever = false
    unit                = "Items"
  }

  phase {
    automatic_deployment_targets = []
    optional_deployment_targets  = [octopusdeploy_environment.development_environment.id]
    name                         = octopusdeploy_environment.development_environment.name

    release_retention_policy {
      quantity_to_keep    = 1
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }

  phase {
    automatic_deployment_targets = []
    optional_deployment_targets  = [octopusdeploy_environment.production_environment.id]
    name                         = octopusdeploy_environment.production_environment.name

    release_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }
}

output "infrastructure_lifecycle_id" {
  value = octopusdeploy_lifecycle.infrastructure_lifecycle.id
}

resource "octopusdeploy_lifecycle" "administration_lifecycle" {
  description = "The administration lifecycle."
  name        = "Administration"

  release_retention_policy {
    quantity_to_keep    = 1
    should_keep_forever = true
    unit                = "Days"
  }

  tentacle_retention_policy {
    quantity_to_keep    = 30
    should_keep_forever = false
    unit                = "Items"
  }

  phase {
    automatic_deployment_targets = []
    optional_deployment_targets  = [octopusdeploy_environment.administration_environment.id]
    name                         = octopusdeploy_environment.administration_environment.name

    release_retention_policy {
      quantity_to_keep    = 1
      should_keep_forever = true
      unit                = "Days"
    }

    tentacle_retention_policy {
      quantity_to_keep    = 30
      should_keep_forever = false
      unit                = "Items"
    }
  }
}

output "administration_lifecycle_id" {
  value = octopusdeploy_lifecycle.administration_lifecycle.id
}