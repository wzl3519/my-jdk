import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class DelayQueueDemo {


    public static void main(String[] args) throws InterruptedException {


        Message item1 = new Message("消息1",5, TimeUnit.SECONDS);
        Message item2 = new Message("消息2",10, TimeUnit.SECONDS);
        Message item3 = new Message("消息3",15, TimeUnit.SECONDS);

        DelayQueue<Message> queue = new DelayQueue<>();
        queue.put(item1);
        queue.put(item2);
        queue.put(item3);
        System.out.println(printDate() + "开始");
        for (int i= 0; i<3; i++) {
            Message take = queue.take();
            System.out.format(printDate() + " 消息出队,属性name=%s%n", take.name);
        }
        System.out.println(printDate()+ "结束");
// 错误写法：foreach 里直接调用集合的 remove
        for (Message x : queue) {
                queue.remove(2); // modCount++，但迭代器的 expectedModCount 没更新

        }

    }

    public static String printDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.format(new Date()) + " ";
    }

}
