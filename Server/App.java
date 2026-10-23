import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {// 【1】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);// 【1】
        server.createContext("/", exchange -> {// 【毎】
            System.out.println("ハンドラが動いた");
            String message = "Hello, 世界。こんにちは";// 【毎】
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");// 【毎】
            byte[] body = message.getBytes("UTF-8");// 【毎】
            exchange.sendResponseHeaders(200, body.length);// 【毎】
            exchange.getResponseBody().write(body);// 【毎】
            exchange.getResponseBody().close();// 【毎】

        });
        server.start();
        System.out.println("サーバー起動: http: http://localhost:8080 （止めるときは Ctrl+C）"); // 【1】
    }
}
