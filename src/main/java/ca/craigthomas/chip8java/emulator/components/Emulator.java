/*
 * Copyright (C) 2013-2025 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import ca.craigthomas.chip8java.emulator.common.IO;
import ca.craigthomas.chip8java.emulator.listeners.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.*;
import java.util.Timer;
import java.util.logging.Logger;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * The main Emulator class.
 */
public class Emulator
{
    // The number of buffers to use for bit blitting
    private static final int DEFAULT_NUMBER_OF_BUFFERS = 2;

    // The default title for the emulator window
    private static final String DEFAULT_TITLE = "Yet Another Super Chip 8 Emulator";

    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Emulator.class.getName());

    // The font file for the Chip 8
    private static final String FONT_FILE = "FONTS.chip8";

    // The Chip8 components
    private CentralProcessingUnit cpu;
    private Screen screen;
    private Keyboard keyboard;
    private Memory memory;

    // Emulator window and frame elements
    private JMenuBar menuBar;
    private Canvas canvas;
    private JFrame container;

    // The current state of the emulator and associated tasks
    private volatile EmulatorState state;
    private int cpuCycleTime;
    private Timer timer;
    private TimerTask timerTask;

    private long window;

    /**
     * Convenience constructor that sets the emulator running with a 1x
     * screen scale, a cycle time of 0, a null rom, and trace mode off.
     */
    public Emulator() {
        this(1, 0, null, false, "#000000", "#666666", "#BBBBBB", "#FFFFFF", false, false, false, false, false);
    }

    /**
     * Initializes an Emulator based on the parameters passed.
     *
     * @param scale the screen scaling to apply to the emulator window
     * @param maxTicks the maximum number of operations per second to execute
     * @param rom the rom filename to load
     * @param memSize4k whether to set memory size to 4k
     * @param color0 the bitplane 0 color
     * @param color1 the bitplane 1 color
     * @param color2 the bitplane 2 color
     * @param color3 the bitplane 3 color
     * @param shiftQuirks whether to enable shift quirks or not
     * @param logicQuirks whether to enable logic quirks or not
     * @param jumpQuirks whether to enable logic quirks or not
     * @param clipQuirks whether to enable clip quirks or not
     */
    public Emulator(
            int scale,
            int maxTicks,
            String rom,
            boolean memSize4k,
            String color0,
            String color1,
            String color2,
            String color3,
            boolean shiftQuirks,
            boolean logicQuirks,
            boolean jumpQuirks,
            boolean indexQuirks,
            boolean clipQuirks
    ) {
        if (color0.length() != 6) {
            System.out.println("color_0 parameter must be 6 characters long");
            System.exit(1);
        }

        if (color1.length() != 6) {
            System.out.println("color_1 parameter must be 6 characters long");
            System.exit(1);
        }

        if (color2.length() != 6) {
            System.out.println("color_2 parameter must be 6 characters long");
            System.exit(1);
        }

        if (color3.length() != 6) {
            System.out.println("color_3 parameter must be 6 characters long");
            System.exit(1);
        }

        PixelColor converted_color0 = null;
        try {
            converted_color0 = new PixelColor(Color.decode("#" + color0));
        } catch (NumberFormatException e) {
            System.out.println("color_0 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        PixelColor converted_color1 = null;
        try {
            converted_color1 = new PixelColor(Color.decode("#" + color1));
        } catch (NumberFormatException e) {
            System.out.println("color_1 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        PixelColor converted_color2 = null;
        try {
            converted_color2 = new PixelColor(Color.decode("#" + color2));
        } catch (NumberFormatException e) {
            System.out.println("color_2 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        PixelColor converted_color3 = null;
        try {
            converted_color3 = new PixelColor(Color.decode("#" + color3));
        } catch (NumberFormatException e) {
            System.out.println("color_3 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        keyboard = new Keyboard();
        memory = new Memory(memSize4k);
        screen = new Screen(scale, converted_color0, converted_color1, converted_color2, converted_color3);
        cpu = new CentralProcessingUnit(memory, keyboard, screen);
        cpu.setShiftQuirks(shiftQuirks);
        cpu.setLogicQuirks(logicQuirks);
        cpu.setJumpQuirks(jumpQuirks);
        cpu.setIndexQuirks(indexQuirks);
        cpu.setClipQuirks(clipQuirks);
        cpu.setMaxTicks(maxTicks);

        // Load the font file into memory
        InputStream fontFileStream = IO.openInputStreamFromResource(FONT_FILE);
        if (!memory.loadStreamIntoMemory(fontFileStream, 0)) {
            LOGGER.severe("Could not load font file");
            kill();
        }
        IO.closeStream(fontFileStream);

        // Attempt to load specified ROM file
        setPaused();
        if (rom != null) {
            InputStream romFileStream = IO.openInputStream(rom);
            if (!memory.loadStreamIntoMemory(romFileStream,
                    CentralProcessingUnit.PROGRAM_COUNTER_START)) {
                LOGGER.severe("Could not load ROM file [" + rom + "]");
            } else {
                setRunning();
            }
            IO.closeStream(romFileStream);
        }

        init();
    }

    /**
     * Starts the main emulator loop running. Fires at the rate of 60Hz,
     * will repaint the screen and listen for any debug key presses.
     */
    public void start() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                refreshScreen();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0L, 17L);

         while (!glfwWindowShouldClose(window)) {
//            if (state != EmulatorState.PAUSED) {
//                if (!cpu.isAwaitingKeypress()) {
//                    cpu.fetchIncrementExecute();
//                } else {
//                    cpu.decodeKeypressAndContinue();
//                }

                glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT);
                glfwSwapBuffers(window);
//                glReadPixels();
                glfwPollEvents();
//            }
        }

        kill();
        System.exit(0);
    }

    void init() {
        int scaleFactor = screen.getScale();
        int scaledWidth = Screen.WIDTH * scaleFactor;
        int scaledHeight = Screen.HEIGHT * scaleFactor;

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(scaledWidth, scaledHeight, DEFAULT_TITLE, NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFWErrorCallback errorCallback = new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                System.out.println("Error: " + error + ", " + description);
            }
        };
        glfwSetErrorCallback(errorCallback);

        // The main emulator keyboard callback function
        GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    keyboard.keyPressed(key);
                }

                if (action == GLFW_RELEASE) {
                    keyboard.keyReleased(key);
                }

                if (key == Keyboard.CHIP8_QUIT && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        };
        glfwSetKeyCallback(window, keyCallback);

        // Get the thread stack and push a new frame
//        try ( MemoryStack stack = stackPush() ) {
//            IntBuffer pWidth = stack.mallocInt(1); // int*
//            IntBuffer pHeight = stack.mallocInt(1); // int*
//            glfwGetWindowSize(window, pWidth, pHeight);
//            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
//        }

        glfwMakeContextCurrent(window);
//        glfwSwapInterval(1);
//        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

//        glViewport(0, 0, scaledWidth, scaledHeight);
    }

    /**
     * Returns the main frame for the emulator.
     *
     * @return the JFrame containing the emulator
     */
    public JFrame getEmulatorFrame() {
        return container;
    }

    public CentralProcessingUnit getCPU() {
        return this.cpu;
    }

    public Memory getMemory() {
        return memory;
    }

    /**
     * Initializes the JFrame that the emulator will use to draw onto. Will set up the menu system and
     * link the action listeners to the menu items. Returns the JFrame that contains all of the emulator
     * screen elements.
     */
    private void initEmulatorJFrame() {
        container = new JFrame(DEFAULT_TITLE);
        menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openFile = new JMenuItem("Open", KeyEvent.VK_O);
        openFile.addActionListener(new OpenROMFileActionListener(this));
        fileMenu.add(openFile);
        fileMenu.addSeparator();

        JMenuItem quitFile = new JMenuItem("Quit", KeyEvent.VK_Q);
        quitFile.addActionListener(new QuitActionListener(this));
        fileMenu.add(quitFile);
        menuBar.add(fileMenu);

        // CPU menu
        JMenu cpuMenu = new JMenu("CPU");
        cpuMenu.setMnemonic(KeyEvent.VK_C);

        // Reset CPU menu item
        JMenuItem resetCPU = new JMenuItem("Reset", KeyEvent.VK_R);
        resetCPU.addActionListener(new ResetMenuItemActionListener(cpu));
        cpuMenu.add(resetCPU);
        cpuMenu.addSeparator();
        menuBar.add(cpuMenu);

        attachCanvas();
    }

    /**
     * Generates the canvas of the appropriate size and attaches it to the
     * main jFrame for the emulator.
     */
    private void attachCanvas() {
        int scaleFactor = screen.getScale();
        int scaledWidth = Screen.WIDTH * scaleFactor;
        int scaledHeight = Screen.HEIGHT * scaleFactor;

        JPanel panel = (JPanel) container.getContentPane();
        panel.removeAll();
        panel.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        panel.setLayout(null);

        canvas = new Canvas();
        canvas.setBounds(0, 0, scaledWidth, scaledHeight);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        container.setJMenuBar(menuBar);
        container.pack();
        container.setResizable(false);
        container.setVisible(true);
        container.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.createBufferStrategy(DEFAULT_NUMBER_OF_BUFFERS);
        canvas.setFocusable(true);
        canvas.requestFocus();

//        canvas.addKeyListener(keyboard);
    }

    /**
     * Will redraw the contents of the screen to the emulator window.
     */
    private void refreshScreen() {
        glfwSwapBuffers(window);
    }

    /**
     * Kills the CPU, any emulator based timers, and disposes the main
     * emulator JFrame before calling System.exit.
     */
    public void kill() {
        cpu.kill();
        timer.cancel();
        timer.purge();
        timerTask.cancel();
//        dispose();
        state = EmulatorState.KILLED;
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    /**
     * Disposes of the main emulator JFrame.
     */
    public void dispose() {
        container.dispose();
    }

    /**
     * Sets the emulator running.
     */
    public void setRunning() {
        state = EmulatorState.RUNNING;
    }

    /**
     * Pauses the emulator.
     */
    public void setPaused() {
        state = EmulatorState.PAUSED;
    }
}
