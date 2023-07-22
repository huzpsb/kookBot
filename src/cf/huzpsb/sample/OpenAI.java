package cf.huzpsb.sample;

import nano.http.d2.console.Logger;
import nano.http.d2.json.NanoJSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class OpenAI {
    private static final String base = "https://api.openai.com/v1/chat/completions";
    private static final String json = "{\n" +
            "  \"model\": \"gpt-3.5-turbo\",\n" +
            "  \"messages\": [{\"role\": \"user\", \"content\": \"_MSG_\"}]\n" +
            "}\n";
    private static final Random random = new Random();

    public static String gpt(String prompt, String suffix) {
        Logger.info("GPT-3: " + prompt);
        if (Filter.isBanned(prompt)) {
            return "OpenAI拒绝了回答此问题(-1I)。";
        }
        try {
            NanoJSON result = new NanoJSON(jsonPost2(json.replace("_MSG_", prompt.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "").replace("\"", "\\\"").replace("\t", "\\t")), "Bearer " + Main.keys[random.nextInt(Main.keys.length)]));
            String tmp = result.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            if (Filter.isBanned(tmp)) {
                return "OpenAI拒绝了回答此问题(-3O)。";
            }
            return tmp + suffix;
        } catch (Exception e) {
            return "网络错误 嘤嘤嘤~";
        }
    }

    public static String jsonPost2(String data, String token) {
        try {
            URL url = new URL(base);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", token);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();
            return response.toString();
        } catch (Exception e) {
            return "寄";
        }
    }
}
