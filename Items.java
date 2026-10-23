public class Items {
    public Items() {
    }

    public static void main(String[] var0) {
        String[] var1 = new String[] { "牛乳を買う", "", "パンを買う", "掃除をする" };
        boolean[] var2 = new boolean[] { true, false, false, false };

        for (int var3 = 0; var3 < var1.length; ++var3) {
            if (!var1[var3].isEmpty()) {
                String var4 = var2[var3] ? "[済] " : "";
                System.out.println("<li>" + var4 + var1[var3] + "</li>");
            }
        }

    }
}
