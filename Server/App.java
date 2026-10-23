import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための読み込み
import java.net.InetSocketAddress; // 待ち受け先のアドレスを使うための読み込み

public class App { // Appという名前のプログラムを定義
    public static void main(String[] args) throws Exception { // プログラムの開始位置を定義
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートで待ち受けるサーバーを作成
        server.createContext("/", exchange -> { // / にアクセスされたときの処理を設定
            String message = "Hello, 世界。こんにちは"; // ブラウザに返す文字を用意
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す文字の種類と文字コードを指定
            byte[] body = message.getBytes("UTF-8"); // 返す文字をUTF-8のデータに変換
            exchange.sendResponseHeaders(200, body.length); // 正常を表す番号とデータの長さを送信
            exchange.getResponseBody().write(body); // ブラウザへ文字を送信
            exchange.getResponseBody().close(); // 送信を終了
        }); // / の処理設定を終了
        server.start(); // サーバーの待ち受けを開始
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示
    } // mainメソッドを終了
} // Appクラスを終了
