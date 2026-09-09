import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CyclicBarrierDemo1 {
    /**
     * 根据品类ID获取商品列表
     *
     * @return
     */
    private static int[] getProductsByCategoryId() {
        // 商品列表编号为从1～10的数字
        return IntStream.rangeClosed(1, 10).toArray();
    }

    /**
     * 商品编号与所对应的价格，当然真实的电商系统中不可能仅存在这两个字段
     */
    private static class ProductPrice {
        private final int prodID;
        private double price;

        private ProductPrice(int prodID) {
            this(prodID, -1);
        }

        private ProductPrice(int prodID, double price) {
            this.prodID = prodID;
            this.price = price;
        }

        int getProdID() {
            return prodID;
        }

        void setPrice(double price) {
            this.price = price;
        }

        @Override
        public String toString() {
            return "ProductPrice{" + "prodID=" + prodID + ", price=" + price + '}';
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 根据商品品类获取一组商品ID
        final int[] products = getProductsByCategoryId();
        // 通过转换将商品编号转换为ProductPrice
        List<ProductPrice> list = Arrays.stream(products).mapToObj(ProductPrice::new).collect(Collectors.toList());
        // 1. 定义CyclicBarrier ，指定parties为子任务数量
        final CyclicBarrier barrier = new CyclicBarrier(list.size());
        // 2.用于存放线程任务的list
        final List<Thread> threadList = new ArrayList<>();
        list.forEach(pp -> {
            Thread thread = new Thread(() -> {
                System.out.println(pp.getProdID() + "开始计算商品价格.");
                try {
                    TimeUnit.SECONDS.sleep(new Random().nextInt(10));
                    if (pp.prodID % 2 == 0) {
                        pp.setPrice(pp.prodID * 0.9D);
                    } else {
                        pp.setPrice(pp.prodID * 0.71D);
                    }
                    System.out.println(pp.getProdID() + "->价格计算完成.");
                } catch (InterruptedException e) {
                    // ignore exception
                } finally {
                    try {
                        // 3.在此等待其他子线程到达barrier point
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                    }
                }
            });
            threadList.add(thread);
            thread.start();
        });
        // 4. 等待所有子任务线程结束
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("所有价格计算完成.");
        list.forEach(System.out::println);
    }
}
