package tech.incineratez.randomspawn.utils;

public class Pair {
    private int a, b;

    public Pair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public Pair(int[] ab) {
        a = ab[0];
        b = ab[1];
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int[] get() {
        return new int[] {a, b};
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.a;
        hash = 47 * hash + this.b;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair other = (Pair) obj;
        if (this.a != other.a) {
            return false;
        }
        if (this.b != other.b) {
            return false;
        }
        return true;
    }
}
