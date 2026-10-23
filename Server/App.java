import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String message;
            if (path.equals("/hello")) { // パスが /hello か比べます
                String query = exchange.getRequestURI().getRawQuery(); // URLの ? 以降を取り出します
                String name = query == null ? "ゲスト" : query.substring(5); // queryがnullならゲスト、そうでなければname=の後ろを使います
                message = "こんにちは、" + name + "さん！"; // 名前をあいさつに加えます
            } else if (path.equals("/bye")) { // パスが /bye か比べます
                message = "さようなら！";
            } else if (path.equals("/items")) { // パスが /items か比べます
                message = "牛乳を買う\nパンを買う\n掃除をする\n";
            } else {
                message = "ページが見つかりません";
            }
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            byte[] body = message.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}
