import java.math.BigInteger;

public class FloatingPoint {

    public int mantLen, expLen;
    public int sign, exp, mant;
    public int outputMantLen;
    private boolean needNorm;

    public FloatingPoint(BigInteger originalNumber, boolean single) {
        if (single) {
            expLen = 8;
            mantLen = 23;
            outputMantLen = 6;
        } else {
            expLen = 5;
            mantLen = 10;
            outputMantLen = 3;
        }
        needNorm = true;
        sign = originalNumber.shiftRight(expLen + mantLen).intValue();
        mant = originalNumber.mod(BigInteger.valueOf(1 << mantLen)).intValue();
        exp = (originalNumber.shiftRight(mantLen)).intValue() % (1 << expLen) - (1 << (expLen - 1)) + 1;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("0x");
        if (isZero()) {
            s.append(this.outputMantLen == 6 ? "0.000000p+0" : "0.000p+0");
            return sign == 1 ? "-" + s : s.toString();
        }
        if (isInf()) {
            return getInf(sign);
        }
        if (isNan()) {
            return "nan";
        }
        if (isDenormalized()) {
            this.normalize();

            if (mantLen - 4 * outputMantLen >= 0) mant >>>= (mantLen - 4 * outputMantLen);
            else mant <<= (4 * outputMantLen - mantLen);
            s.append("1.");
            s.append("0".repeat(outputMantLen - Integer.toHexString(mant).length()))
                    .append(Integer.toHexString(mant)).append("p").append(exp);
            return sign == 1 ? "-" + s : s.toString();
        }

        mant <<= (4 * outputMantLen - mantLen);

        s.append("1.").append("0".repeat(outputMantLen - Integer.toHexString(mant).length()))
                .append(Integer.toHexString(mant)).append("p");
        s.append(exp >= 0 ? "+" : "").append(exp);

        return sign == 1 ? "-" + s : s.toString();
    }

    public String add(FloatingPoint other) {
        //special cases
        if (this.isNan() || other.isNan()) {
            return "nan";
        }
        if (this.isInf() && !other.isInf()) {
            return getInf(this.sign);
        }
        if (!this.isInf() && other.isInf()) {
            return getInf(other.sign);
        }
        if (this.isInf() && other.isInf()) {
            return this.sign == other.sign ? getInf(this.sign) : "nan";
        }
        if (this.isZero() && other.isZero()) {
            if (this.sign == 0 || other.sign == 0) this.sign = 0;
            else this.sign = 1;
            return this.toString();
        }
        if (this.isZero()) return other.toString();
        if (other.isZero()) return this.toString();
        // normalizing
        int e1, e2, m1, m2;
        e1 = exp;
        m1 = mant;
        e2 = other.exp;
        m2 = other.mant;

        this.normalize();
        other.normalize();

        m1 += 1 << mantLen;
        m2 += 1 << mantLen;
        BigInteger bm1 = BigInteger.valueOf(this.sign == 1 ? -m1 : m1);
        BigInteger bm2 = BigInteger.valueOf(other.sign == 1 ? -m2 : m2);

        int newExp;
        if (e1 < e2) {
            newExp = e1;
            bm2 = bm2.shiftLeft(Math.min(Math.abs(e2 - e1), mantLen));
        } else {
            bm1 = bm1.shiftLeft(Math.min(Math.abs(e2 - e1), mantLen));
            newExp = e2;
        }

        BigInteger newMant = bm1.add(bm2);

        int newSign = newMant.signum() == -1 ? 1 : 0;
        newMant = newMant.abs().shiftRight(Math.min(Math.abs(e2 - e1), mantLen));

        //making 1.(...)
        int i = 0;
        while (newMant.shiftRight(i).compareTo(BigInteger.ZERO) > 0) {
            i++;
        }
        i--;
        // i - count of signs after left 1
        newExp += (i - mantLen + Math.abs(e1 - e2));
        newMant = newMant.shiftRight(i - mantLen).mod(BigInteger.ONE.shiftLeft(mantLen));

        this.sign = newSign;
        this.exp = newExp;
        this.mant = newMant.intValue();
        if (this.exp > (1 << (expLen - 1)) - 1) return getInf(this.sign);
        if (this.exp < -((1 << (expLen - 1)) + mantLen - 1)) {
            return (this.sign == other.sign ? "" : "-") + (this.outputMantLen == 6 ? "0x0.000000p+0" : "0x0.000p+0");
        }
        needNorm = false;
        return this.toString();

    }


    public String sub(FloatingPoint other) {
        other.sign = other.sign == 1 ? 0 : 1;
        return add(other);
    }

    public String mul(FloatingPoint other) {
        if (this.isNan() || other.isNan()) {
            return "nan";
        }
        if (this.isInf() && other.isInf() && this.sign == other.sign) {
            return getInf(0);
        }
        if (this.isInf() && other.isInf() || this.isInf() && other.isZero() || other.isInf() && this.isZero()) {
            return "nan";
        }
        if (this.isInf()) return getInf(this.sign);
        if (other.isInf()) return getInf(other.sign);
        int newSign = this.sign == other.sign ? 0 : 1;
        if (this.isZero() || other.isZero())
            return (newSign == 0 ? "" : "-") + (this.outputMantLen == 6 ? "0x0.000000p+0" : "0x0.000p+0");


        this.normalize();
        other.normalize();
        int m1 = this.mant + (1 << mantLen);
        int m2 = other.mant + (1 << mantLen);

        int newExp = this.exp + other.exp;
        BigInteger newMant = BigInteger.valueOf(m1).multiply(BigInteger.valueOf(m2));

        if (newMant.bitLength() == this.mantLen * 2 + 2) newExp += 1;

        if (newMant.bitLength() == 2 * mantLen + 2) {
            newMant = newMant.shiftRight(mantLen + 1).mod(BigInteger.ONE.shiftLeft(mantLen));
        } else if (newMant.bitLength() == 2 * mantLen + 1) {
            newMant = newMant.shiftRight(mantLen).mod(BigInteger.ONE.shiftLeft(mantLen));
        }

        
        this.mant = newMant.intValue();
        this.exp = newExp;
        this.sign = newSign;
        if (this.exp > (1 << (expLen - 1)) - 1) return getInf(this.sign);
        if (this.exp < -((1 << (expLen - 1)) + mantLen - 1)) {
            return (this.sign == other.sign ? "" : "-") + (this.outputMantLen == 6 ? "0x0.000000p+0" : "0x0.000p+0");
        }
        needNorm = false;
        return this.toString();
    }

    public String div(FloatingPoint other) {
        // special cases:
        if (this.isNan() || other.isNan() || this.isInf() && other.isInf() || this.isZero() && other.isZero()) {
            return "nan";
        }
        if (other.isZero()) {
            return getInf(this.sign == other.sign ? 0 : 1);
        }
        if (this.isInf()) return getInf(this.sign == other.sign ? 0 : 1);
        if (this.isZero()) {
            this.sign = this.sign == other.sign ? 0 : 1;
            return this.toString();
        }
        if (other.isInf()) {
            return (this.sign == other.sign ? "" : "-") + (this.outputMantLen == 6 ? "0x0.000000p+0" : "0x0.000p+0");
        }



        this.normalize();
        other.normalize();
        int m1 = this.mant + (1 << mantLen);
        int m2 = other.mant + (1 << mantLen);
        int newExp = this.exp - other.exp;

        BigInteger newMant = BigInteger.valueOf(m1).shiftLeft(mantLen);
        newMant = newMant.divide(BigInteger.valueOf(m2));
        if (newMant.bitLength() <= mantLen) {
            newExp -= (mantLen - newMant.bitLength() + 1);
            newMant = newMant.mod(BigInteger.ONE.shiftLeft(mantLen - 1)).shiftLeft(1);
        }
        newMant = newMant.mod(BigInteger.ONE.shiftLeft(mantLen));
        this.sign = this.sign == other.sign ? 0 : 1;
        this.exp = newExp;
        this.mant = newMant.intValue();
        if (this.exp > (1 << (expLen - 1)) - 1) return getInf(this.sign);
        if (this.exp < -((1 << (expLen - 1)) + mantLen - 1)) {
            return (this.sign == other.sign ? "" : "-") + (this.outputMantLen == 6 ? "0x0.000000p+0" : "0x0.000p+0");
        }
        needNorm = false;
        return this.toString();
    }

    public boolean isNan() {
        return exp == 1 << expLen - 1 && mant != 0;
    }

    public boolean isInf() {
        return (exp >= 1 << expLen - 1 && mant == 0);
    }

    public boolean isZero() {
        return exp == 1 - (1 << expLen - 1) && mant == 0;
    }

    public String getInf(int sign) {
        return sign == 1 ? "-inf" : "inf";
    }

    public boolean isDenormalized() {
        return exp < 2 - (1 << (expLen - 1)) && mant != 0;
    }

    public void normalize() {
        if (isDenormalized() && needNorm) {
            if (this.exp == 1 - (1 << (expLen - 1))) {
                this.exp++;

                while (this.mant >>> (mantLen) == 0) {
                    this.mant <<= 1;
                    this.exp--;
                }
                this.mant %= (1 << mantLen);
            }
        } else if (isDenormalized()) {
            int i = exp;
            int c = 1;
            while (i != 2 - (1 << (expLen - 1))){
                mant = mant & (1<<c);
                c++;
                i++;
            }
        }
    }
}
