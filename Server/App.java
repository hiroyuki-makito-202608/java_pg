import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder; // 日本語を元の文字に戻すために使います
import java.nio.charset.StandardCharsets; // UTF-8を指定するために使います
import java.sql.Connection; // ★ SQLiteへの接続に使います
import java.sql.DriverManager; // ★ JDBC接続を作るために使います
import java.sql.PreparedStatement; // ★ SQLを安全に実行するために使います
import java.sql.ResultSet; // ★ SELECTの結果を読むために使います
import java.sql.SQLException; // ★ JDBC処理の例外に使います
import java.sql.Statement; // ★ テーブル作成に使います
import java.util.ArrayList; // ★ SELECT結果の一覧を作るために使います
import java.util.List; // ★ Todo一覧を保持するために使います

public class App {
    private static final String DATABASE_URL = "jdbc:sqlite:Server/todos.db"; // ★ SQLiteデータベースの接続先です

    public static void main(String[] args) throws Exception {
        initializeDatabase(); // ★ 起動時にtodos表を用意します
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0); // HTTPサーバーを作ります

        httpServer.createContext("/api/todos", exchange -> { // Todo一覧を返すAPIの入口を作成します。
            if (!exchange.getRequestMethod().equals("GET")) { // GET以外のリクエストを確認します。
                exchange.sendResponseHeaders(405, -1); // GET以外には405を返します。
                exchange.close(); // レスポンスを閉じます。
                return; // ここでリクエスト処理を終了します。
            } // メソッドの確認を終えます。
            try { // Todo一覧の読み込みとJSON生成を行います。
                List<Todo> todos = loadTodos(); // データベースから全Todoを読み込みます。
                StringBuilder json = new StringBuilder("["); // JSON配列の組み立てを始めます。
                for (int i = 0; i < todos.size(); i++) { // Todoを順番にJSONへ追加します。
                    if (i > 0) json.append(","); // 2件目以降の前にカンマを付けます。
                    Todo todo = todos.get(i); // 現在のTodoを取り出します。
                    json.append("{\"title\":\"").append(escapeJsonString(todo.getTitle())) // エスケープしたタイトルを追加します。
                            .append("\",\"done\":").append(todo.isDone()).append("}"); // 完了状態を追加します。
                } // 全Todoの追加を終えます。
                json.append("]"); // JSON配列を閉じます。
                byte[] body = json.toString().getBytes(StandardCharsets.UTF_8); // JSONをUTF-8のバイト列にします。
                exchange.getResponseHeaders().set("Content-Type", "application/json"); // charsetを付けずにContent-Typeを設定します。
                exchange.sendResponseHeaders(200, body.length); // 成功レスポンスと本文の長さを送ります。
                exchange.getResponseBody().write(body); // JSON本文を返します。
            } catch (IOException e) { // Todo一覧の読み込みに失敗した場合を処理します。
                exchange.sendResponseHeaders(500, -1); // 内部エラーを返します。
            } finally { // 成功・失敗のどちらでも実行します。
                exchange.close(); // 接続を閉じます。
            } // レスポンス処理を終えます。
        }); // APIの入口を登録します。
        httpServer.createContext("/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String requestMethod = exchange.getRequestMethod(); // GETやPOSTの種類を取り出します
            String responseMessage;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答をUTF-8のテキストにします
            if (requestPath.equals("/add") && requestMethod.equals("POST")) { // POSTで /add に送られたTodoを受け取ります
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた中身をUTF-8で読みます
                String title = "";
                for (String formField : requestBody.split("&")) {
                    if (formField.startsWith("todo=")) {
                        title = URLDecoder.decode(formField.substring(5), StandardCharsets.UTF_8);
                    }
                }
                if (!title.isEmpty()) { // ★ タイトルが空でなければDBへ追加します
                    addTodo(title); // ★ INSERTでTodoを追加します
                } // ★ 追加処理を終えます
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします
                exchange.sendResponseHeaders(303, -1); // トップページへ移動する返答を送ります
                exchange.close(); // 通信を閉じます
                return; // このリクエストの処理を終えます
            } else if (requestPath.equals("/done") && requestMethod.equals("GET")) { // ★ 完了操作を処理します
                int id = getRequestedId(exchange.getRequestURI().getQuery()); // ★ クエリからTodoのIDを取得します
                if (id > 0) { // ★ 有効なIDの場合に更新します
                    completeTodo(id); // ★ UPDATEでTodoを完了にします
                } // ★ 更新処理を終えます
                exchange.getResponseHeaders().set("Location", "/"); // ★ トップページへ戻します
                exchange.sendResponseHeaders(303, -1); // ★ リダイレクトを返します
                exchange.close(); // ★ 通信を閉じます
                return; // ★ このリクエストの処理を終えます
            } else if (requestPath.equals("/delete") && requestMethod.equals("GET")) { // ★ 削除操作を処理します
                int id = getRequestedId(exchange.getRequestURI().getQuery()); // ★ クエリからTodoのIDを取得します
                if (id > 0) { // ★ 有効なIDの場合に削除します
                    deleteTodo(id); // ★ DELETEでTodoを削除します
                } // ★ 削除処理を終えます
                exchange.getResponseHeaders().set("Location", "/"); // ★ トップページへ戻します
                exchange.sendResponseHeaders(303, -1); // ★ リダイレクトを返します
                exchange.close(); // ★ 通信を閉じます
                return; // ★ このリクエストの処理を終えます
            } else if (requestPath.equals("/")) { // トップページにフォームとTodo一覧を表示します
                String htmlContent = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>my ToDo</title>"
                        + "<style>body { max-width: 600px; margin: 2rem auto; padding: 0 1rem; font-size: 1rem; }</style>"
                        + "</head><body><h1>my ToDo</h1>"
                        + "<form method='post' action='/add'>やること：<input name='todo'>" // ★ DBに保存するタイトルを入力します
                        + "<button>追加</button></form>";
                List<Todo> todos = loadTodos(); // ★ SELECTでDBから最新の一覧を読み込みます
                if (todos.isEmpty()) {
                    htmlContent += "<p>やることは、いまゼロです</p>";
                } else {
                    htmlContent += "<table><tr><th>番号</th><th>やること</th><th>操作</th></tr>"; // ★ DBに保存する項目を表示します
                    for (Todo todo : todos) { // ★ SELECT結果を表示します
                        String mark = todo.isDone() ? " ✔" : ""; // ★ 完了済みの印を付けます
                        htmlContent += "<tr><td>" + todo.getId() + "</td><td>" + todo.getTitle() + mark
                                + "</td><td><a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></td></tr>"; // ★ 操作リンクを付けます
                    } // ★ SELECT結果の表示を終えます
                    htmlContent += "</table>";
                }
                responseMessage = htmlContent + "</body></html>"; // 作ったHTMLを返答にします
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // トップページをUTF-8のHTMLにします
            } else {
                responseMessage = "ページが見つかりません";
            }
            byte[] responseBody = responseMessage.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.getResponseBody().close();
        });httpServer.start();System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");

    }

    private static void initializeDatabase() throws SQLException { // ★ DBへ接続してテーブルを初期化します
        try (Connection connection = DriverManager.getConnection(DATABASE_URL); // ★ SQLiteへ接続します
                Statement statement = connection.createStatement()) { // ★ テーブル作成用のStatementを作ります
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"); // ★
                                                                                                            // todos表を用意します
        }
    } // ★ DB初期化を終えます

    private static void addTodo(String title) throws IOException { // ★ TodoをDBへ追加します
        String sql = "INSERT INTO todos (title, done) VALUES (?, ?)"; // ★ 追加用SQLを用意します
        try (Connection connection = DriverManager.getConnection(DATABASE_URL); // ★ SQLiteへ接続します
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ PreparedStatementを作ります
            statement.setString(1, title); // ★ タイトルを設定します
            statement.setInt(2, 0); // ★ 未完了として設定します
            statement.executeUpdate(); // ★ INSERTを実行します
        } catch (SQLException e) { // ★ DBエラーを処理します
            throw new IOException("Todoの追加に失敗しました", e); // ★ エラーを呼び出し元へ通知します
        } // ★ DB処理を終えます
    } // ★ 追加処理を終えます

    private static void completeTodo(int id) throws IOException { // ★ Todoを完了状態にします
        String sql = "UPDATE todos SET done = 1 WHERE id = ?"; // ★ 完了更新用SQLを用意します
        try (Connection connection = DriverManager.getConnection(DATABASE_URL); // ★ SQLiteへ接続します
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ PreparedStatementを作ります
            statement.setInt(1, id); // ★ 更新対象のIDを設定します
            statement.executeUpdate(); // ★ UPDATEを実行します
        } catch (SQLException e) { // ★ DBエラーを処理します
            throw new IOException("Todoの完了更新に失敗しました", e); // ★ エラーを呼び出し元へ通知します
        } // ★ DB処理を終えます
    } // ★ 完了処理を終えます

    private static void deleteTodo(int id) throws IOException { // ★ TodoをDBから削除します
        String sql = "DELETE FROM todos WHERE id = ?"; // ★ 削除用SQLを用意します
        try (Connection connection = DriverManager.getConnection(DATABASE_URL); // ★ SQLiteへ接続します
                PreparedStatement statement = connection.prepareStatement(sql)) { // ★ PreparedStatementを作ります
            statement.setInt(1, id); // ★ 削除対象のIDを設定します
            statement.executeUpdate(); // ★ DELETEを実行します
        } catch (SQLException e) { // ★ DBエラーを処理します
            throw new IOException("Todoの削除に失敗しました", e); // ★ エラーを呼び出し元へ通知します
        } // ★ DB処理を終えます
    } // ★ 削除処理を終えます

    private static List<Todo> loadTodos() throws IOException { // ★ SELECTでTodo一覧を読み込みます
        List<Todo> todos = new ArrayList<>(); // ★ SELECT結果を入れる一覧を作ります
        String sql = "SELECT id, title, done FROM todos ORDER BY id"; // ★ 一覧取得用SQLを用意します
        try (Connection connection = DriverManager.getConnection(DATABASE_URL); // ★ SQLiteへ接続します
                PreparedStatement statement = connection.prepareStatement(sql); // ★ SELECT用PreparedStatementを作ります
                ResultSet resultSet = statement.executeQuery()) { // ★ SELECTを実行します
            while (resultSet.next()) { // ★ 次の行がある間繰り返します
                todos.add(
                        new Todo(resultSet.getInt("id"), resultSet.getString("title"), resultSet.getInt("done") != 0)); // ★
                                                                                                                        // DBの1行を一覧へ追加します
            }
        } catch (SQLException e) { // ★ DBエラーを処理します
            throw new IOException("Todo一覧の読み込みに失敗しました", e); // ★ エラーを呼び出し元へ通知します
        }
        return todos; // ★ 読み込んだ一覧を返します
    } // ★ 一覧取得を終えます

    private static int getRequestedId(String query) { // ★ リクエストからTodoのIDを取得します
        if (query == null || !query.startsWith("id=") || query.length() <= 3) { // ★ クエリ形式を確認します
            return -1; // ★ IDがない場合は無効値を返します
        } // ★ クエリ形式の確認を終えます
        try { // ★ IDを整数に変換します
            return Integer.parseInt(query.substring(3)); // ★ クエリからIDを返します
        } catch (NumberFormatException e) { // ★ 数値以外のIDを処理します
            return -1; // ★ 不正なIDは無効値として扱います
        } // ★ ID変換を終えます
    } // ★ ID取得を終えます

    private static String escapeJsonString(String value) { // JSON文字列に必要なエスケープを行います。
        StringBuilder escaped = new StringBuilder(); // エスケープ後の文字列を組み立てます。
        for (int i = 0; i < value.length(); i++) { // タイトルの各文字を確認します。
            char c = value.charAt(i); // 現在の文字を取り出します。
            switch (c) { // 特別な意味を持つ文字を判定します。
                case '"':
                    escaped.append("\\\"");
                    break; // 二重引用符をエスケープします。
                case '\\':
                    escaped.append("\\\\");
                    break; // バックスラッシュをエスケープします。
                case '\b':
                    escaped.append("\\b");
                    break; // バックスペースをエスケープします。
                case '\f':
                    escaped.append("\\f");
                    break; // フォームフィードをエスケープします。
                case '\n':
                    escaped.append("\\n");
                    break; // 改行をエスケープします。
                case '\r':
                    escaped.append("\\r");
                    break; // 復帰をエスケープします。
                case '\t':
                    escaped.append("\\t");
                    break; // タブをエスケープします。
                default: // その他の文字を処理します。
                    if (c < 0x20)
                        escaped.append(String.format("\\u%04x", (int) c)); // その他の制御文字をUnicode形式にします。
                    else
                        escaped.append(c); // 通常の文字をそのまま追加します。
            } // 文字ごとの処理を終えます。
        } // 全文字の処理を終えます。
        return escaped.toString(); // エスケープ済み文字列を返します。
    } // JSON文字列のエスケープ処理を終えます。
}

class Todo { // ★ DBから読み込んだTodoを表します
    private final int id; // ★ TodoのIDです
    private final String title; // ★ Todoのタイトルです
    private final boolean done; // ★ 完了状態です

    Todo(int id, String title, boolean done) { // ★ DBの値からTodoを作ります
        this.id = id; // ★ IDを設定します
        this.title = title; // ★ タイトルを設定します
        this.done = done; // ★ 完了状態を設定します
    } // ★ Todoの生成を終えます

    int getId() { // ★ IDを取得します
        return id; // ★ IDを返します
    } // ★ ID取得を終えます

    String getTitle() { // ★ タイトルを取得します
        return title; // ★ タイトルを返します
    } // ★ タイトル取得を終えます

    boolean isDone() { // ★ 完了状態を取得します
        return done; // ★ 完了状態を返します
    } // ★ 完了状態取得を終えます
} // ★ Todoクラスを終えます

