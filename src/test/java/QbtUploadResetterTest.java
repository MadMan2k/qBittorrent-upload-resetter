import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.Objects;

public class QbtUploadResetterTest {

    @Test
    void encodeHexData_shouldEncodeHexDataCorrectly() {
        // Test encoding in hex
        String hexData = "48656C6C6F20576F726C64"; // "Hello World" in hexadecimal

        // Load the file from resources
        File fileHelloWorld = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("HelloWorld.txt")).getFile());

        String decodedData = QbtUploadResetter.encodeHexData(fileHelloWorld);
        assertEquals(hexData, decodedData);

        // Test encoding empty file
        String emptyData = "";
        File fileEmpty = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("Empty.txt")).getFile());
        String emptyDecodedData = QbtUploadResetter.encodeHexData(fileEmpty);
        assertEquals(emptyData, emptyDecodedData);
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
//
//    @Test
//    void resetUploadValue_shouldResetUploadValueInHexData() {
//        // Test resetting upload value in hex data
//        String originalHexData = "31343A746F74616C5F75706C6F616465646965383A747261636B657273";
//        String modifiedHexData = QbtUploadResetter.resetUploadValue(originalHexData);
//        assertEquals("31343A746F74616C5F75706C6F61646564693065383A747261636B657273", modifiedHexData);
//    }
//
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
//
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
//
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
//    // Utility method to recursively delete a directory
//    private void deleteDirectory(File directory) {
//        if (directory.exists()) {
//            File[] files = directory.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isDirectory()) {
//                        deleteDirectory(file);
//                    } else {
//                        file.delete();
//                    }
//                }
//            }
//            directory.delete();
//        }
//    }
}
