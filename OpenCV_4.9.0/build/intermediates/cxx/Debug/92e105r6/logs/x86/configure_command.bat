@echo off
"C:\\Users\\asus\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HF:\\Capstone\\Nanta_integrated\\Capstone_BlindNav\\OpenCV_4.9.0\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=24" ^
  "-DANDROID_PLATFORM=android-24" ^
  "-DANDROID_ABI=x86" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86" ^
  "-DANDROID_NDK=C:\\Users\\asus\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\asus\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\asus\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\asus\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=F:\\Capstone\\Nanta_integrated\\Capstone_BlindNav\\OpenCV_4.9.0\\build\\intermediates\\cxx\\Debug\\92e105r6\\obj\\x86" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=F:\\Capstone\\Nanta_integrated\\Capstone_BlindNav\\OpenCV_4.9.0\\build\\intermediates\\cxx\\Debug\\92e105r6\\obj\\x86" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BF:\\Capstone\\Nanta_integrated\\Capstone_BlindNav\\OpenCV_4.9.0\\.cxx\\Debug\\92e105r6\\x86" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
