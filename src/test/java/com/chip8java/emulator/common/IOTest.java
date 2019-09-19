/*
 * Copyright (C) 2019 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.common;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class IOTest
{
    private static final String GOOD_STREAM_FILE = "test_stream_file.bin";

    @Test
    public void testOpenInputStreamFromResourceReturnsNullOnBadFile() {
        InputStream result = IO.openInputStreamFromResource("this_file_does_not_exist.bin");
        assertNull(result);
    }

    @Test
    public void testOpenInputStreamFromResourceReturnsNullOnNull() {
        InputStream result = IO.openInputStreamFromResource(null);
        assertNull(result);
    }

    @Test
    public void testOpenInputStreamFromResourceWorksCorrectly() throws IOException {
        InputStream stream = IO.openInputStreamFromResource(GOOD_STREAM_FILE);
        byte[] result = IOUtils.toByteArray(stream);
        byte[] expected = {0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x74, 0x65, 0x73, 0x74};
        assertArrayEquals(expected, result);
    }

    @Test
    public void testOpenInputStreamReturnsNotNull() {
        File resourceFile = new File(getClass().getClassLoader().getResource(GOOD_STREAM_FILE).getFile());
        InputStream result = IO.openInputStream(resourceFile.getPath());
        assertNotNull(result);
    }

    @Test
    public void testOpenInputStreamReturnsNullWithBadFilename() {
        InputStream result = IO.openInputStream("this_file_does_not_exist.bin");
        assertNull(result);
    }

    @Test
    public void testCloseStreamWorksCorrectly() throws IOException {
        ByteArrayOutputStream stream = spy(ByteArrayOutputStream.class);
        boolean result = IO.closeStream(stream);
        Mockito.verify(stream).close();
        assertTrue(result);
    }

    @Test
    public void testCloseStreamOnNullStreamDoesNotThrowException() {
        boolean result = IO.closeStream(null);
        assertFalse(result);
    }

    @Test
    public void testCopyStreamFailsWhenSourceIsNull() {
        short[] target = new short[14];
        assertFalse(IO.copyStreamToShortArray(null, target, 0));
    }

    @Test
    public void testCopyStreamFailsWhenTargetIsNull() {
        File resourceFile = new File(getClass().getClassLoader().getResource(GOOD_STREAM_FILE).getFile());
        InputStream stream = IO.openInputStream(resourceFile.getPath());
        assertFalse(IO.copyStreamToShortArray(stream, null, 0));
    }

    @Test
    public void testCopyStreamFailsWhenSourceBiggerThanTarget() {
        File resourceFile = new File(getClass().getClassLoader().getResource(GOOD_STREAM_FILE).getFile());
        InputStream stream = IO.openInputStream(resourceFile.getPath());
        short[] target = new short[2];
        assertFalse(IO.copyStreamToShortArray(stream, target, 0));
    }

    @Test
    public void testCopyStreamWorksCorrectly() {
        short[] expected = {0x54, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x74, 0x65, 0x73, 0x74};
        File resourceFile = new File(getClass().getClassLoader().getResource(GOOD_STREAM_FILE).getFile());
        InputStream stream = IO.openInputStream(resourceFile.getPath());
        short[] target = new short[14];
        assertTrue(IO.copyStreamToShortArray(stream, target, 0));
        assertArrayEquals(expected, target);
    }
}