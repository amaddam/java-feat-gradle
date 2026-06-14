rootProject.name = "java-feat-gradle"
include("feat-network")
include("feat-gui")
include("feat-gui:feat-gui-idea")
//这里的findProject(":feat-gui:feat-gui-idea")?.name = "feat-gui-idea"是idea生成的, 指定了feat-gui-idea模块的名字是feat-gui-idea
findProject(":feat-gui:feat-gui-idea")?.name = "feat-gui-idea"
include("feat-java-base")
include("feat-java-base:feat-record")
findProject(":feat-java-base:feat-record")?.name = "feat-record"
include("feat-juc")