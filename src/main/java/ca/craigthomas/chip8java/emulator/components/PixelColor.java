package ca.craigthomas.chip8java.emulator.components;

import java.awt.*;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

public class PixelColor
{
    private final int red;
    private final int green;
    private final int blue;

    public PixelColor(Color color) {
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void setGLPixelColor() {
        glColor3i(red, green, blue);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PixelColor that = (PixelColor) o;
        return red == that.red && green == that.green && blue == that.blue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }
}
