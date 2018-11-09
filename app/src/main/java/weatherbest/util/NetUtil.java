package weatherbest.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil {
    public static final int NETWORN_NONE=0;
    public static final int NETWORN_WIFI=1;
    public static final int NETWORN_MOBILE=2;
//在NetUtil类中实现getNetworkState方法
    public static int getNetworkState(Context context){//判断手机网络状况
        ConnectivityManager connManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo == null) {//没有网络
            return NETWORN_NONE;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            return NETWORN_MOBILE;//移动数据
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            return NETWORN_WIFI;//wifi状态
        }
        return NETWORN_NONE;

    }

}
