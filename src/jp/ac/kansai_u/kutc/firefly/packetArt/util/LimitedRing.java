package jp.ac.kansai_u.kutc.firefly.packetArt.util;

/**
 *
 * @author sya-ke
*/
public class LimitedRing<E> extends LimitedQueue<E> {

    public LimitedRing(int lim){
        super(lim);
    }

    /**
     * peekしたものは最後尾に回ります。
     *
     * @return 先頭のオブジェクト
    */
    public synchronized E peek() {
        E o = super.remove();
        super.add(o);
        return o;
    }

    /**
     * peekFirstしたものは最後尾に回りません。
     *
     * @return 先頭のオブジェクト
    */
    public synchronized E peekFirst() {
        return super.peek();
    }
}
