# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.
add_library(
        shape-lib

        SHARED
        src/main/cpp/shapelib/CHbxFishEye.cpp
        src/main/cpp/shapelib/CHbxLsxFishEye.cpp
        src/main/cpp/HbxList.c
        src/main/cpp/shapelib/Jni_ShapeManager.cpp
)
add_library( # Sets the name of the library.
             media-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/medialib/native-lib.cpp
             src/main/cpp/medialib/videoplayer.cpp
             src/main/cpp/medialib/Livevideo.cpp
             src/main/cpp/medialib/HbxBaseFile.cpp
             src/main/cpp/medialib/HbxFrame.cpp
             src/main/cpp/medialib/HbxThread.cpp
             src/main/cpp/medialib/HbxAndroidAudioPlay.cpp
             src/main/cpp/medialib/HbxAudioPlay.cpp
             src/main/cpp/medialib/HbxAudioThread.cpp
             src/main/cpp/medialib/HbxBaseTasksCheduler.cpp
             src/main/cpp/medialib/HbxReadThread.cpp
             src/main/cpp/medialib/HbxListFrame.cpp
             src/main/cpp/medialib/HbxMediaPlay.cpp
             src/main/cpp/medialib/HbxVideoThread.cpp
             src/main/cpp/medialib/HbxWavePlay.cpp
             src/main/cpp/medialib/HbxAndroidAudioPlay.cpp
             src/main/cpp/medialib/HbxVideoDecodec.cpp
             src/main/cpp/medialib/sunInfoPaser.cpp
             src/main/cpp/HbxList.c
             src/main/cpp/H264toJpg.cpp
             src/main/cpp/H264toJpg_jni.cpp
            )

add_library(video-util
            SHARED
            src/main/cpp/video-util.cpp
            src/main/cpp/VideoCut.cpp
            src/main/cpp/VideoTranslate.cpp
            )
add_library( # Sets the name of the library.
             hbxfisheye

             # Sets the library as a shared library.
             SHARED
            src/main/cpp/hbxFisheye/Hjni_HbxFishEye.cpp
            src/main/cpp/hbxFisheye/HbxFishEyeList.cpp
            src/main/cpp/hbxFisheye/HbxFishEyeManager.cpp
            src/main/cpp/hbxFisheye/HbxFishEyeMatlab.cpp
            src/main/cpp/hbxFisheye/HbxFishEyeParameters.cpp
            src/main/cpp/hbxList.c
            src/main/cpp/hbxFisheye/HbxReadFileInfo.cpp
            src/main/cpp/hbxFisheye/HbxVerticesFile.cpp
            src/main/cpp/hbxFisheye/HbxWriteFileInfo.cpp
            src/main/cpp/hbxFisheye/Hjni_HbxFishEye.cpp

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
            )
# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )
 find_library( # Sets the name of the path variable.
               android-lib

               # Specifies the name of the NDK library that
               # you want CMake to locate.
               android )
 find_library( # Sets the name of the path variable.
                z-lib

                # Specifies the name of the NDK library that
                # you want CMake to locate.
                z )
find_library( # Sets the name of the path variable.
                pthread-lib

                # Specifies the name of the NDK library that
                # you want CMake to locate.
                pthread)
find_library( # Sets the name of the path variable.
               gles-lib

               # Specifies the name of the NDK library that
               # you want CMake to locate.
               GLESv2 )
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

add_library(ffmpeg-lib  SHARED IMPORTED )
set_target_properties(ffmpeg-lib PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libffmpeg.so )
include_directories(src/main/cpp/include/)
include_directories(src/main/cpp/hbxFisheye)
include_directories(src/main/cpp/)
target_link_libraries( # Specifies the target library.
                        shape-lib
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                        ${android-lib}
                        )
target_link_libraries( # Specifies the target library.
                       media-lib
                       ffmpeg-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                        ${android-lib}
                        ${z-lib}
                       ${gles-lib} )
target_link_libraries( # Specifies the target library.
                       video-util
                       ffmpeg-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                        ${android-lib}
                        ${z-lib}
                        )
target_link_libraries( # Specifies the target library.
                       hbxfisheye

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                        ${android-lib}
                       )