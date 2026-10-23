import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.ArrayList; // Todoの一覧を作るために使います
import java.util.List; // Todoの一覧を入れる型です

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答をUTF-8のテキストにします
            if (path.equals("/hello")) { // パスが /hello か比べます
                String query = exchange.getRequestURI().getRawQuery(); // URLの ? 以降を取り出します
                String name = query == null ? "ゲスト" : query.substring(5); // queryがnullならゲスト、そうでなければname=の後ろを使います
                message = "こんにちは、" + name + "さん！"; // 名前をあいさつに加えます
            } else if (path.equals("/todos")) { // /todos のときの処理です
                List<String> todos = new ArrayList<>(); // Todoを入れる一覧を作ります
                todos.add("牛乳を買う"); // 1件目のTodoを追加します
                todos.add("卵を買う"); // 2件目のTodoを追加します
                todos.add("パンを買う"); // 3件目のTodoを追加します
                String html = "<ul>"; // HTMLのリストを開始します
                for (String todo : todos) { // Todoを1件ずつ取り出します
                    html += "<li>" + todo + "</li>"; // TodoをHTMLの項目として加えます
                } // TodoをすべてHTMLに加え終わりました
                html += "</ul>"; // HTMLのリストを閉じます
                message = html; // 組み立てたHTMLを返答にします
                // exchange.getResponseHeaders().set("Content-Type", "text/plain;
                // charset=UTF-8"); // 通常の返答をUTF-8のテキストにします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // /todos
                                                                                               // の返答をUTF-8のHTMLにします

            } else if (path.equals("/bye")) { // パスが /bye か比べます
                message = "さようなら！";
            } else if (path.equals("/items")) { // パスが /items か比べます
                message = "牛乳を買う\nパンを買う\n掃除をする\n";
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}
