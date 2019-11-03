load("//tools/maven:package.bzl", "maven_package")
load("//modules/events-broker:version.bzl", "EVENT_BROKER_VERSION")

MAVEN_REPOSITORY = "sonatype-nexus-staging"

URL = "https://oss.sonatype.org/content/repositories/snapshots" if EVENT_BROKER_VERSION.endswith("-SNAPSHOT") else "https://oss.sonatype.org/service/local/staging/deploy/maven2"

maven_package(
    src = {
        "events-broker": "//modules/events-broker:libevents-broker-src.jar",
    },
    doc = {
        "events-broker": "//modules/events-broker:events-broker-javadoc",
    },
    jar = {
        "events-broker": "//modules/events-broker:events-broker",
    },
    repository = "sonatype-nexus-staging",
    url = "gs://gerrit-maven",
    version = EVENT_BROKER_VERSION,
)
