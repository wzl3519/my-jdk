import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 实现商品服务接口限流
 */
public class SemaphoreDemo {

    /**
     * 同一时刻最多只允许有两个并发
     */
    private static Semaphore semaphore = new Semaphore(2);
    private static Executor executor = Executors.newFixedThreadPool(10);
    public static void main(String[] args) {
        for(int i=0;i<10;i++){
            executor.execute(()->getProductInfo());
        }
    }
    public static String getProductInfo() {
        try {
            semaphore.acquire();
            System.out.println(Thread.currentThread() +"请求服务");
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            semaphore.release();
            System.out.println(Thread.currentThread() +"释放服务");
        }
        return "返回商品详情信息";
    }

    public static String getProductInfo2() {

        if(!semaphore.tryAcquire()){
            System.out.println("请求被流控了");
            return "请求被流控了";
        }
        try {
            System.out.println("请求服务");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            semaphore.release();
            System.out.println("释放服务");
        }
        return "返回商品详情信息";
    }




}
