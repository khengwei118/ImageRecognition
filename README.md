# RadSnapper
Simple image recognition app created with TensorFlow Lite.

## Description
This app allows users to capture an image, or choose an image from the phone's photo gallery, and uses Machine Learning to output the name of recognized object. 
It outputs the highest confidence object first, and will also print the 2nd and 3rd most confident recognized objects in smaller text.

### Features
- Switch to turn on or off display of confidence level. This is off by default.
- Button to play the recognized object's pronunciation.

## Use case
This app can be used by anyone who would like to learn English. 
This is especially helpful for young children or non-English speakers who would like to learn the language.
As a "point and shoot" users can learn what the object is in English and even hear the pronunciation.
This app can also be used by anyone who would just like to try a simple Machine Learning app on their phone.

## Technologies
- TensorFlow Lite 
- Mobilenet_V2_1.0_224_quant from https://www.tensorflow.org/lite/guide/hosted_models#quantized_models
- Android SDK
- Java
- XML

## Known issues
- Image Recognition accuracy is not very high
- Certain phones would get image rotated in certain cases due to EXIF data






