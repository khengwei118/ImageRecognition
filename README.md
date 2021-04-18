# RadSnapper
Simple image recognition app created with TensorFlow Lite.

## Description
This app allows users to capture an image, or choose an image from the phone's photo gallery, and uses Machine Learning to output the name of recognized object. 
It outputs the highest confidence object first, and will also print the 2nd and 3rd most confident recognized objects in smaller text.

#### Features
- Switch to turn on or off display of confidence level. This is off by default.
- Button to play the recognized object's pronunciation.

#### Use case
This app can be used to identify hundreds of animal species and breeds. These include:
- Domestic Cats such as Tabby, Persian and Siamese
- Wildcats such as Lion, Cheetah and Jaguar
- Over 110 breeds of Domestic Dogs such as Retrievers, Terriers and Spaniels
- Small birds such as Jays, Magpies and Finches
- Bigger birds such as Geese, Crane, Heron and Egrets
- Reptiles such as Snakes, Lizards, Salamanders and Crocodiles
- Shellfish such as Crabs and Lobsters
- Other Aquatic animals such as Fish, Sharks and Whales
- Insects such as Beetles, Butterflies and Spiders

This app is especially helpful for passionate wildlife/animal observers and non-English speakers who would like to learn the language.
As a "point and shoot" users can learn what the object is in English and even hear the pronunciation.
This app can also be used by anyone who would just like to try a simple Machine Learning app on their phone.


## Demo
#### YouTube Video
https://youtu.be/R2xjVT7bIYU

#### Screenshots
##### From Camera
<img src="/app/src/main/assets/screenshots/Screenshot_20210417-231427.png" width="200" height="400"> <img src="/app/src/main/assets/screenshots/Screenshot_20210417-231438.png" width="200" height="400"> 
<img src="/app/src/main/assets/screenshots/Screenshot_20210417-231609.png" width="200" height="400"> 
<img src="/app/src/main/assets/screenshots/Screenshot_20210417-231625.png" width="200" height="400">
##### From Gallery
<img src="/app/src/main/assets/screenshots/Screenshot_20210417-223104.png" width="200" height="400"> <img src="/app/src/main/assets/screenshots/Screenshot_20210417-223113.png" width="200" height="400">
<img src="/app/src/main/assets/screenshots/Screenshot_20210417-223122.png" width="200" height="400">
<img src="/app/src/main/assets/screenshots/Screenshot_20210417-223045.png" width="200" height="400">


## Technologies
- TensorFlow Lite 
- Mobilenet_V2_1.0_224_quant from https://www.tensorflow.org/lite/guide/hosted_models#quantized_models
- Android SDK
- Java
- XML


## Known issues
- Image Recognition generally works well, but due to labels limitation it can only recognize 1000 objects.
- Certain phones would get image rotated in certain cases due to EXIF data.


## Credits
Credits to Adriana for drawing app logos and graphics.






