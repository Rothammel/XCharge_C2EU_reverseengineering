package com.xcharge.charger.protocol.monitor.util;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;

public class Md5 {
    private static final byte[] PADDING;
    private static final int S11 = 7;
    private static final int S12 = 12;
    private static final int S13 = 17;
    private static final int S14 = 22;
    private static final int S21 = 5;
    private static final int S22 = 9;
    private static final int S23 = 14;
    private static final int S24 = 20;
    private static final int S31 = 4;
    private static final int S32 = 11;
    private static final int S33 = 16;
    private static final int S34 = 23;
    private static final int S41 = 6;
    private static final int S42 = 10;
    private static final int S43 = 15;
    private static final int S44 = 21;
    private byte[] buffer = new byte[64];
    private long[] count = new long[2];
    private byte[] digest = new byte[16];
    private String digestHexStr;
    private long[] state = new long[4];

    static {
        byte[] bArr = new byte[64];
        bArr[0] = AnyoMessage.CMD_RESET_CHARGE;
        PADDING = bArr;
    }

    private Md5() {
        md5Init();
    }

    public static Md5 getInstance() {
        return new Md5();
    }

    public static long b2iu(byte b) {
        if (b < 0) {
            b &= 255;
        }
        return (long) b;
    }

    public static String byteHEX(byte ib) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        return new String(new char[]{Digit[(ib >>> 4) & 15], Digit[ib & HeartBeatRequest.PORT_STATUS_FAULT]});
    }

    public String md5_32(String inbuf) {
        md5Update(inbuf.getBytes(), inbuf.length());
        md5Final();
        this.digestHexStr = "";
        for (int i = 0; i < 16; i++) {
            this.digestHexStr = String.valueOf(this.digestHexStr) + byteHEX(this.digest[i]);
        }
        return this.digestHexStr;
    }

    public String md5_16(String inbuf) {
        return md5_32(inbuf).substring(8, 24);
    }

    public String md5_8(String inbuf) {
        return md5_32(inbuf).substring(0, 8);
    }

    private void md5Init() {
        this.count[0] = 0;
        this.count[1] = 0;
        this.state[0] = 1732584193;
        this.state[1] = 4023233417L;
        this.state[2] = 2562383102L;
        this.state[3] = 271733878;
    }

    /* renamed from: F */
    private long m27F(long x, long y, long z) {
        return (x & y) | ((-1 ^ x) & z);
    }

    /* renamed from: G */
    private long m29G(long x, long y, long z) {
        return (x & z) | ((-1 ^ z) & y);
    }

    /* renamed from: H */
    private long m31H(long x, long y, long z) {
        return (x ^ y) ^ z;
    }

    /* renamed from: I */
    private long m33I(long x, long y, long z) {
        return ((-1 ^ z) | x) ^ y;
    }

    /* renamed from: FF */
    private long m28FF(long a, long b, long c, long d, long x, long s, long ac) {
        long a2 = a + m27F(b, c, d) + x + ac;
        return ((long) ((((int) a2) << ((int) s)) | (((int) a2) >>> ((int) (32 - s))))) + b;
    }

    /* renamed from: GG */
    private long m30GG(long a, long b, long c, long d, long x, long s, long ac) {
        long a2 = a + m29G(b, c, d) + x + ac;
        return ((long) ((((int) a2) << ((int) s)) | (((int) a2) >>> ((int) (32 - s))))) + b;
    }

    /* renamed from: HH */
    private long m32HH(long a, long b, long c, long d, long x, long s, long ac) {
        long a2 = a + m31H(b, c, d) + x + ac;
        return ((long) ((((int) a2) << ((int) s)) | (((int) a2) >>> ((int) (32 - s))))) + b;
    }

    /* renamed from: II */
    private long m34II(long a, long b, long c, long d, long x, long s, long ac) {
        long a2 = a + m33I(b, c, d) + x + ac;
        return ((long) ((((int) a2) << ((int) s)) | (((int) a2) >>> ((int) (32 - s))))) + b;
    }

    private void md5Update(byte[] inbuf, int inputLen) {
        int i;
        byte[] block = new byte[64];
        int index = ((int) (this.count[0] >>> 3)) & 63;
        long[] jArr = this.count;
        long j = jArr[0] + ((long) (inputLen << 3));
        jArr[0] = j;
        if (j < ((long) (inputLen << 3))) {
            long[] jArr2 = this.count;
            jArr2[1] = jArr2[1] + 1;
        }
        long[] jArr3 = this.count;
        jArr3[1] = jArr3[1] + ((long) (inputLen >>> 29));
        int partLen = 64 - index;
        if (inputLen >= partLen) {
            md5Memcpy(this.buffer, inbuf, index, 0, partLen);
            md5Transform(this.buffer);
            i = partLen;
            while (i + 63 < inputLen) {
                md5Memcpy(block, inbuf, 0, i, 64);
                md5Transform(block);
                i += 64;
            }
            index = 0;
        } else {
            i = 0;
        }
        md5Memcpy(this.buffer, inbuf, index, i, inputLen - i);
    }

    private void md5Final() {
        byte[] bits = new byte[8];
        encode(bits, this.count, 8);
        int index = ((int) (this.count[0] >>> 3)) & 63;
        md5Update(PADDING, index < 56 ? 56 - index : 120 - index);
        md5Update(bits, 8);
        encode(this.digest, this.state, 16);
    }

    private void md5Memcpy(byte[] output, byte[] input, int outpos, int inpos, int len) {
        for (int i = 0; i < len; i++) {
            output[outpos + i] = input[inpos + i];
        }
    }

    private void md5Transform(byte[] block) {
        long a = this.state[0];
        long b = this.state[1];
        long c = this.state[2];
        long d = this.state[3];
        long[] x = new long[16];
        decode(x, block, 64);
        long a2 = m28FF(a, b, c, d, x[0], 7, 3614090360L);
        long d2 = m28FF(d, a2, b, c, x[1], 12, 3905402710L);
        long c2 = m28FF(c, d2, a2, b, x[2], 17, 606105819);
        long b2 = m28FF(b, c2, d2, a2, x[3], 22, 3250441966L);
        long a3 = m28FF(a2, b2, c2, d2, x[4], 7, 4118548399L);
        long d3 = m28FF(d2, a3, b2, c2, x[5], 12, 1200080426);
        long c3 = m28FF(c2, d3, a3, b2, x[6], 17, 2821735955L);
        long b3 = m28FF(b2, c3, d3, a3, x[7], 22, 4249261313L);
        long a4 = m28FF(a3, b3, c3, d3, x[8], 7, 1770035416);
        long d4 = m28FF(d3, a4, b3, c3, x[9], 12, 2336552879L);
        long c4 = m28FF(c3, d4, a4, b3, x[10], 17, 4294925233L);
        long b4 = m28FF(b3, c4, d4, a4, x[11], 22, 2304563134L);
        long a5 = m28FF(a4, b4, c4, d4, x[12], 7, 1804603682);
        long d5 = m28FF(d4, a5, b4, c4, x[13], 12, 4254626195L);
        long c5 = m28FF(c4, d5, a5, b4, x[14], 17, 2792965006L);
        long b5 = m28FF(b4, c5, d5, a5, x[15], 22, 1236535329);
        long a6 = m30GG(a5, b5, c5, d5, x[1], 5, 4129170786L);
        long d6 = m30GG(d5, a6, b5, c5, x[6], 9, 3225465664L);
        long c6 = m30GG(c5, d6, a6, b5, x[11], 14, 643717713);
        long b6 = m30GG(b5, c6, d6, a6, x[0], 20, 3921069994L);
        long a7 = m30GG(a6, b6, c6, d6, x[5], 5, 3593408605L);
        long d7 = m30GG(d6, a7, b6, c6, x[10], 9, 38016083);
        long c7 = m30GG(c6, d7, a7, b6, x[15], 14, 3634488961L);
        long b7 = m30GG(b6, c7, d7, a7, x[4], 20, 3889429448L);
        long a8 = m30GG(a7, b7, c7, d7, x[9], 5, 568446438);
        long d8 = m30GG(d7, a8, b7, c7, x[14], 9, 3275163606L);
        long c8 = m30GG(c7, d8, a8, b7, x[3], 14, 4107603335L);
        long b8 = m30GG(b7, c8, d8, a8, x[8], 20, 1163531501);
        long a9 = m30GG(a8, b8, c8, d8, x[13], 5, 2850285829L);
        long d9 = m30GG(d8, a9, b8, c8, x[2], 9, 4243563512L);
        long c9 = m30GG(c8, d9, a9, b8, x[7], 14, 1735328473);
        long b9 = m30GG(b8, c9, d9, a9, x[12], 20, 2368359562L);
        long a10 = m32HH(a9, b9, c9, d9, x[5], 4, 4294588738L);
        long d10 = m32HH(d9, a10, b9, c9, x[8], 11, 2272392833L);
        long c10 = m32HH(c9, d10, a10, b9, x[11], 16, 1839030562);
        long b10 = m32HH(b9, c10, d10, a10, x[14], 23, 4259657740L);
        long a11 = m32HH(a10, b10, c10, d10, x[1], 4, 2763975236L);
        long d11 = m32HH(d10, a11, b10, c10, x[4], 11, 1272893353);
        long c11 = m32HH(c10, d11, a11, b10, x[7], 16, 4139469664L);
        long b11 = m32HH(b10, c11, d11, a11, x[10], 23, 3200236656L);
        long a12 = m32HH(a11, b11, c11, d11, x[13], 4, 681279174);
        long d12 = m32HH(d11, a12, b11, c11, x[0], 11, 3936430074L);
        long c12 = m32HH(c11, d12, a12, b11, x[3], 16, 3572445317L);
        long b12 = m32HH(b11, c12, d12, a12, x[6], 23, 76029189);
        long a13 = m32HH(a12, b12, c12, d12, x[9], 4, 3654602809L);
        long d13 = m32HH(d12, a13, b12, c12, x[12], 11, 3873151461L);
        long c13 = m32HH(c12, d13, a13, b12, x[15], 16, 530742520);
        long b13 = m32HH(b12, c13, d13, a13, x[2], 23, 3299628645L);
        long a14 = m34II(a13, b13, c13, d13, x[0], 6, 4096336452L);
        long d14 = m34II(d13, a14, b13, c13, x[7], 10, 1126891415);
        long c14 = m34II(c13, d14, a14, b13, x[14], 15, 2878612391L);
        long b14 = m34II(b13, c14, d14, a14, x[5], 21, 4237533241L);
        long a15 = m34II(a14, b14, c14, d14, x[12], 6, 1700485571);
        long d15 = m34II(d14, a15, b14, c14, x[3], 10, 2399980690L);
        long c15 = m34II(c14, d15, a15, b14, x[10], 15, 4293915773L);
        long b15 = m34II(b14, c15, d15, a15, x[1], 21, 2240044497L);
        long a16 = m34II(a15, b15, c15, d15, x[8], 6, 1873313359);
        long d16 = m34II(d15, a16, b15, c15, x[15], 10, 4264355552L);
        long c16 = m34II(c15, d16, a16, b15, x[6], 15, 2734768916L);
        long b16 = m34II(b15, c16, d16, a16, x[13], 21, 1309151649);
        long a17 = m34II(a16, b16, c16, d16, x[4], 6, 4149444226L);
        long d17 = m34II(d16, a17, b16, c16, x[11], 10, 3174756917L);
        long c17 = m34II(c16, d17, a17, b16, x[2], 15, 718787259);
        long b17 = m34II(b16, c17, d17, a17, x[9], 21, 3951481745L);
        long[] jArr = this.state;
        jArr[0] = jArr[0] + a17;
        long[] jArr2 = this.state;
        jArr2[1] = jArr2[1] + b17;
        long[] jArr3 = this.state;
        jArr3[2] = jArr3[2] + c17;
        long[] jArr4 = this.state;
        jArr4[3] = jArr4[3] + d17;
    }

    private void encode(byte[] output, long[] input, int len) {
        int i = 0;
        for (int j = 0; j < len; j += 4) {
            output[j] = (byte) ((int) (input[i] & 255));
            output[j + 1] = (byte) ((int) ((input[i] >>> 8) & 255));
            output[j + 2] = (byte) ((int) ((input[i] >>> 16) & 255));
            output[j + 3] = (byte) ((int) ((input[i] >>> 24) & 255));
            i++;
        }
    }

    private void decode(long[] output, byte[] input, int len) {
        int i = 0;
        for (int j = 0; j < len; j += 4) {
            output[i] = b2iu(input[j]) | (b2iu(input[j + 1]) << 8) | (b2iu(input[j + 2]) << 16) | (b2iu(input[j + 3]) << 24);
            i++;
        }
    }
}
