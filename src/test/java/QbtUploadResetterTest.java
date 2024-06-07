import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.BencodeException;
import com.dampcake.bencode.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class QbtUploadResetterTest {
    private static final String HEX_FIRST_ARGUMENT = "first";
    private static final String HEX_SECOND_ARGUMENT = "second";
    private static final String HEX_ZERO = "0";

//    @Test
//    void encodeHexData_shouldEncodeHexDataCorrectly() {
//        // Test encoding in hex
//        String hexData = "48656C6C6F20576F726C64"; // "Hello World" in hexadecimal
//
//        // Load the file from resources
//        File fileHelloWorld = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("HelloWorld.txt")).getFile());
//
//        String decodedData = QbtUploadResetter.encodeHexData(fileHelloWorld);
//        assertEquals(hexData, decodedData);
//
//        // Test encoding empty file
//        String emptyData = "";
//        File fileEmpty = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("Empty.txt")).getFile());
//        String emptyDecodedData = QbtUploadResetter.encodeHexData(fileEmpty);
//        assertEquals(emptyData, emptyDecodedData);
//    }

    @TempDir
    Path tempDir;

    @Test
    public void testEncodeHexData() throws IOException {
        File tempFile = tempDir.resolve("testfile.txt").toFile();

        // Write some data to the file
        String content = "Hello, World!";
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content.getBytes());
        }

        // Expected hex representation of "Hello, World!"
        String expectedHex = DatatypeConverter.printHexBinary(content.getBytes());

        // Call the method and get the result
        String result = QbtUploadResetter.encodeHexData(tempFile);

        // Verify the result
        assertEquals(expectedHex, result);
    }

    @Test
    public void testEncodeHexDataEmptyFile() throws IOException {
        File emptyFile = tempDir.resolve("emptyfile.txt").toFile();
        emptyFile.createNewFile();

        // Expected hex representation of an empty file
        String expectedHex = "";

        // Call the method and get the result
        String result = QbtUploadResetter.encodeHexData(emptyFile);

        // Verify the result
        assertEquals(expectedHex, result);
    }

    @Test
    public void testEncodeHexData_ExceptionHandling() {
        File nonExistentFile = new File("nonexistentfile.txt");

        // Expected hex representation of a non-existent file
        String expectedHex = "";

        // Call the method and get the result
        String result = QbtUploadResetter.encodeHexData(nonExistentFile);

        // Verify the result
        assertEquals(expectedHex, result);
    }

//    @Test
//    void saveFileWithResetData_shouldSaveFileWithModifiedData() throws IOException {
//        // Create a temporary file with sample data
//        File tempFile = Files.createTempFile("temp", ".txt").toFile();
//        String originalData = "Original data";
//        Files.write(tempFile.toPath(), originalData.getBytes());
//
//        // Reset the upload value in the hex data and save it to the temporary file
//        String resetHexData = "48656C6C6F20576F726C64"; // "Hello World" in hexadecimal
//        QbtUploadResetter.saveFileWithResetData(resetHexData, tempFile.getPath());
//
//        // Read the content of the temporary file
//        byte[] fileContent = Files.readAllBytes(tempFile.toPath());
//        String savedData = new String(fileContent);
//
//        // Verify that the file content matches the modified data
//        assertEquals("Hello World", savedData);
//
//        // Clean up: delete the temporary file
//        tempFile.delete();
//    }

    @Test
    public void testSaveFileWithResetData() throws IOException {
        // Prepare test data
        String resetHexData = "48656C6C6F2C20576F726C64"; // Hexadecimal representation of "Hello, World!"
        byte[] expectedBytes = DatatypeConverter.parseHexBinary(resetHexData);

        // Create a temporary file
        File tempFile = tempDir.resolve("testfile.txt").toFile();

        // Call the method to save the data to the file
        QbtUploadResetter.saveFileWithResetData(resetHexData, tempFile.getAbsolutePath());

        // Read the content of the file
        byte[] actualBytes;
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            actualBytes = bos.toByteArray();
        }

        // Verify that the content of the file matches the expected bytes
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    void testSaveFileWithResetData_ExceptionHandling() {
        // Redirect System.err to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outputStream));

        // Create an invalid path to provoke an IOException
        String invalidPath = "invalid/path.txt";

        // Call the method with invalid path and an empty resetHexData
        QbtUploadResetter.saveFileWithResetData("", invalidPath);

        // Get the output from System.err
        String errorOutput = outputStream.toString();

        // Restore System.err
        System.setErr(System.err);

        // Check if the error message contains the expected string
        assertTrue(errorOutput.contains("Failed to save the file: " + invalidPath));
    }

//    @Test
//    void resetUploadValue_shouldResetUploadValueInHexData() {
//        // Test resetting upload value in hex data
//        String originalHexData = "31343A746F74616C5F75706C6F616465646965383A747261636B657273";
//        String modifiedHexData = QbtUploadResetter.resetUploadValue(originalHexData);
//        assertEquals("31343A746F74616C5F75706C6F61646564693065383A747261636B657273", modifiedHexData);
//    }

    @Test
    void resetUploadValue_BothArgumentsPresent() {
        String hexData = "somePrefix" + QbtUploadResetter.HEX_FIRST_ARGUMENT + "middlePart" + QbtUploadResetter.HEX_SECOND_ARGUMENT + "someSuffix";
        String expectedResult = "somePrefix" + QbtUploadResetter.HEX_FIRST_ARGUMENT + QbtUploadResetter.HEX_ZERO + QbtUploadResetter.HEX_SECOND_ARGUMENT + "someSuffix";
        assertEquals(expectedResult, QbtUploadResetter.resetUploadValue(hexData));
    }

    @Test
    void resetUploadValue_FirstArgumentMissing() {
        String hexData = "somePrefix" + "someSuffix" + QbtUploadResetter.HEX_SECOND_ARGUMENT;
        assertThrows(IllegalArgumentException.class, () -> QbtUploadResetter.resetUploadValue(hexData));
    }

    @Test
    void resetUploadValue_SecondArgumentMissing() {
        String hexData = "somePrefix" + QbtUploadResetter.HEX_FIRST_ARGUMENT + "someSuffix";
        assertThrows(IllegalArgumentException.class, () -> QbtUploadResetter.resetUploadValue(hexData));
    }



    @Test
    void resetUploadValue_SecondArgumentBeforeFirst() {
        String hexData = "somePrefix" + QbtUploadResetter.HEX_SECOND_ARGUMENT + "middlePart" + QbtUploadResetter.HEX_FIRST_ARGUMENT + "someSuffix";
        assertThrows(IllegalArgumentException.class, () -> QbtUploadResetter.resetUploadValue(hexData));
    }

    @Test
    void resetUploadValue_EmptyInput() {
        String hexData = "";
        assertThrows(IllegalArgumentException.class, () -> QbtUploadResetter.resetUploadValue(hexData));
    }

//    @Test
//    void promptUserForReset_shouldPromptUserAndReturnBoolean() {
//        // Test user confirmation
//        ByteArrayInputStream in = new ByteArrayInputStream("yes".getBytes());
//        System.setIn(in);
//        assertTrue(QbtUploadResetter.promptUserForReset("test-file-path"));
//        System.setIn(System.in);
//
//        // Test user cancellation
//        ByteArrayInputStream cancelIn = new ByteArrayInputStream("no".getBytes());
//        System.setIn(cancelIn);
//        assertFalse(QbtUploadResetter.promptUserForReset("test-file-path"));
//        System.setIn(System.in);
//    }

    @Test
    void promptUserForReset_YesInput() {
        // Mocking input stream with "yes" input
        String input = "yes\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        // Call the method and capture the return value
        boolean result = QbtUploadResetter.promptUserForReset("examplePath");

        // Restore original System.in
        System.setIn(System.in);

        // Verify that the method returns true for "yes" input
        assertTrue(result);
    }

    @Test
    void promptUserForReset_YInput() {
        // Mocking input stream with "yes" input
        String input = "y\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        // Call the method and capture the return value
        boolean result = QbtUploadResetter.promptUserForReset("examplePath");

        // Restore original System.in
        System.setIn(System.in);

        // Verify that the method returns true for "yes" input
        assertTrue(result);
    }

    @Test
    void promptUserForReset_NoInput() {
        // Mocking input stream with "no" input
        String input = "no\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        // Call the method and capture the return value
        boolean result = QbtUploadResetter.promptUserForReset("examplePath");

        // Restore original System.in
        System.setIn(System.in);

        // Verify that the method returns false for "no" input
        assertFalse(result);
    }

    @Test
    void decodeTorrentFile_ValidFile_ReturnsMap() {
        // Load the test torrent file from resources
        File testFile = new File(getClass().getResource("/bunny.torrent").getFile());
        try {
            // Call the method under test
            Map<String, Object> result = QbtUploadResetter.decodeTorrentFile(testFile);
            // Assert that the result is not null
            assertNotNull(result);
            // Assert that the result contains expected keys/values
            assertTrue(result.containsKey("website"));
            assertEquals("http://bbb3d.renderfarming.net", result.get("website"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void decodeTorrentFile_InvalidFile_ReturnsNull() {
        // Create an invalid test torrent file
        File testFile = new File("invalid.torrent");
        // Call the method under test
        Map<String, Object> result = QbtUploadResetter.decodeTorrentFile(testFile);
        // Assert that the result is null
        assertNull(result);
        // Clean up the test file
        testFile.delete();
    }

//    @Test
//    void getTorrentName_shouldExtractTorrentNameFromFilePath() {
//        // Test with valid .fastresume file path
//        String validFastresumeFilePath = "sample.fastresume";
//        String torrentName = QbtUploadResetter.getTorrentName(validFastresumeFilePath);
//        assertEquals("sample", torrentName);
//
//        // Test with invalid .fastresume file path
//        String invalidFastresumeFilePath = "invalid.fastresume";
//        String defaultName = QbtUploadResetter.getTorrentName(invalidFastresumeFilePath);
//        assertEquals("Unknown Torrent", defaultName);
//    }

    @Test
    void getTorrentName_ValidFile_ReturnsName() {
        // Create a mock fastresume file path
        String fastresumeFilePath = "src/test/resources/bunny.fastresume";

        // Call the method under test
        String torrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);

        // Assert that the result is the expected torrent name
        assertEquals("bbb_sunflower_1080p_30fps_stereo_abl.mp4", torrentName);
    }

    @Test
    void getTorrentName_DecodedDataNotNull_ReturnsTorrentName() {
        // Provide a path to the fastresume file
        String fastresumeFilePath = "src/test/resources/invalid.fastresume";

        // Get the expected torrent name
        String expectedTorrentName = "Unknown Torrent";

        // Call the method under test
        String actualTorrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);

        // Assert that the actual torrent name matches the expected torrent name
        assertEquals(expectedTorrentName, actualTorrentName);
    }

    @Test
    void getTorrentName_InfoIsNull_ReturnsNull() {
        // Provide a path to the fastresume file
        String fastresumeFilePath = "src/test/resources/invalid-info-null.fastresume";

        // Get the expected torrent name
        String expectedTorrentName = "Unknown Torrent";

        // Call the method under test
        String actualTorrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);

        // Assert that the actual torrent name matches the expected torrent name
        assertEquals(expectedTorrentName, actualTorrentName);
    }

    @Test
    void getTorrentName_NameObjNotString_ReturnsNull() {
        // Provide a path to the fastresume file
        String fastresumeFilePath = "src/test/resources/invalid-name-not-string.fastresume";

        // Get the expected torrent name
        String expectedTorrentName = "Unknown Torrent";

        // Call the method under test
        String actualTorrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);

        // Assert that the actual torrent name matches the expected torrent name
        assertEquals(expectedTorrentName, actualTorrentName);
    }

//    @Test
//    void processFiles_shouldProcessFilesCorrectly() {
//        // Test processing files with valid data
//        // Create temporary directory with sample .fastresume files
//        File tempDir = Files.createTempDirectory("temp-dir").toFile();
//        File validFile1 = new File(tempDir, "valid1.fastresume");
//        File validFile2 = new File(tempDir, "valid2.fastresume");
//        try {
//            validFile1.createNewFile();
//            validFile2.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Test with valid files and single file mode disabled
//        QbtUploadResetter.processFiles(tempDir.getPath(), false);
//
//        // Test with valid files and single file mode enabled
//        QbtUploadResetter.processFiles(tempDir.getPath(), true);
//
//        // Clean up: delete temporary directory and files
//        deleteDirectory(tempDir);
//
//        // Test processing files with invalid data
//        // TODO: Implement this test
//    }
//
    // Utility method to recursively delete a directory
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
