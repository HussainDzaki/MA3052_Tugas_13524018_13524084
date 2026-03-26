package ma3052.dsu;

import java.util.HashMap;
import java.util.Random;

public class DisjointSetUnion<T> {
    private HashMap<T, T> dsuParent = new HashMap<>();
    private Random rng = new Random();

    public T getParent(T a) {
        if (dsuParent.containsKey(a)) {
            T parentA = getParent(dsuParent.get(a));
            dsuParent.put(a, parentA);
            return parentA;
        } else {
            return a;
        }
    }

    public void unite(T a, T b) {
        if (isSameSet(a, b)) {
            return;
        }
        if (rng.nextBoolean()) {
            dsuParent.put(getParent(a), getParent(b));
        } else {
            dsuParent.put(getParent(b), getParent(a));
        }
    }

    public boolean isSameSet(T a, T b) {
        return getParent(a) == getParent(b);
    }

}
