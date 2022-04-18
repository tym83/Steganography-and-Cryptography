package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import kotlin.experimental.xor

fun main() {
    val cryptographic = Cryptographic()
    cryptographic.doMainMenu()
}

class Cryptographic {
    private var password = ""
    private val inputInfo = mutableMapOf("inputImage" to "", "outputImage" to "", "message" to "")
    private val pattern = byteArrayOf(0.toByte(), 0.toByte(), 3.toByte())

    fun doMainMenu() {
        while (true) {
            println("Task (hide, show, exit):")
            when (val input = readln()) {
                "exit" -> println("Bye!").also { return }
                "hide" -> doHideMenu().also { hideMessageInImage() }
                "show" -> showMessageFromImage()
                else -> println("Wrong task: $input")
            }
        }
    }

    private fun doHideMenu() {
        println("Input image file:")
        inputInfo["inputImage"] = readln()
        println("Output image file:")
        inputInfo["outputImage"] = readln()
        println("Message to hide:")
        inputInfo["message"] = readln()
        println("Password:")
        password = readln()
    }

    private fun codeMessage(): String {
        val cryptoPass = password.repeat(inputInfo["message"]!!.length / password.length + 1).encodeToByteArray()
        val message = inputInfo["message"]!!.encodeToByteArray()
        var unitMessage = byteArrayOf()

        for (i in message.indices) {
            unitMessage += message[i] xor cryptoPass[i]
        }

        unitMessage += pattern
        val bitMessage = mutableListOf<String>()
        unitMessage.forEach { bitMessage.add("0".repeat(8 - it.toString(2).length) + it.toString(2)) }
        return bitMessage.joinToString("")
    }

    private fun hideMessageInImage() {
        try {
            val image: BufferedImage = ImageIO.read(File(inputInfo["inputImage"]!!))
            var bits = codeMessage()

            if (bits.length > image.width * image.height) {
                println("The input image is not large enough to hold this message.")
                return
            }

            for (height in 0 until image.height) {
                for (width in 0 until image.width) {
                    val inputColor = Color(image.getRGB(width, height))
                    if (bits.isNotEmpty()) {
                        val outputColor = Color(inputColor.red, inputColor.green,
                            (inputColor.blue and 254 or bits[0].toString().toInt()) % 256)
                        image.setRGB(width, height, outputColor.rgb)
                        bits = bits.drop(1)
                    }
                }
            }
            ImageIO.write(image, "png", File(inputInfo["outputImage"]!!))
            println("Message saved in ${inputInfo["outputImage"]} image.")
            return
        } catch (e: Exception) {
            println(e.message)
            return
        }
    }

    private fun encodeMessage(message: ByteArray): ByteArray {
        val cryptoPass = password.repeat(inputInfo["message"]!!.length / password.length + 1).encodeToByteArray()
        var originalMessage = byteArrayOf()

        for (i in message.indices) {
            originalMessage += message[i] xor cryptoPass[i]
        }

        return originalMessage
    }

    private fun showMessageFromImage() {
        val endPattern = listOf("00000000".toUByte(2), "00000000".toUByte(2), "00000011".toUByte(2))
        var bits = ""
        var messageList = ubyteArrayOf()

        try {
            println("Input image file:")
            val image: BufferedImage = ImageIO.read(File(readln()))

            for (height in 0 until image.height) {
                for (width in 0 until image.width) {
                    if (bits.length == 8) {
                        messageList += bits.toUByte(2)
                        bits = ""
                        if (messageList.size >= 3) {
                            val index = messageList.lastIndex
                            val lastThreeBytes =
                                listOf(messageList[index - 2], messageList[index - 1], messageList[index])
                            if (lastThreeBytes == endPattern) {
                                val message = messageList.dropLast(3).toUByteArray().toByteArray()
                                println("Message: ${message.toString(Charsets.UTF_8)}")
                                return
                            }
                        }
                    }
                    val color = Color(image.getRGB(width, height))
                    bits += (color.blue % 2).toString()
                }
            }
            val message = encodeMessage(messageList.dropLast(3).toUByteArray().toByteArray())

            println("Message: ${message.toString(Charsets.UTF_8)}")
        } catch (e: Exception) {
            println(e)
        }
    }
}
