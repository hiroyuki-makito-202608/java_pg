import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder; // 日本語を元の文字に戻すために使います
import java.nio.charset.StandardCharsets; // UTF-8を指定するために使います
import java.util.ArrayList; // Todoの一覧を作るために使います
import java.util.List; // Todoの一覧を入れる型です

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        List<String> todos = new ArrayList<>(); // 追加されたTodoを保存する空の一覧です

        httpServer.createContext("/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String requestMethod = exchange.getRequestMethod(); // GETやPOSTの種類を取り出します
            String responseMessage;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答をUTF-8のテキストにします
            if (requestPath.equals("/add") && requestMethod.equals("POST")) { // POSTで /add に送られたTodoを受け取ります
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた中身をUTF-8で読みます
                String encodedTodo = requestBody.substring(5); // todo= の5文字を取り除きます
                String todoItem = URLDecoder.decode(encodedTodo, StandardCharsets.UTF_8); // URL形式の日本語を元に戻します
                if (!todoItem.isEmpty()) { // 入力が空でないときだけ追加します
                    todos.add(todoItem); // Todoを一覧に追加します
                } // 空でないTodoの追加を終えます
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします
                exchange.sendResponseHeaders(303, -1); // トップページへ移動する返答を送ります
                exchange.close(); // 通信を閉じます
                return; // このリクエストの処理を終えます
            } else if (requestPath.equals("/")) { // トップページにフォームとTodo一覧を表示します
                String htmlContent = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // Todo追加フォームと一覧を作ります
                for (String todoItem : todos) { // Todoを1件ずつ取り出します
                    htmlContent += "<li>" + todoItem + "</li>"; // Todoを一覧に加えます
                } // Todoをすべて一覧に加え終わります
                htmlContent += "</ul>"; // 一覧を閉じます
                responseMessage = htmlContent; // 作ったHTMLを返答にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // トップページをUTF-8のHTMLにします
            } else {
                responseMessage = "ページが見つかりません";
            }
            byte[] responseBody = responseMessage.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.getResponseBody().close();
        });
        httpServer.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}
