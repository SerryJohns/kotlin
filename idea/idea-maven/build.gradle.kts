
apply { plugin("kotlin") }

dependencies {
    compileOnly(ideaSdkDeps("openapi", "idea", "gson"))
    //compileOnly(ideaPluginDeps("maven", "maven-server-api", plugin = "maven"))

    compile(project(":core:util.runtime"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:util"))
    compile(project(":compiler:cli-common"))
    compile(project(":kotlin-build-common"))

    compile(project(":js:js.frontend"))

    compile(project(":idea"))
    compile(project(":idea:idea-jvm"))
    compile(project(":idea:idea-jps-common"))

    testCompile(projectTests(":idea"))
    testCompile(projectTests(":compiler:tests-common"))
    testCompile(project(":idea:idea-test-framework"))
    testCompileOnly(ideaSdkDeps("openapi", "idea", "gson", "idea_rt"))
//    testCompileOnly(ideaPluginDeps("maven", "maven-server-api", plugin = "maven"))

    testRuntime(projectDist(":kotlin-reflect"))
    testRuntime(project(":idea:idea-jvm"))
    testRuntime(project(":idea:idea-android"))
    testRuntime(project(":plugins:android-extensions-ide"))
    testRuntime(project(":plugins:lint"))
    testRuntime(project(":sam-with-receiver-ide-plugin"))
    testRuntime(project(":allopen-ide-plugin"))
    testRuntime(project(":noarg-ide-plugin"))
    testRuntime(ideaSdkDeps("resources"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaPluginDeps("resources_en", plugin = "junit"))
    testRuntime(ideaPluginDeps("jcommander", "resources_en", plugin = "testng"))
    testRuntime(ideaPluginDeps("resources_en", plugin = "properties"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "gradle"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "Groovy"))
    testRuntime(ideaPluginDeps("jacocoant", plugin = "coverage"))
    //testRuntime(ideaPluginDeps("*.jar", plugin = "maven"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "android"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "smali"))
}

sourceSets {
    "main" { /*projectDefault()*/ }
    "test" { /*projectDefault()*/ }
}

testsJar()

projectTest {
    workingDir = rootDir
}
