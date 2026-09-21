/*
 * Copyright (c) 1997, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import sun.misc.SharedSecrets;

/**
 * Hash table based implementation of the <tt>Map</tt> interface.  This
 * implementation provides all of the optional map operations, and permits
 * <tt>null</tt> values and the <tt>null</tt> key.  (The <tt>HashMap</tt>
 * class is roughly equivalent to <tt>Hashtable</tt>, except that it is
 * unsynchronized and permits nulls.)  This class makes no guarantees as to
 * the order of the map; in particular, it does not guarantee that the order
 * will remain constant over time.
 *
 * <p>This implementation provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the hash function
 * disperses the elements properly among the buckets.  Iteration over
 * collection views requires time proportional to the "capacity" of the
 * <tt>HashMap</tt> instance (the number of buckets) plus its size (the number
 * of key-value mappings).  Thus, it's very important not to set the initial
 * capacity too high (or the load factor too low) if iteration performance is
 * important.
 *
 * <p>An instance of <tt>HashMap</tt> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the hash table is <i>rehashed</i> (that is, internal data
 * structures are rebuilt) so that the hash table has approximately twice the
 * number of buckets.
 *
 * <p>As a general rule, the default load factor (.75) offers a good
 * tradeoff between time and space costs.  Higher values decrease the
 * space overhead but increase the lookup cost (reflected in most of
 * the operations of the <tt>HashMap</tt> class, including
 * <tt>get</tt> and <tt>put</tt>).  The expected number of entries in
 * the map and its load factor should be taken into account when
 * setting its initial capacity, so as to minimize the number of
 * rehash operations.  If the initial capacity is greater than the
 * maximum number of entries divided by the load factor, no rehash
 * operations will ever occur.
 *
 * <p>If many mappings are to be stored in a <tt>HashMap</tt>
 * instance, creating it with a sufficiently large capacity will allow
 * the mappings to be stored more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.  Note that using
 * many keys with the same {@code hashCode()} is a sure way to slow
 * down performance of any hash table. To ameliorate impact, when keys
 * are {@link Comparable}, this class may use comparison order among
 * keys to help break ties.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a hash map concurrently, and at least one of
 * the threads modifies the map structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more mappings; merely changing the value
 * associated with a key that an instance already contains is not a
 * structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.
 *
 * If no such object exists, the map should be "wrapped" using the
 * {@link Collections#synchronizedMap Collections.synchronizedMap}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the map:<pre>
 *   Map m = Collections.synchronizedMap(new HashMap(...));</pre>
 *
 * <p>The iterators returned by all of this class's "collection view methods"
 * are <i>fail-fast</i>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> method, the iterator will throw a
 * {@link ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author  Doug Lea
 * @author  Josh Bloch
 * @author  Arthur van Hoff
 * @author  Neal Gafter
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     TreeMap
 * @see     Hashtable
 * @since   1.2
 */
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * Implementation notes.
     *
     * This map usually acts as a binned (bucketed) hash table, but
     * when bins get too large, they are transformed into bins of
     * TreeNodes, each structured similarly to those in
     * java.util.TreeMap. Most methods try to use normal bins, but
     * relay to TreeNode methods when applicable (simply by checking
     * instanceof a node).  Bins of TreeNodes may be traversed and
     * used like any others, but additionally support faster lookup
     * when overpopulated. However, since the vast majority of bins in
     * normal use are not overpopulated, checking for existence of
     * tree bins may be delayed in the course of table methods.
     *
     * Tree bins (i.e., bins whose elements are all TreeNodes) are
     * ordered primarily by hashCode, but in the case of ties, if two
     * elements are of the same "class C implements Comparable<C>",
     * type then their compareTo method is used for ordering. (We
     * conservatively check generic types via reflection to validate
     * this -- see method comparableClassFor).  The added complexity
     * of tree bins is worthwhile in providing worst-case O(log n)
     * operations when keys either have distinct hashes or are
     * orderable, Thus, performance degrades gracefully under
     * accidental or malicious usages in which hashCode() methods
     * return values that are poorly distributed, as well as those in
     * which many keys share a hashCode, so long as they are also
     * Comparable. (If neither of these apply, we may waste about a
     * factor of two in time and space compared to taking no
     * precautions. But the only known cases stem from poor user
     * programming practices that are already so slow that this makes
     * little difference.)
     *
     * Because TreeNodes are about twice the size of regular nodes, we
     * use them only when bins contain enough nodes to warrant use
     * (see TREEIFY_THRESHOLD). And when they become too small (due to
     * removal or resizing) they are converted back to plain bins.  In
     * usages with well-distributed user hashCodes, tree bins are
     * rarely used.  Ideally, under random hashCodes, the frequency of
     * nodes in bins follows a Poisson distribution
     * (http://en.wikipedia.org/wiki/Poisson_distribution) with a
     * parameter of about 0.5 on average for the default resizing
     * threshold of 0.75, although with a large variance because of
     * resizing granularity. Ignoring variance, the expected
     * occurrences of list size k are (exp(-0.5) * pow(0.5, k) /
     * factorial(k)). The first values are:
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: less than 1 in ten million
     *
     * The root of a tree bin is normally its first node.  However,
     * sometimes (currently only upon Iterator.remove), the root might
     * be elsewhere, but can be recovered following parent links
     * (method TreeNode.root()).
     *
     * All applicable internal methods accept a hash code as an
     * argument (as normally supplied from a public method), allowing
     * them to call each other without recomputing user hashCodes.
     * Most internal methods also accept a "tab" argument, that is
     * normally the current table, but may be a new or old one when
     * resizing or converting.
     *
     * When bin lists are treeified, split, or untreeified, we keep
     * them in the same relative access/traversal order (i.e., field
     * Node.next) to better preserve locality, and to slightly
     * simplify handling of splits and traversals that invoke
     * iterator.remove. When using comparators on insertion, to keep a
     * total ordering (or as close as is required here) across
     * rebalancings, we compare classes and identityHashCodes as
     * tie-breakers.
     *
     * The use and transitions among plain vs tree modes is
     * complicated by the existence of subclass LinkedHashMap. See
     * below for hook methods defined to be invoked upon insertion,
     * removal and access that allow LinkedHashMap internals to
     * otherwise remain independent of these mechanics. (This also
     * requires that a map instance be passed to some utility methods
     * that may create new nodes.)
     *
     * The concurrent-programming-like SSA-based coding style helps
     * avoid aliasing errors amid all of the twisty pointer operations.
     */

    /**
     * 默认初始容量(16)-必须是2的幂
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大值容量
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认负载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     *  树化阈值:当一个桶（链表）里的节点数达到 8 个时，链表会转化为红黑树；当节点数减少时，又会退化为链表。
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 退化阈值：6（红黑树 → 链表，UNTREEIFY_THRESHOLD）
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 允许桶被树化的最小表容量。 哈希表的容量（数组长度）必须 ≥ 64，才允许将链表转为红黑树。
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 存储结构的最基础单元
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;  // 缓存的 hash 值（已扰动）
        final K key;    // key（不可变）
        V value;    // value（可变，支持覆盖）
        Node<K,V> next; // 链表下一个节点

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /* ---------------- Static utilities -------------- */

    /**
     * Computes key.hashCode() and spreads (XORs) higher bits of hash
     * to lower.  Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    static final int hash(Object key) {
        int h;
        // 高 16 位的特征被“混合”到了低 16 位中。高位间接参与运算，使得分布更加随机、均匀。
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 用来严格检查一个对象的类是否实现了 Comparable<自身类型>
     *     不是简单地检查是否实现了 Comparable 接口，而是要检查泛型参数是否匹配。
     * 核心判断：Comparable 的类型参数必须是类自身。
     *
     * 错误示例：
     * class MyKey implements Comparable<SomeOtherType> {
     *     // 实现了 compareTo(SomeOtherType o)
     *     // 但传入的 o 不是 MyKey 类型！
     * }
     *
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {  // 快速判断：是否实现了 Comparable 接口 （注意：这里只是运行时类型检查，还没检查泛型。）
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;     //  变量声明
            if ((c = x.getClass()) == String.class) // String 是最常见的 key 类型，它实现了 Comparable<String>
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {  // 1.获取类实现的泛型接口
                for (int i = 0; i < ts.length; ++i) {   // 遍历所有接口，查找匹配的 Comparable
                    if (((t = ts[i]) instanceof ParameterizedType) &&   // 2.检查当前接口是否是参数化类型（即带泛型的接口，如 Comparable<MyKey>）。
                        ((p = (ParameterizedType)t).getRawType() ==  // 获取原始类型
                         Comparable.class) &&   //  3.判断原始类型是不是 Comparable.class
                        (as = p.getActualTypeArguments()) != null &&   //  4.获取泛型的实际类型参数数组。
                        as.length == 1 && as[0] == c) // 只有一个类型参数（as.length == 1）。类型参数就是 c 本身（as[0] == c）。
                        return c;
                }
            }
        }
        return null;
    }

    /**
     *  比较 k 和 x
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /**
     * 返回大于等于给定数值的最小的 2 的幂。
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;    // 减 1 是为了保护本身就是 2 的幂的情况，防止结果翻倍。
        // 5 次右移 + 或运算
        /* 目标：把 n 的二进制中，最高位 1 以下的所有位全部置为 1。
           原理：每次把已有的高位 1 向右"复制"到更低的位上，覆盖范围依次翻倍。*/
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- Fields -------------- */

    /**
     * 哈希表本体
     */
    transient Node<K,V>[] table;

    /**
     * 缓存的 Entry 集合视图
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * key-value 对的总数量
     */
    transient int size;

    /**
     * 结构性修改计数器
     */
    transient int modCount;

    /**
     * 扩容阈值
     */
    int threshold;

    /**
     *  负载因子
     */
    final float loadFactor;

    /* ---------------- Public operations -------------- */

    /**
     * 创建指定初始容量和负载因子的实例
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)    // 初始容量合法性检查
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) // 负载因子合法性检查
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;   // 字段赋值 loadFactor 是 final 的，这里一次性赋值，后续不可更改。
        this.threshold = tableSizeFor(initialCapacity); // 大于等于给定数值的最小的 2 的幂
    }

    /**
     *  创建指定初始容量和默认负载因子的实例
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造默认初始容量的空和默认负载因子的实例
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    /**
     * 构造已有数据的实例
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * 批量写入
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            // ------ 预计算容量 ------
            if (table == null) {
                float ft = ((float)s / loadFactor) + 1.0F;  // 容量反推公式
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);   // 上限截断
                if (t > threshold)
                    threshold = tableSizeFor(t);    // 更新 threshold
            }
            else if (s > threshold)    // table != null 且 s > threshold → 先扩容
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {   // 逐个放入
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 && // 判空
            (first = tab[(n - 1) & hash]) != null) {       // 定位桶，获取头节点
            if (first.hash == hash &&
                ((k = first.key) == key || (key != null && key.equals(k)))) // 情况一：头节点就是目标节点
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode) // 情况二：遇到红黑树节点
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do { // 情况三：普通链表遍历
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) // 已匹配到目标节点
                        return e;
                } while ((e = e.next) != null);  // 没找到，继续遍历下一个
            }
        }
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * 插入
     * key 唯一，重复 put 同一个 key 会覆盖旧值。
     *  key 之前不存在 → 返回 null。
     *  key 之前存在，但对应的 value 就是 null → 也返回 null。
     * HashMap 允许 null value，所以无法仅通过返回值区分"不存在"和"值为 null"。如果需要区分，用 containsKey(key) 先判断。
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 插入
     * @param hash 扰动后的 hash 值
     * @param key 原始 key
     * @param value 原始 value
     * @param onlyIfAbsent false 表示"如果已存在就覆盖"
     * @param evict true 表示"这是一次正常的 put 操作"
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0) // ======>1、 初始化检查（懒加载）
            n = (tab = resize()).length;    // 初始化
        if ((p = tab[i = (n - 1) & hash]) == null) // ======> 2、计算下标 & 桶为空的情况
            tab[i] = newNode(hash, key, value, null); // 直接创建一个新Node节点，当头节点放桶内
        else { // ======> 3、桶不为空
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k)))) // 情况 A：头节点就是目标 key（直接命中）
                e = p;
            else if (p instanceof TreeNode) // 情况 B：已经是红黑树节点（调用树插入）
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value); // 如果已存在，e是旧节点；如果是新插入，e 为 null。
            else { // 情况 C：还是普通链表（遍历链表）
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) { // 走到链表尾部，插入新节点
                        p.next = newNode(hash, key, value, null); // 创建一个新Node节点（尾插法，避免并发死循环）
                        if (binCount >= TREEIFY_THRESHOLD - 1) // 检查是否需要树化 （ 链表长度 >= 树化阈值 8 ）
                            treeifyBin(tab, hash); // 树化
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) // 中途找到相同 key
                        break;
                    p = e; // 指针后移
                }
            }
            if (e != null) { // 处理已存在 key 的覆盖逻辑
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value; // 直接覆盖旧 value
                afterNodeAccess(e); // 给 LinkedHashMap 用的回调（记录访问顺序）。
                return oldValue; // 返回旧值
            }
        }
        ++modCount; // 结构性修改次数 +1（用于 fail-fast 迭代器检测并发修改）。
        if (++size > threshold) //  size 是 HashMap 中键值对的总数。
            resize(); // 触发扩容
        afterNodeInsertion(evict); // 提供子类用 (给 LinkedHashMap 用的回调（可能移除最老的元素）。)
        return null;
    }

    /**
     * 初始化(扩容)
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table; // 旧数组
        int oldCap = (oldTab == null) ? 0 : oldTab.length; // 旧数组长度
        int oldThr = threshold; // 旧扩容阈值
        // ---------- 计算新容量和阈值 ---------------
        int newCap, newThr = 0; // 新容量和阈值，待计算
        if (oldCap > 0) { // 场景 1：已初始化过
            if (oldCap >= MAXIMUM_CAPACITY) { // 如果旧容量已经达到最大值（1 << 30），无法再扩容。
                threshold = Integer.MAX_VALUE; // 把阈值设为 Integer.MAX_VALUE，直接返回旧表（不再扩容，但允许继续插入）。
                return oldTab;
            }
            // 如果翻倍后不超上限，且旧容量 ≥ 默认初始容量（16），阈值也翻倍（oldThr << 1）。
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && // 正常扩容：容量翻倍
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // 容量翻倍后，阈值也翻倍，保持负载因子（0.75）不变。
        }
        else if (oldThr > 0) // 场景 2：未初始化，但 threshold 有值（带参构造）
            newCap = oldThr;   // 首次 resize 时，直接把这个值作为新容量。
        else {      // 场景 3：完全使用默认值（无参构造）
            newCap = DEFAULT_INITIAL_CAPACITY; // 默认初始化容量（16）。
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY); // 默认阈值 （0.75 * 16） 12。
        }
        if (newThr == 0) {  // 兜底：计算阈值（如果前面没算）
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        //  ----------  保存阈值 & 创建新数组  ----------
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap]; // oldTab ==null; 就是初始化数组，要不就是扩容新数组
        table = newTab;
        // ---------- 数据迁移（核心精华）----------
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {  // 遍历旧数组的每个桶 j
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {  // 读取 该桶的头节点
                    oldTab[j] = null;   //  把旧桶置 null（帮助 GC）
                    if (e.next == null) //  ------->  情况 1：桶中只有一个节点
                        newTab[e.hash & (newCap - 1)] = e; // 直接重新计算下标放入新数组
                    else if (e instanceof TreeNode) //  -------> 情况 2：桶中是红黑树
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // -------> 情况 3：桶中是普通链表（JDK 8 的精妙优化）
                        Node<K,V> loHead = null, loTail = null; //  留在原下标 j 的节点。
                        Node<K,V> hiHead = null, hiTail = null; //  迁移到新下标 j + oldCap 的节点。
                        Node<K,V> next;
                        do { // 遍历链表，按位分流 --- 把当前节点 e 挂到 lo 链表或 hi 链表的尾部（尾插法，保持原顺序）。 ----
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {   // 扩容后取模结果不变，留在原位置 j
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            } else {  // 扩容后取模结果多了 oldCap，去 j + oldCap。
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;   // 把尾节点的 next 置 null，切断旧引用。
                            newTab[j] = loHead; // lo 链表放到新数组的原位置 j
                        }
                        if (hiTail != null) {
                            hiTail.next = null;     // 把尾节点的 next 置 null，切断旧引用。
                            newTab[j + oldCap] = hiHead; // hi 链表放到新数组的 j + oldCap 位置。
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * 决定是否树化
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) //  如果表为 null 或长度 < 64 → 不树化，直接扩容。
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) { // 获取桶头节点
            TreeNode<K,V> hd = null, tl = null; // hd（head）：新 TreeNode 链表的头。tl（tail）：新链表的当前尾部（用于尾插法）。
            do {    // --------  遍历旧链表， 逐个创建TreeNode  --------
                TreeNode<K,V> p = replacementTreeNode(e, null); // 为旧 Node e 创建一个对应的 TreeNode p，
                // ------ 构建TreeNode的双向链表关系 --------
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null); //  移动到旧链表的下一个节点
            if ((tab[index] = hd) != null)  // 替换桶头
                hd.treeify(tab);    //  启动建树
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    /**
     * Implements Map.remove and related methods.
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to match if matchValue, else ignored
     * @param matchValue if true only remove if value is equal
     * @param movable if false do not move other nodes while removing
     * @return the node, or null if none
     */
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // Overrides of JDK8 Map extension methods

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            else
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
        }
        else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // Cloning and serialization

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    // These methods are also used when serializing HashSets
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the
     *             bucket array) is emitted (int), followed by the
     *             <i>size</i> (an int, the number of key-value
     *             mappings), followed by the key (Object) and value (Object)
     *             for each key-value mapping.  The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * Reconstitutes this map from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws IOException if an I/O error occurs
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();

        // Read loadFactor (ignore threshold)
        float lf = fields.get("loadFactor", 0.75f);
        if (lf <= 0 || Float.isNaN(lf))
            throw new InvalidObjectException("Illegal load factor: " + lf);

        lf = Math.min(Math.max(0.25f, lf), 4.0f);
        HashMap.UnsafeHolder.putLoadFactor(this, lf);

        reinitialize();

        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0) {
            throw new InvalidObjectException("Illegal mappings count: " + mappings);
        } else if (mappings == 0) {
            // use defaults
        } else if (mappings > 0) {
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);

            // Check Map.Entry[].class since it's the nearest public type to
            // what we're actually creating.
            SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    // Support for resetting final field during deserializing
    private static final class UnsafeHolder {
        private UnsafeHolder() { throw new InternalError(); }
        private static final sun.misc.Unsafe unsafe
                = sun.misc.Unsafe.getUnsafe();
        private static final long LF_OFFSET;
        static {
            try {
                LF_OFFSET = unsafe.objectFieldOffset(HashMap.class.getDeclaredField("loadFactor"));
            } catch (NoSuchFieldException nfe) {
                throw new InternalError();
            }
        }
        static void putLoadFactor(HashMap<?, ?> map, float lf) {
            unsafe.putFloat(map, LF_OFFSET, lf);
        }
    }

    /* ------------------------------------------------------------ */
    // iterators

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return
        Node<K,V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    /* ------------------------------------------------------------ */
    // spliterators

    static class HashMapSpliterator<K,V> {
        final HashMap<K,V> map;
        Node<K,V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index
        int est;                    // size estimate
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap support


    /*
     * The following package-protected methods are designed to be
     * overridden by LinkedHashMap, but not by any other subclass.
     * Nearly all other internal methods are also package-protected
     * but are declared final, so can be used by LinkedHashMap, view
     * classes, and HashSet.
     */

    // 创建一个新 Node 节点
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // 只拷贝 hash、key、value，丢弃所有红黑树相关字段（parent、left、right、prev、red）。
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 复制 hash/key/value，next 暂时设为 null。
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * Reset to initial default state.  Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     */
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * 找树的根节点
         */
        final TreeNode<K,V> root() {
            // 循环向上遍历找父节点
            for (TreeNode<K,V> r = this, p;;) { // 初始为 this（从自己开始）
                if ((p = r.parent) == null) // 如果 p == null：说明 r 没有父节点 → r 就是根节点 → 返回 r
                    return r;
                r = p;  // 如果 r 有父节点，把 r 向上移动一步
            }
        }

        /**
         *
         * 确保红黑树的根节点始终位于桶（bin）链表的头部。
         */
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {  // 边界检查
                int index = (n - 1) & root.hash;   //    计算桶下标
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];    //  获取当前头节点
                if (root != first) {    // 判断是否需要移动
                    Node<K,V> rn;   // 临时保存
                    tab[index] = root;  // 把 root 从双向链表中"摘出来"
                    TreeNode<K,V> rp = root.prev;  // 第一步：先把桶头指针直接指向 root
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp; // 绕过了 root，把 root 后面的节点和前面的节点连起来。
                    if (rp != null)
                        rp.next = rn;   // 完成 root 前面节点 和 后面节点 的双向链接 （摘除 root）
                    if (first != null)
                        first.prev = root; // 把 root 插入到链表头部
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);   // 断言验证 （验证结构完整性）
            }
        }

        /**
         * 红黑树中的通用查找引擎，按 hash 和 key 在树中搜索匹配的节点
         * @param h 目标 key 的 hash
         * @param k 目标 key
         * @param kc 目标 key 的 Comparable 筛选类（可为 null，方法内会延迟初始化）
         * @return  找到的 TreeNode；找不到返回 null
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this; // 当前遍历节点，从 this（调用者，通常是根或子树根）开始。
            do {
                int ph, dir; K pk; // ph：当前节点 p 的 hash；dir：比较方向；pk：当前节点 p 的 key
                TreeNode<K,V> pl = p.left, pr = p.right, q; // pl：左孩子；pr：右孩子；q：递归搜索的临时结果
                if ((ph = p.hash) > h)  // 1、hash 比较
                    p = pl; // 目标 hash 更小 → 往左走
                else if (ph < h)
                    p = pr; // 目标 hash 更大 → 往右走
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))  // 2、hash 相同 → 检查 key 是否匹配
                    return p;   // 匹配 → 找到了 → 直接返回 p
                // 3、hash 相同但 key 不同 → 处理 hash 冲突
                else if (pl == null) // 分支1：左子树为空 → 只能往右
                    p = pr;
                else if (pr == null) // 分支2：右子树为空 → 只能往左
                    p = pl;
                else if ((kc != null || // 分支3：左右都不为空 → 尝试用 Comparable 决定方向
                          (kc = comparableClassFor(k)) != null) && // 确认 k 自身是"合格"的 Comparable
                         (dir = compareComparables(kc, k, pk)) != 0) // 在 k 合格的前提下，比较出大小
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null) // 分支4：Comparable 无法决定 → 递归搜索 （先递归搜索右子树）
                    return q;   // 如果右子树找到了 → 直接返回 q
                else
                    p = pl; // 右子树没找到 → 去左子树继续循环搜索
            } while (p != null); // 走到了叶子节点的空孩子 → 没找到 → 返回 null。
            return null;
        }

        /**
         * Calls find for root node.
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {

            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * 当 hashCode 相等且不可比较时，用于排序插入的平局打破工具。
         * @param a 当前要插入的 key
         * @param b 树中已有的 key
         * @return  方向标志，最终返回 -1（a 放左边）或 1（a 放右边）。
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)   // 判断：null 检查 + 类名比较
                // 兜底逻辑：用 identityHashCode 决出胜负。
                // identityHashCode：原始哈希码——就是 Object.hashCode() 的默认实现值（基于对象内存地址或 JVM 内部标识），不受对象重写的 hashCode() 方法影响。
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ? // 对象内存地址的哈希
                     -1 : 1);
            return d;
        }

        /**
         * 树化：从零建树（链表→树）
         * @param tab  哈希表数组，用于最后把根节点放到桶头。
         */
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;  // 红黑树的根节点，初始为 null。
            // 遍历链表（外部循环）
            for (TreeNode<K,V> x = this, next; x != null; x = next) {   // 逐个遍历链表中的每个 TreeNode。 （this 是链表的头节点。）
                next = (TreeNode<K,V>)x.next;   // 提前保存下一个节点（因为后面会修改 x 的指针）。
                x.left = x.right = null; // 清空原有的树指针：因为 TreeNode 可能之前是树结构（如 split 后保留的），现在要重新建树，所以先把左右孩子置空。
                if (root == null) { // 说明这是第一个节点。
                    x.parent = null;
                    x.red = false;
                    root = x;   // 第一个节点 → 成为根
                }
                else {  //  后续节点 → 按 BST 规则查找插入位置
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null; // 用于后续判断是否可以按 Comparable 比较。
                    for (TreeNode<K,V> p = root;;) {    // 内部循环：从根开始找插入点
                        int dir, ph;    // dir：方向标志，-1 表示向左，1 表示向右。
                        K pk = p.key;   // ph / pk： 当前节点 p 的 hash 和 key。
                        // -----------   找位置    ---------------
                        if ((ph = p.hash) > h)  // 分支1: 比较 hash 决定方向
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) || // 确认 k 自身是"合格"的 Comparable
                                 (dir = compareComparables(kc, k, pk)) == 0)    // 分支2: 在 k 合格的前提下，安全比较 k 和 x
                            dir = tieBreakOrder(k, pk); // 分支3: 兜底逻辑： 类名 + System.identityHashCode 做一个稳定且不会死循环的排序。

                        // -----------   找到空位，挂载新节点   ---------------
                        TreeNode<K,V> xp = p; // 保存当前节点 p（作为父节点）。
                        // ========>  根据 dir 走向左孩子或右孩子。如果走到 null，说明找到了插入位置。
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;  // 设置 插入节点的 父节点
                            if (dir <= 0)
                                xp.left = x;    // 挂载到父节点 的 左节点位置
                            else
                                xp.right = x;   // 挂载到父节点 的 右节点位置
                            root = balanceInsertion(root, x);   // ------> 红黑树插入修复
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root); //  收尾：确保根节点在桶头
        }

        /**
         * 退化为普通链表
         */
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null; // 新链表的头节点、尾节点
            for (Node<K,V> q = this; q != null; q = q.next) {   // 遍历 TreeNode 链表（利用TreeNode中双向/单链表的那层结构）
                Node<K,V> p = map.replacementNode(q, null); // 创建替代节点
                //  尾插法构建新链表
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * 在已有树中插入/查找
         */
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;   // 标记是否已经做过全子树搜索（防止重复搜索）。
            TreeNode<K,V> root = (parent != null) ? root() : this;  // 找到树的根节点
            for (TreeNode<K,V> p = root;;) {    // 主循环：在树中查找插入位置
                // -----------   找位置    ---------------
                int dir, ph; K pk;  // dir 方向标志，-1 表示向左，1 表示向右。 ph / pk： 当前节点 p 的 hash 和 key。
                if ((ph = p.hash) > h)  // 分支1: 比较 hash 决定方向
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||  //  检查 k 是否 implements Comparable<自身>。
                         (dir = compareComparables(kc, k, pk)) == 0) {  // 分支2: 在 k 合格的前提下，安全比较 k 和 x
                    if (!searched) {    // 需要特殊处理
                        TreeNode<K,V> q, ch;    //  q：搜索结果。  ch：左右孩子临时变量。
                        searched = true;    // 防止后续循环中重复搜索
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||    //  在左子树中递归搜索 key
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))  //  在右子树中递归搜索 key
                            return q;
                    }
                    dir = tieBreakOrder(k, pk); // 分支3: 兜底逻辑： 类名 + System.identityHashCode 做一个稳定且不会死循环的排序。
                }
                // -----------   找到空位，挂载新节点   ---------------
                TreeNode<K,V> xp = p;   // 保存当前节点 p（作为父节点）。
                // ========>  根据 dir 走向左孩子或右孩子。如果走到 null，说明找到了插入位置。
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);    // 创建TreeNode新节点 （尾插法）
                    if (dir <= 0)
                        xp.left = x;     // 挂载到父节点 的 左节点位置
                    else
                        xp.right = x;   // 挂载到父节点 的 右节点位置
                    xp.next = x;
                    x.parent = x.prev = xp; // 建立双向链表关系（xp 和 x）
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x; // 新节点 x 被精确插入到 xp 和 xpn 之间
                    moveRootToFront(tab, balanceInsertion(root, x));    // 平衡修复 & 根归位
                    return null;
                }
            }
        }

        /**
         * Removes the given node, that must be present before this call.
         * This is messier than typical red-black deletion code because we
         * cannot swap the contents of an interior node with a leaf
         * successor that is pinned by "next" pointers that are accessible
         * independently during traversal. So instead we swap the tree
         * linkages. If the current tree appears to have too few nodes,
         * the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root == null
                || (movable
                    && (root.right == null
                        || (rl = root.left) == null
                        || rl.left == null))) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    (root = replacement).red = false;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * 负责将一棵红黑树拆分成两组，并根据节点数量决定保持树化还是退化为链表。
         * @param map
         * @param tab   新哈希表数组
         * @param index 当前树桶在旧表中的下标
         * @param bit   拆分依据位 （旧数组长度）
         */
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this; //  当前树桶的头节点
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;    //  分别记录 lo 组和 hi 组的节点计数。
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;   // 获取 下一个节点 （TreeNode同时维护了双向链表（prev / next）和红黑树（parent / left / right）两种结构。）
                e.next = null;  //  断开原链表连接，准备重新挂载到 lo 或 hi 链表上。
                //   ------  判断去向：低位 or 高位  ------
                if ((e.hash & bit) == 0) {  //  下标不变，归入 lo 组。（挂载到 lo 链表，尾插法）
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                } else {  //  归入 hi 组（新下标 = index + oldCap）。 （挂载到 hi 链表，尾插法）
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {   // ------>  处理 lo 组（留在原下标 index）
                if (lc <= UNTREEIFY_THRESHOLD)  // 如果 lo 组节点数 ≤ 6，调用 untreeify() 退化为普通链
                    tab[index] = loHead.untreeify(map);
                else {  // 保留树结构，把 loHead 直接放到 tab[index]
                    tab[index] = loHead;
                    if (hiHead != null) // 说明原来完整的树 被拆分开了，需要重建红黑树。
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {   // ------>   处理 hi 组（迁移到 index + bit）
                if (hc <= UNTREEIFY_THRESHOLD)  // 如果 hi 组节点数 ≤ 6，调用 untreeify() 退化为普通链
                    tab[index + bit] = hiHead.untreeify(map);
                else {  // 保留树结构，把 hiHead 直接放到 tab[[index + bit]
                    tab[index + bit] = hiHead;
                    if (loHead != null)     // 说明原来完整的树 被拆分开了，需要重建红黑树。
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        /**
         * 红黑树中左旋
         * @param root 根节点
         * @param p 旋转支点
         *     pp (可能是null)  ->左旋后：  pp
         *      \                          \
         *       p                          r
         *      / \                        / \
         *     L   r                      p   RR
         *        / \                    / \
         *       rl  RR                 L  rl
         */
        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            // r：p 的右孩子（旋转后会成为新的“支点”）；pp：p 的父节点。 rl：r 的左孩子（旋转后需要挂到 p 的右边）。
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {  // 2. 前置条件检查 （p 必须有右孩子）
                if ((rl = p.right = r.left) != null) // 3. 步骤一：把 r 的左孩子挂到 p 的右边
                    rl.parent = p;
                // 如果 pp == null：说明 p 原来就是整棵树的根节点。现在 r 取代了 p，所以 r 变成了新根。
                if ((pp = r.parent = p.parent) == null) // 4. 步骤二：把 r 提升到 p 原来的位置
                    (root = r).red = false; // 更新 root 引用；红黑树性质要求根节点必须是黑色，所以强制把 r 染黑。
                else if (pp.left == p)  // 原来 p 有父节点，判断 p 是 pp 的左孩子还是右孩子，对应地把 pp.left 或 pp.right 设为 r
                    pp.left = r;
                else
                    pp.right = r;
                // 5. 步骤三：把 p 降为 r 的左孩子
                r.left = p; // 把 p 挂在 r 的左边
                p.parent = r; // r 变成 p 的父节点
            }
            return root;
        }


        /**
         *  红黑树中右旋
         *
         *     pp (祖父的父)
         *         |
         *         p   ->右旋后    l
         *        / \            / \
         *       l   pr         ll  p
         *      / \                / \
         *     ll  lr             lr pr
         * @param root 根节点
         * @param p 旋转支点
         */
        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) { // 前置条件检查 （p 必须有左孩子）
                if ((lr = p.left = l.right) != null) // 步骤一：把 l 的右孩子挂到 p 的左边
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null) // 步骤二：把 l 提升到 p 原来的位置
                    (root = l).red = false; // 如父节点为空， l 成了新根，新根必须涂黑
                else if (pp.right == p) //原来 p 有父节点，判断 p 是 pp 的左孩子还是右孩子，对应地把 pp.left 或 pp.right 设为 l
                    pp.right = l;
                else
                    pp.left = l;
                // 步骤三：把 p 降为 l 的右孩子
                l.right = p; // 把 p 挂在 l 的右边
                p.parent = l; // l 变成 p 的父节点
            }
            return root;
        }

        /**
         * @param root 父节点
         * @param x 新增节点
         */
        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true; // 新插入的节点统一先设为红色。
            // xp：父节点；xpp：祖父节点；xppl：左叔叔；xppr：右叔叔
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                // 情况一：x 就是根节点
                if ((xp = x.parent) == null) { // 父节点 为空 （说明 x 就是整棵树的根。）
                    x.red = false; // 根据性质2（根必须为黑），将 x.red 设为 false。
                    return x; // 直接返回 x 作为新的根节点，结束调整。
                }
                // 情况二：父节点是黑色，或没有祖父节点
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root; // 不需要做任何调整，直接返回原来的 root。
                // 前提 父节点是红色
                // 分支 A：父节点是祖父节点的【左孩子】
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) { // 子情况 A1：叔叔节点（祖父的右孩子）是红色
                        // 改父、叔为黑，祖父变红；关注节点移到祖父。 重新循环
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else { // 子情况 A2：叔叔节点是黑色（或 null）
                        if (x == xp.right) { //如果是“LR型” x 是父节点的右孩子
                            root = rotateLeft(root, x = xp); // // 以父亲为轴，左旋
                            xpp = (xp = x.parent) == null ? null : xp.parent; // 更新引用，并重新获取父节点和祖父节点。
                        }
                        // 统一处理“LL 型”（x 是父节点的左孩子，或刚转成了LL型）：
                        if (xp != null) {
                            xp.red = false;  // 父亲变黑
                            if (xpp != null) {
                                xpp.red = true; // 祖父变红
                                root = rotateRight(root, xpp); // 以祖父为轴，右旋
                            }
                        }
                    }
                }
                // 分支 B：父节点是祖父节点的【右孩子】（与分支 A 完全对称）
                else {
                    if (xppl != null && xppl.red) {  // 子情况 B1：叔叔节点（祖父的左孩子）是红色
                        // 改父、叔为黑，祖父变红；关注节点移到祖父
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    // 子情况 B2：叔叔节点是黑色
                    else {
                        if (x == xp.left) { // 如果是“RL 型”（x 是父节点的左孩子）：
                            root = rotateRight(root, x = xp); // // 以父亲为轴，右旋，转成 RR 型
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) { // 统一处理“RR 型”：
                            xp.red = false; // 父亲变黑
                            if (xpp != null) {
                                xpp.red = true; // 祖父变红
                                root = rotateLeft(root, xpp); // 以祖父为轴，左旋
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * 在 assert 模式下验证红黑树 + 双向链表的结构完整性。
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            // tp:父节点; tl:左孩子;tr:右孩子;tb:前驱节点;tn:后继节点
            if (tb != null && tb.next != t) //  检查 1：如果 t 有前驱 tb，那么 tb 的后继必须是 t 本身。
                return false;   // 违反场景：链表指针错乱
            if (tn != null && tn.prev != t) // 检查 2：如果 t 有后继 tn，那么 tn 的前驱必须是 t 本身。
                return false;   // 违反场景：双向链表断裂
            if (tp != null && t != tp.left && t != tp.right)    // 检查 3：如果 t 有父节点 tp，那 t 必须是 tp 的左孩子或右孩子。
                return false;   // 违反场景：旋转（rotateLeft/rotateRight）后父指针没更新正确。
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) // 检查 4 ：如果左孩子 tl 存在。a:父指针正确；b:左孩子的 hash 不能大于 父节点的 hash
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) // 检查 5：如果右孩子 tr 存在。a:父指针正确；b:右孩子的 hash 不能小于 父节点的 hash。
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red) // 检查 6：红黑树颜色性质（不能有连续红节点）
                return false;
            if (tl != null && !checkInvariants(tl)) // 检查 7 ：递归验证左子树
                return false;
            if (tr != null && !checkInvariants(tr)) // 检查 8 ：递归验证右子树
                return false;
            return true;
        }
    }

}
