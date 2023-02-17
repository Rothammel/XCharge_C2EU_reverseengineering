package org.apache.mina.filter.codec;

import java.net.SocketAddress;
import java.util.Queue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.NothingWrittenException;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class ProtocolCodecFilter extends IoFilterAdapter {
    private final ProtocolCodecFactory factory;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolCodecFilter.class);
    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
    private static final IoBuffer EMPTY_BUFFER = IoBuffer.wrap(new byte[0]);
    private static final AttributeKey ENCODER = new AttributeKey(ProtocolCodecFilter.class, "encoder");
    private static final AttributeKey DECODER = new AttributeKey(ProtocolCodecFilter.class, "decoder");
    private static final AttributeKey DECODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "decoderOut");
    private static final AttributeKey ENCODER_OUT = new AttributeKey(ProtocolCodecFilter.class, "encoderOut");

    public ProtocolCodecFilter(ProtocolCodecFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory");
        }
        this.factory = factory;
    }

    public ProtocolCodecFilter(final ProtocolEncoder encoder, final ProtocolDecoder decoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("encoder");
        }
        if (decoder == null) {
            throw new IllegalArgumentException("decoder");
        }
        this.factory = new ProtocolCodecFactory() { // from class: org.apache.mina.filter.codec.ProtocolCodecFilter.1
            @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
            public ProtocolEncoder getEncoder(IoSession session) {
                return encoder;
            }

            @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
            public ProtocolDecoder getDecoder(IoSession session) {
                return decoder;
            }
        };
    }

    public ProtocolCodecFilter(Class<? extends ProtocolEncoder> encoderClass, Class<? extends ProtocolDecoder> decoderClass) {
        if (encoderClass == null) {
            throw new IllegalArgumentException("encoderClass");
        }
        if (decoderClass == null) {
            throw new IllegalArgumentException("decoderClass");
        }
        if (!ProtocolEncoder.class.isAssignableFrom(encoderClass)) {
            throw new IllegalArgumentException("encoderClass: " + encoderClass.getName());
        }
        if (!ProtocolDecoder.class.isAssignableFrom(decoderClass)) {
            throw new IllegalArgumentException("decoderClass: " + decoderClass.getName());
        }
        try {
            encoderClass.getConstructor(EMPTY_PARAMS);
            try {
                decoderClass.getConstructor(EMPTY_PARAMS);
                try {
                    final ProtocolEncoder encoder = encoderClass.newInstance();
                    try {
                        final ProtocolDecoder decoder = decoderClass.newInstance();
                        this.factory = new ProtocolCodecFactory() { // from class: org.apache.mina.filter.codec.ProtocolCodecFilter.2
                            @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
                            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                                return encoder;
                            }

                            @Override // org.apache.mina.filter.codec.ProtocolCodecFactory
                            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                                return decoder;
                            }
                        };
                    } catch (Exception e) {
                        throw new IllegalArgumentException("decoderClass cannot be initialized");
                    }
                } catch (Exception e2) {
                    throw new IllegalArgumentException("encoderClass cannot be initialized");
                }
            } catch (NoSuchMethodException e3) {
                throw new IllegalArgumentException("decoderClass doesn't have a public default constructor.");
            }
        } catch (NoSuchMethodException e4) {
            throw new IllegalArgumentException("encoderClass doesn't have a public default constructor.");
        }
    }

    public ProtocolEncoder getEncoder(IoSession session) {
        return (ProtocolEncoder) session.getAttribute(ENCODER);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPreAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        if (parent.contains(this)) {
            throw new IllegalArgumentException("You can't add the same filter instance more than once.  Create another instance and add it.");
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void onPostRemove(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        disposeCodec(parent.getSession());
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        ProtocolDecoderException pde;
        LOGGER.debug("Processing a MESSAGE_RECEIVED for session {}", Long.valueOf(session.getId()));
        if (!(message instanceof IoBuffer)) {
            nextFilter.messageReceived(session, message);
            return;
        }
        IoBuffer in = (IoBuffer) message;
        ProtocolDecoder decoder = this.factory.getDecoder(session);
        ProtocolDecoderOutput decoderOut = getDecoderOut(session, nextFilter);
        while (in.hasRemaining()) {
            int oldPos = in.position();
            try {
                synchronized (session) {
                    decoder.decode(session, in, decoderOut);
                }
                decoderOut.flush(nextFilter, session);
            } catch (Exception e) {
                if (e instanceof ProtocolDecoderException) {
                    pde = (ProtocolDecoderException) e;
                } else {
                    pde = new ProtocolDecoderException(e);
                }
                if (pde.getHexdump() == null) {
                    int curPos = in.position();
                    in.position(oldPos);
                    pde.setHexdump(in.getHexDump());
                    in.position(curPos);
                }
                decoderOut.flush(nextFilter, session);
                nextFilter.exceptionCaught(session, pde);
                if (!(e instanceof RecoverableProtocolDecoderException) || in.position() == oldPos) {
                    return;
                }
            }
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (!(writeRequest instanceof EncodedWriteRequest)) {
            if (writeRequest instanceof MessageWriteRequest) {
                MessageWriteRequest wrappedRequest = (MessageWriteRequest) writeRequest;
                nextFilter.messageSent(session, wrappedRequest.getParentRequest());
                return;
            }
            nextFilter.messageSent(session, writeRequest);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object encodedMessage;
        Object message = writeRequest.getMessage();
        if ((message instanceof IoBuffer) || (message instanceof FileRegion)) {
            nextFilter.filterWrite(session, writeRequest);
            return;
        }
        ProtocolEncoder encoder = this.factory.getEncoder(session);
        ProtocolEncoderOutput encoderOut = getEncoderOut(session, nextFilter, writeRequest);
        if (encoder == null) {
            throw new ProtocolEncoderException("The encoder is null for the session " + session);
        }
        try {
            encoder.encode(session, message, encoderOut);
            Queue<Object> bufferQueue = ((AbstractProtocolEncoderOutput) encoderOut).getMessageQueue();
            while (!bufferQueue.isEmpty() && (encodedMessage = bufferQueue.poll()) != null) {
                if (!(encodedMessage instanceof IoBuffer) || ((IoBuffer) encodedMessage).hasRemaining()) {
                    SocketAddress destination = writeRequest.getDestination();
                    WriteRequest encodedWriteRequest = new EncodedWriteRequest(encodedMessage, null, destination);
                    nextFilter.filterWrite(session, encodedWriteRequest);
                }
            }
            nextFilter.filterWrite(session, new MessageWriteRequest(writeRequest));
        } catch (Exception e) {
            if (e instanceof ProtocolEncoderException) {
                ProtocolEncoderException pee = (ProtocolEncoderException) e;
                throw pee;
            } else {
                ProtocolEncoderException pee2 = new ProtocolEncoderException(e);
            }
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        ProtocolDecoder decoder = this.factory.getDecoder(session);
        ProtocolDecoderOutput decoderOut = getDecoderOut(session, nextFilter);
        try {
            try {
                decoder.finishDecode(session, decoderOut);
                disposeCodec(session);
                decoderOut.flush(nextFilter, session);
                nextFilter.sessionClosed(session);
            } catch (Exception e) {
                if (e instanceof ProtocolDecoderException) {
                    ProtocolDecoderException pde = (ProtocolDecoderException) e;
                    throw pde;
                } else {
                    ProtocolDecoderException pde2 = new ProtocolDecoderException(e);
                }
            }
        } catch (Throwable th) {
            disposeCodec(session);
            decoderOut.flush(nextFilter, session);
            throw th;
        }
    }

    /* loaded from: classes.dex */
    private static class EncodedWriteRequest extends DefaultWriteRequest {
        public EncodedWriteRequest(Object encodedMessage, WriteFuture future, SocketAddress destination) {
            super(encodedMessage, future, destination);
        }

        @Override // org.apache.mina.core.write.DefaultWriteRequest, org.apache.mina.core.write.WriteRequest
        public boolean isEncoded() {
            return true;
        }
    }

    /* loaded from: classes.dex */
    private static class MessageWriteRequest extends WriteRequestWrapper {
        public MessageWriteRequest(WriteRequest writeRequest) {
            super(writeRequest);
        }

        @Override // org.apache.mina.core.write.WriteRequestWrapper, org.apache.mina.core.write.WriteRequest
        public Object getMessage() {
            return ProtocolCodecFilter.EMPTY_BUFFER;
        }

        @Override // org.apache.mina.core.write.WriteRequestWrapper
        public String toString() {
            return "MessageWriteRequest, parent : " + super.toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ProtocolDecoderOutputImpl extends AbstractProtocolDecoderOutput {
        @Override // org.apache.mina.filter.codec.ProtocolDecoderOutput
        public void flush(IoFilter.NextFilter nextFilter, IoSession session) {
            Queue<Object> messageQueue = getMessageQueue();
            while (!messageQueue.isEmpty()) {
                nextFilter.messageReceived(session, messageQueue.poll());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class ProtocolEncoderOutputImpl extends AbstractProtocolEncoderOutput {
        private final SocketAddress destination;
        private final IoFilter.NextFilter nextFilter;
        private final IoSession session;

        public ProtocolEncoderOutputImpl(IoSession session, IoFilter.NextFilter nextFilter, WriteRequest writeRequest) {
            this.session = session;
            this.nextFilter = nextFilter;
            this.destination = writeRequest.getDestination();
        }

        @Override // org.apache.mina.filter.codec.ProtocolEncoderOutput
        public WriteFuture flush() {
            Object encodedMessage;
            Queue<Object> bufferQueue = getMessageQueue();
            WriteFuture future = null;
            while (!bufferQueue.isEmpty() && (encodedMessage = bufferQueue.poll()) != null) {
                if (!(encodedMessage instanceof IoBuffer) || ((IoBuffer) encodedMessage).hasRemaining()) {
                    future = new DefaultWriteFuture(this.session);
                    this.nextFilter.filterWrite(this.session, new EncodedWriteRequest(encodedMessage, future, this.destination));
                }
            }
            if (future == null) {
                WriteFuture future2 = DefaultWriteFuture.newNotWrittenFuture(this.session, new NothingWrittenException(AbstractIoSession.MESSAGE_SENT_REQUEST));
                return future2;
            }
            return future;
        }
    }

    private void disposeCodec(IoSession session) {
        disposeEncoder(session);
        disposeDecoder(session);
        disposeDecoderOut(session);
    }

    private void disposeEncoder(IoSession session) {
        ProtocolEncoder encoder = (ProtocolEncoder) session.removeAttribute(ENCODER);
        if (encoder != null) {
            try {
                encoder.dispose(session);
            } catch (Exception e) {
                LOGGER.warn("Failed to dispose: " + encoder.getClass().getName() + " (" + encoder + ')');
            }
        }
    }

    private void disposeDecoder(IoSession session) {
        ProtocolDecoder decoder = (ProtocolDecoder) session.removeAttribute(DECODER);
        if (decoder != null) {
            try {
                decoder.dispose(session);
            } catch (Exception e) {
                LOGGER.warn("Failed to dispose: " + decoder.getClass().getName() + " (" + decoder + ')');
            }
        }
    }

    private ProtocolDecoderOutput getDecoderOut(IoSession session, IoFilter.NextFilter nextFilter) {
        ProtocolDecoderOutput out = (ProtocolDecoderOutput) session.getAttribute(DECODER_OUT);
        if (out == null) {
            ProtocolDecoderOutput out2 = new ProtocolDecoderOutputImpl();
            session.setAttribute(DECODER_OUT, out2);
            return out2;
        }
        return out;
    }

    private ProtocolEncoderOutput getEncoderOut(IoSession session, IoFilter.NextFilter nextFilter, WriteRequest writeRequest) {
        ProtocolEncoderOutput out = (ProtocolEncoderOutput) session.getAttribute(ENCODER_OUT);
        if (out == null) {
            ProtocolEncoderOutput out2 = new ProtocolEncoderOutputImpl(session, nextFilter, writeRequest);
            session.setAttribute(ENCODER_OUT, out2);
            return out2;
        }
        return out;
    }

    private void disposeDecoderOut(IoSession session) {
        session.removeAttribute(DECODER_OUT);
    }
}