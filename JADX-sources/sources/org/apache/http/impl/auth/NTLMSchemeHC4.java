package org.apache.http.impl.auth;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.NTCredentials;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
/* loaded from: classes.dex */
public class NTLMSchemeHC4 extends AuthSchemeBaseHC4 {
    private String challenge;
    private final NTLMEngine engine;
    private State state;

    /* loaded from: classes.dex */
    enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        MSG_TYPE1_GENERATED,
        MSG_TYPE2_RECEVIED,
        MSG_TYPE3_GENERATED,
        FAILED;

        /* renamed from: values  reason: to resolve conflict with enum method */
        public static State[] valuesCustom() {
            State[] valuesCustom = values();
            int length = valuesCustom.length;
            State[] stateArr = new State[length];
            System.arraycopy(valuesCustom, 0, stateArr, 0, length);
            return stateArr;
        }
    }

    public NTLMSchemeHC4(NTLMEngine engine) {
        Args.notNull(engine, "NTLM engine");
        this.engine = engine;
        this.state = State.UNINITIATED;
        this.challenge = null;
    }

    public NTLMSchemeHC4() {
        this(new NTLMEngineImpl());
    }

    public String getSchemeName() {
        return "ntlm";
    }

    public String getParameter(String name) {
        return null;
    }

    public String getRealm() {
        return null;
    }

    public boolean isConnectionBased() {
        return true;
    }

    @Override // org.apache.http.impl.auth.AuthSchemeBaseHC4
    protected void parseChallenge(CharArrayBuffer buffer, int beginIndex, int endIndex) throws MalformedChallengeException {
        this.challenge = buffer.substringTrimmed(beginIndex, endIndex);
        if (this.challenge.length() == 0) {
            if (this.state == State.UNINITIATED) {
                this.state = State.CHALLENGE_RECEIVED;
            } else {
                this.state = State.FAILED;
            }
        } else if (this.state.compareTo(State.MSG_TYPE1_GENERATED) < 0) {
            this.state = State.FAILED;
            throw new MalformedChallengeException("Out of sequence NTLM response message");
        } else if (this.state == State.MSG_TYPE1_GENERATED) {
            this.state = State.MSG_TYPE2_RECEVIED;
        }
    }

    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        String response;
        try {
            NTCredentials ntcredentials = (NTCredentials) credentials;
            if (this.state == State.FAILED) {
                throw new AuthenticationException("NTLM authentication failed");
            }
            if (this.state == State.CHALLENGE_RECEIVED) {
                response = this.engine.generateType1Msg(ntcredentials.getDomain(), ntcredentials.getWorkstation());
                this.state = State.MSG_TYPE1_GENERATED;
            } else if (this.state == State.MSG_TYPE2_RECEVIED) {
                response = this.engine.generateType3Msg(ntcredentials.getUserName(), ntcredentials.getPassword(), ntcredentials.getDomain(), ntcredentials.getWorkstation(), this.challenge);
                this.state = State.MSG_TYPE3_GENERATED;
            } else {
                throw new AuthenticationException("Unexpected state: " + this.state);
            }
            CharArrayBuffer buffer = new CharArrayBuffer(32);
            if (isProxy()) {
                buffer.append(HttpHeaders.PROXY_AUTHORIZATION);
            } else {
                buffer.append("Authorization");
            }
            buffer.append(": NTLM ");
            buffer.append(response);
            return new BufferedHeader(buffer);
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException("Credentials cannot be used for NTLM authentication: " + credentials.getClass().getName());
        }
    }

    public boolean isComplete() {
        return this.state == State.MSG_TYPE3_GENERATED || this.state == State.FAILED;
    }
}
