package cf.huzpsb.sample;

import java.util.HashMap;
import java.util.Map;

public class Active {
    private static final Map<String, Integer> count = new HashMap<>();
    private static long lastTime = 0;

    public static String act(String user) {
        count.put(user, count.getOrDefault(user, 0) + 1);
        if (lastTime > System.currentTimeMillis()) {
            return null;
        }
        lastTime = System.currentTimeMillis() + 3600000;//1 hour
        int max = 0;
        String maxUser = "";
        for (Map.Entry<String, Integer> entry : count.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                maxUser = entry.getKey();
            }
        }
        count.clear();
        User u = Utils.db.query(maxUser);
        u.tokens += 10;
        Utils.db.set(maxUser, u);
        return "恭喜 " + maxUser + " 触发了活跃奖励！(10积分)已发放至其账户。";
    }
}
