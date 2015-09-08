# Yet Another Chip 8 Emulator

[![Build Status](https://travis-ci.org/craigthomas/Chip8Java.svg?branch=master)](https://travis-ci.org/craigthomas/Chip8Java) 
[![Coverage Status](http://coveralls.io/repos/craigthomas/Chip8Java/badge.svg?branch=master)](http://coveralls.io/r/craigthomas/Chip8Java?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/55ef3f691e87ad001900006a/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55ef3f691e87ad001900006a)

## What is it?

This project is a Chip 8 emulator written in Java. There are two other versions
of the emulator written in different languages:

* [Chip8Python](https://github.com/craigthomas/Chip8Python)
* [Chip8C](https://github.com/craigthomas/Chip8C)

The original goal of these projects was to learn how to code a simple emulator.


## License

This project makes use of an MIT style license. Please see the file called 
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

The compiled Jar file will be placed in the `build/libs` directory.


## Running

The command-line interface currently requires a single argument, which
is the full path to a Chip 8 ROM:

    java -jar build/libs/emulator-1.0.jar /path/to/rom/filename

This will start the emulator with the specified ROM. The emulator also
takes optional parameters. The `-s` switch will scale the size of the
window (the original size at 1x scale is 64 x 32):

    java -jar build/libs/emulator-1.0.jar /path/to/rom/filename -s 10

The command above will scale the window so that it is 10 times the normal
size. You may also wish to experiment with the `-d` switch, which instructs
the emulator to add a delay to every operation that is executed. For example,

    java -jar build/libs/emulator-1.0.jar /path/to/rom/filename -d 10

The command above will add a 10 ms delay to every opcode that is executed.
This is useful for very fast computers (note that it is difficult to find
information regarding opcode execution times, as such, I have not attempted
any fancy timing mechanisms to ensure that instructions are executed in a
set amount of time).

Finally, you can also ask the emulator to start in debug mode, where each
instruction is disassembled and displayed in the bottom left hand corner
of the screen on a semi-transparent overlay. To do this:

    java -jar build/libs/emulator-1.0.jar /path/to/rom/filename -t

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


## Current Status - March 21, 2015

### Operational

- CPU fully implemented and debugged.
- The emulator can load a ROM file and parse options. 
- The screen will be properly drawn.
- Keyboard input works.
- Delay timer works.
- Sound timer works.
- CPU menu system (reset, trace, and step).
- CPU runs in a separate thread.
- Screen redraws and keyboard polling run in a separate thread.
- File menu options (load, quit).
- CPU delay implemented to slow down execution to something reasonable.
- Emulator properly requests focus when initially drawn.
- Sound (via Midi playback).

## Third Party Licenses and Attributions

### Apache Commons CLI

This links to the Apache Commons CLI, which is licensed under the 
Apache License, Version 2.0. The license can be downloaded from
http://www.apache.org/licenses/LICENSE-2.0.html. The source code for this
software is available from http://commons.apache.org/cli

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

