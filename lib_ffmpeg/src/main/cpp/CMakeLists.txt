
cmake_minimum_required(VERSION 3.4.1)

add_library(
             myffmpeg-lib


             SHARED


             myffmpeg-lib.cpp )



find_library(
              log-lib


              log )


target_link_libraries(
                       myffmpeg-lib


                       ${log-lib}
                         )