/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import com.chip8java.emulator.listeners.ResetMenuItemActionListener;
import com.chip8java.emulator.listeners.StepMenuItemListener;
import com.chip8java.emulator.listeners.TraceMenuItemListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.logging.Logger;

/**
 * The main Emulator class. Follows the Builder pattern for constructing an
 * Emulator object (call new Emulator.Builder().build())
 */
public class Emulator {

    // The number of buffers to use for bit blitting
    private static final int DEFAULT_NUMBER_OF_BUFFERS = 2;
    // The default title for the emulator window
    private static final String DEFAULT_TITLE = "Yet Another Chip8 Emulator";
    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Emulator.class.getName());
    // The font file for the Chip 8
    private static final String FONT_FILE = "FONTS.chip8";
    // The Chip8 CPU
    private CentralProcessingUnit mCPU;
    // The Chip8 Screen
    private Screen mScreen;
    // The Chip8 Keyboard
    private Keyboard mKeyboard;
    // The Chip8 Memory
    private Memory mMemory;
    // The mOverlayScreen background color
    private final Color sOverlayBackColor = new Color(0.0f, 0.27f, 0.0f, 1.0f);
    // The mOverlayScreen border color
    private final Color sOverlayBorderColor = new Color(0.0f, 0.70f, 0.0f, 1.0f);
    // The mOverlayScreen screen to print when trace is turned on
    BufferedImage mOverlayScreen;
    // The font to use for the mOverlayScreen
    private Font mOverlayFont;
    // The font file to use for the mOverlayScreen
    private static final String DEFAULT_FONT = "VeraMono.ttf";
    // The Canvas on which all the drawing will take place
    private Canvas mCanvas;
    // The main Emulator container
    private JFrame mContainer;
    // Whether the Emulator is in trace mode
    private boolean mTrace;
    // Whether the Emulator is in step mode
    private boolean mStep;
    // The Trace menu item
    JCheckBoxMenuItem mTraceMenuItem;
    // The Step menu item
    JCheckBoxMenuItem mStepMenuItem;

    /**
     * Builder for an Emulator object.
     */
    public static class Builder {
        // Sets whether the Emulator should start in trace mode
        private boolean mTrace;
        // Sets the initial screen scaling
        private int mScale;
        // The name of the Rom to load on startup
        private String mRom;

        public Builder() {
            mTrace = false;
            mScale = Runner.SCALE_DEFAULT;
            mRom = null;
        }

        /**
         * Sets the Emulator to start in trace mode.
         *
         * @return the Builder for the Emulator
         */
        public Builder setTrace() {
            mTrace = true;
            return this;
        }

        /**
         * Sets the initial scale for the Emulator window.
         *
         * @param scale the Scale factor to apply to the Emulator
         * @return the Builder for the Emulator
         */
        public Builder setScale(int scale) {
            mScale = scale;
            return this;
        }

        /**
         * Sets the Rom to load on startup.
         *
         * @param rom a filename that corresponds to a Chip8 Rom file
         * @return the Builder for the Emulator
         */
        public Builder setRom(String rom) {
            mRom = rom;
            return this;
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
    private Emulator (Builder builder) {
        ClassLoader classLoader = getClass().getClassLoader();
        mKeyboard = new Keyboard();
        mMemory = new Memory(Memory.MEMORY_4K);

        // Attempt to initialize the screen
        try {
            mScreen = new Screen(builder.mScale);
        } catch (Exception e) {
            LOGGER.severe("Could not initialize screen");
            LOGGER.severe(e.getMessage());
            System.exit(1);
        }

        // Initialize the CPU
        mCPU = new CentralProcessingUnit(mMemory, mKeyboard, mScreen);

        // Load the font file into memory
        InputStream fontFileStream = classLoader.getResourceAsStream(FONT_FILE);
        if (!mMemory.loadStreamIntoMemory(fontFileStream, 0)) {
            LOGGER.severe("Could not load font file");
            System.exit(1);
        }
        closeStream(fontFileStream);

        // Attempt to load specified ROM file
        if (builder.mRom != null) {
            InputStream romFileStream = openStream(builder.mRom);
            if (!mMemory.loadStreamIntoMemory(romFileStream,
                    CentralProcessingUnit.PROGRAM_COUNTER_START)) {
                LOGGER.severe("Could not load ROM file [" + builder.mRom + "]");
            }
            closeStream(romFileStream);
        } else {
            mCPU.setPaused(true);
        }

        // Initialize the screen, keyboard listeners, and mOverlayScreen information
        JFrame jFrame = initEmulatorJFrame(mScreen, mCPU);
        mCanvas.addKeyListener(mKeyboard);
        initializeOverlay();
        mTrace = builder.mTrace;
    }

    /**
     * Starts the main emulator loop running. Fires at the rate of 60Hz,
     * will repaint the screen and listen for any debug key presses.
     */
    public void start() {
        mCPU.start();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                refreshScreen();
                interpretDebugKey();
            }
        };
        timer.scheduleAtFixedRate(task, 0l, 33l);
    }

    public InputStream openStream(String filename) {
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

    public void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.severe("Error closing stream");
            LOGGER.severe(e.getMessage());
        }
    }

    private void initializeOverlay() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            InputStream fontFile = classLoader.getResourceAsStream(DEFAULT_FONT);
            mOverlayFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            mOverlayFont = mOverlayFont.deriveFont(11F);
            mOverlayScreen = new BufferedImage(342, 53, BufferedImage.TYPE_4BYTE_ABGR);
            fontFile.close();
        } catch (Exception e) {
            LOGGER.severe("Could not initialize mOverlayScreen");
            LOGGER.severe(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /**
     * Initializes the JFrame that the emulator will use to draw onto. Will set up the menu system and
     * link the action listeners to the menu items. Returns the JFrame that contains all of the emulator
     * screen elements.
     *
     * @param screen the Chip8 Screen to bind to the JFrame
     * @return the initialized JFrame
     */
    public JFrame initEmulatorJFrame(Screen screen, CentralProcessingUnit cpu) {
        mContainer = new JFrame(DEFAULT_TITLE);
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openFile = new JMenuItem("Open", KeyEvent.VK_O);
        fileMenu.add(openFile);

        JMenuItem closeFile = new JMenuItem("Close", KeyEvent.VK_C);
        fileMenu.add(closeFile);

        fileMenu.addSeparator();

        JMenuItem exitFile = new JMenuItem("Exit", KeyEvent.VK_X);
        fileMenu.add(exitFile);

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
        mTraceMenuItem = new JCheckBoxMenuItem("Trace Mode");
        mTraceMenuItem.setMnemonic(KeyEvent.VK_T);
        mTraceMenuItem.addItemListener(new TraceMenuItemListener(this));
        debugMenu.add(mTraceMenuItem);

        // Step menu item
        mStepMenuItem = new JCheckBoxMenuItem("Step Mode");
        mStepMenuItem.setMnemonic(KeyEvent.VK_S);
        mStepMenuItem.addItemListener(new StepMenuItemListener(this));
        debugMenu.add(mStepMenuItem);
        menuBar.add(debugMenu);

        int scaledWidth = screen.getWidth() * screen.getScale();
        int scaledHeight = screen.getHeight() * screen.getScale();

        JPanel panel = (JPanel) mContainer.getContentPane();
        panel.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        panel.setLayout(null);

        mCanvas = new Canvas();
        mCanvas.setBounds(0, 0, scaledWidth, scaledHeight);
        mCanvas.setIgnoreRepaint(true);

        panel.add(mCanvas);

        mContainer.setJMenuBar(menuBar);
        mContainer.pack();
        mContainer.setResizable(false);
        mContainer.setVisible(true);

        mCanvas.createBufferStrategy(DEFAULT_NUMBER_OF_BUFFERS);

        return mContainer;
    }

    /**
     * Will redraw the contents of the screen to the emulator window. Optionally, if
     * mTrace is True, will also draw the contents of the mOverlayScreen to the screen.
     */
    public void refreshScreen() {
        Graphics2D graphics = (Graphics2D) mCanvas.getBufferStrategy().getDrawGraphics();
        graphics.drawImage(mScreen.getBuffer(), null, 0, 0);
        if (mTrace) {
            updateOverlayInformation();
            Composite composite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.7f);
            graphics.setComposite(composite);
            graphics.drawImage(mOverlayScreen, null, 5, (mScreen.getHeight() * mScreen.getScale()) - 57);
        }
        graphics.dispose();
        mCanvas.getBufferStrategy().show();
    }

    /**
     * Write the current status of the CPU to the mOverlayScreen window.
     */
    public void updateOverlayInformation() {
        Graphics2D graphics = mOverlayScreen.createGraphics();

        graphics.setColor(sOverlayBorderColor);
        graphics.fillRect(0, 0, 342, 53);

        graphics.setColor(sOverlayBackColor);
        graphics.fillRect(1, 1, 340, 51);

        graphics.setColor(Color.white);
        graphics.setFont(mOverlayFont);

        String line1 = mCPU.cpuStatusLine1();
        String line2 = mCPU.cpuStatusLine2();
        String line3 = mCPU.cpuStatusLine3();

        graphics.drawString(line1, 5, 16);
        graphics.drawString(line2, 5, 31);
        graphics.drawString(line3, 5, 46);
        graphics.dispose();
    }

    /**
     * Sets whether or not the mOverlayScreen information for the CPU should be turned
     * off or on. If set to true, writes CPU information.
     *
     * @param trace
     *             Whether or not to print CPU information
     */
    public void setTrace(boolean trace) {
        mTrace = trace;
        mTraceMenuItem.setState(trace);
    }

    /**
     * Returns whether the Emulator is in trace mode.
     *
     * @return True if the Emulator is in trace mode, false otherwise
     */
    public boolean getTrace() {
        return mTrace;
    }

    /**
     * Sets or clears step mode.
     *
     * @param step
     */
    public void setStep(boolean step) {
        mStep = step;
        mStepMenuItem.setState(step);
        if (step) {
            setTrace(step);
        }
        mCPU.setPaused(step);
    }

    /**
     * Returns whether the Emulator is in step mode.
     *
     * @return True if the Emulator is in step mode, false otherwise
     */
    public boolean getStep() {
        return mStep;
    }

    /**
     * Destroys the Emulator container. This should be called before exiting.
     */
    public void dispose() {
        mContainer.dispose();
    }

    /**
     * Will check to see if a debugging key was pressed. Will return true if
     * one was pressed. Will also set the correct trace and step flags
     * depending on what debug key was pressed.
     */
    public void interpretDebugKey() {
        int key = mKeyboard.getDebugKey();
        switch (key) {
            case Keyboard.CHIP8_NORMAL:
                setTrace(false);
                setStep(false);
                break;

            case Keyboard.CHIP8_STEP:
                setStep(!mStep);
                break;

            case Keyboard.CHIP8_TRACE:
                setTrace(!mTrace);
                break;

            case Keyboard.CHIP8_NEXT:
                mCPU.fetchIncrementExecute();
                break;

            default:
                break;
        }
    }
}
