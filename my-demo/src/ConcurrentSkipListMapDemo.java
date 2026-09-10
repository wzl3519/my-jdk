import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListMapDemo {
    public static void main(String[] args) {
        ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<>();

        // 添加元素
        map.put(1, "a");
        map.put(3, "c");
        map.put(2, "b");
        map.put(4, "d");

        // 获取元素
        String value1 = map.get(2);
        System.out.println(value1); // 输出：b

        // 遍历元素
        for (Integer key : map.keySet()) {
            String value = map.get(key);
            System.out.println(key + " : " + value);
        }

        // 删除元素
        String value2 = map.remove(3);
        System.out.println(value2); // 输出：c
    }
}
