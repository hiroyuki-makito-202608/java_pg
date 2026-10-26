public class Items {
    public Items() {
    }

    public static void main(String[] args) {
        String[] todoItems = new String[] { "牛乳を買う", "", "パンを買う", "掃除をする" };
        boolean[] completedFlags = new boolean[] { true, false, false, false };

        for (int index = 0; index < todoItems.length; ++index) {
            if (!todoItems[index].isEmpty()) {
                String statusPrefix = completedFlags[index] ? "[済] " : "";
                System.out.println("<li>" + statusPrefix + todoItems[index] + "</li>");
            }
        }

    }
}
