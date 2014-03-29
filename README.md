# Yet Another Chip 8 Emulator

[![Build Status](https://travis-ci.org/craigthomas/Chip8Java.svg?branch=master)](https://travis-ci.org/craigthomas/Chip8Java)

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
* [Apache Maven](http://maven.apache.org/download.cgi) 3.0.3 or later

To build the project, switch to the root of the source directory, and
type:

    mvn clean package

The compiled Jar file will be placed in the `target` sub-directory.


## Running

The command-line interface currently requires a single argument, which
is the full path to a Chip 8 ROM:

    java -jar target/emulator-ALPHA.jar /path/to/rom/filename

This will start the emulator with the specified ROM. The emulator also
takes optional parameters. The `-s` switch will scale the size of the
window (the original size at 1x scale is 64 x 32):

    java -jar target/emulator-ALPHA.jar /path/to/rom/filename -s 10

The command above will scale the window so that it is 10 times the normal
size. You may also wish to experiment with the `-d` switch, which instructs
the emulator to add a delay to every operation that is executed. For example,

    java -jar target/emulator-ALPHA.jar /path/to/rom/filename -d 10

The command above will add a 10 ms delay to every opcode that is executed.
This is useful for very fast computers (note that it is difficult to find
information regarding opcode execution times, as such, I have not attempted
any fancy timing mechanisms to ensure that instructions are executed in a
set amount of time).

Finally, you can also ask the emulator to start in debug mode, where each
instruction is disassembled and displayed in the bottom left hand corner
of the screen on a semi-transparent overlay. To do this:

    java -jar target/emulator-ALPHA.jar /path/to/rom/filename -t


## Current Status - March 29, 2014

### Operational

- CPU fully implemented and debugged.
- The emulator can load a ROM file and parse options. 
- The screen will be properly drawn.
- Keyboard input works.
- Delay timer works.
- Sound timer works.

### Yet to be Implemented

- Menu system.
- Sound.

### Known Bugs

- When the emulator screen is first drawn does not grab keyboard focus.
- Sprites are not being drawn properly.

## Third Party Licenses and Attributions

### Apache Commons CLI

This links to the Apache Commons CLI, which is licensed under the 
Apache License, Version 2.0. The license can be downloaded from
http://www.apache.org/licenses/LICENSE-2.0.html. The source code for this
software is available from http://commons.apache.org/cli

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

