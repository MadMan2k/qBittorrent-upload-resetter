import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.BencodeException;
import com.dampcake.bencode.Type;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * QbtUploadResetter is a tool to reset the upload count for torrents in qBittorrent.
 * It processes .fastresume files found in the BT_backup folder, resetting the
 * total_uploaded value to 0.
 *
 * Command-line options:
 *  -p, --path <path>   Specify the path to the BT_backup folder.
 *  -s, --single        Prompt for confirmation before resetting the upload count for each file.
 *  -h, --help          Display this help message.
 */
public class QbtUploadResetter {
    private static final String TORRENT_FILE_EXTENSION = ".torrent";
    private static final String FASTRESUME_FILE_EXTENSION = ".fastresume";

    public static final String HEX_FIRST_ARGUMENT = "31343A746F74616C5F75706C6F6164656469"; // "14:total_uploadedi" in hexadecimal
    public static final String HEX_SECOND_ARGUMENT = "65383A747261636B657273"; // "e8:trackers" in hexadecimal
    public static final String HEX_ZERO = "30";
    public static List<String> successfulResets = new ArrayList<>();

    public static void main(String[] args) {
        String path = null;
        boolean singleFileMode = false;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-p":
                case "--path":
                    if (i + 1 < args.length) {
                        path = args[i + 1];
                        i++; // Skip next argument since it's the path value
                    } else {
                        System.err.println("Missing value for -p/--path option");
                        return;
                    }
                    break;
                case "-s":
                case "--single":
                    singleFileMode = true;
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    return;
                default:
                    System.err.println("Unknown option: " + arg);
                    return;
            }
        }

        // Use parsed arguments in your application logic
        if (path != null) {
            System.out.println("Path specified: " + path);
            processFiles(path, singleFileMode);
        } else {
            System.out.println("Using default path");
            processFiles(System.getenv("LocalAppData") + "\\qBittorrent\\BT_backup", singleFileMode);
        }

        // Print the list of successfully updated torrents
        printSuccessList();
    }

    /**
     * Processes the .fastresume files in the specified path, resetting the upload count for each file.
     *
     * @param path            The path to the BT_backup folder.
     * @param singleFileMode  Whether to prompt for confirmation before resetting each file.
     */
    public static void processFiles(String path, boolean singleFileMode) {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid path specified or path is not a directory.");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(FASTRESUME_FILE_EXTENSION));

        if (files != null && files.length > 0) {
            for (File file : files) {
                String hexData = encodeHexData(file);
                if (!hexData.isEmpty()) {
                    System.out.println("Processing file: " + file.getName());
                    boolean reset = true;
                    if (singleFileMode) {
                        reset = promptUserForReset(file.getPath());
                    }
                    String torrentName = getTorrentName(file.getPath());
                    if (reset) {
                        String resetHexData = resetUploadValue(hexData);
                        saveFileWithResetData(resetHexData, file.getPath());
                        System.out.println("Upload value reset successfully for torrent: " + torrentName);
                        successfulResets.add(torrentName);
                    } else {
                        System.out.println("Skipping torrent: " + torrentName);
                    }
                }
            }
        } else {
            System.out.println("No .fastresume files found in the specified path");
        }
    }

    /**
     * Saves the modified hex data back to the file.
     *
     * @param resetHexData The modified hex data.
     * @param path         The file path to save the data.
     */
    public static void saveFileWithResetData(String resetHexData, String path) {
        byte[] bytes = DatatypeConverter.parseHexBinary(resetHexData);
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(bytes);
        } catch (IOException e) {
            System.err.println("Failed to save the file: " + path);
            e.printStackTrace();
        }
    }

    /**
     * Reads and encodes hex data from a file.
     *
     * @param file The file to read from.
     * @return The hex data as a string.
     */
    public static String encodeHexData(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            return DatatypeConverter.printHexBinary(data);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + file.getPath());
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Prompts the user for confirmation before resetting a file.
     *
     * @param fastresumeFilePath The path of the .fastresume file.
     * @return true if the user confirms, false otherwise.
     */
    public static boolean promptUserForReset(String fastresumeFilePath) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Reset upload value for torrent: " + getTorrentName(fastresumeFilePath) + "? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y");
    }

    /**
     * Resets the upload value in the hex data.
     *
     * @param hexData The original hex data.
     * @return The modified hex data with the upload value reset.
     */
    public static String resetUploadValue(String hexData) {
        int startFirstArgument = hexData.indexOf(HEX_FIRST_ARGUMENT);
        int startSecondArgument = hexData.indexOf(HEX_SECOND_ARGUMENT);

        if (startFirstArgument == -1 || startSecondArgument == -1 || startSecondArgument < startFirstArgument) {
            throw new IllegalArgumentException("HEX_FIRST_ARGUMENT and HEX_SECOND_ARGUMENT are not found in the string in the correct order.");
        }

        String before = hexData.substring(0, startFirstArgument + HEX_FIRST_ARGUMENT.length());
        String after = hexData.substring(startSecondArgument);

        return before + HEX_ZERO + after;
    }

    /**
     * Decodes a .torrent file.
     *
     * @param file The .torrent file.
     * @return A map containing the decoded data.
     */
    public static Map<String, Object> decodeTorrentFile(File file) {
        byte[] fileContent = readFileToByteArray(file);
        Bencode bencode = new Bencode();
        try {
            return bencode.decode(fileContent, Type.DICTIONARY);
        } catch (BencodeException e) {
            System.err.println("Failed to decode: " + file.getPath());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads a file to a byte array.
     *
     * @param file The file to read.
     * @return The file content as a byte array.
     */
    public static byte[] readFileToByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.err.println("Failed to read file: " + file.getPath());
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Extracts the torrent name from a .fastresume file path.
     *
     * @param fastresumeFilePath The path to the .fastresume file.
     * @return The torrent name, or a default message if not found.
     */
    public static String getTorrentName(String fastresumeFilePath) {
        String torrentFilePath = fastresumeFilePath.replace(FASTRESUME_FILE_EXTENSION, TORRENT_FILE_EXTENSION);
        File torrentFile = new File(torrentFilePath);

        if (torrentFile.exists()) {
            Map<String, Object> decodedTorrentData = decodeTorrentFile(torrentFile);
            if (decodedTorrentData != null) {
                Map<String, Object> info = (Map<String, Object>) decodedTorrentData.get("info");
                if (info != null) {
                    Object nameObj = info.get("name");
                    if (nameObj instanceof String) {
                        return (String) nameObj;
                    }
                }
            }
        }

        return "Unknown Torrent";
    }

    /**
     * Prints the help message.
     */
    private static void printHelp() {
        System.out.println("Usage: java QbtUploadResetter [options]");
        System.out.println("Options:");
        System.out.println("  -p, --path <path>   Specify the path to the BT_backup folder.");
        System.out.println("  -s, --single        Prompt for confirmation before resetting the upload count for each file.");
        System.out.println("  -h, --help          Display this help message.");
    }

    /**
     * Prints the list of successfully reset torrents.
     */
    private static void printSuccessList() {
        if (!successfulResets.isEmpty()) {
            System.out.println("\nUpload value reset successfully for the following torrents:");
            for (int i = 0; i < successfulResets.size(); i++) {
                System.out.println((i + 1) + ". " + successfulResets.get(i));
            }
        } else {
            System.out.println("No upload values were reset.");
        }
    }
}
