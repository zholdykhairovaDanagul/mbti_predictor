package kz.application.mbti.predictor.predictor;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

import kz.application.mbti.predictor.Config;

public class ImagePredictor {

    public static String predict(Bitmap bitmap, AssetManager assetManager) {
        int class_id;
        try {
            TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(assetManager, Config.MODEL_FILE);

            final int inputSize = 1;
            final int width = 64;
            final int height = 64;

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);


            int[] intValues = new int[inputSize * inputSize];
            float[] floatValues = new float[inputSize * inputSize * 3];

            scaled.getPixels(intValues, 0, scaled.getWidth(), 0, 0, width, height);
            for (int i = 0; i < intValues.length; i++) {
                int val = intValues[i];
                floatValues[i * 3] = (val >> 16) & 0xFF;
                floatValues[i * 3 + 1] = (val >> 8) & 0xFF;
                floatValues[i * 3 + 2] = val & 0xFF;
            }

            inferenceInterface.feed(Config.INPUT_NODE, floatValues, 1, Config.INPUT_SIZE.length, Config.INPUT_SIZE.length, 1);
            inferenceInterface.run(new String[]{Config.OUTPUT_NODE});
            float[] results = new float[Config.CLASS_SIZE];
            Arrays.fill(results, 0.0f);
            inferenceInterface.fetch(Config.OUTPUT_NODE, results);
            class_id = Config.getMaxArgument(results);
        }catch (Exception ex) {
            class_id = (int) (Math.random() * 16);
        }
        return Config.MBTI_TYPES[class_id];
    }
}
