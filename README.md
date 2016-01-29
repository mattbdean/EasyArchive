Easy Archive
============

EasyArchive aims to be *the* app to have for both casual and power users that just need to unzip that archive.

This app supports the inflation of `7z`, `bzip2`, `gzip`, `jar`, `lzma`, `pack200`, `rar`, `tar`, `xz`, and `zip` archives.

##The Project, Explained

###The App
All Android UI components are stored in the [`app`](https://github.com/thatJavaNerd/EasyArchive/tree/master/app) module.

###The Library
>Note: If you come from a Windows background or do not understand the difference between archiving and compression, please read [this article](http://www.linuxjournal.com/article/9370)

The Easy Archive [`library`](https://github.com/thatJavaNerd/EasyArchive/tree/master/lib) module is what powers the app. It revolves around the `Inflater` interface. All classes that aim to provide archive-extracting functionality must implement this class. Each Inflater takes off a "layer" of compression or extracts the files. For example, the `GzipInflater` would take an input file of `sample.tar.gz` and produce an output file of `sample.tar`. Note that what the user would really want is to extract the files in the `tar` archive, and for that we would use the `TarInflater` class to finish the job.

For ease of use, the library comes with the `InflaterAggregation` class, which will automatically invoke the correct `Inflater`s for the given input archive. For example, `InflaterAggregation` would take an input file of `sample.tar.gz` and produce an output of the contents of the underlying `tar` file.

###The Scripts
The scripts that make testing the app and library are found in the [`scripts/`](https://github.com/thatJavaNerd/EasyArchive/tree/master/scripts) directory. The two bash scripts that are found here are `mk.sh` and `pu.sh`.

####[`mk.sh`](https://github.com/thatJavaNerd/EasyArchive/blob/master/scripts/mk.sh)

This script makes several archives all from one common directory: the [`sources`](https://github.com/thatJavaNerd/EasyArchive/tree/master/library/src/test/resources/sources) folder.

####[`pu.sh`](https://github.com/thatJavaNerd/EasyArchive/blob/master/scripts/pu.sh)

This script uses `adb` to push all of the archives created from `mk.sh` to the currently connected Android device at `/mnt/sdcard/`

