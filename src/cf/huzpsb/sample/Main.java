package cf.huzpsb.sample;

import nano.http.bukkit.api.BukkitServerProvider;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.json.JSONArray;
import nano.http.d2.json.NanoJSON;
import nano.http.d2.utils.Request;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main extends BukkitServerProvider {
    private static final String base = "https://www.kookapp.cn/api/v3";
    public static String preUri;
    public static File dir;
    public static NanoJSON pr;
    public static String[] keys;
    public static String name;
    private static String auth;

    @Override
    public void onEnable(String name, File dir, String uri) {
        Main.name = name;
        Main.dir = dir;
        Main.preUri = uri;
        File config = new File(Main.dir, "config.json");
        if (!config.exists()) {
            throw new RuntimeException("Config file not found.");
        }
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(config, "UTF-8");
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pr = new NanoJSON(sb.toString());
        keys = pr.getString("keys").split(",");
        auth = pr.getString("auth");
        Console.register("kook", new Command());
    }

    @Override
    public void onDisable() {
        Utils.db.save();
        Utils.db2.save();
        Console.unregister("kook");
        Logger.info("DemoPlugin: Module has been unloaded");
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        if (!parms.containsKey("json")) {
            return new Response(Status.HTTP_BADREQUEST, Mime.MIME_PLAINTEXT, "Expected json parameter");
        }
        NanoJSON json = new NanoJSON(parms.getProperty("json"));
        try {
            if (json.getInt("s") != 0) {
                throw new Exception("s is not 0");
            }
            json = json.getJSONObject("d");
            int type = json.getInt("type");
            NanoJSON result = new NanoJSON();
            switch (type) {
                case 255:
                    result.put("challenge", json.getString("challenge"));
                    return new Response(Status.HTTP_OK, Mime.MIME_JSON, result.toString());
                case 9:
                    String content = json.getString("content");
                    String sender = json.getString("author_id");
                    if (!Utils.db.contains(sender)) {
                        Utils.db.set(sender, new User());
                    }
                    String response;
                    Logger.info("Kook: " + content);
                    if (!json.getString("channel_type").equals("GROUP")) {
                        Logger.info("Not a group");
                        throw new Exception("Not a group");
                    }

                    response = Admin.handle(content, sender);
                    Logger.info("Admin: " + response);
                    JSONArray ja = pr.getJSONArray("prompts");
                    if (response == null) {
                        for (int i = 0; i < ja.length(); i++) {
                            NanoJSON now = ja.getJSONObject(i);
                            if (content.contains(now.getString("trigger"))) {
                                User u = Utils.db.query(sender);
                                int tokens = now.getInt("tokens");
                                if (u.tokens < tokens) {
                                    response = "你的余额不足";
                                    break;
                                }
                                u.tokens -= tokens;
                                Utils.db.set(sender, u);
                                String target = json.getString("target_id");
                                new Thread(() -> {
                                    String question = content.replaceAll(now.getString("trigger"), "");
                                    String suffix = "\n以上回答由GPT-4生成";
                                    if (question.length() > 700) {
                                        question = question.substring(0, 350) + "..." + question.substring(question.length() - 350);
                                        suffix = "\n以上回答由GPT-3生成";
                                    }
                                    String res = OpenAI.gpt(now.getString("prompt") + question, suffix);
                                    Properties p = new Properties();
                                    p.put("Authorization", "Bot " + auth);
                                    result.put("content", res);
                                    result.put("target_id", target);
                                    Logger.info(Request.jsonPost(base + "/message/create", result.toString(), p));
                                }).start();
                                response = "正在生成，请稍后...您还剩余" + u.tokens + "点积分~";
                                break;
                            }
                        }
                    }
                    if (response == null) {
                        response = Active.act(sender);
                    }
                    if (response != null) {
                        Properties p = new Properties();
                        p.put("Authorization", "Bot " + auth);
                        result.put("content", response);
                        result.put("target_id", json.getString("target_id"));
                        Logger.info(Request.jsonPost(base + "/message/create", result.toString(), p));
                    }
                    throw new Exception("Done");
                default:
                    throw new Exception("Ignore");
            }
        } catch (Exception e) {
            return new Response(Status.HTTP_OK, Mime.MIME_JSON, "{}");
        }
    }

    @Override
    public Response fallback(String uri, String method, Properties header, Properties parms, Properties files) {
        return null;
    }
}
