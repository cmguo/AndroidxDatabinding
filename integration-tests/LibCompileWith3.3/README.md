This project is used to have a library that is compiled
with AGP 3.3.1. We don't compile it as part of tests 
since the goal is to have a project compiled with an old
version.

Currently, it is in MultiModuleTestApp to reproduce b/122936785.
If you wish to modify this, don't forget to update the aar in
MultiModuleTestApp/app/libs. 

`./gradlew assemble && cp library/build/outputs/aar/library.aar ../MultiModuleTestApp/app/libs/lib_compiled_with_v2_agp_3_3_1.aar`
