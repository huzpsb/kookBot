package cf.huzpsb.sample;

import nano.http.d2.database.NanoDb;

public class Utils {
    public static final NanoDb<String, User> db;
    public static final NanoDb<String, Code> db2;

    static {
        try {
            db = new NanoDb<>("users", User.class.getClassLoader());
            db2 = new NanoDb<>("codes", Code.class.getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
