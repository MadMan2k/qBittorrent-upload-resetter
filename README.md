[![Java CI](https://github.com/MadMan2k/qBittorrent-upload-resetter/actions/workflows/main.yml/badge.svg)](https://github.com/MadMan2k/qBittorrent-upload-resetter/actions/workflows/main.yml)
[![codecov](https://codecov.io/gh/MadMan2k/qBittorrent-upload-resetter/graph/badge.svg?token=Y1P3K94KJZ)](https://codecov.io/gh/MadMan2k/qBittorrent-upload-resetter)

# qBittorrent upload resetter

qBittorrent upload resetter is a simple Java application designed to reset the upload value in `.fastresume` files used by qBittorrent.

## Features

- Automatically detects and resets the upload value in `.fastresume` files.
- Option to prompt for confirmation before resetting each file.

## Prerequisites

- Java Development Kit (JDK) 8 or higher.

## Usage

If you want to compile code by yourself, check Compilation part. If you want to use already compiled application - just [download](https://github.com/MadMan2k/qBittorrent-upload-reset/blob/main/LICENSE) latest jar file and run following comand from your terminal(Mac/Linux) or command line(Windows)

To run the application, use the following command:
```sh
java QbtUpldReset [options]
```

## Compilation

To compile the source code, follow these steps:

1. Open a terminal or command prompt on your Windows/Mac/Linux machine.
2. Navigate to the directory containing `QbtUpldReset.java`.
    ```sh
   qBittorrent-upload-reset/src/main/java/
3. Run the following command:

   ```sh
   javac QbtUpldResetter.java
This will generate a QbtUpldReset.class file in the same directory

## Options

| Short Option | Long Option   | Description                                                  |
|--------------|---------------|--------------------------------------------------------------|
| -p           | --path <path> | Specify the path to the BT_backup folder. If not specified, the default path is used. |
| -s           | --single      | Prompt for confirmation before resetting the upload count for each file. |
| -e           | --encoding <encoding> | Specify the character encoding (default is Windows-1251). |
| -h           | --help        | Display the help message.                                    |


## Default settings

The application is configured by default for Windows users with the character encoding set to Windows-1251 for the names of the torrents.

For Mac/Linux users, you will need to specify the path to the `BT_backup` folder when running the application.

The character encoding can also be changed if needed. For more details on how to specify a different path or character encoding, please refer to the "Options" and "Example" sections of this document.

## Example

To reset the upload counts in the default `BT_backup` folder on a Windows system:
```sh
java QbtUpldReset
```
To reset the upload counts in a custom `BT_backup` folder or on a Mac/Linux system:
```sh
java QbtUpldReset -p /path/to/qBittorrent/BT_backup
```
To reset the upload counts with confirmation for each file:
```sh
java QbtUpldReset -s
```
To reset the upload counts with a specific encoding:
```sh
java QbtUpldReset -e UTF-8
```

## Example Output
```yaml
Path specified: C:\Users\YourUsername\AppData\Local\qBittorrent\BT_backup
Processing file: example.fastresume
Reset upload count for torrent: Example Torrent? (y/n): y
Upload count reset successfully for torrent: Example Torrent

Successfully reset torrents:
1. Example Torrent
```

## Notes
*   Ensure that qBittorrent is not running while you are modifying the .fastresume files to avoid any conflicts.
*   Make a backup of your `BT_backup` folder before running this application to prevent any accidental data loss.

## License
This project is licensed under the MIT License. See the [LICENSE](https://github.com/MadMan2k/qBittorrent-upload-reset/blob/main/LICENSE) file for details.