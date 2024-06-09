import org.junit.jupiter.api.Test;
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
    void testMainWithPathArgument() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = {"--path", "test_path"};
        QbtUploadResetter.main(args);
        assertTrue(outContent.toString().contains("Path specified: test_path"));
        System.setOut(System.out);
    }

    @Test
    void testMainWithPathArgumentMissedValue() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String[] args = {"--path"};
        QbtUploadResetter.main(args);
        assertTrue(errContent.toString().contains("Missing value for -p/--path option"));
        System.setOut(System.out);
    }

    @Test
    void testMainWithSingleArgument() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = {"--single"};
        QbtUploadResetter.main(args);
        System.setOut(System.out);
        assertTrue(outContent.toString().contains("Using single file mode"));
    }

    @Test
    void testMainWithoutArguments() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = {};
        QbtUploadResetter.main(args);
        assertTrue(outContent.toString().contains("Using default path"));
        System.setOut(System.out);
    }

    @Test
    void testMainWithHelpArgument() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String[] args = {"--help"};
        QbtUploadResetter.main(args);
        assertTrue(outContent.toString().contains("Usage: java QbtUploadResetter"));
        System.setOut(System.out);
    }

    @Test
    void testMainWithUnknownArgument() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String[] args = {"--unknown"};
        QbtUploadResetter.main(args);
        assertTrue(errContent.toString().contains("Unknown option: --unknown"));
        System.setOut(System.out);
    }

    @Test
    public void testEncodeHexData() throws IOException {
        File tempFile = tempDir.resolve("testfile.txt").toFile();
        String content = "Hello, World!";
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content.getBytes());
        }
        String expectedHex = DatatypeConverter.printHexBinary(content.getBytes());
        String result = QbtUploadResetter.encodeHexData(tempFile);
        assertEquals(expectedHex, result);
    }

    @Test
    public void testEncodeHexDataEmptyFile() throws IOException {
        File emptyFile = tempDir.resolve("emptyfile.txt").toFile();
        emptyFile.createNewFile();
        String expectedHex = "";
        String result = QbtUploadResetter.encodeHexData(emptyFile);
        assertEquals(expectedHex, result);
    }

    @Test
    public void testEncodeHexData_ExceptionHandling() {
        File nonExistentFile = new File("nonexistentfile.txt");
        String expectedHex = "";
        String result = QbtUploadResetter.encodeHexData(nonExistentFile);
        assertEquals(expectedHex, result);
    }

    @Test
    public void testSaveFileWithResetData() throws IOException {
        String resetHexData = "48656C6C6F2C20576F726C64"; // Hexadecimal representation of "Hello, World!"
        byte[] expectedBytes = DatatypeConverter.parseHexBinary(resetHexData);
        File tempFile = tempDir.resolve("testfile.txt").toFile();
        QbtUploadResetter.saveFileWithResetData(resetHexData, tempFile.getAbsolutePath());
        byte[] actualBytes;
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outContent.write(buffer, 0, bytesRead);
            }
            actualBytes = outContent.toByteArray();
        }
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    void testSaveFileWithResetData_ExceptionHandling() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String invalidPath = "invalid/path.txt";
        QbtUploadResetter.saveFileWithResetData("", invalidPath);
        String errorOutput = errContent.toString();
        System.setErr(System.err);
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
        String input = "yes\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        boolean result = QbtUploadResetter.promptUserForReset("examplePath");
        System.setIn(System.in);
        assertTrue(result);
    }

    @Test
    void promptUserForReset_YInput() {
        String input = "y\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        boolean result = QbtUploadResetter.promptUserForReset("examplePath");
        System.setIn(System.in);
        assertTrue(result);
    }

    @Test
    void promptUserForReset_NoInput() {
        String input = "no\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        boolean result = QbtUploadResetter.promptUserForReset("examplePath");
        System.setIn(System.in);
        assertFalse(result);
    }

    @Test
    void decodeTorrentFile_ValidFile_ReturnsMap() {
        File testFile = new File(getClass().getResource("/testDir/bunny.torrent").getFile());
        try {
            Map<String, Object> result = QbtUploadResetter.decodeTorrentFile(testFile);
            assertNotNull(result);
            assertTrue(result.containsKey("website"));
            assertEquals("http://bbb3d.renderfarming.net", result.get("website"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void decodeTorrentFile_InvalidFile_ReturnsNull() {
        File testFile = new File("invalid.torrent");
        Map<String, Object> result = QbtUploadResetter.decodeTorrentFile(testFile);
        assertNull(result);
        testFile.delete();
    }

    @Test
    void getTorrentName_ValidFile_ReturnsName() {
        String fastresumeFilePath = "src/test/resources/testDir/bunny.fastresume";
        String torrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);
        assertEquals("bbb_sunflower_1080p_30fps_stereo_abl.mp4", torrentName);
    }

    @Test
    void getTorrentName_DecodedDataNotNull_ReturnsTorrentName() {
        String fastresumeFilePath = "src/test/resources/invalid.fastresume";
        String expectedTorrentName = "Unknown Torrent";
        String actualTorrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);
        assertEquals(expectedTorrentName, actualTorrentName);
    }

    @Test
    void getTorrentName_InfoIsNull_ReturnsNull() {
        String fastresumeFilePath = "src/test/resources/invalid-info-null.fastresume";
        String expectedTorrentName = "Unknown Torrent";
        String actualTorrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);
        assertEquals(expectedTorrentName, actualTorrentName);
    }

    @Test
    void getTorrentName_NameObjNotString_ReturnsNull() {
        String fastresumeFilePath = "src/test/resources/invalid-name-not-string.fastresume";
        String expectedTorrentName = "Unknown Torrent";
        String actualTorrentName = QbtUploadResetter.getTorrentName(fastresumeFilePath);
        assertEquals(expectedTorrentName, actualTorrentName);
    }

    @Test
    void testFilesFoundSingleFileModeDisabled() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
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
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String invalidPath = "invalid/path";
        QbtUploadResetter.processFiles(invalidPath, false);
        String expectedErrorMessage = "Invalid path specified or path is not a directory.";
        assertEquals(expectedErrorMessage, errContent.toString().trim());
        System.setErr(System.err);
    }

    @Test
    void processFiles_NotADirectory() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String invalidPath = "src/test/resources/invalid.torrent";
        QbtUploadResetter.processFiles(invalidPath, false);
        String expectedErrorMessage = "Invalid path specified or path is not a directory.";
        assertEquals(expectedErrorMessage, errContent.toString().trim());
        System.setErr(System.err);
    }

    @Test
    void processFiles_NoFastresumeFilesInNonEmptyDir_PrintsNoFilesFoundMessage() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tempDir");
        tempDir.mkdir();
        File tempFile = new File(tempDir, "test.txt");
        tempFile.createNewFile();
        QbtUploadResetter.processFiles(tempDir.getAbsolutePath(), false);
        String expectedMessage = "No .fastresume files found in the specified path";
        assertEquals(expectedMessage, outContent.toString().trim());
        System.setOut(System.out);
        tempFile.delete();
        tempDir.delete();
    }

    @Test
    void processFiles_WithFastresumeFiles_ProcessesFiles() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tempDir");
        tempDir.mkdir();
        File fastresumeFile = new File(tempDir, "sample.fastresume");
        try {
            fastresumeFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        QbtUploadResetter.processFiles(tempDir.getAbsolutePath(), false);
        System.out.println(outContent.toString());
        System.setOut(System.out);
        fastresumeFile.delete();
        tempDir.delete();
    }
}
