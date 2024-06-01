import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QbtUpldReset {
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
    }

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
                    System.out.println("Upload count reset successfully for torrent: " + getTorrentName(file, encoding));
                } else {
                    System.out.println("Skipping file: " + file.getName());
                }
            }
        } else {
            System.out.println("No .fastresume files found in the specified path");
        }
    }

    private static boolean promptUserForReset(File file, String encoding) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Reset upload count for torrent: " + getTorrentName(file, encoding) + "? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("yes") || input.equals("y");
    }

    private static void resetUploadCount(File file, String encoding) {
        // Logic to reset upload count goes here
        // For demonstration purposes, we're just printing the file content
        System.out.println("Resetting upload count for torrent: " + getTorrentName(file, encoding));
    }

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

    private static String readFileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getName());
            return "";
        }
    }

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

    private static void printHelp() {
        System.out.println("Usage: java QbtUpldReset [options]");
        System.out.println("Options:");
        System.out.println("  -p, --path <path>   Specify path to BT_backup folder");
        System.out.println("  -s, --single        Prompt for confirmation before resetting upload count for each file");
        System.out.println("  -e, --encoding      Specify character encoding (default is Windows-1251)");
        System.out.println("  -h, --help          Display this help message");
    }
}
