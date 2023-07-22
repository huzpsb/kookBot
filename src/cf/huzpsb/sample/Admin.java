package cf.huzpsb.sample;

public class Admin {
    public static String handle(String content, String sender) {
        if (content.contains("#菜单")) {
            return Main.pr.getString("help");
        }
        if (content.contains("#余额")) {
            User u = Utils.db.query(sender);
            return "你的余额为" + u.tokens + "点";
        }
        if (content.contains("#签到")) {
            User u = Utils.db.query(sender);
            long delta = System.currentTimeMillis() - u.lastSign;
            if (delta < 86400000) {
                return "冷却中：" + (24 - (int) (delta / 3600000)) + "小时";
            }
            if (u.vipExpire < System.currentTimeMillis() && u.vipExpire != 0) {
                u.vipExpire = 0;
                u.vipSign = 0;
                u.vipName = "普通用户";
            }
            u.lastSign = System.currentTimeMillis();
            int gain = 5;
            gain += u.vipSign;
            u.tokens += gain;
            Utils.db.set(sender, u);
            return "签到成功，获得" + gain + "点!(用户组 " + u.vipName + ")";
        }
        if (content.contains("#邀请")) {
            return "邀请好友输入 #兑换 " + sender + " 即可让好友获得10点积分，自己获得20点积分~";
        }
        if (content.contains("#兑换")) {
            String[] args = content.split(" ");
            if (args.length != 2) {
                return "参数错误~ 请使用 “#兑换 兑换码” 来兑换积分~!";
            }
            if (args[1].equals(sender)) {
                return "不能兑换自己的兑换码哦~";
            }
            User u = Utils.db.query(sender);
            if (u.invited) {
                return "你已经兑换过此类兑换码了哦~";
            }
            if (!Utils.db.contains(args[1])) {
                return "兑换码不存在";
            }
            u.invited = true;
            User u2 = Utils.db.query(args[1]);
            u.tokens += 20;
            u2.tokens += 10;
            Utils.db.set(sender, u);
            Utils.db.set(args[1], u2);
            return "兑换成功~你获得了10点积分~";
        }
        if (content.contains("#激活")) {
            String[] args = content.split(" ");
            if (args.length != 2) {
                return "参数错误~ 请使用 “#激活 VIP码” 来激活VIP~!";
            }
            User u = Utils.db.query(sender);
            if (!Utils.db2.contains(args[1])) {
                return "指定的VIP码不存在";
            }
            Code vc = Utils.db2.query(args[1]);
            if (vc.tokens > 0) {
                u.tokens += vc.tokens;
                Utils.db.set(sender, u);
                Utils.db2.remove(args[1]);
                return "激活成功，获得" + vc.tokens + "点积分";
            }
            if (u.vipExpire > System.currentTimeMillis()) {
                return "你已经是VIP用户了哦~如果有必要，可以使用 #放弃VIP 来放弃VIP身份 (请三思！)";
            }
            u.vipName = vc.vipName;
            u.vipSign = vc.vipSign;
            u.vipExpire = System.currentTimeMillis() + vc.vipExpire;
            Utils.db.set(sender, u);
            Utils.db2.remove(args[1]);
            return "激活成功，获得VIP身份: " + vc.vipName + " (有效期: " + vc.vipExpire / 86400000 + "天)";
        }
        if (content.contains("#放弃VIP")) {
            User u = Utils.db.query(sender);
            if (u.vipExpire == 0) {
                return "你不是VIP用户";
            }
            u.vipExpire = 0;
            u.vipSign = 0;
            u.vipName = "普通用户";
            Utils.db.set(sender, u);
            return "已放弃VIP身份...";
        }
        return null;
    }
}
