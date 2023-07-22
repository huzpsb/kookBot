package cf.huzpsb.sample;

import java.io.Serializable;

public class User implements Serializable {
    public int tokens = 10;
    public long lastSign = 0L;
    public long vipExpire = 0L;
    public int vipSign = 0;
    public String vipName = "普通用户";
    public boolean invited = false;
}
