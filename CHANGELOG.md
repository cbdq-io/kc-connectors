# Changelog


## Unreleased

### Fix

* CVE-2025-55163. [Ben Dalling]

### Build

* Bump base image (confluentinc/cp-kafka-connect) from 7.9.3 to 7.9.4. [Ben Dalling]

* Bump github/codeql-action from 3 to 4. [dependabot[bot]]

  Bumps [github/codeql-action](https://github.com/github/codeql-action) from 3 to 4.
  - [Release notes](https://github.com/github/codeql-action/releases)
  - [Changelog](https://github.com/github/codeql-action/blob/main/CHANGELOG.md)
  - [Commits](https://github.com/github/codeql-action/compare/v3...v4)

  ---
  updated-dependencies:
  - dependency-name: github/codeql-action
    dependency-version: '4'
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...


## 0.6.2 (2025-10-07)

### Fix

* CVE-2025-5914. [Ben Dalling]

* CVE-2025-48734. [Ben Dalling]

* CVE-2025-47273. [Ben Dalling]

* CVE-2025-5115. [Ben Dalling]

### Build

* Release/0.6.2. [Ben Dalling]

* Bump peter-evans/create-issue-from-file from 5 to 6. [dependabot[bot]]

  Bumps [peter-evans/create-issue-from-file](https://github.com/peter-evans/create-issue-from-file) from 5 to 6.
  - [Release notes](https://github.com/peter-evans/create-issue-from-file/releases)
  - [Commits](https://github.com/peter-evans/create-issue-from-file/compare/v5...v6)

  ---
  updated-dependencies:
  - dependency-name: peter-evans/create-issue-from-file
    dependency-version: '6'
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Bump actions/setup-python from 5 to 6. [dependabot[bot]]

  Bumps [actions/setup-python](https://github.com/actions/setup-python) from 5 to 6.
  - [Release notes](https://github.com/actions/setup-python/releases)
  - [Commits](https://github.com/actions/setup-python/compare/v5...v6)

  ---
  updated-dependencies:
  - dependency-name: actions/setup-python
    dependency-version: '6'
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Bump aquasecurity/trivy-action from 0.33.0 to 0.33.1. [dependabot[bot]]

  Bumps [aquasecurity/trivy-action](https://github.com/aquasecurity/trivy-action) from 0.33.0 to 0.33.1.
  - [Release notes](https://github.com/aquasecurity/trivy-action/releases)
  - [Commits](https://github.com/aquasecurity/trivy-action/compare/0.33.0...0.33.1)

  ---
  updated-dependencies:
  - dependency-name: aquasecurity/trivy-action
    dependency-version: 0.33.1
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...

* Bump aquasecurity/trivy-action from 0.32.0 to 0.33.0. [dependabot[bot]]

  Bumps [aquasecurity/trivy-action](https://github.com/aquasecurity/trivy-action) from 0.32.0 to 0.33.0.
  - [Release notes](https://github.com/aquasecurity/trivy-action/releases)
  - [Commits](https://github.com/aquasecurity/trivy-action/compare/0.32.0...0.33.0)

  ---
  updated-dependencies:
  - dependency-name: aquasecurity/trivy-action
    dependency-version: 0.33.0
    dependency-type: direct:production
    update-type: version-update:semver-minor
  ...

* Bump actions/checkout from 4 to 5. [dependabot[bot]]

  Bumps [actions/checkout](https://github.com/actions/checkout) from 4 to 5.
  - [Release notes](https://github.com/actions/checkout/releases)
  - [Changelog](https://github.com/actions/checkout/blob/main/CHANGELOG.md)
  - [Commits](https://github.com/actions/checkout/compare/v4...v5)

  ---
  updated-dependencies:
  - dependency-name: actions/checkout
    dependency-version: '5'
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

### Other

* Build(pkg) update the base image to confluentinc/cp-kafka-connect:7.9.3. [Ben Dalling]


## 0.6.1 (2025-09-18)

### Fix

* Add CVE-2025-5115 & CVE-2025-55163 to allowed vulnerability list. [Ben Dalling]

* Bump ServiceBus version from 7.17.13 to 7.17.14. [Ben Dalling]

* Bump prometheus-metrics-core from 1.3.10 to 1.4.1. [Ben Dalling]

* Upgrade libarchive (CVE-2025-5914) [Ben Dalling]

### Build

* Hotfix/0.6.1. [Ben Dalling]


## 0.6.0 (2025-08-12)

### Features

* Allow the Prometheus port for kccinit to be configured. [Ben Dalling]

### Build

* Release/0.6.0. [Ben Dalling]

* Bump Python Prometheus client from 0.21.1 to 0.22.1. [Ben Dalling]


## 0.5.2 (2025-08-01)

### Fix

* Resolve CVE-2025-49794, CVE-2025-49796, CVE-2025-7425, CVE-2024-12718, CVE-2025-4138, CVE-2025-4517, CVE-2024-12718, CVE-2025-4138, CVE-2025-4517 and CVE-2025-6965. CVE-2025-49794, CVE-2025-49796, CVE-2025-7425, CVE-2024-12718, CVE-2025-4138, CVE-2025-4517, CVE-2024-12718, CVE-2025-4138, CVE-2025-4517 and CVE-2025-6965. [Ben Dalling]

* Refresh of Java dependency versions. [Ben Dalling]

### Build

* Release/0.5.2. [Ben Dalling]

* Upgrade libxml2, platform-python and sqlite-libs. [Ben Dalling]

* Bump aquasecurity/trivy-action from 0.31.0 to 0.32.0. [dependabot[bot]]

  Bumps [aquasecurity/trivy-action](https://github.com/aquasecurity/trivy-action) from 0.31.0 to 0.32.0.
  - [Release notes](https://github.com/aquasecurity/trivy-action/releases)
  - [Commits](https://github.com/aquasecurity/trivy-action/compare/0.31.0...0.32.0)

  ---
  updated-dependencies:
  - dependency-name: aquasecurity/trivy-action
    dependency-version: 0.32.0
    dependency-type: direct:production
    update-type: version-update:semver-minor
  ...

### Continuous Integration

* Fix the CD pipelines. [Ben Dalling]

### Refactor

* Change the Maven project layout. [Ben Dalling]


## 0.5.1 (2025-07-01)

### Fix

* CVE-2024-57699 and CVE-2025-24970 are no longer present. [Ben Dalling]

### Build

* Release/0.5.1. [Ben Dalling]

* Update build dependancies. [Ben Dalling]

* Bump underlying image confluentinc/cp-kafka-connect from 7.9.1 to 7.9.2. [Ben Dalling]

* Bump urllib3 from 2.3.0 to 2.5.0. [dependabot[bot]]

  Bumps [urllib3](https://github.com/urllib3/urllib3) from 2.3.0 to 2.5.0.
  - [Release notes](https://github.com/urllib3/urllib3/releases)
  - [Changelog](https://github.com/urllib3/urllib3/blob/main/CHANGES.rst)
  - [Commits](https://github.com/urllib3/urllib3/compare/2.3.0...2.5.0)

  ---
  updated-dependencies:
  - dependency-name: urllib3
    dependency-version: 2.5.0
    dependency-type: direct:production
  ...

* Bump requests from 2.32.3 to 2.32.4. [dependabot[bot]]

  Bumps [requests](https://github.com/psf/requests) from 2.32.3 to 2.32.4.
  - [Release notes](https://github.com/psf/requests/releases)
  - [Changelog](https://github.com/psf/requests/blob/main/HISTORY.md)
  - [Commits](https://github.com/psf/requests/compare/v2.32.3...v2.32.4)

  ---
  updated-dependencies:
  - dependency-name: requests
    dependency-version: 2.32.4
    dependency-type: direct:production
  ...

* Bump pycares from 4.5.0 to 4.9.0. [dependabot[bot]]

  Bumps [pycares](https://github.com/saghul/pycares) from 4.5.0 to 4.9.0.
  - [Release notes](https://github.com/saghul/pycares/releases)
  - [Changelog](https://github.com/saghul/pycares/blob/master/ChangeLog)
  - [Commits](https://github.com/saghul/pycares/compare/v4.5.0...v4.9.0)

  ---
  updated-dependencies:
  - dependency-name: pycares
    dependency-version: 4.9.0
    dependency-type: direct:production
  ...

* Bump google/osv-scanner-action from 2.0.2 to 2.0.3. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 2.0.2 to 2.0.3.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v2.0.2...v2.0.3)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-version: 2.0.3
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...

* Bump aquasecurity/trivy-action from 0.30.0 to 0.31.0. [dependabot[bot]]

  Bumps [aquasecurity/trivy-action](https://github.com/aquasecurity/trivy-action) from 0.30.0 to 0.31.0.
  - [Release notes](https://github.com/aquasecurity/trivy-action/releases)
  - [Commits](https://github.com/aquasecurity/trivy-action/compare/0.30.0...0.31.0)

  ---
  updated-dependencies:
  - dependency-name: aquasecurity/trivy-action
    dependency-version: 0.31.0
    dependency-type: direct:production
    update-type: version-update:semver-minor
  ...

### Continuous Integration

* Refactor GitHub workflows. [Ben Dalling]

* Updated periodic-trivy-scan.yml with github.repository variable. [James Loughlin]

* Update the periodic Trivy scan. [Ben Dalling]

### Tests

* Update Python packages. [Ben Dalling]


## 0.5.0 (2025-05-30)

### Features

* Add the kcstatus convenience script. [Ben Dalling]

### Fix

* Report when a connector recovers from a non-running state. [Ben Dalling]

* Clean up after dnf operations. [Ben Dalling]

* Add CVE-2025-47273 and CVE-2025-48734 vulnerabilities to the allowed list. [Ben Dalling]

* CVE-2024-56171 is no longer present in the image. [Ben Dalling]

* CVE-2024-53990 is no longer present in the image. [Ben Dalling]

* CVE-2024-52046 no longer present in the image. [Ben Dalling]

### Build

* Release/0.5.0. [Ben Dalling]

* Migrate to a containerised change log generator. [Ben Dalling]

* Bump tag of confluentinc/cp-kafka-connect from 7.9.0 to 7.9.1. [Ben Dalling]


## 0.4.2 (2025-05-15)

### Fix

* Remove the large.message.threshold.bytes config option. [Ben Dalling]

* Send messages individually if too large for a batch send. [Ben Dalling]

* More robust testing around the creation of a batch of messags for sending. [Ben Dalling]


## 0.4.1 (2025-05-13)

### Fix

* More robust checking to avoid sending empty batches and ensure an unconfugured sender will throw an exception. [Ben Dalling]


## 0.4.0 (2025-05-09)

### Features

* Allow the user to specify exponential back off during retry. [Ben Dalling]

* Add sidecar option to kccinit. [Ben Dalling]

### Fix

* Batch the sending of messages. [Ben Dalling]


## 0.3.5 (2025-05-06)

### Fix

* Migrate the SDK from Qpid JMS to Azure Service Bus. [Ben Dalling]

### Build

* Bump google/osv-scanner-action from 2.0.1 to 2.0.2. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 2.0.1 to 2.0.2.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v2.0.1...v2.0.2)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-version: 2.0.2
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...

* Bump peter-evans/create-issue-from-file from 4 to 5. [dependabot[bot]]

  Bumps [peter-evans/create-issue-from-file](https://github.com/peter-evans/create-issue-from-file) from 4 to 5.
  - [Release notes](https://github.com/peter-evans/create-issue-from-file/releases)
  - [Commits](https://github.com/peter-evans/create-issue-from-file/compare/v4...v5)

  ---
  updated-dependencies:
  - dependency-name: peter-evans/create-issue-from-file
    dependency-version: '5'
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Bump github/codeql-action from 2 to 3. [dependabot[bot]]

  Bumps [github/codeql-action](https://github.com/github/codeql-action) from 2 to 3.
  - [Release notes](https://github.com/github/codeql-action/releases)
  - [Changelog](https://github.com/github/codeql-action/blob/main/CHANGELOG.md)
  - [Commits](https://github.com/github/codeql-action/compare/v2...v3)

  ---
  updated-dependencies:
  - dependency-name: github/codeql-action
    dependency-version: '3'
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...


## 0.3.4 (2025-04-15)

### Fix

* Reduce complexity of the processRecord method. Also ensures at-least-once delivery. [Ben Dalling]

* Periodic Trivy Scans. [Ben Dalling]


## 0.3.3 (2025-04-14)

### Fix

* Really resolve CVE-2025-27363. [Ben Dalling]

* Periodic Trivy Scans. [Ben Dalling]

### Build

* Bump google/osv-scanner-action from 2.0.0 to 2.0.1. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 2.0.0 to 2.0.1.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v2.0.0...v2.0.1)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-version: 2.0.1
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...

* Bump google/osv-scanner-action from 1.9.2 to 2.0.0. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 1.9.2 to 2.0.0.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v1.9.2...v2.0.0)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Bump aquasecurity/trivy-action from 0.29.0 to 0.30.0. [dependabot[bot]]

  Bumps [aquasecurity/trivy-action](https://github.com/aquasecurity/trivy-action) from 0.29.0 to 0.30.0.
  - [Release notes](https://github.com/aquasecurity/trivy-action/releases)
  - [Commits](https://github.com/aquasecurity/trivy-action/compare/0.29.0...0.30.0)

  ---
  updated-dependencies:
  - dependency-name: aquasecurity/trivy-action
    dependency-type: direct:production
    update-type: version-update:semver-minor
  ...


## 0.3.2 (2025-04-14)

### Fix

* Resolve CVE-2025-27363. [Ben Dalling]


## 0.3.1 (2025-03-18)

### Fix

* Handle messages with no key. [Ben Dalling]


## 0.3.0 (2025-03-16)

### Features

* Add the set.kafka.partition.as.session.id. Also ensure that the Kafka key and partition are set as properties on the message. [Ben Dalling]

### Fix

* Add CVE-2024-56171 & CVE-2025-24928 to the Trivy ignore list. [Ben Dalling]

* Correct race condition when starting the Prometheus client. [Ben Dalling]

* Resolve CVE-2025-27516. [Ben Dalling]


## 0.2.10 (2025-02-20)

### Fix

* Ensure that when connections are recovered, the correct destination topic name is used. [Ben Dalling]


## 0.2.9 (2025-02-19)

### Fix

* CVE-2024-52046 and CVE-2024-53990 no longer present. [Ben Dalling]

* Bump confluentinc/cp-kafka-connect from 7.8.0 to 7.9.0. [Ben Dalling]

* CVE-2024-47535 no longer present. [Ben Dalling]

* Give more detail at INFO level of logs when a topic rename happens.' [Ben Dalling]

* Bump qpid-jms-client.version from 2.6.1 to 2.7.0. [Ben Dalling]

* Make kccinit more robust against exceptions. [Ben Dalling]


## 0.2.8 (2025-02-10)

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

### Fix

* Ensure passwords ending with "=" are not truncated. [Ben Dalling]

* Ensure connection string parser honours UseDevelopmentEmulator. [Ben Dalling]

### Changes

* Add CVE-2024-52046 and CVE-2024-53990 to the allowed vulnerabilities. [Ben Dalling]

### Build

* Bump jinja2 from 3.1.4 to 3.1.5. [dependabot[bot]]

  Bumps [jinja2](https://github.com/pallets/jinja) from 3.1.4 to 3.1.5.
  - [Release notes](https://github.com/pallets/jinja/releases)
  - [Changelog](https://github.com/pallets/jinja/blob/main/CHANGES.rst)
  - [Commits](https://github.com/pallets/jinja/compare/3.1.4...3.1.5)

  ---
  updated-dependencies:
  - dependency-name: jinja2
    dependency-type: direct:production
  ...

* Bump google/osv-scanner-action from 1.9.1 to 1.9.2. [dependabot[bot]]

  Bumps [google/osv-scanner-action](https://github.com/google/osv-scanner-action) from 1.9.1 to 1.9.2.
  - [Release notes](https://github.com/google/osv-scanner-action/releases)
  - [Commits](https://github.com/google/osv-scanner-action/compare/v1.9.1...v1.9.2)

  ---
  updated-dependencies:
  - dependency-name: google/osv-scanner-action
    dependency-type: direct:production
    update-type: version-update:semver-patch
  ...

* Bump google/osv-scanner-action from 1.9.0 to 1.9.1. [dependabot[bot]]

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

### Fix

* Resolve CVE-2024-47554, CVE-2024-47561 and CVE-2024-7254. [Ben Dalling]

### New

* Add the connector initialiser script to the image. [Ben Dalling]

* Add Prometheus metrics. [Ben Dalling]

* Implement topic.rename.format. [Ben Dalling]

### Changes

* Bump qpid-jms-client from 1.12.0 to 2.0.16 (Jakarta). [Ben Dalling]

* Upgrade Kafka Connect API version from 2.8.2 to 3.8.1. [Ben Dalling]

### Build

* Bump actions/setup-python from 4 to 5. [dependabot[bot]]

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

### Fix

* Add health check to the container. [Ben Dalling]

* Resolve CVE-2024-10963. [Ben Dalling]

### New

* Create SECURITY.md. [Ben Dalling]

  Some more work towards #24.

### Changes

* Bump Kafka Connect from 7.7.1 to 7.8.0. [Ben Dalling]

### Build

* Bump aquasecurity/trivy-action from 0.28.0 to 0.29.0. [dependabot[bot]]

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

### Fix

* CVE-2024-3596. [Ben Dalling]

* Ensure the connector is more robust in attempting to reconnect on losing connection. [Ben Dalling]

* Migrate to the Qpid JMS SDK as it supports ASB and ActiveMQ/Artemis as an emulator. [Ben Dalling]

### New

* Add a code of conduct document. [Ben Dalling]

* Add a contributing guide. [Ben Dalling]

* Add some basic documentation. [Ben Dalling]

* Pulumi scripts to deploy an Azure Service Bus namespace and test topics/subscriptions. [Ben Dalling]

* Initial prototype. [Ben Dalling]

### Build

* Bump docker/setup-buildx-action from 2 to 3. [dependabot[bot]]

  Bumps [docker/setup-buildx-action](https://github.com/docker/setup-buildx-action) from 2 to 3.
  - [Release notes](https://github.com/docker/setup-buildx-action/releases)
  - [Commits](https://github.com/docker/setup-buildx-action/compare/v2...v3)

  ---
  updated-dependencies:
  - dependency-name: docker/setup-buildx-action
    dependency-type: direct:production
    update-type: version-update:semver-major
  ...

* Bump actions/checkout from 3 to 4. [dependabot[bot]]

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


