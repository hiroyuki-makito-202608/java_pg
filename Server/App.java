import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder; // 日本語を元の文字に戻すために使います
import java.nio.charset.StandardCharsets; // UTF-8を指定するために使います
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList; // Todoの一覧を作るために使います
import java.util.List; // Todoの一覧を入れる型です

public class App {
    static List<Todo> todos = new ArrayList<>(); // ★変更
    static int nextId = 1; // ★変更

    public static void main(String[] args) throws Exception {
        load(); // 起動時にtodos.csvからTodo一覧を読み込みます
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);

        httpServer.createContext("/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します
            String requestMethod = exchange.getRequestMethod(); // GETやPOSTの種類を取り出します
            String responseMessage;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答をUTF-8のテキストにします
            if (requestPath.equals("/add") && requestMethod.equals("POST")) { // POSTで /add に送られたTodoを受け取ります
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた中身をUTF-8で読みます
                String title = "";
                String dueDateValue = "";
                for (String formField : requestBody.split("&")) {
                    if (formField.startsWith("todo=")) {
                        title = URLDecoder.decode(formField.substring(5), StandardCharsets.UTF_8);
                    } else if (formField.startsWith("dueDate=")) {
                        dueDateValue = URLDecoder.decode(formField.substring(8), StandardCharsets.UTF_8);
                    }
                }
                if (!title.isEmpty()) { // ★変更
                    LocalDate dueDate = dueDateValue.isEmpty() ? null : LocalDate.parse(dueDateValue);
                    todos.add(new Todo(nextId, title, dueDate));
                    nextId++;
                    save(); // Todoを追加した一覧をtodos.csvへ保存します
                } // ★変更
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします
                exchange.sendResponseHeaders(303, -1); // トップページへ移動する返答を送ります
                exchange.close(); // 通信を閉じます
                return; // このリクエストの処理を終えます
            } else if (requestPath.equals("/done") && requestMethod.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        for (Todo todo : todos) { // ★追加
                            if (todo.getId() == id) { // ★追加
                                todo.setDone(true); // ★追加
                                save(); // 完了状態を変更した一覧をtodos.csvへ保存します
                                break; // ★追加
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                return; // ★追加
            } else if (requestPath.equals("/delete") && requestMethod.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        boolean removed = todos.removeIf(todo -> todo.getId() == id); // ★追加
                        if (removed) { // 削除で一覧が変わった場合だけ保存します
                            save(); // 削除後の一覧をtodos.csvへ保存します
                        } // 削除がなければ保存しません
                    } catch (NumberFormatException e) { // ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                return; // ★追加
            } else if (requestPath.equals("/")) { // トップページにフォームとTodo一覧を表示します
                String htmlContent = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>my ToDo</title>"
                        + "<style>body { max-width: 600px; margin: 2rem auto; padding: 0 1rem; font-size: 1rem; }</style>"
                        + "</head><body><h1>my ToDo</h1>"
                        + "<form method='post' action='/add'>やること：<input name='todo'><br>"
                        + "<label>実行期限日：<input type='date' name='dueDate'></label>"
                        + "<button>追加</button></form>";
                if (todos.isEmpty()) {
                    htmlContent += "<p>やることは、いまゼロです</p>";
                } else {
                    htmlContent += "<table><tr><th>番号</th><th>やること</th><th>設定日</th><th>実行期限日</th><th>操作</th></tr>";
                    for (Todo todo : todos) { // ★変更
                        String mark = ""; // ★変更
                        if (todo.isDone()) { // ★変更
                            mark = " ✔"; // ★変更
                        } // ★変更
                        htmlContent += "<tr><td>" + todo.getId() + "</td><td>" + todo.getTitle() + mark
                                + "</td><td>" + todo.getSetDate() + "</td><td>" + todo.getDueDate()
                                + "</td><td><a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></td></tr>"; // ★追加
                    } // Todoをすべて一覧に加え終わります
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
        });
        httpServer.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }

    static void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Todo todo : todos) {
            String title = todo.getTitle();
            if (title.contains(",") || title.contains("\"")) {
                title = "\"" + title.replace("\"", "\"\"") + "\"";
            }
            lines.add(todo.getId() + "," + (todo.isDone() ? "1" : "0") + "," + title);
        }
        Files.write(Path.of("todos.csv"), lines, StandardCharsets.UTF_8);
    }

    static void load() throws IOException {
        Path path = Path.of("todos.csv");
        if (!Files.exists(path)) {
            return;
        }

        int maxId = 0;
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            int firstComma = line.indexOf(',');
            int secondComma = line.indexOf(',', firstComma + 1);
            if (firstComma < 0 || secondComma < 0) {
                throw new IOException("todos.csvの形式が正しくありません: " + line);
            }

            int id = Integer.parseInt(line.substring(0, firstComma));
            String doneValue = line.substring(firstComma + 1, secondComma);
            if (!doneValue.equals("0") && !doneValue.equals("1")) {
                throw new IOException("todos.csvの完了状態が正しくありません: " + line);
            }
            String title = line.substring(secondComma + 1);
            if (title.startsWith("\"")) {
                if (title.length() < 2 || !title.endsWith("\"")) {
                    throw new IOException("todos.csvのタイトルが正しくありません: " + line);
                }
                title = title.substring(1, title.length() - 1).replace("\"\"", "\"");
            }

            Todo todo = new Todo(id, title, null);
            todo.setDone(doneValue.equals("1"));
            todos.add(todo);
            maxId = Math.max(maxId, id);
        }
        nextId = maxId + 1;
    }
}

class Todo { // ★変更
    private final int id; // ★変更
    private final String title; // ★変更
    private final LocalDate setDate;
    private final LocalDate dueDate;
    private boolean done; // ★変更

    Todo(int id, String title, LocalDate dueDate) {
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.setDate = LocalDate.now();
        this.dueDate = dueDate == null ? setDate.plusDays(7) : dueDate;
        this.done = false; // ★変更
    } // ★変更

    int getId() { // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更
        return title; // ★変更
    } // ★変更

    LocalDate getSetDate() {
        return setDate;
    }

    LocalDate getDueDate() {
        return dueDate;
    }

    boolean isDone() { // ★変更
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // ★変更
        this.done = done; // ★変更
    } // ★変更
} // ★変更
