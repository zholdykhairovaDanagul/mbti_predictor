package kz.application.mbti.predictor;

public class Config {

    public static final String MODEL_FILE = "file:///android_asset/model.pb";
    public static final String INPUT_NODE = "import/dense_1_input:0";
    public static final String OUTPUT_NODE = "import/dense_3/Softmax:0";
    public static final int CLASS_SIZE = 16;
    public static int[] INPUT_SIZE = {-1, 64, 64, 1};
    public static final String[] MBTI_TYPES = new String[]{
            "ISTJ",
            "ISFJ",
            "INFJ",
            "INTJ",
            "ISTP",
            "ISFP",
            "INFP",
            "INTP",
            "ESTP",
            "ESFP",
            "ENFP",
            "ENTP",
            "ESTJ",
            "ESFJ",
            "ENFJ",
            "ENTJ"
    };

    public static final String[] MBTI_TYPE_SPECIALS = new String[]{
            "Logistician",
            "Defender",
            "Advocate",
            "Architect",
            "Virtuoso",
            "Adventurer",
            "Mediator",
            "Logician",
            "Entrepreneur",
            "Entertainer",
            "Campaigner",
            "Debater",
            "Executive",
            "Consul",
            "Protagonist",
            "Commander"
    };

    public static int getMaxArgument(float[] elements) {
        int maxIndex = -1;
        float max = -1;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] > max) {
                max = elements[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
