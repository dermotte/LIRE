# How to local features & LIRE on Ubuntu
Nelson Silva provided a step by step guide on the lire-dev mailing list and allowed for publication. Thanks go to him! 

## 1. Install Dependencies
```
sudo apt-get update
sudo apt-get install -y build-essential
sudo apt-get install -y cmake
sudo apt-get install -y libgtk2.0-dev
sudo apt-get install -y pkg-config
sudo apt-get install -y python-numpy python-dev
sudo apt-get install -y libavcodec-dev libavformat-dev libswscale-dev
sudo apt-get install -y libjpeg-dev libpng-dev libtiff-dev libjasper-dev
 
sudo apt-get -qq install libopencv-dev build-essential checkinstall cmake pkg-config yasm libjpeg-dev libjasper-dev libavcodec-dev libavformat-dev libswscale-dev libdc1394-22-dev libxine-dev libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev libv4l-dev python-dev python-numpy libtbb-dev libqt4-dev libgtk2.0-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev libtheora-dev libvorbis-dev libxvidcore-dev x264 v4l-utils
```

## 2. Download OpenCV-2.4.11
```
wget http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.11/opencv-2.4.11.zip
unzip opencv-2.4.11.zip
cd opencv-2.4.11
mkdir release
cd release
``` 
## 3. Compile and Install
```
cmake -G "Unix Makefiles" -D CMAKE_CXX_COMPILER=/usr/bin/g++ CMAKE_C_COMPILER=/usr/bin/gcc -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_TBB=ON -D BUILD_NEW_PYTHON_SUPPORT=ON -D WITH_V4L=ON -D INSTALL_C_EXAMPLES=ON -D INSTALL_PYTHON_EXAMPLES=ON -D BUILD_EXAMPLES=ON -D WITH_QT=ON -D WITH_OPENGL=ON -D BUILD_FAT_JAVA_LIB=ON -D INSTALL_TO_MANGLED_PATHS=ON -D INSTALL_CREATE_DISTRIB=ON -D INSTALL_TESTS=ON -D ENABLE_FAST_MATH=ON -D WITH_IMAGEIO=ON -D BUILD_SHARED_LIBS=OFF -D WITH_GSTREAMER=ON ..
sudo make all -j4 # 4 cores
sudo make install
```
# Needed for LIRE
**(1a)** you should either extract the provided zip file (LIRE project folder) with the necessary opencv files for Ubuntu (extract to the lib folder of the respective LIRE examples)

**(1b)** or... copy the files manually in Ubuntu from folder: /usr/local/share/OpenCV/java to the LIRE simpleapplication/lib
	- you should see the files: opencv-2411.jar and libopencv_java2411.so
	
**(2)** finally make sure that the simpleapplication build.gradle file contains:

```
task runIndexLocalSLocalFeat(type: JavaExec) {
    classpath = sourceSets.main.runtimeClasspath
    systemProperty "java.library.path", "/home/nsilva/Documents/LIRE/LIREGit_latest/LIRE/samples/simpleapplication/lib" 

    main = 'net.semanticmetadata.lire.sampleapp.IndexingAndSearchWithLocalFeatures'

    // Define the directory where to find the images to index.
    //args '/home/nsilva/Documents/images/europeana _jewlery'
}
```
 

# Extra: Not Needed for LIRE
```
ignore libdc1394 error http://stackoverflow.com/questions/12689304/ctypes-error-libdc1394-error-failed-to-initialize-libdc1394
#python
#> import cv2
#> cv2.SIFT
#<built-in function SIFT>
```
