import java.io.File;
import java.util.Scanner;

public class QbtUpldReset {
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
    }

    private static void processFiles(String path, boolean singleFileMode) {
        File folder = new File(path);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".fastresume"));

        if (files != null && files.length > 0) {
            int count = 1;
            for (File file : files) {
                System.out.println("Processing file: " + file.getName());
                boolean reset = true;
                if (singleFileMode) {
                    reset = promptUserForReset(file.getName());
                }
                if (reset) {
                    resetUploadCount(file);
                    System.out.println("Upload count reset for file: " + file.getName());
                } else {
                    System.out.println("Skipping file: " + file.getName());
                }
                count++;
            }
        } else {
            System.out.println("No .fastresume files found in the specified path");
        }
    }

    private static boolean promptUserForReset(String fileName) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Reset upload count for file " + fileName + "? (yes/no): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("yes") || input.equals("y");
    }

    private static void resetUploadCount(File file) {
        // Logic to reset upload count goes here
        // For demonstration purposes, we're just printing the file content
        System.out.println("Resetting upload count for file: " + file.getName());
    }

    private static void printHelp() {
        System.out.println("Usage: java QbtUpldReset [options]");
        System.out.println("Options:");
        System.out.println("  -p, --path <path>   Specify path to BT_backup folder");
        System.out.println("  -s, --single        Prompt for confirmation before resetting upload count for each file");
        System.out.println("  -h, --help          Display this help message");
    }
}
