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
   6. [Quirks Modes](#quirks-modes)
       1. [Shift Quirks](#shift-quirks)
   7. [Memory Size](#memory-size)
   8. [Colors](#colors)
5. [Customization](#customization)
   1. [Keys](#keys)
   2. [Debug Keys](#debug-keys)
6. [ROM Compatibility](#rom-compatibility)
7. [Third Party Licenses and Attributions](#third-party-licenses-and-attributions)
   1. [JCommander](#jcommander)
   2. [Apache Commons IO](#apache-commons-io)
    
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

### Quirks Modes

Over time, various extensions to the Chip8 mnemonics were developed, which
resulted in an interesting fragmentation of the Chip8 language specification.
As discussed in Octo's [Mastering SuperChip](https://github.com/JohnEarnest/Octo/blob/gh-pages/docs/SuperChip.md)
documentation, one version of the SuperChip instruction set subtly changed
the meaning of a few instructions from their original Chip8 definitions.
This change went mostly unnoticed for many implementations of the Chip8
language. Problems arose when people started writing programs using the
updated language model - programs written for "pure" Chip8 ceased to
function correctly on emulators making use of the altered specification.

To address this issue, [Octo](https://github.com/JohnEarnest/Octo) implements
a number of _quirks_ modes so that all Chip8 software can run correctly,
regardless of which specification was used when developing the Chip8 program.
This same approach is used here, such that there are several `quirks` flags
that can be passed to the emulator at startup to force it to run with
adjustments to the language specification.

Additional quirks and their impacts on the running Chip8 interpreter are
examined in great depth at Chromatophore's [HP48-Superchip](https://github.com/Chromatophore/HP48-Superchip)
repository. Many thanks for this detailed explanation of various quirks
found in the wild!

#### Shift Quirks

The `--shift_quirks` flag will change the way that register shift operations work.
In the original language specification two registers were required: the
destination register `x`, and the source register `y`. The source register `y`
value was shifted one bit left or right, and stored in `x`. For example,
shift left was defined as:

    Vx = Vy << 1

However, with the updated language specification, the source and destination
register are assumed to always be the same, thus the `y` register is ignored and
instead the value is sourced from `x` as such:

    Vx = Vx << 1

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

| ROM Name                                                                                          |      Working       |     Flags     | 
|:--------------------------------------------------------------------------------------------------|:------------------:|:-------------:|
| [1D Cellular Automata](https://johnearnest.github.io/chip8Archive/play.html?p=1dcell)             | :heavy_check_mark: |               |
| [8CE Attourny - Disc 1](https://johnearnest.github.io/chip8Archive/play.html?p=8ceattourny_d1)    | :heavy_check_mark: |               |
| [8CE Attourny - Disc 2](https://johnearnest.github.io/chip8Archive/play.html?p=8ceattourny_d2)    |     :question:     |               |
| [8CE Attourny - Disc 3](https://johnearnest.github.io/chip8Archive/play.html?p=8ceattourny_d3)    |     :question:     |               |
| [Bad Kaiju Ju](https://johnearnest.github.io/chip8Archive/play.html?p=BadKaiJuJu)                 | :heavy_check_mark: |               |
| [Br8kout](https://johnearnest.github.io/chip8Archive/play.html?p=br8kout)                         | :heavy_check_mark: |               |
| [Carbon8](https://johnearnest.github.io/chip8Archive/play.html?p=carbon8)                         | :heavy_check_mark: |               |
| [Cave Explorer](https://johnearnest.github.io/chip8Archive/play.html?p=caveexplorer)              | :heavy_check_mark: |               |
| [Chipquarium](https://johnearnest.github.io/chip8Archive/play.html?p=chipquarium)                 | :heavy_check_mark: |               |
| [Danm8ku](https://johnearnest.github.io/chip8Archive/play.html?p=danm8ku)                         |        :x:         |               |
| [down8](https://johnearnest.github.io/chip8Archive/play.html?p=down8)                             | :heavy_check_mark: |               |
| [Falling Ghosts](https://veganjay.itch.io/falling-ghosts)                                         |     :question:     |               |
| [Flight Runner](https://johnearnest.github.io/chip8Archive/play.html?p=flightrunner)              |     :question:     |               |
| [Fuse](https://johnearnest.github.io/chip8Archive/play.html?p=fuse)                               |     :question:     |               |
| [Ghost Escape](https://johnearnest.github.io/chip8Archive/play.html?p=ghostEscape)                |     :question:     |               | 
| [Glitch Ghost](https://johnearnest.github.io/chip8Archive/play.html?p=glitchGhost)                |     :question:     |               |
| [Horse World Online](https://johnearnest.github.io/chip8Archive/play.html?p=horseWorldOnline)     |     :question:     |               |
| [Invisible Man](https://mremerson.itch.io/invisible-man)                                          |     :question:     | `clip_quirks` |
| [Knumber Knower](https://internet-janitor.itch.io/knumber-knower)                                 |     :question:     |               | 
| [Masquer8](https://johnearnest.github.io/chip8Archive/play.html?p=masquer8)                       |     :question:     |               |
| [Mastermind](https://johnearnest.github.io/chip8Archive/play.html?p=mastermind)                   |     :question:     |               |
| [Mini Lights Out](https://johnearnest.github.io/chip8Archive/play.html?p=mini-lights-out)         |     :question:     |               |
| [Octo: a Chip 8 Story](https://johnearnest.github.io/chip8Archive/play.html?p=octoachip8story)    |     :question:     |               |
| [Octogon Trail](https://tarsi.itch.io/octogon-trail)                                              |     :question:     |               |
| [Octojam 1 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam1title)           |     :question:     |               |
| [Octojam 2 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam2title)           |     :question:     |               |
| [Octojam 3 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam3title)           |     :question:     |               |
| [Octojam 4 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam4title)           |     :question:     |               |
| [Octojam 5 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam5title)           |     :question:     |               |
| [Octojam 6 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam6title)           |     :question:     |               |
| [Octojam 7 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam7title)           |     :question:     |               |
| [Octojam 8 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam8title)           |     :question:     |               |
| [Octojam 9 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam9title)           |     :question:     |               |
| [Octojam 10 Title](https://johnearnest.github.io/chip8Archive/play.html?p=octojam10title)         |     :question:     |               |
| [Octo Rancher](https://johnearnest.github.io/chip8Archive/play.html?p=octorancher)                |     :question:     |               |
| [Outlaw](https://johnearnest.github.io/chip8Archive/play.html?p=outlaw)                           |     :question:     |               |
| [Pet Dog](https://johnearnest.github.io/chip8Archive/play.html?p=petdog)                          |     :question:     |               | 
| [Piper](https://johnearnest.github.io/chip8Archive/play.html?p=piper)                             |     :question:     |               | 
| [Pumpkin "Dress" Up](https://johnearnest.github.io/chip8Archive/play.html?p=pumpkindressup)       |     :question:     |               |
| [RPS](https://johnearnest.github.io/chip8Archive/play.html?p=RPS)                                 |     :question:     |               |
| [Slippery Slope](https://johnearnest.github.io/chip8Archive/play.html?p=slipperyslope)            |     :question:     |               |
| [Snek](https://johnearnest.github.io/chip8Archive/play.html?p=snek)                               |     :question:     |               |
| [Space Jam](https://johnearnest.github.io/chip8Archive/play.html?p=spacejam)                      |     :question:     |               |
| [Spock Paper Scissors](https://johnearnest.github.io/chip8Archive/play.html?p=spockpaperscissors) |     :question:     |               |
| [Super Pong](https://johnearnest.github.io/chip8Archive/play.html?p=superpong)                    |     :question:     |               |
| [Tank!](https://johnearnest.github.io/chip8Archive/play.html?p=tank)                              |     :question:     |               |
| [TOMB STON TIPP](https://johnearnest.github.io/chip8Archive/play.html?p=tombstontipp)             |     :question:     |               |
| [WDL](https://johnearnest.github.io/chip8Archive/play.html?p=wdl)                                 |     :question:     |               |

### Super Chip ROMs

| ROM Name                                                                                     |   Working   | Flags | 
|:---------------------------------------------------------------------------------------------|:-----------:|:-----:|
| [Applejak](https://johnearnest.github.io/chip8Archive/play.html?p=applejak)                  | :question:  |       |
| [Bulb](https://johnearnest.github.io/chip8Archive/play.html?p=bulb)                          | :question:  |       |
| [Black Rainbow](https://johnearnest.github.io/chip8Archive/play.html?p=blackrainbow)         | :question:  |       |
| [Chipcross](https://tobiasvl.itch.io/chipcross)                                              | :question:  |       |
| [Chipolarium](https://tobiasvl.itch.io/chipolarium)                                          | :question:  |       |
| [Collision Course](https://ninjaweedle.itch.io/collision-course)                             | :question:  |       |
| [Dodge](https://johnearnest.github.io/chip8Archive/play.html?p=dodge)                        | :question:  |       |
| [DVN8](https://johnearnest.github.io/chip8Archive/play.html?p=DVN8)                          | :question:  |       |
| [Eaty the Alien](https://johnearnest.github.io/chip8Archive/play.html?p=eaty)                | :question:  |       |
| [Grad School Simulator 2014](https://johnearnest.github.io/chip8Archive/play.html?p=gradsim) | :question:  |       |
| [Horsey Jump](https://johnearnest.github.io/chip8Archive/play.html?p=horseyJump)             | :question:  |       |
| [Knight](https://johnearnest.github.io/chip8Archive/play.html?p=knight)                      |     :x:     |       |
| [Mondri8](https://johnearnest.github.io/chip8Archive/play.html?p=mondri8)                    | :question:  |       |
| [Octopeg](https://johnearnest.github.io/chip8Archive/play.html?p=octopeg)                    | :question:  |       |
| [Octovore](https://johnearnest.github.io/chip8Archive/play.html?p=octovore)                  | :question:  |       |
| [Rocto](https://johnearnest.github.io/chip8Archive/play.html?p=rockto)                       | :question:  |       |
| [Sens8tion](https://johnearnest.github.io/chip8Archive/play.html?p=sens8tion)                | :question:  |       |
| [Snake](https://johnearnest.github.io/chip8Archive/play.html?p=snake)                        | :question:  |       |
| [Squad](https://johnearnest.github.io/chip8Archive/play.html?p=squad)                        | :question:  |       |
| [Sub-Terr8nia](https://johnearnest.github.io/chip8Archive/play.html?p=sub8)                  | :question:  |       |
| [Super Octogon](https://johnearnest.github.io/chip8Archive/play.html?p=octogon)              | :question:  |       |
| [Super Square](https://johnearnest.github.io/chip8Archive/play.html?p=supersquare)           | :question:  |       |
| [The Binding of COSMAC](https://johnearnest.github.io/chip8Archive/play.html?p=binding)      | :question:  |       |
| [Turnover '77](https://johnearnest.github.io/chip8Archive/play.html?p=turnover77)            | :question:  |       |

### XO Chip ROMs

| ROM Name                                                                                              |   Working   | Flags | 
|:------------------------------------------------------------------------------------------------------|:-----------:|:-----:|
| [An Evening to Die For](https://johnearnest.github.io/chip8Archive/play.html?p=anEveningToDieFor)     | :question:  |       |
| [Business Is Contagious](https://johnearnest.github.io/chip8Archive/play.html?p=businessiscontagious) | :question:  |       |
| [Chicken Scratch](https://johnearnest.github.io/chip8Archive/play.html?p=chickenScratch)              | :question:  |       |
| [Civiliz8n](https://johnearnest.github.io/chip8Archive/play.html?p=civiliz8n)                         | :question:  |       |
| [Flutter By](https://johnearnest.github.io/chip8Archive/play.html?p=flutterby)                        | :question:  |       |
| [Into The Garlicscape](https://johnearnest.github.io/chip8Archive/play.html?p=garlicscape)            | :question:  |       |
| [jub8 Song 1](https://johnearnest.github.io/chip8Archive/play.html?p=jub8-1)                          | :question:  |       |
| [jub8 Song 2](https://johnearnest.github.io/chip8Archive/play.html?p=jub8-2)                          | :question:  |       |
| [Kesha Was Biird](https://johnearnest.github.io/chip8Archive/play.html?p=keshaWasBiird)               | :question:  |       |
| [Kesha Was Niinja](https://johnearnest.github.io/chip8Archive/play.html?p=keshaWasNiinja)             | :question:  |       |
| [Octo paint](https://johnearnest.github.io/chip8Archive/play.html?p=octopaint)                        | :question:  |       |
| [Octo Party Mix!](https://johnearnest.github.io/chip8Archive/play.html?p=OctoPartyMix)                | :question:  |       |
| [Octoma](https://johnearnest.github.io/chip8Archive/play.html?p=octoma)                               | :question:  |       |
| [Red October V](https://johnearnest.github.io/chip8Archive/play.html?p=redOctober)                    | :question:  |       |
| [Skyward](https://johnearnest.github.io/chip8Archive/play.html?p=skyward)                             | :question:  |       |
| [Spock Paper Scissors](https://johnearnest.github.io/chip8Archive/play.html?p=spockpaperscissors)     | :question:  |       |
| [T8NKS](https://johnearnest.github.io/chip8Archive/play.html?p=t8nks)                                 | :question:  |       |
| [Tapeworm](https://tarsi.itch.io/tapeworm)                                                            | :question:  |       |
| [Truck Simul8or](https://johnearnest.github.io/chip8Archive/play.html?p=trucksimul8or)                | :question:  |       |
| [SK8 H8 1988](https://johnearnest.github.io/chip8Archive/play.html?p=sk8)                             | :question:  |       |
| [Super NeatBoy](https://johnearnest.github.io/chip8Archive/play.html?p=superneatboy)                  | :question:  |       |
| [Wonky Pong](https://johnearnest.github.io/chip8Archive/play.html?p=wonkypong)                        | :question:  |       |

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