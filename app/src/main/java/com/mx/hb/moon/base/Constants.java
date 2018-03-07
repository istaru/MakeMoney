package com.mx.hb.moon.base;

/**
 * Created by Moon on 2016/4/5.
 */
public class Constants {
    /** 广告详情的页面 */
    public static String HTML5 = "http://es3.laizhuan.com/resource/app/html/index.html";
//    public static String HTML5 = "file:///android_asset/html/index.html";
    /** 广告详情的页面 */
//    public static String ERROR_HTML5 = "http://es3.laizhuan.com/resource/app/html/error.html";
    public static String ERROR_HTML5 = "file:///android_asset/html/error.html";
    /** Login */
//    public static final String LOGIN_HTML5 = "http://es3.laizhuan.com/resource/app/html/login.html";
    public static final String LOGIN_HTML5 = "file:///android_asset/html/login.html";
    /** 分享页 */
    public static final String SHARE_HTML5 = "http://es3.laizhuan.com/resource/app/html/share.html";
//    public static String SHARE_HTML5 = "file:///android_asset/html/share.html";
    /** 帮助页 */
    public static final String HELP_HTML5 = "http://es3.laizhuan.com/resource/app/html/help.html";
//    public static String HELP_HTML5 = "file:///android_asset/html/help.html";

    /** */
    public static final String REQUEST = "http://es3.laizhuan.com";
//    public static final String REQUEST = "http://192.168.1.210";
    /** App缓存地址 */
    public static final String CACHEDIRECTORY_PATH = "laizhuan/";
    /** App更新地址 */
    public static final String UPDATE_APP_PATH = "http://gdown.baidu.com/data/wisegame/fd84b7f6746f0b18/baiduyinyue_4802.apk";
    /** 刷新首页的余额 */
    public static final String SENDMSG_REFRESH = "com.main.refresh.money";
    public static final String ERROR = "error";
    public static final String NET_ERROR = "网络无连接";
    public static final String FIND_ERROR = "查询出错";
    public static final String NONE = "暂无";
    public static final String ANALYTICAL_ERROR = "解析出错";
    public static final String LOADING = "加载中...";

    //友盟
    /** 友盟应用的唯一标识 */
    public static final String UM_APPID = "56d7a9c9e0f55a45020017ef";

    //微信
    /** 微信应用发布的唯一标识(开放平台) */
//    public static final String WX_APPID = "wx3d2513adde8ef417";
    public static final String WX_APPID = "wx9cef7c63353f7ed6";
    /** （公众平台）*/
    public static final String WX_APPID_PUBLIC = "wx7b84c857f45b67ca";
    /** 微信应用发布的密钥 */
//    public static final String WX_SECRET = "c5bcbbf02598201f24a401f7dfa6b8ea";
    public static final String WX_SECRET = "c3168339be31c67e5c1e156ab11ecb77";
    /** 微信朋友圈、好友分享链接 */
    public static final String WX_SHARE_URL = "http://laizhuan.com/?webpage=qr&u=c8afb9g983";
    /** 微信权限 */
    public static final String WX_SCOPE = "snsapi_userinfo";
    /** 微信请求和回调的状态 */
    public static final String WX_STATE = "wechat_laizhuan_login";
    /** 通过code获取access_token的接口 */
    public static final String WX_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";
    /** 刷新或续期access_token使用 */
    public static final String WX_REFRESH_TOKEN = "https://api.weixin.qq.com/sns/oauth2/refresh_token";
    /** 获取用户个人信息 */
    public static final String WX_USER_INFO = "https://api.weixin.qq.com/sns/userinfo";

    //有米
    /** 有米应用发布的唯一标识 */
    public static final String YM_APPID = "4dd76da75a4b46f9";
    /** 有米应用发布的密钥 */
    public static final String YM_SECRET = "80e2ff6bb4991832";

    //万普
    /** 万普应用发布的唯一标识 */
    public static final String WP_APPID = "7867dcb2b46ab495d3b8fb56cf1bbdf9";
    /** 万普分发渠道标识 */
    public static final String WP_APPPID = "";

    //点乐
    /** 点乐应用发布的唯一标识 */
    public static final String DL_DIANLE_APPID = "ab51272806d2f3753d331f4e41ac7c2b";

    //兑吧
    /** 兑吧提现的key */
    public static final String DB_KEY = "4CgXMXbZYifpSWv1wHszsN9UWr2z";

    /** 请求固定信息 */
    public static final String REQUEST_MSG = "laizhuan";

    /** 网络请求的token */
    public static final String TOKENT = "4c6d819ae6d50125158d253622f43868";

    public static final String U_TOKENT = "f87fab59c74d781235ed521582e2b975";

    /** 登录或注册 */
    public static final String REGISTER_OR_LOGIN_PATH = REQUEST + "/android/login/";

    /** 获取验证码 */
    public static final String GET_CODE = REQUEST + "/android/vcode";

    /** 验证验证码是否正确 */
    public static final String TEST_VCODE_SU = REQUEST + "/android/members_three_bind_remove";

    /** 更新用户账户号设备码 */
    public static final String EQUIPMENT_UPDATE = REQUEST + "/android/members_only_update/";

    /** 绑定第三方账号(登录相关) */
    public static final String EQUIPMENT_BIND = REQUEST + "/android/members_three_bind/";

    /** 绑定第三方账号(其他账号相关) */
    public static final String EQUIPMENT_NEW_BIND = REQUEST + "/android/members_three_bind_update/";

    /** 轮播 */
    public static final String LUNBO_PATH = REQUEST + "/android/lunbo/";
//    public static final String LUNBO_PATH = "http://wapsh.189.cn:8080/NOS/appIOS/advertisementList.htm";

    /** 获取任务信息 */
    public static final String FIND_TASK_CONTENT = REQUEST + "/backend/members_task_browse/";

    /** 用户的支付 */
    public static final String ZFB_DIPPER = REQUEST + "/duiba/zhida/?dbredirect=http%3A%2F%2Fwww.duiba.com.cn%2Fmobile%2Fdetail%3FitemId%3D53&id=";

    /** 查找用户信息 */
    public static final String FIND_USER_MSG = REQUEST + "/android/members_info_update";

    /** 查找任务收入列表 */
    public static final String FIND_TASK_RECORD = REQUEST + "/android/members_info_task";

    /** 查找提现记录 */
    public static final String FIND_CASH_RECORD = REQUEST + "/android/members_info_balance/";

    /** 查找好有提成列表 */
    public static final String FIND_FRIENDS_RECORD = REQUEST + "/android/members_info_task_level";

    public static final String UPDATE_APP = REQUEST + "/release/";

    /** 生成二维码 */
    public static final String CREATE_QRCODE = REQUEST + "/android/members_friend_add/";

    /** 上传任务 */
//    public static final String UPLOAD_TASKS = REQUEST + "/android/filter/";
//    public static final String UPLOAD_TASKS = REQUEST + "/android/filters/";
    public static final String UPLOAD_TASKS = REQUEST + "/android/filters_check";

    /** 申请提现 */
    public static final String APPLY_DIPPER = REQUEST + "/duiba/exchange";

    /** 登录信息记录 */
    public static final String LOGIN_UPDATE = REQUEST + "/android/login_update";


}
