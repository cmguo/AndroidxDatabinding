This project is used to have a library that is compiled
with AGP 3.1.4. We don't compile it as part of tests 
since the goal is to have a project compiled with an old
version.

Currently, it is used to reproduce b/116361870 as in the
MultiModuleTestApp. If you wish to modify this, don't
forget to update the aar in MultiModuleTestApp/app/libs. 

./gradlew assemble && cp library/build/outputs/aar/library.aar ../MultiModuleTestApp/app/libs/lib_compiled_with_v1_agp_3_1_4.aar
