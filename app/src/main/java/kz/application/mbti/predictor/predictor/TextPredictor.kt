package kz.application.mbti.predictor.predictor

import android.content.res.AssetManager
import android.os.AsyncTask
import android.text.TextUtils
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TextPredictor(
    private val assets: AssetManager?,
    private val filename: String,
    val message: String
) {

    private var callback: DataCallback? = null
    private var maxlen: Int = 171
    private var vocabData: HashMap<String, Int>? = null


    fun loadData() {
        val loadVocabularyTask = LoadVocabularyTask(callback)
        loadVocabularyTask.execute(loadJSONFromAsset(filename))
    }

    private fun loadJSONFromAsset(filename: String?): String? {
        var json: String? = null
        try {
            assets?.let {
                val inputStream = it.open(filename!!)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer)
            }

        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun setCallback(callback: DataCallback) {
        this.callback = callback
    }

    fun tokenize(message: String): IntArray {
        val parts: List<String> = message.split(" ")
        val tokenizedMessage = ArrayList<Int>()
        for (part in parts) {
            if (part.trim() != "") {
                var index: Int? = 0
                if (vocabData!![part] == null) {
                    index = 0
                } else {
                    index = vocabData!![part]
                }
                tokenizedMessage.add(index!!)
            }
        }
        return tokenizedMessage.toIntArray()
    }

    fun padSequence(sequence: IntArray): IntArray {
        val maxlen = this.maxlen
        return when {
            sequence.size > maxlen -> sequence.sliceArray(0..maxlen)
            sequence.size < maxlen -> {
                val array = ArrayList<Int>()
                array.addAll(sequence.asList())
                for (i in array.size until maxlen) {
                    array.add(0)
                }
                array.toIntArray()
            }
            else -> sequence
        }
    }

    fun setVocab(data: HashMap<String, Int>?) {
        this.vocabData = data
    }

    interface DataCallback {
        fun onDataProcessed(result: HashMap<String, Int>?)
    }

    private inner class LoadVocabularyTask(callback: DataCallback?) :
        AsyncTask<String, Void, HashMap<String, Int>?>() {

        private var callback: DataCallback? = callback

        override fun doInBackground(vararg params: String?): HashMap<String, Int>? {
            val jsonObject = JSONObject(params[0])
            val iterator: Iterator<String> = jsonObject.keys()
            val data = HashMap<String, Int>()
            while (iterator.hasNext()) {
                val key = iterator.next()
                data[key] = jsonObject.get(key) as Int
            }
            return data
        }

        override fun onPostExecute(result: HashMap<String, Int>?) {
            super.onPostExecute(result)
            callback?.onDataProcessed(result)
            if (!TextUtils.isEmpty(message)) {
                setVocab(result)
                val tokenizedMessage = tokenize(message)
                val paddedMessage = padSequence(tokenizedMessage)
                val traits = arrayOf("IE", "NS", "FT", "PJ")
                var resultType = ""
                for (i in 0..3) {
                    val results = classifySequence(paddedMessage, traits[i])
                    val class1 = results[0]
                    val class2 = results[1]
                    resultType += if (class1 > class2) {
                        traits[i][0]
                    }else {
                        traits[i][1]
                    }
                }
                val map = HashMap<String, Int>()
                map[resultType] = 0
                callback?.onDataProcessed(map)
            }
        }

    }


    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        assets?.let {
            val assetFileDescriptor = assets.openFd("model.tflite")
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startoffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength)
        }
        throw IOException("IOEXCEPTION!!")
    }

    fun classifySequence(sequence: IntArray, traitLetter: String): FloatArray {
        val interpreter = Interpreter(loadModelFile())
        val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
        val outputs: Array<FloatArray> = arrayOf(floatArrayOf(0.0f, 0.0f))
        interpreter.run(inputs, outputs)
        return outputs[0]
    }

}