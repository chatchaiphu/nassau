package org.jvirtanen.nassau.moldudp64;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64DefaultMessageStore.*;
import static org.jvirtanen.nassau.util.Strings.*;
import static org.jvirtanen.nio.ByteBuffers.*;

import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MoldUDP64DefaultMessageStoreTest {

    private static final int LENGTH = 2 * MIN_BLOCK_SIZE / 3;

    private MoldUDP64DefaultMessageStore store;

    private ByteBuffer buffer;

    @Before
    public void setUp() {
        store = new MoldUDP64DefaultMessageStore(MIN_BLOCK_SIZE);

        buffer = ByteBuffer.allocate(2 * MIN_BLOCK_SIZE);
    }

    @Test
    public void blockAllocation() throws Exception {
        List<String> messages = asList(repeat('A', LENGTH), repeat('B', LENGTH), repeat('C', LENGTH));

        for (String message : messages)
            store.put(wrap(message));

        int messageCount = store.get(buffer, 1, messages.size());
        assertEquals(messageCount, 2);

        buffer.flip();

        for (int i = 0; i < 2; i++) {
            String message = messages.get(i);

            assertEquals(message.length(), getUnsignedShort(buffer));
            assertEquals(message, get(buffer, message.length()));
        }

        assertFalse(buffer.hasRemaining());
    }

    @Test
    public void downstreamPacket() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        MoldUDP64DownstreamPacket packet = new MoldUDP64DownstreamPacket();

        for (String message : messages)
            packet.put(wrap(message));

        packet.payload().flip();

        store.put(packet);

        int messageCount = store.get(buffer, 1, messages.size());
        assertEquals(messageCount, messages.size());

        buffer.flip();

        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);

            assertEquals(message.length(), getUnsignedShort(buffer));
            assertEquals(message, get(buffer, message.length()));
        }

        assertFalse(buffer.hasRemaining());
    }

}