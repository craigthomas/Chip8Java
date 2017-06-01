/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import com.chip8java.emulator.listeners.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.Timer;
import java.util.logging.Logger;

/**
 * The main Emulator class. Follows the Builder pattern for constructing an
 * Emulator object (call new Emulator.Builder().build())
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

    // The Chip8 CPU
    private CentralProcessingUnit cpu;

    // The Chip8 Screen
    private Screen screen;

    // The Chip8 Keyboard
    private Keyboard keyboard;

    // The Chip8 Memory
    private Memory memory;

    // The overlay screen background color
    private final Color overlayBackColor = new Color(0.0f, 0.27f, 0.0f, 1.0f);

    // The overlay screen border color
    private final Color overlayBorderColor = new Color(0.0f, 0.70f, 0.0f, 1.0f);

    // The overlay screen to print when trace is turned on
    private BufferedImage overlayScreen;

    // The font to use for the overlay screen
    private Font overlayFont;

    // The font file to use for the overlay screen
    private static final String DEFAULT_FONT = "VeraMono.ttf";

    // The Canvas on which all the drawing will take place
    private Canvas canvas;

    // The main Emulator container
    private JFrame container;

    // Whether the Emulator is in trace mode
    private boolean isInTraceMode;

    // Whether the Emulator is in step mode
    private boolean isInStepMode;

    // The Trace menu item
    private JCheckBoxMenuItem traceMenuItem;

    // The Step menu item
    private JCheckBoxMenuItem mStepMenuItem;

    private JMenuBar menuBar;

    /**
     * Builder for an Emulator object.
     */
    public static class Builder
    {
        // Sets whether the Emulator should start in trace mode
        private boolean traceModeEnabled;

        // Sets the initial screen scaling
        private int initialScale;

        // The name of the Rom to load on startup
        private String initialRom;

        // The CPU cycle time
        private long initialCycleTime;

        public Builder() {
            traceModeEnabled = false;
            initialScale = Runner.SCALE_DEFAULT;
            initialRom = null;
            initialCycleTime = CentralProcessingUnit.DEFAULT_CPU_CYCLE_TIME;
        }

        /**
         * Sets the Emulator to start in trace mode.
         */
        void setTrace() {
            traceModeEnabled = true;
        }

        /**
         * Sets the initial scale for the Emulator window.
         *
         * @param scale the Scale factor to apply to the Emulator
         */
        void setScale(int scale) {
            initialScale = scale;
        }

        /**
         * Sets the Rom to load on startup.
         *
         * @param rom a filename that corresponds to a Chip8 Rom file
         */
        void setRom(String rom) {
            initialRom = rom;
        }

        /**
         * Sets the CPU cycle time (in milliseconds).
         *
         * @param cycleTime the new CPU cycle time (in milliseconds)
         */
        void setCycleTime(long cycleTime) {
            initialCycleTime = cycleTime;
        }

        /**
         * Builds the Emulator and returns an Emulator object.
         *
         * @return the newly instantiated Emulator
         */
        public Emulator build() {
            return new Emulator(this);
        }
    }

    /**
     * Initializes an Emulator based on the attributes set in the Builder.
     *
     * @param builder the Builder with the emulator attributes
     */
    private Emulator(Builder builder) {
        ClassLoader classLoader = getClass().getClassLoader();
        keyboard = new Keyboard();
        memory = new Memory(Memory.MEMORY_4K);

        // Attempt to initialize the screen
        try {
            screen = new Screen(builder.initialScale);
        } catch (Exception e) {
            LOGGER.severe("Could not initialize screen");
            LOGGER.severe(e.getMessage());
            System.exit(1);
        }

        // Initialize the CPU
        cpu = new CentralProcessingUnit(memory, keyboard, screen);
        cpu.setCPUCycleTime(builder.initialCycleTime);

        // Load the font file into memory
        InputStream fontFileStream = classLoader.getResourceAsStream(FONT_FILE);
        if (!memory.loadStreamIntoMemory(fontFileStream, 0)) {
            LOGGER.severe("Could not load font file");
            System.exit(1);
        }
        closeStream(fontFileStream);

        // Attempt to load specified ROM file
        if (builder.initialRom != null) {
            InputStream romFileStream = openStream(builder.initialRom);
            if (!memory.loadStreamIntoMemory(romFileStream,
                    CentralProcessingUnit.PROGRAM_COUNTER_START)) {
                LOGGER.severe("Could not load ROM file [" + builder.initialRom + "]");
            }
            closeStream(romFileStream);
        } else {
            cpu.setPaused(true);
        }

        // Initialize the screen, keyboard listeners, and overlayScreen information
        initEmulatorJFrame();
        initializeOverlay();
        isInTraceMode = builder.traceModeEnabled;
    }

    /**
     * Starts the main emulator loop running. Fires at the rate of 60Hz,
     * will repaint the screen and listen for any debug key presses.
     */
    void start() {
        cpu.start();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                refreshScreen();
                interpretDebugKey();
            }
        };
        timer.scheduleAtFixedRate(task, 0L, 33L);
    }

    /**
     * Attempts to open the specified filename as an InputStream. Will return null if there is
     * an error.
     *
     * @param filename The String containing the full path to the filename to open
     * @return An opened InputStream, or null if there is an error
     */
    private InputStream openStream(String filename) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(filename));
            return inputStream;
        } catch (FileNotFoundException e) {
            LOGGER.severe("Error opening file");
            LOGGER.severe(e.getMessage());
            return null;
        }
    }

    /**
     * Closes an open InputStream.
     *
     * @param stream the Input Stream to close
     */
    private void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.severe("Error closing stream");
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Initializes the overlay buffer.
     */
    private void initializeOverlay() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            InputStream fontFile = classLoader.getResourceAsStream(DEFAULT_FONT);
            overlayFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            overlayFont = overlayFont.deriveFont(11F);
            overlayScreen = new BufferedImage(342, 53, BufferedImage.TYPE_4BYTE_ABGR);
            fontFile.close();
        } catch (Exception e) {
            LOGGER.severe("Could not initialize overlayScreen");
            LOGGER.severe(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /**
     * Loads a file into memory and sets the CPU running the new ROM. Will open alert dialogs if there are errors
     * opening the file.
     */
    public void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter filter1 = new FileNameExtensionFilter("CHIP8 Rom File (*.chip8)", "chip8");
        fileChooser.setCurrentDirectory(new java.io.File("."));
        fileChooser.setDialogTitle("Open ROM file");
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(filter1);
        if (fileChooser.showOpenDialog(container) == JFileChooser.APPROVE_OPTION) {
            cpu.setPaused(true);
            InputStream inputStream = openStream(fileChooser.getSelectedFile().toString());
            if (inputStream == null) {
                JOptionPane.showMessageDialog(container, "Error opening file.", "File Open Problem",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!memory.loadStreamIntoMemory(inputStream, CentralProcessingUnit.PROGRAM_COUNTER_START)) {
                JOptionPane.showMessageDialog(container, "Error reading file.", "File Read Problem",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            cpu.reset();
            cpu.setPaused(false);
        }
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
        openFile.addActionListener(new OpenMenuItemActionListener(this));
        fileMenu.add(openFile);
        fileMenu.addSeparator();

        JMenuItem quitFile = new JMenuItem("Quit", KeyEvent.VK_Q);
        quitFile.addActionListener(new QuitMenuItemActionListener());
        fileMenu.add(quitFile);
        menuBar.add(fileMenu);

        // CPU menu
        JMenu debugMenu = new JMenu("CPU");
        debugMenu.setMnemonic(KeyEvent.VK_C);

        // Reset CPU menu item
        JMenuItem resetCPU = new JMenuItem("Reset", KeyEvent.VK_R);
        resetCPU.addActionListener(new ResetMenuItemActionListener(cpu));
        debugMenu.add(resetCPU);
        debugMenu.addSeparator();

        // Trace menu item
        traceMenuItem = new JCheckBoxMenuItem("Trace Mode");
        traceMenuItem.setMnemonic(KeyEvent.VK_T);
        traceMenuItem.addItemListener(new TraceMenuItemListener(this));
        debugMenu.add(traceMenuItem);

        // Step menu item
        mStepMenuItem = new JCheckBoxMenuItem("Step Mode");
        mStepMenuItem.setMnemonic(KeyEvent.VK_S);
        mStepMenuItem.addItemListener(new StepMenuItemListener(this));
        debugMenu.add(mStepMenuItem);
        menuBar.add(debugMenu);

        attachCanvas();
    }

    /**
     * Generates the canvas of the appropriate size and attaches it to the
     * main jFrame for the emulator.
     */
    private void attachCanvas() {
        int scaleFactor = screen.getScale();
        int scaledWidth = screen.getWidth() * scaleFactor;
        int scaledHeight = screen.getHeight() * scaleFactor;

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

        canvas.addKeyListener(keyboard);
    }

    /**
     * Will redraw the contents of the screen to the emulator window. Optionally, if
     * isInTraceMode is True, will also draw the contents of the overlayScreen to the screen.
     */
    private void refreshScreen() {

        // Check to see if the canvas should be regenerated
        if (screen.getStateChanged()) {
            attachCanvas();
            screen.clearStateChanged();
            screen.clearScreen();
        }

        Graphics2D graphics = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
        graphics.drawImage(screen.getBuffer(), null, 0, 0);

        // If in trace mode, then draw the trace overlay
        if (isInTraceMode) {
            updateOverlayInformation();
            Composite composite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.7f);
            graphics.setComposite(composite);
            graphics.drawImage(overlayScreen, null, 5, (screen.getHeight() * screen.getScale()) - 57);
        }
        graphics.dispose();
        canvas.getBufferStrategy().show();
    }

    /**
     * Write the current status of the CPU to the overlayScreen window.
     */
    private void updateOverlayInformation() {
        Graphics2D graphics = overlayScreen.createGraphics();

        graphics.setColor(overlayBorderColor);
        graphics.fillRect(0, 0, 342, 53);

        graphics.setColor(overlayBackColor);
        graphics.fillRect(1, 1, 340, 51);

        graphics.setColor(Color.white);
        graphics.setFont(overlayFont);

        String line1 = cpu.cpuStatusLine1();
        String line2 = cpu.cpuStatusLine2();
        String line3 = cpu.cpuStatusLine3();

        graphics.drawString(line1, 5, 16);
        graphics.drawString(line2, 5, 31);
        graphics.drawString(line3, 5, 46);
        graphics.dispose();
    }

    /**
     * Sets whether or not the overlayScreen information for the CPU should be turned
     * off or on. If set to true, writes CPU information.
     *
     * @param trace Whether or not to print CPU information
     */
    public void setTrace(boolean trace) {
        isInTraceMode = trace;
        traceMenuItem.setState(trace);
    }

    /**
     * Returns whether the Emulator is in trace mode.
     *
     * @return True if the Emulator is in trace mode, false otherwise
     */
    public boolean getTrace() {
        return isInTraceMode;
    }

    /**
     * Sets or clears step mode.
     *
     * @param step whether step mode should be enabled
     */
    public void setStep(boolean step) {
        isInStepMode = step;
        mStepMenuItem.setState(step);
        if (step) {
            setTrace(true);
        }
        cpu.setPaused(step);
    }

    /**
     * Returns whether the Emulator is in step mode.
     *
     * @return True if the Emulator is in step mode, false otherwise
     */
    public boolean getStep() {
        return isInStepMode;
    }

    /**
     * Destroys the Emulator container. This should be called before exiting.
     */
    public void dispose() {
        container.dispose();
    }

    /**
     * Will check to see if a debugging key was pressed. Will return true if
     * one was pressed. Will also set the correct trace and step flags
     * depending on what debug key was pressed.
     */
    private void interpretDebugKey() {
        int key = keyboard.getDebugKey();
        switch (key) {
            case Keyboard.CHIP8_NORMAL:
                setTrace(false);
                setStep(false);
                break;

            case Keyboard.CHIP8_STEP:
                setStep(!isInStepMode);
                break;

            case Keyboard.CHIP8_TRACE:
                setTrace(!isInTraceMode);
                break;

            case Keyboard.CHIP8_NEXT:
                cpu.fetchIncrementExecute();
                break;

            default:
                break;
        }
    }
}
