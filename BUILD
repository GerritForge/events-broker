package(default_visibility = ["//visibility:public"])

load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_DEPS_NEVERLINK",
    "PLUGIN_TEST_DEPS",
)

java_library(
    name = "events-broker",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = PLUGIN_DEPS_NEVERLINK,
)

junit_tests(
    name = "events_broker_tests",
    size = "small",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["events-broker"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":events-broker",
    ],
)
