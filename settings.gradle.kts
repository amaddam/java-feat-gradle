rootProject.name = "java-feat-gradle"
include("feat-record")
include("feat-socket")
include("feat-gui")
include("feat-gui:feat-gui-idea")
findProject(":feat-gui:feat-gui-idea")?.name = "feat-gui-idea"
