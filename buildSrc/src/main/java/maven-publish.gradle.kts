plugins {
  id("com.vanniktech.maven.publish")
}

group = GROUP
version = VERSION_NAME

mavenPublish {
  sonatypeHost = "S01"
}
