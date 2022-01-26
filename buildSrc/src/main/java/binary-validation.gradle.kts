/*
 * We use JetBrain's Kotlin Binary Compatibility Validator to track changes to our public binary
 * APIs.
 * When making a change that results in a public ABI change, the apiCheck task will fail. When this
 * happens, run ./gradlew apiDump to generate updated *.api files, and add those to your commit.
 * See https://github.com/Kotlin/binary-compatibility-validator
 */

plugins {
  id("binary-compatibility-validator")
}

apiValidation {
  // Ignore all sample projects, since they're not part of our API.
  // Only leaf project name is valid configuration, and every project must be individually ignored.
  // See https://github.com/Kotlin/binary-compatibility-validator/issues/3
  ignoredProjects += project("samples").subprojects.collect { it.name }
}
