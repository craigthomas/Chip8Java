# Yet Another (Super) Chip 8 Emulator

[![Build Status](https://img.shields.io/travis/craigthomas/Chip8Java?style=flat-square)](https://travis-ci.org/craigthomas/Chip8Java) 
[![Coverage Status](https://img.shields.io/codecov/c/gh/craigthomas/Chip8Java?style=flat-square)](https://codecov.io/gh/craigthomas/Chip8Java)
[![Codacy Badge](https://img.shields.io/codacy/grade/51b8560fe61441a3b05b83c1e7a5eee6?style=flat-square)](https://www.codacy.com/app/craig-thomas/Chip8Java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=craigthomas/Chip8Java&amp;utm_campaign=Badge_Grade)
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
    6. [Debug Mode](#debug-mode)
5. [Keys](#keys)
    1. [Regular Keys](#regular-keys)
    2. [Debug Keys](#debug-keys)
6. [Modes](#modes)
    1. [Trace Mode](#trace-mode)
    2. [Step Mode](#step-mode)
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

Simply copy the source files to a directory of your choice. In addition
to the source, you will need the following required software packages:

* [Java JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7.0 u51 or later

To build the project, switch to the root of the source directory, and
type:

    ./gradlew build

On Windows, switch to the root of the source directory, and type:

    gradlew.bat build

The compiled Jar file will be placed in the `build/libs` directory, as a file called
`emulator-1.0-all.jar`.


## Running

### Requirements

To run the emulator, you will need to install the Java Runtime Environment (JRE)
on your computer. The Oracle JRE can be downloaded from [www.java.com](http://www.java.com). 

### Starting the Emulator

By default, the emulator can start up without a ROM loaded. Simply double click
the JAR file, or run it with the following command line:

    java -jar emulator-1.0-all.jar
    
### Running a ROM

The command-line interface currently requires a single argument, which
is the full path to a Chip 8 ROM:

    java -jar emulator-1.0-all.jar /path/to/rom/filename

This will start the emulator with the specified ROM. 

### Screen Scale

The `-s` switch will scale the size of the window (the original size
at 1x scale is 64 x 32):

    java -jar emulator-1.0-all.jar /path/to/rom/filename -s 10

The command above will scale the window so that it is 10 times the normal
size. 

### Execution Delay

You may also wish to experiment with the `-d` switch, which instructs
the emulator to add a delay to every operation that is executed. For example,

    java -jar emulator-1.0-all.jar /path/to/rom/filename -d 10

The command above will add a 10 ms delay to every opcode that is executed.
This is useful for very fast computers (note that it is difficult to find
information regarding opcode execution times, as such, I have not attempted
any fancy timing mechanisms to ensure that instructions are executed in a
set amount of time).

### Debug Mode

You can also ask the emulator to start in debug mode, where each
instruction is disassembled and displayed in the bottom left hand corner
of the screen on a semi-transparent overlay. To do this:

    java -jar emulator-1.0-all.jar /path/to/rom/filename -t

## Keys

There are two sets of keys that the emulator uses: debug keys and regular
keys.

### Regular Keys

The original Chip 8 had a keypad with the numbered keys 0 - 9 and A - F (16
keys in total). Without any modifications to the emulator, the keys are mapped
as follows:

| Chip 8 Key | Keyboard Key |
| :--------: | :----------: |
| `1`        | `4`          |
| `2`        | `5`          |
| `3`        | `6`          |
| `4`        | `7`          |
| `5`        | `R`          |
| `6`        | `T`          |
| `7`        | `Y`          |
| `8`        | `U`          |
| `9`        | `F`          |
| `0`        | `G`          |
| `A`        | `H`          |
| `B`        | `J`          |
| `C`        | `V`          |
| `D`        | `B`          |
| `E`        | `N`          |
| `F`        | `M`          |

### Debug Keys

Pressing a debug key at any time will cause the emulator to enter into a
different mode of operation. The debug keys are:

| Keyboard Key | Effect |
| :----------: | ------ |
| `ESC`        | Quits the emulator             |
| `X`          | Enters CPU trace mode          |
| `Z`          | Enters CPU trace and step mode |
| `N`          | Next key while in step mode    |
| `C`          | Exits CPU trace or step mode   |

## Modes

### Trace Mode

When started with the `-t` option, or when put into trace mode by pressing the
`X` key, the Chip 8 will provide an overlay on the screen that will show the
contents of all registers, including the program counter. Pressing `C` or 
`X` will exit trace mode.

Trace mode can also be accessed by clicking on `CPU`->`Trace Mode`.

### Step Mode

When put into step mode by pressing the `Z` key, the Chip 8 will provide an 
overlay on the screen exactly the same as trace mode, but will also pause 
after every instruction. By pressing the `N` key, the emulator will execute 
the next instruction and again pause. Pressing the `Z` key will leave the 
emulator in trace mode, but will cause it to continue executing instructions 
as normal. Pressing `C` or `Z` will cancel step and trace modes, and cause 
it to continue executing instructions as normal.

Step mode can also be accessed by clicking on `CPU`->`Step Mode`.


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

### Vera Mono Font

This project includes an unmodified "Vera Mono" font under the
following license agreement:

    Copyright (c) 2003 by Bitstream, Inc. All Rights Reserved. Bitstream
    Vera is a trademark of Bitstream, Inc.

    Permission is hereby granted, free of charge, to any person obtaining
    a copy of the fonts accompanying this license ("Fonts") and associated
    documentation files (the "Font Software"), to reproduce and distribute
    the Font Software, including without limitation the rights to use,
    copy, merge, publish, distribute, and/or sell copies of the Font
    Software, and to permit persons to whom the Font Software is furnished
    to do so, subject to the following conditions:

    The above copyright and trademark notices and this permission notice
    shall be included in all copies of one or more of the Font Software
    typefaces.

    The Font Software may be modified, altered, or added to, and in
    particular the designs of glyphs or characters in the Fonts may be
    modified and additional glyphs or characters may be added to the
    Fonts, only if the fonts are renamed to names not containing either
    the words "Bitstream" or the word "Vera".

    This License becomes null and void to the extent applicable to Fonts
    or Font Software that has been modified and is distributed under the
    "Bitstream Vera" names.

    The Font Software may be sold as part of a larger software package but
    no copy of one or more of the Font Software typefaces may be sold by
    itself.

    THE FONT SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT
    OF COPYRIGHT, PATENT, TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL
    BITSTREAM OR THE GNOME FOUNDATION BE LIABLE FOR ANY CLAIM, DAMAGES OR
    OTHER LIABILITY, INCLUDING ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL,
    OR CONSEQUENTIAL DAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR
    OTHERWISE, ARISING FROM, OUT OF THE USE OR INABILITY TO USE THE FONT
    SOFTWARE OR FROM OTHER DEALINGS IN THE FONT SOFTWARE.

    Except as contained in this notice, the names of Gnome, the Gnome
    Foundation, and Bitstream Inc., shall not be used in advertising or
    otherwise to promote the sale, use or other dealings in this Font
    Software without prior written authorization from the Gnome Foundation
    or Bitstream Inc., respectively. For further information, contact:
    fonts at gnome dot org.

