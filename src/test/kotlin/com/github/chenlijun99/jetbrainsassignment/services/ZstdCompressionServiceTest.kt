package com.github.chenlijun99.jetbrainsassignment.services

import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files

import kotlin.io.path.deleteRecursively

import org.junit.Assert.assertArrayEquals

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.components.service

import com.github.chenlijun99.jetbrainsassignment.coroutine.TestAppDispatchers
import com.github.chenlijun99.jetbrainsassignment.coroutine.runDispatchingOnUi
import com.github.chenlijun99.jetbrainsassignment.services.ZstdCompressionService

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class ZstdCompressionServiceTest : BasePlatformTestCase() {
    private lateinit var testTmpDirPath: Path

    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    override fun setUp() {
        super.setUp()

        testTmpDirPath = Paths.get(getTestDataPath(), "tmp")
        if (Files.exists(testTmpDirPath)) {
            testTmpDirPath.deleteRecursively()
        }
        Files.createDirectories(testTmpDirPath)
    }

    fun testCompressionUtf8File() = runDispatchingOnUi {
        val utf8Content = "Hello, world! 你好世界！Café au lait."
        doCompressionRoundtripTest(
            configureVirtualFileByBinaryContent(
                "utf8_test.txt",
                utf8Content.toByteArray(Charsets.UTF_8)
            )
        )
    }

    fun testCompressionUtf16File() = runDispatchingOnUi {
        val utf16Content = "Hello, world! 你好世界！Café au lait."
        doCompressionRoundtripTest(
            configureVirtualFileByBinaryContent(
                "utf16_test.txt",
                utf16Content.toByteArray(Charsets.UTF_16)
            )
        )
    }

    /*
     * This test case fails because Intellij platform cannot open a binary 
     * file in editor. It's not just a limitation of the headless Intellij in
     * the testing sandbox. I tried with a real Intellij IDEA Community 
     * and it is unable to open binary files.
     */
    /*
    fun testCompressionBinaryFile() = testScope.runTest {
        doCompressionRoundtripTest(
            configureVirtualFileByBinaryContent(
                "binary_test.bin",
                byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A)
            )
        )
    }
    */

    /**
     * Tests the ZstdCompressionService by compressing a file, then decompressing it using the zstd CLI,
     * and verifying that the decompressed content matches the original.
     *
     * This test requires the 'zstd' command-line tool to be installed and available in the system's PATH.
     */
    suspend fun doCompressionRoundtripTest(inputFile: VirtualFile) {
        val targetPath = testTmpDirPath.resolve("output.zst")
        assertFalse("Target file should not exist initially", Files.exists(targetPath))

        val compressionJob = service<ZstdCompressionService>().performCompression(
            project,
            inputFile,
            targetPath,
            dispatchers = TestAppDispatchers()
        )
        compressionJob.join()

        assertTrue("Compressed file should exist at $targetPath", Files.exists(targetPath))

        val command = listOf("zstd", "--decompress", "--stdout", targetPath.toString())

        // Merge stdout and stderr for easier debugging if process fails
        val processBuilder = ProcessBuilder(command)
            .redirectErrorStream(true)
        val process = processBuilder.start()
        val decompressedBytes = process.inputStream.readBytes()
        assertTrue(
            "zstd CLI process for $targetPath should complete with exit code 0",
            process.waitFor() == 0
        )

        assertArrayEquals(
            "Decompressed content from zstd CLI should match original file content",
            inputFile.contentsToByteArray(),
            decompressedBytes
        )
    }

    private fun configureVirtualFileByBinaryContent(filePath: String, content: ByteArray): VirtualFile {
        val inputFile = testTmpDirPath.resolve(filePath)
        Files.write(inputFile, content)

        // Copy the file from test data directory into the Intellij platform test project
        val sourcePsiFile = myFixture.configureByFile(Paths.get(getTestDataPath()).relativize(inputFile).toString())
        val virtualFile = sourcePsiFile.virtualFile
        assertNotNull(virtualFile)

        return virtualFile
    }

    override fun getTestDataPath() = "src/test/testData/${getTestName(true)}"
}
