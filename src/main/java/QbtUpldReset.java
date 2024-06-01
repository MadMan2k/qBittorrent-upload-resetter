import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QbtUpldReset is a tool to reset the upload count for torrents in qBittorrent.
 * It processes .fastresume files found in the BT_backup folder, resetting the
 * total_uploaded value to 0.
 *
 * Command-line options:
 *  -p, --path <path>   Specify the path to the BT_backup folder.
 *  -s, --single        Prompt for confirmation before resetting the upload count for each file.
 *  -e, --encoding      Specify the character encoding (default is Windows-1251).
 *  -h, --help          Display this help message.
 */
public class QbtUpldReset {
    private static List<String> successfulResets = new ArrayList<>();

    public static void main(String[] args) {
        String path = null;
        boolean singleFileMode = false;
        String encoding = "Windows-1251"; // Default encoding

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
                case "-e":
                case "--encoding":
                    if (i + 1 < args.length) {
                        encoding = args[i + 1];
                        i++; // Skip next argument since it's the encoding value
                    } else {
                        System.err.println("Missing value for -e/--encoding option");
                        return;
                    }
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
            processFiles(path, singleFileMode, encoding);
        } else {
            System.out.println("Using default path");
            processFiles(System.getenv("LocalAppData") + "\\qBittorrent\\BT_backup", singleFileMode, encoding);
        }

        // Print the list of successfully updated torrents
        printSuccessList();
    }

    /**
     * Processes the .fastresume files in the specified path, resetting the upload count for each file.
     *
     * @param path            The path to the BT_backup folder.
     * @param singleFileMode  Whether to prompt for confirmation before resetting each file.
     * @param encoding        The character encoding to use.
     */
    private static void processFiles(String path, boolean singleFileMode, String encoding) {
        File folder = new File(path);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".fastresume"));

        if (files != null && files.length > 0) {
            for (File file : files) {
                System.out.println("Processing file: " + file.getName());
                boolean reset = true;
                if (singleFileMode) {
                    reset = promptUserForReset(file, encoding);
                }
                if (reset) {
                    resetUploadCount(file, encoding);
                    String torrentName = getTorrentName(file, encoding);
                    System.out.println("Upload count reset successfully for torrent: " + torrentName);
                    successfulResets.add(torrentName);
                } else {
                    System.out.println("Skipping file: " + file.getName());
                }
            }
        } else {
            System.out.println("No .fastresume files found in the specified path");
        }
    }

    /**
     * Prompts the user for confirmation before resetting the upload count for the specified file.
     *
     * @param file     The .fastresume file.
     * @param encoding The character encoding to use.
     * @return Whether the user confirmed the reset.
     */
    private static boolean promptUserForReset(File file, String encoding) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Reset upload count for torrent: " + getTorrentName(file, encoding) + "? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("yes") || input.equals("y");
    }

    /**
     * Resets the upload count for the specified .fastresume file by setting the total_uploaded value to 0.
     *
     * @param file     The .fastresume file.
     * @param encoding The character encoding to use.
     */
    private static void resetUploadCount(File file, String encoding) {
        try {
            // Read file content
            String fileContent = readFileContent(file);

            // Define pattern to find 'total_uploaded' value
            Pattern pattern = Pattern.compile("14:total_uploadedi(\\d+)e");
            Matcher matcher = pattern.matcher(fileContent);

            // Replace the value of total_uploaded with 0 if found
            if (matcher.find()) {
                String newFileContent = matcher.replaceAll("14:total_uploadedi0e");
                Files.write(file.toPath(), newFileContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Upload count reset for file: " + file.getName());
            } else {
                System.err.println("No total_uploaded field found in file: " + file.getName());
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + file.getName());
            e.printStackTrace();
        }
    }

    /**
     * Extracts the torrent name from the specified .fastresume file.
     *
     * @param file     The .fastresume file.
     * @param encoding The character encoding to use.
     * @return The torrent name, or "No torrent name found in file" if the name could not be extracted.
     */
    private static String getTorrentName(File file, String encoding) {
        // Read the contents of the file
        String fileContent = readFileContent(file);

        // Extract the torrent name from the file content
        String torrentName = extractTorrentName(fileContent, encoding);

        // Return the extracted torrent name
        if (torrentName != null && !torrentName.isEmpty()) {
            return torrentName;
        } else {
            return "No torrent name found in file";
        }
    }

    /**
     * Reads the contents of the specified file and returns it as a string.
     *
     * @param file The file to read.
     * @return The contents of the file as a string.
     */
    private static String readFileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getName());
            return "";
        }
    }

    /**
     * Extracts the torrent name from the file content using a regular expression.
     *
     * @param fileContent The content of the .fastresume file.
     * @param encoding    The character encoding to use.
     * @return The extracted torrent name, or an empty string if the name could not be extracted.
     */
    private static String extractTorrentName(String fileContent, String encoding) {
        // Define the pattern to match the torrent name
        Pattern pattern = Pattern.compile("name=(.*?)ee11:upload_mode", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fileContent);

        // Extract the torrent name if found
        if (matcher.find()) {
            String encodedName = matcher.group(1);

            try {
                return URLDecoder.decode(encodedName, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Prints the list of torrents for which the upload count was successfully reset.
     */
    private static void printSuccessList() {
        if (successfulResets.isEmpty()) {
            System.out.println("No torrents were updated successfully.");
        } else {
            System.out.println("\nSuccessfully reset torrents:");
            for (int i = 0; i < successfulResets.size(); i++) {
                System.out.println((i + 1) + ". " + successfulResets.get(i));
            }
        }
    }

    /**
     * Prints the help message, describing the usage and options of the application.
     */
    private static void printHelp() {
        System.out.println("Usage: java QbtUpldReset [options]");
        System.out.println("Options:");
        System.out.println("  -p, --path <path>   Specify path to BT_backup folder");
        System.out.println("  -s, --single        Prompt for confirmation before resetting upload count for each file");
        System.out.println("  -e, --encoding      Specify character encoding (default is Windows-1251)");
        System.out.println("  -h, --help          Display this help message");
    }
}
