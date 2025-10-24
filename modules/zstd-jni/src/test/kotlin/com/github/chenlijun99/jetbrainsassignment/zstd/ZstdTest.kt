package com.github.chenlijun99.jetbrainsassignment.zstd

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@RunWith(Parameterized::class)
class ZstdTest(
	private val input: ByteArray,
	private val level: Int,
	private val inputType: InputType,
	private val methodName: String,
	private val compressionFunction: (ByteArray, Int) -> Any
) {

	enum class InputType {
		EMPTY, SMALL, LARGE
	}

	companion object {
		// Common input data
		private val emptyInput = ByteArray(0)
		private val smallInput = "Hello, Zstd compression!".toByteArray(StandardCharsets.UTF_8)
		private val largeInput = "This is a larger string that should be highly compressible. ".repeat(100)
			.toByteArray(StandardCharsets.UTF_8)

		// Common compression levels to test
		private val compressionLevels = listOf(1, 3, 10)

		@JvmStatic
		@Parameterized.Parameters(name = "{4} - Input: {2}, Level: {1}")
		fun data(): Collection<Array<Any>> {
			val params = mutableListOf<Array<Any>>()

			val inputs = listOf(
				Triple(emptyInput, InputType.EMPTY, "Empty"),
				Triple(smallInput, InputType.SMALL, "Small"),
				Triple(largeInput, InputType.LARGE, "Large")
			)

			val compressionMethods = listOf(
				"compress" to { data: ByteArray, level: Int -> Zstd.compress(data, level) },
				"compressCritical" to { data: ByteArray, level: Int ->
					Zstd.compressCritical(
						data,
						level
					)
				},
				"compressToByteBuffer" to { data: ByteArray, level: Int ->
					Zstd.compressToByteBuffer(
						data,
						level
					)
				},
				"compressCriticalToByteBuffer" to { data: ByteArray, level: Int ->
					Zstd.compressCriticalToByteBuffer(
						data,
						level
					)
				}
			)

			for ((inputBytes, inputType, _) in inputs) {
				for (level in compressionLevels) {
					for ((methodName, func) in compressionMethods) {
						params.add(arrayOf(inputBytes, level, inputType, methodName, func))
					}
				}
			}
			return params
		}
	}

	@Test
	fun testZstdCliDecompressionRoundtrip() {
		val compressedBytes: ByteArray = when (val result = compressionFunction(input, level)) {
			is ByteArray -> result
			is ByteBuffer -> {
				// Ensure buffer is ready for reading from position 0 up to limit
				val bytes = ByteArray(result.remaining())
				result.get(bytes)
				bytes
			}

			else -> error("Unexpected compression result type for CLI decompression: ${result::class.java.simpleName}")
		}

		assertNotNull(compressedBytes)

		if (compressedBytes.isNotEmpty()) {
			val decompressed = decompressWithZstdCli(compressedBytes)
			assertArrayEquals(
				"Decompressed data from CLI should match original input for methodName '$methodName', inputType '$inputType', level $level",
				input,
				decompressed
			)
		}
	}

	private fun decompressWithZstdCli(compressedData: ByteArray): ByteArray {
		val processBuilder = ProcessBuilder("zstd", "-d")
		processBuilder.redirectErrorStream(true)
		val process = processBuilder.start()

		process.outputStream.use { it.write(compressedData) }

		val decompressedBytes = process.inputStream.readAllBytes()

		val exitCode = process.waitFor()
		assertEquals(
			"zstd CLI decompression failed with exit code $exitCode. Output: ${
				String(
					decompressedBytes,
					StandardCharsets.UTF_8
				) // Use decompressedBytes for error message
			}", 0, exitCode
		)

		return decompressedBytes
	}
}
