[![Java CI](https://github.com/MadMan2k/qBittorrent-upload-resetter/actions/workflows/main.yml/badge.svg)](https://github.com/MadMan2k/qBittorrent-upload-resetter/actions/workflows/main.yml)
[![codecov](https://codecov.io/gh/MadMan2k/qBittorrent-upload-resetter/graph/badge.svg?token=Y1P3K94KJZ)](https://codecov.io/gh/MadMan2k/qBittorrent-upload-resetter)

# qBittorrent Upload Resetter

<img src='https://github.com/MadMan2k/qBittorrent-upload-resetter/blob/main/src/main/resources/img.png' alt='img' width='861'/>

qBittorrent Upload Resetter is a simple Java application designed to reset the uploaded amount in `.fastresume` files used by qBittorrent.

## Features

- Automatically detects and resets the uploaded amount in `.fastresume` files.
- Option to prompt for confirmation before resetting each file.

## Prerequisites

- Java Development Kit (JDK) 8 or higher.

## Usage

You can compile the code yourself if you wish. Alternatively, you can [download](https://github.com/MadMan2k/qBittorrent-upload-resetter/releases/tag/qBittorrent-upload-resetter-1.0) the pre-compiled ready to use `.jar` file.

Once downloaded or compiled, you can run the `.jar` file from any directory. You do not need to place it in the `BT_backup` folder or any specific qBittorrent folder. Simply navigate to the directory containing the `.jar` file using your terminal (Mac/Linux) or command line (Windows). Then run the following command:

```sh
java -jar qBittorrent-upload-resetter.jar [options]
```
Alternatively, you can run the application from any location by specifying the full `<path>` to the .jar file:
```sh
java -jar /full/path/to/qBittorrent-upload-resetter.jar [options]
```
## Options

| Short Option | Long Option   | Description                                                                          |
|--------------|---------------|--------------------------------------------------------------------------------------|
| -p `<path>`  | --path `<path>`| Specify the path to the BT_backup folder. If not specified, the default path is used.|
| -s           | --single      | Prompt for confirmation before resetting the uploaded amount for each file.          |
| -h           | --help        | Display the help message.                                                            |


## Default settings

The application is configured by default for Windows users.

For Mac/Linux users, you will need to specify the `<path>` to the `BT_backup` folder when running the application.

## Example

To reset the uploaded amount in the default `BT_backup` folder on a Windows system:
```sh
java -jar qBittorrent-upload-resetter.jar
```
To reset the uploaded amount in a custom `BT_backup` folder or on a Mac/Linux system:
```sh
java -jar qBittorrent-upload-resetter.jar -p /path/to/qBittorrent/BT_backup
```
To reset the uploaded amount with confirmation for each file:
```sh
java -jar qBittorrent-upload-resetter.jar -s
```

## Example Output
```yaml
Using single file mode
Path specified: C:\Users\YourUsername\AppData\Local\qBittorrent\BT_backup
Processing file: example.fastresume
Reset uploaded amount for torrent: Example Torrent? (y/n): y
Uploaded amount reset successfully for torrent: Example Torrent

Uploaded amount reset successfully for the following torrents:
1. Example Torrent
```

## Notes
*   Ensure that qBittorrent is not running while you are modifying the `.fastresume` files to avoid any conflicts.
*   Make a backup of your `BT_backup` folder before running this application to prevent any accidental data loss.

## License
This project is licensed under the MIT License. See the [LICENSE](https://github.com/MadMan2k/qBittorrent-upload-reset/blob/main/LICENSE) file for details.