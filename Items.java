public class Items { // クラス（プログラムをまとめる名前）を Items にします
    public static void main(String[] args) { // プログラムの実行開始場所です
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" }; // Todo（やること）の文字列を配列（複数の値をまとめる入れ物）に入れます
        boolean[] done = { true, false, false, false }; // 各Todoが済んだかどうかを同じ順番で記録します
        for (int i = 0; i < todos.length; i++) { // 配列の先頭（0番目）から最後まで順に繰り返します

            if (!todos[i].isEmpty()) { // 空文字列（文字が入っていない項目）でなければ出力します
                String mark = done[i] ? "[済] " : ""; // 済んでいる場合だけ付ける印を決めます
                System.out.println("<li>" + mark + todos[i] + "</li>"); // 印とTodoを<li>と</li>で囲んで1行出力します
            } // if文（条件による分岐）の終わりです

        } // for文（繰り返し処理）の終わりです
    } // mainメソッド（実行開始場所）の終わりです
} // Itemsクラスの終わりです
