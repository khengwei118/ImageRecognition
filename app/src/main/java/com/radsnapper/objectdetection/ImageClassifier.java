package com.radsnapper.objectdetection;

import android.app.Activity;
import android.graphics.Bitmap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImageClassifier {

    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    private static final float IMAGE_STD = 1.0f;
    private static final float IMAGE_MEAN = 0.0f;
    private static final int MAX_SIZE = 0;


    private final Interpreter tensorClassifier;
    private final List<String> labels;
    private final int imageResizeX;
    private final int imageResizeY;
    private TensorImage inputImageBuffer;
    private final TensorBuffer probabilityImageBuffer;
    private final TensorProcessor probabilityProcessor;

    ImageClassifier(Activity activity) throws IOException {
        //loading the model
        @NonNull MappedByteBuffer classifierModel = FileUtil.loadMappedFile(activity,
                "mobilenet_v2_1.0_224_quant.tflite");
        labels = FileUtil.loadLabels(activity, "labels_capitalized.txt");

        tensorClassifier = new Interpreter(classifierModel, null);

        int imageTensorIndex = 0; //input
        int probabilityTensorIndex = 0; //output

        int[] inputImageShape = tensorClassifier.getInputTensor(imageTensorIndex).shape();
        DataType inputDataType = tensorClassifier.getInputTensor(imageTensorIndex).dataType();

        int[] outputImageShape = tensorClassifier.getOutputTensor(probabilityTensorIndex).shape();
        DataType outputDataType = tensorClassifier.getOutputTensor(probabilityTensorIndex).dataType();

        imageResizeX = inputImageShape[1];
        imageResizeY = inputImageShape[2];

        inputImageBuffer = new TensorImage(inputDataType);

        probabilityImageBuffer = TensorBuffer.createFixedSize(outputImageShape, outputDataType);

        probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD))
                .build();
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap, final int sensorOrientation) {
        List<Recognition> recognitions = new ArrayList<>();
        inputImageBuffer = loadImage(bitmap, sensorOrientation);
        tensorClassifier.run(inputImageBuffer.getBuffer(), probabilityImageBuffer.getBuffer().rewind());
        Map<String, Float> labelledProbability = new TensorLabel(labels,
                probabilityProcessor.process(probabilityImageBuffer)).getMapWithFloatValue();
        for (Map.Entry<String, Float> entry : labelledProbability.entrySet()) {
            recognitions.add(new Recognition(entry.getKey(), entry.getValue()));
        }
        // sorting predictions based on confidence
        Collections.sort(recognitions);
        return recognitions;
    }

    private TensorImage loadImage(Bitmap bitmap, int sensorOrientation) {
        inputImageBuffer.load(bitmap);
        int noOfRotations = sensorOrientation / 90;
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                .add(new ResizeOp(imageResizeY, imageResizeX, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new Rot90Op(noOfRotations))
                .add(new NormalizeOp(IMAGE_MEAN, IMAGE_STD))
               // .add(new QuantizeOp(128.0f, 1/128.0f))
                .build();
        return imageProcessor.process(inputImageBuffer);

    }

    class Recognition implements Comparable {
        private String name;
        private float confidence;

        public Recognition() {
        }

        public Recognition(String name, float confidence) {
            this.name = name;
            this.confidence = confidence;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getConfidence() {
            return confidence;
        }

        public void setConfidence(float confidence) {
            this.confidence = confidence;
        }

        @Override
        public int compareTo(Object o) {
            return Float.compare(((Recognition) o).confidence, this.confidence);
        }
    }
}
