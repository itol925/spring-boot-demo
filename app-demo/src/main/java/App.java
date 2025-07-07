import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String filePath = "/Users/panyinglong/Desktop/orderid.csv"; // 文件路径
        long start = 1000167032629719L;
        int rows = 120000;
        String title = "\"orderid\"";
        writeNumberSequence(filePath, title, start, rows);
        System.out.println("文件生成完成！");
    }

    public static void writeNumberSequence(String filePath, String title, long start, int rows) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(String.valueOf(title));
            writer.newLine(); // 写入换行符
            for (long n = start; n <= start + rows; n++) {
                writer.write(String.valueOf(n));
                writer.newLine(); // 写入换行符

                // 每100行打印一次进度（可选）
                if (n % 100 == 0) {
                    System.out.println("已写入 " + (n-start) + " 行");
                }
            }
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
