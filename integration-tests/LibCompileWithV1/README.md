This project is used to have a library that is compiled
with AGP 3.1.4 and V1. We don't compile it as part of tests
since the goal is to have a project compiled with an old
version using V1.

Currently, it is used to reproduce b/117666264 as in the
MultiModuleTestApp. If you wish to modify this, don't
forget to update the maven repo in MultiModuleTestApp.

to update:
```
./gradlew -Pmaven_repo=/path/to/MultiModuleTestApp/testlibrary1/repo uploadArchives
 ```
