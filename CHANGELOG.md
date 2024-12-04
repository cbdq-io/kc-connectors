# Changelog


## 0.1.1

### New

* Create SECURITY.md. [Ben Dalling]

  Some more work towards #24.

### Changes

* Bump Kafka Connect from 7.7.1 to 7.8.0. [Ben Dalling]

### Fix

* Add health check to the container. [Ben Dalling]

* Resolve CVE-2024-10963. [Ben Dalling]

### Other

* Build(deps): bump aquasecurity/trivy-action from 0.28.0 to 0.29.0. [dependabot[bot]]

  Bumps [aquasecurity/trivy-action](https://github.com/aquasecurity/trivy-action) from 0.28.0 to 0.29.0.
  - [Release notes](https://github.com/aquasecurity/trivy-action/releases)
  - [Commits](https://github.com/aquasecurity/trivy-action/compare/0.28.0...0.29.0)

  ---
  updated-dependencies:
  - dependency-name: aquasecurity/trivy-action
    dependency-type: direct:production
    update-type: version-update:semver-minor
  ...


## 0.1.1 (2024-11-17)

### Fix

* Correct container publish pipeline. [Ben Dalling]


## 0.1.0 (2024-11-17)

### New

* Add a code of conduct document. [Ben Dalling]

* Add a contributing guide. [Ben Dalling]

* Add some basic documentation. [Ben Dalling]

* Pulumi scripts to deploy an Azure Service Bus namespace and test topics/subscriptions. [Ben Dalling]

* Initial prototype. [Ben Dalling]

### Fix

* CVE-2024-3596. [Ben Dalling]

* Ensure the connector is more robust in attempting to reconnect on losing connection. [Ben Dalling]

* Migrate to the Qpid JMS SDK as it supports ASB and ActiveMQ/Artemis as an emulator. [Ben Dalling]

### Other

* Build(deps): bump docker/setup-buildx-action from 2 to 3. [dependabot[bot]]

  Bumps [docker/setup-buildx-action](https://github.com/docker/setup-buildx-action) from 2 to 3.
  - [Release notes](https://github.com/docker/setup-buildx-action/releases)
  - [Commits](https://github.com/docker/setup-buildx-action/compare/v2...v3)

  ---
  updated-dependencies:
  - dependency-name: docker/setup-buildx-action
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Build(deps): bump actions/checkout from 3 to 4. [dependabot[bot]]

  Bumps [actions/checkout](https://github.com/actions/checkout) from 3 to 4.
  - [Release notes](https://github.com/actions/checkout/releases)
  - [Changelog](https://github.com/actions/checkout/blob/main/CHANGELOG.md)
  - [Commits](https://github.com/actions/checkout/compare/v3...v4)

  ---
  updated-dependencies:
  - dependency-name: actions/checkout
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Initial commit. [Ben Dalling]


