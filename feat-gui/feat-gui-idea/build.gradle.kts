plugins {
    id("java")

}

group = "com.self"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //一个比较好看的ui外观
    implementation("com.formdev:flatlaf:3.5.1")
    //idea的表单库, 但是最近一次更新是在2008年,
    // 而intellij community idea中的表单库并没有使用这个库,而是自己实现了一套,
    // 我猜测这个库是为了兼容一些东西的, 至少在idea中不引用这个库也是可以的, 应该是用的当前idea软件的库
    // 只是当GUI Designer使用java source code时, 生成的代码会爆红
    //所以我先引入这个库, 以后再看看怎么解决
    implementation ("com.intellij:forms_rt:7.0.3")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    // 为 JAR 文件设置入口点, 这样可以直接运行 JAR 文件
    manifest {
        attributes["Main-Class"] = "com.self.gui.DemoForm" // 为 JAR 文件设置入口点
    }

    // 将所有依赖打包到 JAR 中
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
tasks.test {
    useJUnitPlatform()
}