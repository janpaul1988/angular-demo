import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "3.0.1"
}

repositories {
  ivy {
    url = uri("https://nodejs.org/dist/")
    patternLayout {
      artifact("[revision]/[artifact](-v[revision]-[classifier]).[ext]")
    }
    metadataSources {
      artifact()
    }
    content {
      includeGroup("org.nodejs")
    }
  }
}

node {
  version.set("22.14.0")
  npmVersion.set("10.9.2")
  download.set(true)
}

tasks.register<NpmTask>("installDependencies") {
  args.set(listOf("install"))
}

tasks.register<NpmTask>("ngBuild") {
  dependsOn("installDependencies")
  args.set(listOf("run", "build"))
}

tasks.register<NpmTask>("ngTest") {
  dependsOn("installDependencies")
  args.set(listOf("run", "test", "--", "--watch=false"))
}

tasks.register<NpmTask>("ngServe") {
  dependsOn("installDependencies")
  args.set(listOf("run", "start"))
}
