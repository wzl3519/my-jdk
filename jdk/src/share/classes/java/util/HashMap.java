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
import java.io.*;
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
 * <p>As a general rule, the default load factor (.75) offers a good tradeoff
 * between time and space costs.  Higher values decrease the space overhead
 * but increase the lookup cost (reflected in most of the operations of the
 * <tt>HashMap</tt> class, including <tt>get</tt> and <tt>put</tt>).  The
 * expected number of entries in the map and its load factor should be taken
 * into account when setting its initial capacity, so as to minimize the
 * number of rehash operations.  If the initial capacity is greater
 * than the maximum number of entries divided by the load factor, no
 * rehash operations will ever occur.
 *
 * <p>If many mappings are to be stored in a <tt>HashMap</tt> instance,
 * creating it with a sufficiently large capacity will allow the mappings to
 * be stored more efficiently than letting it perform automatic rehashing as
 * needed to grow the table.
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

public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable
{

    /**
     * 默认初始容量（16） 必须是 2 的幂
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大容量 （即 2 的 30 次方）
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认负载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 共享空数组实例，所有新 HashMap 共用
     */
    static final Entry<?,?>[] EMPTY_TABLE = {};

    /**
     * 底层哈希桶数组，真正存数据的地方 （长度必须始终是2的幂）
     */
    transient Entry<?,?>[] table = EMPTY_TABLE;

    /**
     * 当前键值对数量
     */
    transient int size;

    /**
     * 扩容阈值
     */
    int threshold;

    /**
     * 实际使用的负载因子，构造后不可变
     */
    final float loadFactor;

    /**
     * 结构修改次数（fail-fast 机制）
     */
    transient int modCount;

    /**
     * 启用 alternative hashing 的阈值
     */
    static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;

    /**
     * 用静态内部类 Holder 延迟初始化，确保 JVM 完全启动后才读取系统属性
     */
    private static class Holder {

        /**
         * 作为是否启用 alternative hashing 的阈值
         */
        static final int ALTERNATIVE_HASHING_THRESHOLD;

        static {
            // 用 doPrivileged 读取系统属性 （—绕过调用者权限限制，确保能读到系统属性—
            String altThreshold = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "jdk.map.althashing.threshold"));

            int threshold;
            try { // 2. 解析阈值
                threshold = (null != altThreshold)
                        ? Integer.parseInt(altThreshold)
                        : ALTERNATIVE_HASHING_THRESHOLD_DEFAULT;

                // 3. 特殊处理 -1
                if (threshold == -1) {
                    threshold = Integer.MAX_VALUE;
                }

                if (threshold < 0) { // 4. 参数校验
                    throw new IllegalArgumentException("value must be positive integer.");
                }
            } catch(IllegalArgumentException failed) {
                throw new Error("Illegal value for 'jdk.map.althashing.threshold'", failed);
            }

            ALTERNATIVE_HASHING_THRESHOLD = threshold;
        }
    }

    /**
     * A randomizing value associated with this instance that is applied to
     * hash code of keys to make hash collisions harder to find. If 0 then
     * alternative hashing is disabled.
     */
    transient int hashSeed = 0;

    /**
     * 创建指定容量和负载系数的实例
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);

        this.loadFactor = loadFactor;
        threshold = initialCapacity;
        init(); // 子类用
    }

    /**
     * 创建指定容量和默认负载系数（0.75）的实例
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 创建默认容量（16）和默认负载系数（0.75）的实例
     */
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 创建已有数据的实例
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        // 根据待拷贝 Map 的大小，反推一个"刚好够用且不触发扩容"的容量。
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        inflateTable(threshold); // 初始化底层数组

        putAllForCreate(m); // 批量插入
    }

    static int roundUpToPowerOf2(int number) {
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
        // highestOneBit(n) 计算的是比 n 小的最近的 2 的幂，而我们要的是大于等于 n 的最小 2 的幂。
        // 这样的话，需要通过返回值判断是否需要翻倍（有了if-else 分支），
        // 而现在用 (number - 1) << 1，把“是否翻倍”的判断转化成了“输入数值大小”的变化（消除了 if-else 分支，让代码变成纯粹的顺序位运算。），
        // 从而让 highestOneBit 内部统一的逻辑自动给出了正确结果。
    }

    /**
     * 初始化底层数组
     */
    private void inflateTable(int toSize) {
        // 1. 计算初始化容量 （必须为 2 的幂次方）
        int capacity = roundUpToPowerOf2(toSize); // 向上取整到最近的 2 的幂
        // 2. 计算扩容阈值
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry<?,?>[capacity]; // 3. 创建底层数组
        initHashSeedAsNeeded(capacity); // 4. 初始化哈希种子 （这个种子会参与 hash() 函数的扰动计算，目的是防止哈希碰撞拒绝服务攻击）
    }

    // internal utilities

    /**
     * Initialization hook for subclasses. This method is called
     * in all constructors and pseudo-constructors (clone, readObject)
     * after HashMap has been initialized but before any entries have
     * been inserted.  (In the absence of this method, readObject would
     * require explicit knowledge of subclasses.)
     */
    void init() {
    }

    /**
     * 初始化生成 随即种子
     * put 触发 inflateTable 扩容时调用
     */
    final boolean initHashSeedAsNeeded(int capacity) {
        // 1. 判断当前状态是否开启
        boolean currentAltHashing = hashSeed != 0; // hashSeed != 0 说明当前已经启用了替代哈希（之前已经生成过随机种子）。
        // 2. 判断目标状态是否开启
        boolean useAltHashing = sun.misc.VM.isBooted() && // 确保 JVM 已经启动完成
                (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD); // 当前容量（或即将扩容到的容量）达到了设定的阈值（系统参数控制）。
        // 3. 核心妙笔：异或判断状态切换 （只有当“当前状态”和“目标状态”不一致时，switching 才为 true。）
        boolean switching = currentAltHashing ^ useAltHashing; //（一种非常优雅的无分支状态机写法，避免了 if-else 的冗长判断。）
        if (switching) {
            hashSeed = useAltHashing
                ? sun.misc.Hashing.randomHashSeed(this) // 开启，生成一个随机种子
                : 0; // 关闭
        }

        return switching;
    }

    /**
     * 计算hash值
     */
    final int hash(Object k) {
        // 1. 随机种子与 String 特殊处理
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k); // String 特殊哈希
        }

        // 2. 初始混合
        /* 如果 hashSeed == 0（未启用替代哈希），h 初始为 0，0 ^ k.hashCode() 就是 k.hashCode() 本身。
           如果 hashSeed != 0，则 hashSeed 先与 hashCode 异或，把随机性混入。 */
        h ^= k.hashCode();

        // 3. 二次散列（扰动）函数  （防碰撞（把哈希值打散）。） ^的规则：相同为 0，不同为 1
       /* 右移（>>>）：相当于把哈希值的“高层信息”往“低层”搬。
          异或（^）：相当于把搬下来的“高层信息”和原来的“低层信息”搅拌在一起。
          多次重复：JDK 7 觉得搅一次不够，所以连续搅了 2 轮（4次移位，5次异或），确保高位的信息彻底渗透到底层。 */
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * 根据键的哈希值 h 和哈希表容量 length，计算出该键值对应该放在哪个桶（数组下标）里。
     * h & (length - 1) 等价于 h % length;
     */
    static int indexFor(int h, int length) {
        // assert Integer.bitCount(length) == 1 : "length must be a non-zero power of 2";
        return h & (length-1); // 一、 核心原理：位运算取模
        //h & (length - 1) 的效果是： 把 h 二进制中高于 length-1 的所有位全部清零，只保留最低的 log2(length) 位。这恰好就是 h 除以 length 的余数。
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
     * 通过 key 查询 value
     *   key 不存在，返回 null；
     *   key 存在，但 value 就是 null（即 put(key, null)），返回null
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (key == null)
            return (V)getForNullKey(); // null key 的特殊处理
        Entry<K,V> entry = getEntry(key); // 正常 key 的查找

        return null == entry ? null : entry.getValue();
    }


    private Object getForNullKey() {
        if (size == 0) {
            return null;
        }
        // 遍历 table[0] 的链表
        for (Entry<?,?> e = table[0]; e != null; e = e.next) {
            if (e.key == null) // null key 用 == 比较（只有 null == null）
                return e.value;
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
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the
     * HashMap.  Returns null if the HashMap contains no mapping
     * for the key.
     */
    @SuppressWarnings("unchecked")
    final Entry<K,V> getEntry(Object key) {
        if (size == 0) {
            return null;
        }

        int hash = (key == null) ? 0 : hash(key); // 计算哈希
        // 3. 定位桶 + 遍历链表
        for (Entry<?,?> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            // 4. 三重比较
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k))))
                return (Entry<K,V>)e;
        }
        return null;
    }

    /**
     * 如果是新增（key 不存在），直接放入。
     * 如果是更新（key 已存在），覆盖旧值，并返回旧值。
     */
    public V put(K key, V value) {
        // 1.延迟初始化（懒加载）
        if (table == EMPTY_TABLE) { // 如果底层数组还是默认的空表
            inflateTable(threshold); // 根据阈值（threshold）初始化底层数组（table）
        }
        // 2.对 null Key 的特殊处理
        if (key == null)
            return putForNullKey(value);
        // 3.计算哈希与定位桶（Bucket）
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)table[i]; // 对应桶中获取链表
        // 4.遍历链表，查找是否已存在该 Key
        for(; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value; // 获取旧值
                e.value = value;  //赋新值
                e.recordAccess(this); // 是一个空方法，专门留给 LinkedHashMap 等子类重写，用来实现 LRU 缓存淘汰策略。
                return oldValue;
            }
        }
        // 5.插入新节点
        modCount++; // 是 HashMap 的快速失败（fail-fast）机制，用于在迭代时防止并发修改。
        addEntry(hash, key, value, i); // 以头插法的方式插入到链表的头部
        return null;
    }

    /**
     * Offloaded version of put for null keys
     */
    private V putForNullKey(V value) {
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)table[0]; // 桶（下标为 0 ）
        for(; e != null; e = e.next) { // 查找是否已存在该 null Key
            if (e.key == null) {
                V oldValue = e.value; // 获取旧值
                e.value = value;  // 更新新值
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
        addEntry(0, null, value, 0); // 以头插法的方式插入到链表的头部
        return null;
    }

    /**
     * 在"已知不会触发扩容"的前提下，往 HashMap 中插入一个键值对
     */
    private void putForCreate(K key, V value) {
        // 1. 计算哈希和定位桶
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        // 2. 遍历链表查重
        for (@SuppressWarnings("unchecked")
             Entry<?,V> e = (Entry<?,V>)table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value; // 找到相同 key，直接替换 value
                return; // 直接返回，不插入新节点
            }
        }
        // 3. 没找到 → 创建新节点
        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    void resize(int newCapacity) { // 入参：新容量
        Entry<?,?>[] oldTable = table;
        int oldCapacity = oldTable.length; // 旧容量
        // 1. 容量已达上限的"兜底"
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return; // 不再创建新数组 （再也不扩容）
        }
        // 2. 创建新数组
        Entry<?,?>[] newTable = new Entry<?,?>[newCapacity];
        // 3. 迁移数据（核心）
        transfer(newTable, initHashSeedAsNeeded(newCapacity));
        // 4. 切换引用 + 更新阈值
        table = newTable;  // 切换 新数组
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);  // 计算 新扩容阈值
    }

    /**
     * 迁移数据 （遍历旧数组的每一个桶、每一条链表，把每个 Entry 重新算位置，用头插法挂到新数组的对应桶里。如果 rehash 为 true，顺便重新计算哈希值。）
     * 迁移完成后，链表完全反转了！（假设 新哈希后还是同一桶）
     * 旧桶:  A → B → C → null
     * 新桶:  C → B → A → null
     */
    @SuppressWarnings("unchecked")
    void transfer(Entry<?,?>[] newTable, boolean rehash) {
        Entry<?,?>[] src = table; // 获取 旧数据
        int newCapacity = newTable.length;
        // 1. 外层：遍历旧数组的每个桶
        for (int j = 0; j < src.length; j++) {
            Entry<K,V> e = (Entry<K,V>)src[j]; // 获取对应桶上的链表数据
            // 2. 内层：遍历桶里的链表
            while(null != e) {
                Entry<K,V> next = e.next;
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key); // 3. 按需重新哈希
                }
                int i = indexFor(e.hash, newCapacity); // 4. 重新定位桶下标
                // 5. 头插法插入新数组（核心！），也是 JDK 7 并发死循环的根源。O(1) 完成。
                e.next = (Entry<K,V>)newTable[i];
                newTable[i] = e;
                e = next; // 6. 推进到下一个节点
            }
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
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        if (table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        /*
         * Expand the map if the map if the number of mappings to be added
         * is greater than or equal to threshold.  This is conservative; the
         * obvious condition is (m.size() + size) >= threshold, but this
         * condition could result in a map with twice the appropriate capacity,
         * if the keys to be added overlap with the keys already in this map.
         * By using the conservative calculation, we subject ourself
         * to at most one extra resize.
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
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
        Entry<K,V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the HashMap.  Returns null if the HashMap contains no mapping
     * for this key.
     */
    final Entry<K,V> removeEntryForKey(Object key) {
        if (size == 0) {
            return null;
        }
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        @SuppressWarnings("unchecked")
            Entry<K,V> prev = (Entry<K,V>)table[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            Object k;
            if (e.hash == hash &&
                ((k = e.key) == key || (key != null && key.equals(k)))) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet using {@code Map.Entry.equals()}
     * for matching.
     */
    final Entry<K,V> removeMapping(Object o) {
        if (size == 0 || !(o instanceof Map.Entry))
            return null;

        Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
        Object key = entry.getKey();
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        @SuppressWarnings("unchecked")
            Entry<K,V> prev = (Entry<K,V>)table[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            if (e.hash == hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        modCount++;
        Arrays.fill(table, null);
        size = 0;
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
        if (value == null)
            return containsNullValue();

        Entry<?,?>[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry<?,?> e = tab[i] ; e != null ; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * Special-case code for containsValue with null argument
     */
    private boolean containsNullValue() {
        Entry<?,?>[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry<?,?> e = tab[i] ; e != null ; e = e.next)
                if (e.value == null)
                    return true;
        return false;
    }

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        HashMap<K,V> result = null;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // assert false;
        }
        if (result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min(
                (int) Math.min(
                    size * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    HashMap.MAXIMUM_CAPACITY),
               table.length));
        }
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);

        return result;
    }

    /**
     * 链表节点类 Entry
     */
    static class Entry<K,V> implements Map.Entry<K,V> {
        final K key; // 键 —— final，创建后不可变
        V value;    // 值 —— 可变，put 时会被替换
        Entry<K,V> next;    // 指向下一个节点（链表指针）
        int hash;   // 缓存 key 的哈希值，避免重复计算

        /**
         * Creates new entry.
         */
        Entry(int h, K k, V v, Entry<K,V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }

        /**
         * 留给子类的钩子 （put 覆盖旧值时调用）
         */
        void recordAccess(HashMap<K,V> m) {
        }

        /**
         * 留给子类的钩子 （remove 时调用）
         */
        void recordRemoval(HashMap<K,V> m) {
        }
    }

    /**
     * 往指定桶里塞一个新键值对。
     * 塞之前先检查：如果"已经够满了"且"这个桶里已经有人了"，就先扩容翻倍、重新算位置，然后再插入。
     */
    void addEntry(int hash, K key, V value, int bucketIndex) {
        // 1. 扩容判断
        if ((size >= threshold) &&  // 当前元素总数 ≥ 扩容阈值
                (null != table[bucketIndex])) { // 要插入的这个桶里已经有元素了
            resize(2 * table.length); // 2. 扩容翻倍
            // 3. 扩容后重新哈希 + 重新定位
            hash = (null != key) ? hash(key) : 0;  // 可能切换了 hashSeed，所以需要重新计算
            bucketIndex = indexFor(hash, table.length);    // 依赖 table.length，所以也需要重新计算
        }
        // 4. 正式插入 （头插法）
        createEntry(hash, key, value, bucketIndex);
    }

    /**
     * 往指定桶里插一个新节点（头插法），不做任何扩容检查，size++，完事。
     */
    void createEntry(int hash, K key, V value, int bucketIndex) {
        @SuppressWarnings("unchecked")
            Entry<K,V> e = (Entry<K,V>)table[bucketIndex]; // 拿到当前桶的头节点
        table[bucketIndex] = new Entry<>(hash, key, value, e); // 新节点挂载到头部
        size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<?,?> next;        // next entry to return
        int expectedModCount;   // For fast-fail
        int index;              // current slot
        Entry<?,?> current;     // current entry

        HashIterator() {
            expectedModCount = modCount;
            if (size > 0) { // advance to first entry
                Entry<?,?>[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        @SuppressWarnings("unchecked")
        final Entry<K,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry<?,?> e = next;
            if (e == null)
                throw new NoSuchElementException();

            if ((next = e.next) == null) {
                Entry<?,?>[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            current = e;
            return (Entry<K,V>)e;
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Object k = current.key;
            current = null;
            HashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }
    }

    private final class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private final class EntryIterator extends HashIterator<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<K> newKeyIterator()   {
        return new KeyIterator();
    }
    Iterator<V> newValueIterator()   {
        return new ValueIterator();
    }
    Iterator<Map.Entry<K,V>> newEntryIterator()   {
        return new EntryIterator();
    }


    // Views

    private transient Set<Map.Entry<K,V>> entrySet = null;

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
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return newKeyIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            return HashMap.this.removeEntryForKey(o) != null;
        }
        public void clear() {
            HashMap.this.clear();
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
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return newValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            HashMap.this.clear();
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
        return entrySet0();
    }

    private Set<Map.Entry<K,V>> entrySet0() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return newEntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Entry<K,V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }
        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }
        public int size() {
            return size;
        }
        public void clear() {
            HashMap.this.clear();
        }
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
        throws IOException
    {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        if (table==EMPTY_TABLE) {
            s.writeInt(roundUpToPowerOf2(threshold));
        } else {
           s.writeInt(table.length);
        }

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        if (size > 0) {
            for(Map.Entry<K,V> e : entrySet0()) {
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    private static final long serialVersionUID = 362498820763181265L;

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
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

        // set other fields that need values
        table = EMPTY_TABLE;

        // Read in number of buckets
        s.readInt(); // ignored.

        // Read number of mappings
        int mappings = s.readInt();
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " + mappings);

        // capacity chosen by number of mappings and desired load (if >= 0.25)
        int capacity = (int) Math.min(
                    mappings * Math.min(1 / loadFactor, 4.0f),
                    // we have limits...
                    HashMap.MAXIMUM_CAPACITY);

        // allocate the bucket array;
        if (mappings > 0) {
            inflateTable(capacity);
        } else {
            threshold = capacity;
        }

        init();  // Give subclass a chance to do its thing.


        // Check Map.Entry[].class since it's the nearest public type to
        // what we're actually creating.
        SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, capacity);

        // Read the keys and values, and put the mappings in the HashMap
        for (int i = 0; i < mappings; i++) {
            @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
            @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
            putForCreate(key, value);
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

    // These methods are used when serializing HashSets
    int   capacity()     { return table.length; }
    float loadFactor()   { return loadFactor;   }
}
