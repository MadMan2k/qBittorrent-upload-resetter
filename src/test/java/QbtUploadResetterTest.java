import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;
import org.junit.jupiter.api.io.TempDir;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class QbtUploadResetterTest {

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
        File testFile = new File(getClass().getResource("/testDir/bunny.torrent").getFile());
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

    @Test
    void getTorrentName_ValidFile_ReturnsName() {
        // Create a mock fastresume file path
        String fastresumeFilePath = "src/test/resources/testDir/bunny.fastresume";

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

    @Test
    void testFilesFoundSingleFileModeDisabled() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Assuming encodeHexData, resetUploadValue, saveFileWithResetData, and getTorrentName
        // are correctly implemented in FileProcessor

        QbtUploadResetter.processFiles("src/test/resources/testDir", false);

        String output = outContent.toString();
        assertTrue(output.contains("Processing file: bunny.fastresume"));
        assertTrue(output.contains("Upload value reset successfully for torrent: bbb_sunflower_1080p_30fps_stereo_abl.mp4"));
        final PrintStream originalOut = System.out;
        System.setOut(originalOut);
    }

    @Test
    void testFilesFoundSingleFileModeEnabledWithSkipping() {
        String input1 = "n";
        ByteArrayInputStream inContent = new ByteArrayInputStream(input1.getBytes());
        System.setIn(inContent);

        PrintStream originalOut = System.out;
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Assuming encodeHexData, resetUploadValue, saveFileWithResetData, and getTorrentName
        // are correctly implemented in FileProcessor

        inContent = new ByteArrayInputStream("n".getBytes());
        System.setIn(inContent);

        QbtUploadResetter.processFiles("src/test/resources/testDir", true);

        String output = outContent.toString();
        assertTrue(output.contains("Processing file: bunny.fastresume"));
        assertTrue(output.contains("Skipping torrent: bbb_sunflower_1080p_30fps_stereo_abl.mp4"));

        System.setOut(originalOut);
    }


    @Test
    void processFiles_InvalidPath_PrintsErrorMessage() {
        // Redirect System.err to capture printed error message
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outputStreamCaptor));

        // Provide an invalid path
        String invalidPath = "invalid/path";

        // Call the method under test
        QbtUploadResetter.processFiles(invalidPath, false);

        // Assert that the error message is printed
        String expectedErrorMessage = "Invalid path specified or path is not a directory.";
        assertEquals(expectedErrorMessage, outputStreamCaptor.toString().trim());

        // Reset System.err
        System.setErr(System.err);
    }

    @Test
    void processFiles_NotADirectory() {
        // Redirect System.err to capture printed error message
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outputStreamCaptor));

        // Provide an invalid path
        String invalidPath = "src/test/resources/invalid.torrent";

        // Call the method under test
        QbtUploadResetter.processFiles(invalidPath, false);

        // Assert that the error message is printed
        String expectedErrorMessage = "Invalid path specified or path is not a directory.";
        assertEquals(expectedErrorMessage, outputStreamCaptor.toString().trim());

        // Reset System.err
        System.setErr(System.err);
    }

//    @Test
//    void processFiles_NoFastresumeFiles_PrintsNoFilesFoundMessage() throws IOException {
//        // Redirect System.out to capture printed message
//        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outputStreamCaptor));
//
//        // Create a temporary directory for testing
//        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tempDir");
//        if (!tempDir.mkdir()) {
//            throw new IOException("Failed to create temporary directory");
//        }
//
//        try {
//            // Call the method under test
//            QbtUploadResetter.processFiles(tempDir.getAbsolutePath(), false);
//
//            // Assert that the message indicating no .fastresume files is printed
//            String expectedMessage = "No .fastresume files found in the specified path";
//            assertEquals(expectedMessage, outputStreamCaptor.toString().trim());
//        } finally {
//            // Reset System.out
//            System.setOut(System.out);
//
//            // Clean up: delete the temporary directory
//            if (!tempDir.delete()) {
//                throw new IOException("Failed to delete temporary directory");
//            }
//        }
//    }

    @Test
    void processFiles_NoFastresumeFilesInNonEmptyDir_PrintsNoFilesFoundMessage() throws IOException {
        // Redirect System.out to capture printed message
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        // Create a temporary directory for testing
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tempDir");
        tempDir.mkdir();

        // Create a temporary file that does not have .fastresume extension in the directory
        File tempFile = new File(tempDir, "test.txt");
        tempFile.createNewFile();

        // Call the method under test
        QbtUploadResetter.processFiles(tempDir.getAbsolutePath(), false);

        // Assert that the message indicating no .fastresume files is printed
        String expectedMessage = "No .fastresume files found in the specified path";
        assertEquals(expectedMessage, outputStreamCaptor.toString().trim());

        // Reset System.out
        System.setOut(System.out);

        // Clean up: delete the temporary file and directory
        tempFile.delete();
        tempDir.delete();
    }

    @Test
    void processFiles_WithFastresumeFiles_ProcessesFiles() {
        // Redirect System.out to capture printed message
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        // Create a temporary directory for testing
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tempDir");
        tempDir.mkdir();

        // Create a sample .fastresume file
        File fastresumeFile = new File(tempDir, "sample.fastresume");
        try {
            fastresumeFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Call the method under test
        QbtUploadResetter.processFiles(tempDir.getAbsolutePath(), false);

        // Print the captured output for debugging
        System.out.println(outputStreamCaptor.toString());

        // Reset System.out
        System.setOut(System.out);

        // Clean up: delete the temporary directory
        fastresumeFile.delete();
        tempDir.delete();
    }



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

@Nested
class MainTest{
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    private final List<String> outMethods = Arrays.asList(
            "testMainWithPathArgument()",
            "testMainWithSingleArgument()",
            "testMainWithoutArguments()",
            "testMainWithHelpArgument()"
    );

    private final List<String> errMethods = Arrays.asList(
            "testMainWithPathArgumentMissedValue()",
            "testMainWithUnknownArgument()"
    );

    @BeforeEach
    void setContent(TestInfo info) {
        if (outMethods.contains(info.getDisplayName())) {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }
        if (errMethods.contains(info.getDisplayName())) {
            errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));
        }
    }

    @AfterEach
    void restoreContent(TestInfo info) {
        if (outMethods.contains(info.getDisplayName())) {
            System.setOut(System.out);
        }
        if (errMethods.contains(info.getDisplayName())) {
            System.setErr(System.err);
        }
    }

    @Test
    void testMainWithPathArgument() {
        // Execute main method with path argument
        String[] args = {"--path", "test_path"};
        QbtUploadResetter.main(args);

        // Check if the output contains the expected message
        assertTrue(outContent.toString().contains("Path specified: test_path"));
    }

    @Test
    void testMainWithPathArgumentMissedValue() {
        // Execute main method with path argument
        String[] args = {"--path"};
        QbtUploadResetter.main(args);

        // Check if the output contains the expected message
        assertTrue(errContent.toString().contains("Missing value for -p/--path option"));
    }

    @Test
    void testMainWithSingleArgument() {
        // Execute main method with single argument
        String[] args = {"--single"};
        QbtUploadResetter.main(args);

        // Check if the output contains the expected message
        assertTrue(outContent.toString().contains("Using single file mode"));

    }

    @Test
    void testMainWithoutArguments() {
        // Execute main method without arguments
        String[] args = {};
        QbtUploadResetter.main(args);

        // Check if the output contains the expected message
        assertTrue(outContent.toString().contains("Using default path"));
    }

    @Test
    void testMainWithHelpArgument() {
        // Execute main method with help argument
        String[] args = {"--help"};
        QbtUploadResetter.main(args);

        // Check if the output contains the expected message
        assertTrue(outContent.toString().contains("Usage: java QbtUploadResetter"));
    }

    @Test
    void testMainWithUnknownArgument() {
        // Execute main method with unknown argument
        String[] args = {"--unknown"};
        QbtUploadResetter.main(args);

        // Check if the output contains the expected message
        assertTrue(errContent.toString().contains("Unknown option: --unknown"));
    }
}