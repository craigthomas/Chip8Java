/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.runner;

import com.beust.jcommander.Parameter;
import com.chip8java.emulator.components.CentralProcessingUnit;

/**
 * A data class that stores the arguments that may be passed to the emulator.
 */
public class Arguments
{
    @Parameter(description="ROM file")
    public String romFile;

    @Parameter(names={"--scale"}, description="scale factor")
    public Integer scale = 7;

    @Parameter(names={"--delay"}, description="delay factor")
    public Integer delay = (int) CentralProcessingUnit.DEFAULT_CPU_CYCLE_TIME;

    @Parameter(names={"--trace"}, description="trace output")
    public Boolean trace = false;

    @Parameter(names={"--step"}, description="step through each instruction")
    public Boolean step = false;
}