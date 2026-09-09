import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CountDownLatchDemo {

    /**
     * 根据品类ID获取商品列表
     *
     * @return
     */
    private static int[] getProductsByCategoryId() {
        // 商品列表编号为从1～10的数字
        return IntStream.rangeClosed(1, 10).toArray();
    }
    // 商品编号与所对应的价格，当然真实的电商系统中不可能仅存在这两个字段
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
        // 首先获取商品编号的列表
        final int[] products = getProductsByCategoryId();
        // 通过stream的map运算将商品编号转换为ProductPrice
        List<ProductPrice> list = Arrays.stream(products).mapToObj(ProductPrice::new).collect(Collectors.toList());
        //1. 定义CountDownLatch，计数器数量为子任务的个数
        final CountDownLatch latch = new CountDownLatch(products.length);
        list.forEach(pp ->
                // 2. 为每一件商品的计算都开辟对应的线程
                new Thread(() -> {
                    System.out.println(pp.getProdID() + "-> 开始计算商品价格.");
                    try {
                        // 模拟其他的系统调用，比较耗时，这里用休眠替代
                        TimeUnit.SECONDS.sleep(new Random().nextInt(10));
                        // 计算商品价格
                        if (pp.prodID % 2 == 0) {
                            pp.setPrice(pp.prodID * 0.9D);
                        } else {
                            pp.setPrice(pp.prodID * 0.71D);
                        }
                        System.out.println(pp.getProdID() + "-> 价格计算完成.");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // 3. 计数器count down，子任务执行完成
                        latch.countDown();
                    }
                }).start());

        // 4.主线程阻塞等待所有子任务结束，如果有一个子任务没有完成则会一直等待
        latch.await();
        System.out.println("所有价格计算完成.");
        list.forEach(System.out::println);
    }
}
