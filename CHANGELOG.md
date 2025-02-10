# Changelog


## 0.2.8

### Fix

* Resolve CVE-2024-1488. [Ben Dalling]

* Use HTTP PUT rather than POST to initialise the connector as it is more idempotent. [Ben Dalling]


## 0.2.7 (2025-01-29)

### Fix

* Make containers both linux/amd64 and linux/arm64 (IV). [Ben Dalling]


## 0.2.6 (2025-01-29)

### Fix

* Make containers both linux/amd64 and linux/arm64 (III). [Ben Dalling]


## 0.2.5 (2025-01-29)

### Fix

* Need buildx for mutil-arch builds. [Ben Dalling]


## 0.2.4 (2025-01-29)

### Fix

* Make containers both linux/amd64 and linux/arm64. [Ben Dalling]


## 0.2.3 (2025-01-08)

### Fix

* Add bind-utils to the image. [Ben Dalling]

* Update copyright notice. [Ben Dalling]


## 0.2.2 (2025-01-04)

### Fix

* Refactor Prometheus metrics. [Ben Dalling]

* Ensure kccinit reports not successful posts to the Kafka Connect endpoint correctly. [Ben Dalling]


## 0.2.1 (2024-12-29)

### Changes

* Add CVE-2024-52046 and CVE-2024-53990 to the allowed vulnerabilities. [Ben Dalling]

### Fix

* Ensure passwords ending with "=" are not truncated. [Ben Dalling]

* Ensure connection string parser honours UseDevelopmentEmulator. [Ben Dalling]

### Other

* Build(deps): bump jinja2 from 3.1.4 to 3.1.5. [dependabot[bot]]

  Bumps [jinja2](https://github.com/pallets/jinja) from 3.1.4 to 3.1.5.
  - [Release notes](https://github.com/pallets/jinja/releases)
  - [Changelog](https://github.com/pallets/jinja/blob/main/CHANGES.rst)
  - [Commits](https://github.com/pallets/jinja/compare/3.1.4...3.1.5)

  ---
  updated-dependencies:
  - dependency-name: jinja2
    dependency-type: direct:production
  ...

* Build(deps): bump google/osv-scanner-action from 1.9.1 to 1.9.2. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 1.9.1 to 1.9.2.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v1.9.1...v1.9.2)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...

* Build(deps): bump google/osv-scanner-action from 1.9.0 to 1.9.1. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 1.9.0 to 1.9.1.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v1.9.0...v1.9.1)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...


## 0.2.0 (2024-12-10)

### New

* Add the connector initialiser script to the image. [Ben Dalling]

* Add Prometheus metrics. [Ben Dalling]

* Implement topic.rename.format. [Ben Dalling]

### Changes

* Bump qpid-jms-client from 1.12.0 to 2.0.16 (Jakarta). [Ben Dalling]

* Upgrade Kafka Connect API version from 2.8.2 to 3.8.1. [Ben Dalling]

### Fix

* Resolve CVE-2024-47554, CVE-2024-47561 and CVE-2024-7254. [Ben Dalling]

### Other

* Build(deps): bump actions/setup-python from 4 to 5. [dependabot[bot]]

  Bumps [actions/setup-python](https://github.com/actions/setup-python) from 4 to 5.
  - [Release notes](https://github.com/actions/setup-python/releases)
  - [Commits](https://github.com/actions/setup-python/compare/v4...v5)

  ---
  updated-dependencies:
  - dependency-name: actions/setup-python
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...


## 0.1.2 (2024-12-04)

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


