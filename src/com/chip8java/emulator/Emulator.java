/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import com.chip8java.emulator.listeners.PauseMenuItemListener;
import com.chip8java.emulator.listeners.ResetMenuItemActionListener;
import com.chip8java.emulator.listeners.StepMenuItemListener;
import com.chip8java.emulator.listeners.TraceMenuItemListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * The main Emulator class.
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
    // The overlay background color
    private Color overlayBackColor;
    // The overlay border color
    private Color overlayBorderColor;
    // The overlay screen to print when trace is turned on
    BufferedImage overlay;
    // The font to use for the overlay
    private Font overlayFont;
    // The font file to use for the overlay
    private static final String DEFAULT_FONT = "VeraMono.ttf";
    // The Canvas on which all the drawing will take place
    private Canvas mCanvas;
    private JFrame mContainer;
    private boolean mTrace;
    private boolean mStep;

    public static class Builder {
        private boolean mTrace;
        private boolean mStep;
        private int mScale;
        private String mRom;

        public Builder() {
            mTrace = false;
            mStep = false;
            mScale = Runner.SCALE_DEFAULT;
            mRom = null;
        }

        public Builder setTrace() {
            mTrace = true;
            return this;
        }

        public Builder setStep() {
            mStep = true;
            return this;
        }

        public Builder setScale(int scale) {
            mScale = scale;
            return this;
        }

        public Builder setRom(String rom) {
            mRom = rom;
            return this;
        }

        public Emulator build() {
            return new Emulator(this);
        }
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

        overlayBackColor = new Color(0.0f, 0.27f, 0.0f, 1.0f);
        overlayBorderColor = new Color(0.0f, 0.78f, 0.0f, 1.0f);
        try {
            InputStream fontFile = classLoader.getResourceAsStream(DEFAULT_FONT);
            overlayFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            overlayFont = overlayFont.deriveFont(11F);
            overlay = new BufferedImage(342, 53, BufferedImage.TYPE_4BYTE_ABGR);
            fontFile.close();
        } catch (Exception e) {
            LOGGER.severe("Could not initialize overlay");
            LOGGER.severe(e.getLocalizedMessage());
            System.exit(1);
        }
    }

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

        // Initialize the screen, keyboard listeners, and overlay information
        JFrame jFrame = initEmulatorJFrame(mScreen, mCPU);
        mCanvas.addKeyListener(mKeyboard);
        initializeOverlay();
        mTrace = builder.mTrace;
    }

    public void start() {
        mCPU.start();

        java.util.Timer timer = new java.util.Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                paint(mScreen);
            }
        };

        timer.scheduleAtFixedRate(task, 0l, 33l);
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

        // Pause menu item
        JCheckBoxMenuItem pauseCPU = new JCheckBoxMenuItem("Pause");
        pauseCPU.setMnemonic(KeyEvent.VK_P);
        pauseCPU.addItemListener(new PauseMenuItemListener(cpu));
        pauseCPU.setSelected(cpu.getPaused());
        debugMenu.add(pauseCPU);

        debugMenu.addSeparator();

        // Trace menu item
        JCheckBoxMenuItem traceCPU = new JCheckBoxMenuItem("Trace Mode");
        traceCPU.setMnemonic(KeyEvent.VK_T);
        traceCPU.addItemListener(new TraceMenuItemListener(this));
        debugMenu.add(traceCPU);

        // Step menu item
        JCheckBoxMenuItem stepCPU = new JCheckBoxMenuItem("Step Mode");
        stepCPU.setMnemonic(KeyEvent.VK_S);
        stepCPU.addItemListener(new StepMenuItemListener(this, traceCPU));
        debugMenu.add(stepCPU);

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

    public void paint(Screen screen) {
        Graphics2D graphics = (Graphics2D) mCanvas.getBufferStrategy()
                .getDrawGraphics();
        graphics.drawImage(screen.getBuffer(), null, 0, 0);
        if (mTrace) {
            updateOverlayInformation();
            Composite composite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.7f);
            graphics.setComposite(composite);
            graphics.drawImage(overlay, null, 5, (mScreen.getHeight() * mScreen.getScale()) - 57);
        }
        graphics.dispose();
        mCanvas.getBufferStrategy().show();
    }

    /**
     * Write the current status of the CPU to the overlay window.
     */
    public void updateOverlayInformation() {
        Graphics2D graphics = overlay.createGraphics();

        graphics.setColor(overlayBorderColor);
        graphics.fillRect(0, 0, 342, 53);

        graphics.setColor(overlayBackColor);
        graphics.fillRect(1, 1, 340, 51);

        graphics.setColor(Color.white);
        graphics.setFont(overlayFont);

        String line1 = mCPU.cpuStatusLine1();
        String line2 = mCPU.cpuStatusLine2();
        String line3 = mCPU.cpuStatusLine3();

        graphics.drawString(line1, 5, 16);
        graphics.drawString(line2, 5, 31);
        graphics.drawString(line3, 5, 46);
        graphics.dispose();
    }

    /**
     * Sets whether or not the overlay information for the CPU should be turned
     * off or on. If set to true, writes CPU information.
     *
     * @param trace
     *             Whether or not to print CPU information
     */
    public void setTrace(boolean trace) {
        mTrace = trace;
    }

    public boolean getTrace() {
        return mTrace;
    }

    public void setStep(boolean step) {
        mStep = step;
        mCPU.setPaused(mStep);
    }

    public boolean getStep() {
        return mStep;
    }

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

        if (key == Keyboard.CHIP8_NORMAL) {
            setTrace(false);
            setStep(false);
        }

        if (key == Keyboard.CHIP8_STEP) {
            setStep(!mStep);
        }

        if (key == Keyboard.CHIP8_TRACE) {
            setTrace(!mTrace);
        }

        if (key == Keyboard.CHIP8_NEXT) {
            mCPU.fetchIncrementExecute();
        }
    }
}
