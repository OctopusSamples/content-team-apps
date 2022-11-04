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
    optional_deployment_targets  = [var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].id : octopusdeploy_environment.development_environment[0].id]
    name                         = var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].name : octopusdeploy_environment.development_environment[0].name

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
    automatic_deployment_targets = [var.octopus_development_security_environment_exists ? data.octopusdeploy_environments.development_security.environments[0].id : octopusdeploy_environment.development_security_environment[0].id]
    name                         = var.octopus_development_security_environment_exists ? data.octopusdeploy_environments.development_security.environments[0].name : octopusdeploy_environment.development_security_environment[0].name
    is_optional_phase            = true

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
    optional_deployment_targets  = [var.octopus_production_app_environment_exists ? data.octopusdeploy_environments.production.environments[0].id : octopusdeploy_environment.production_environment[0].id]
    name                         = var.octopus_production_app_environment_exists ? data.octopusdeploy_environments.production.environments[0].name : octopusdeploy_environment.production_environment[0].name

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
    automatic_deployment_targets = [var.octopus_production_security_environment_exists ? data.octopusdeploy_environments.production_security.environments[0].id : octopusdeploy_environment.production_security_environment[0].id]
    name                         = var.octopus_production_security_environment_exists ? data.octopusdeploy_environments.production_security.environments[0].name : octopusdeploy_environment.production_security_environment[0].name
    is_optional_phase            = true

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

resource "octopusdeploy_lifecycle" "simple_lifecycle" {
  description = "The simple lifecycle."
  name        = "Simple"

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
    optional_deployment_targets  = [var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].id : octopusdeploy_environment.development_environment[0].id]
    name                         = var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].name : octopusdeploy_environment.development_environment[0].name

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
    optional_deployment_targets  = [var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.production.environments[0].id : octopusdeploy_environment.production_environment[0].id]
    name                         = var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].name : octopusdeploy_environment.production_environment[0].name

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

output "simple_lifecycle_id" {
  value = octopusdeploy_lifecycle.simple_lifecycle.id
}

resource "octopusdeploy_lifecycle" "productiononly_lifecycle" {
  description = "The production only lifecycle."
  name        = "Production Only"

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
    optional_deployment_targets  = [var.octopus_production_app_environment_exists ? data.octopusdeploy_environments.production.environments[0].id : octopusdeploy_environment.production_environment[0].id]
    name                         = var.octopus_production_app_environment_exists ? data.octopusdeploy_environments.production.environments[0].name : octopusdeploy_environment.production_environment[0].name

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

output "productiononly_lifecycle_id" {
  value = octopusdeploy_lifecycle.productiononly_lifecycle.id
}

resource "octopusdeploy_lifecycle" "developmentonly_lifecycle" {
  description = "The development only lifecycle."
  name        = "Development Only"

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
    optional_deployment_targets  = [var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].id : octopusdeploy_environment.development_environment[0].id]
    name                         = var.octopus_development_app_environment_exists ? data.octopusdeploy_environments.development.environments[0].name : octopusdeploy_environment.development_environment[0].name

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

output "developmentonly_lifecycle_id" {
  value = octopusdeploy_lifecycle.developmentonly_lifecycle.id
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
    optional_deployment_targets  = [var.octopus_administration_environment_exists ? data.octopusdeploy_environments.administration.environments[0].id : octopusdeploy_environment.administration_environment[0].id]
    name                         = var.octopus_administration_environment_exists ? data.octopusdeploy_environments.administration.environments[0].name : octopusdeploy_environment.administration_environment[0].name

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