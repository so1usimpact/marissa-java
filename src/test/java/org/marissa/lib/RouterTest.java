package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Timeout;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.junit.Before;
import org.junit.Test;
import org.marissa.lib.model.ChannelEvent;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.client.Message;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RouterTest {

    private Router r;
    private Timeout defaultTimeout;
    private Channel<ChannelEvent> dummy;

    @Before
    public void setUp() throws Exception {
        r = new Router(Pattern.quote("@Mars"));
        defaultTimeout = new Timeout(3000, TimeUnit.MILLISECONDS);
        dummy = Channels.newChannel(0);
    }

    @Test
    @Suspendable
    public void testOn() throws Exception {

        Channel<String> channel = Channels.newChannel(0);

        r.on("image\\s+me\\s+ninjas", (request, o) -> channel.send("testOn"));
        r.on("some other stuff", (request, c) -> fail("incorrect handler triggered"));

        r.triggerHandlersForMessageText("@Mars image me ninjas", new Response(Jid.valueOf("abc@abc.com"), dummy));

        String result;
        int recv = 0;

        while((result=channel.receive(defaultTimeout)) != null)
        {
            if (!result.equals("testOn")) {
                fail("incorrect message received from event");
            } else {
                recv++;
            }
        }

        assertTrue("correct message not received before timeout (or received more than once)", recv==1);

    }

    @Test
    @Suspendable
    public void testWhenContains() throws Exception {

        Channel<String> channel = Channels.newChannel(0);

        r.whenContains(".*turtles.*", (request, o) -> channel.send("done"));
        r.on("some other stuff", (request, c) -> fail("incorrect handler triggered"));

        r.triggerHandlersForMessageText("the world loves some turtles now and again", new Response(Jid.valueOf("abc@abc.com"), dummy));

        String result;
        int recv = 0;

        while((result=channel.receive(defaultTimeout)) != null)
        {
            if (!result.equals("done")) {
                fail("incorrect message received from event");
            } else {
                recv++;
            }
        }

        assertTrue("correct message not received before timeout (or received more than once)", recv==1);

    }

}