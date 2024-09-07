# Yet Another (Super) Chip 8 Emulator

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/craigthomas/Chip8Java/gradle.yml?style=flat-square&branch=main)](https://github.com/craigthomas/Chip8Java/actions)
[![Coverage Status](https://img.shields.io/codecov/c/gh/craigthomas/Chip8Java?style=flat-square)](https://codecov.io/gh/craigthomas/Chip8Java)
[![Dependencies](https://img.shields.io/librariesio/github/craigthomas/Chip8Java?style=flat-square)](https://libraries.io/github/craigthomas/Chip8Java)
[![Version](https://img.shields.io/github/release/craigthomas/Chip8Java?style=flat-square)](https://github.com/craigthomas/Chip8Java/releases)
[![Downloads](https://img.shields.io/github/downloads/craigthomas/Chip8Java/total?style=flat-square)](https://github.com/craigthomas/Chip8Java/releases)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)

## Table of Contents

1. [What is it?](#what-is-it)
2. [License](#license)
3. [Compiling](#compiling)
4. [Running](#running)
   1. [Requirements](#requirements)
   2. [Starting the Emulator](#starting-the-emulator)
   3. [Running a ROM](#running-a-rom)
   4. [Screen Scale](#screen-scale)
   5. [Execution Delay](#execution-delay)
   6. [Memory Size](#memory-size)
   7. [Colors](#colors)
5. [Customization](#customization)
   1. [Keys](#keys)
   2. [Debug Keys](#debug-keys)
6. [ROM Compatibility](#rom-compatibility)
7. [Third Party Licenses and Attributions](#third-party-licenses-and-attributions)
   1. [JCommander](#jcommander)
   2. [Apache Commons IO](#apache-commons-io)
   3. [Vera Mono Font](#vera-mono-font)
    
## What is it?

This project is a Chip 8 emulator written in Java. There are two other versions
of the emulator written in different languages:

* [Chip8Python](https://github.com/craigthomas/Chip8Python)
* [Chip8C](https://github.com/craigthomas/Chip8C)

The original goal of these projects was to learn how to code a simple emulator.

In addition to supporting Chip 8 ROMs, the emulator also supports the Super Chip
8 instruction set. Note that no additional configuration is needed to run a 
Super Chip 8 ROM - simply run the ROM the same way you would run a normal Chip
8 ROM.


## License

This project makes use of an MIT license. Please see the file called 
LICENSE for more information. Note that this project may make use of other
software that has separate license terms. See the section called `Third
Party Licenses and Attributions` below for more information on those
software components.


## Compiling

To compile the project, you will need a Java Development Kit (JDK) version 8 or greater installed. 
Recently, Oracle has changed their license agreement to make personal and developmental use of their 
JDK free. However, some other use cases may require a paid subscription. Oracle's version of the 
JDK can be downloaded [here](https://www.oracle.com/technetwork/java/javase/downloads/index.html). 
Alternatively, if you prefer to use a JRE with an open-source license (GPL v2 with Classpath 
Exception), you may visit [https://adoptopenjdk.net](https://adoptopenjdk.net) and install the 
latest Java Development Kit (JDK) for your system. Again, JDK version 8 or better will work correctly.

To build the project, switch to the root of the source directory, and
type:

    ./gradlew build

On Windows, switch to the root of the source directory, and type:

    gradlew.bat build

The compiled JAR file will be placed in the `build/libs` directory, as a file called
`emulator-1.0.1-all.jar`.


## Running

### Requirements

You will need a copy of the Java Runtime Environment (JRE) version 8 or greater installed 
in order to run the compiled JAR file. For most systems, you can install Java 8 JRE by visiting 
[http://java.com](http://java.com) and installing the Oracle Java Runtime Environment for your 
platform. This version of the JRE is free for personal use but contains a custom binary license 
from Oracle. Alternatively, if you prefer to use a JRE with an open-source license (GPL 
v2 with Classpath Exception), you may visit [https://adoptopenjdk.net](https://adoptopenjdk.net) 
and install the latest Java Development Kit (JDK) for your system, which will include an appropriate JRE. 

### Starting the Emulator

By default, the emulator can start up without a ROM loaded. Simply double click
the JAR file, or run it with the following command line:

    java -jar emulator-1.0.1-all.jar
    
### Running a ROM

The command-line interface currently requires a single argument, which
is the full path to a Chip 8 ROM:

    java -jar emulator-1.0.1-all.jar /path/to/rom/filename

This will start the emulator with the specified ROM. 

### Screen Scale

The `--scale` switch will scale the size of the window (the original size
at 1x scale is 64 x 32):

    java -jar emulator-1.0.1-all.jar /path/to/rom/filename --scale 10

The command above will scale the window so that it is 10 times the normal
size. 

### Execution Delay

You may also wish to experiment with the `--delay` switch, which instructs
the emulator to add a delay to every operation that is executed. For example,

    java -jar emulator-1.0.1-all.jar /path/to/rom/filename --delay 10

The command above will add a 10 ms delay to every opcode that is executed.
This is useful for very fast computers (note that it is difficult to find
information regarding opcode execution times, as such, I have not attempted
any fancy timing mechanisms to ensure that instructions are executed in a
set amount of time).

### Memory Size

The original specification of the Chip8 language defined a 4K memory size 
for the interpreter. The addition of the XO Chip extensions require a 64K 
memory size for the interpreter. By default, the interpreter will start w
ith a 64K memory size, but this behavior can be controlled with the 
`--mem_size_4k` flag, which will start the emulator with 4K. 

### Colors

The original Chip8 language specification called for pixels to be turned 
on or off. It did not specify what color the pixel states had to be. The 
emulator lets the user specify what colors they want to use when the emulator 
is running. Color values are specified by using HTML hex values such as 
`AABBCC` without the leading `#`. There are currently 4 color values that can 
be set:

* `--color_0` specifies the background color. This defaults to `000000`.
* `--color_1` specifies bitplane 1 color. This defaults to `FF33CC`.
* `--color_2` specifies bitplane 2 color. This defaults to `33CCFF`.
* `--color_3` specifies bitplane 1 and 2 overlap color. This defaults to `FFFFFF`.
* 
For Chip8 and SuperChip 8 programs, only the background `color color_0` 
(for pixels turned off) and the bitplane 1 `color color_1` (for pixels turned 
on) are used. Only XO Chip programs will use `color_2` and `color_3` when 
the additional bitplanes are potentially used.

## Customization

The file `components/Keyboard.java` contains several variables that can be 
changed to customize the operation of the emulator. The Chip 8 has 16 keys:

### Keys

The original Chip 8 had a keypad with the numbered keys 0 - 9 and A - F (16
keys in total). The original key configuration was as follows:


| `1` | `2` | `3` | `C` |
|-----|-----|-----|-----|
| `4` | `5` | `6` | `D` |
| `7` | `8` | `9` | `E` |
| `A` | `0` | `B` | `F` |

The Chip8Java emulator maps them to the following keyboard keys by default:

| `1` | `2` | `3` | `4` |
|-----|-----|-----|-----|
| `Q` | `W` | `E` | `R` |
| `A` | `S` | `D` | `F` |
| `Z` | `X` | `C` | `V` |


### Debug Keys

Pressing a debug key at any time will cause the emulator to enter into a
different mode of operation. The debug keys are:

| Keyboard Key | Effect                         |
|:------------:|--------------------------------|
|    `ESC`     | Quits the emulator             |

## ROM Compatibility

Here are the list of public domain ROMs and their current status with the emulator, along
with links to public domain repositories where applicable.

### Chip 8 ROMs

| ROM Name                                                              |      Working       |     Flags     | 
|:----------------------------------------------------------------------|:------------------:|:-------------:|
| [down8](https://johnearnest.github.io/chip8Archive/play.html?p=down8) | :heavy_check_mark: |               | 

## Third Party Licenses and Attributions

### JCommander

This links to the JCommander library, which is licensed under the 
Apache License, Version 2.0. The license can be downloaded from
http://www.apache.org/licenses/LICENSE-2.0.html. The source code for this
software is available from [https://github.com/cbeust/jcommander](https://github.com/cbeust/jcommander)

### Apache Commons IO

This links to the Apache Commons IO, which is licensed under the
Apache License, Version 2.0. The license can be downloaded from
http://www.apache.org/licenses/LICENSE-2.0.html. The source code for this
software is available from http://commons.apache.org/io