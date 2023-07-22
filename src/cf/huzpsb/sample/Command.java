package cf.huzpsb.sample;

import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;

import java.util.Random;

public class Command implements Runnable {
    private static final Random random = new Random();

    @Override
    public void run() {
        Logger.info("1-直冲 2-VIP：");
        int i = Integer.parseInt(Console.await());
        if (i == 1) {
            Logger.info("卡面值：");
            int j = Integer.parseInt(Console.await());
            Logger.info("卡数量：");
            int k = Integer.parseInt(Console.await());
            for (int l = 0; l < k; l++) {
                String card = "C_" + (random.nextInt(89999999) + 10000000);
                Logger.info(card);
                Code c = new Code();
                c.tokens = j;
                Utils.db2.set(card, c);
            }
        } else {
            Logger.info("卡数量：");
            int k = Integer.parseInt(Console.await());
            Logger.info("有效期（天）：");
            int j = Integer.parseInt(Console.await());
            Logger.info("用户组：");
            String group = Console.await();
            Logger.info("每日额外点数：");
            int extra = Integer.parseInt(Console.await());
            for (int l = 0; l < k; l++) {
                String card = "V_" + (random.nextInt(89999999) + 10000000);
                Logger.info(card);
                Code c = new Code();
                c.vipExpire = (86400000L) * j;
                c.vipName = group;
                c.vipSign = extra;
                Utils.db2.set(card, c);
            }
        }
    }
}
