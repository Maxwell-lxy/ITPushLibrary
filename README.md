# Innotech推送-android sdk对接文档

#### 关于Android Studio

Android Studio是Google力推的Android开发环境，在IntelliJ IDEA基础上进行了大量功能完善和优化，包括：

- 基于Gradle的构建支持
- Android 专属的重构工具和Instant Run快速修复技术
- 功能强大的布局编辑器，可以让你拖拉 UI 控件并进行效果预览
- 全新的 Android 模拟器大约比之前的模拟器快 3 倍，同时由于 ADB 的增强，传输应用和数据到模拟器上的速度比到物理设备上快 10 倍。
- 提供性能分析工具以捕获性能、可用性、版本兼容性等问题

因此我们强烈推荐Android开发者将现有项目迁移到Android Studio环境，并在Android Studio下更快地实现推送SDK的集成工作。

#### 适用范围

该文档适用于Android组件化推送SDK4.0.0及以上版本。Android组件化SDK适用于Android 4.0 (API Level 14)及以上版本。

#### 准备

申请小米/魅族/华为/个推/友盟5个开发平台的账号，并开通推送功能，从而获得appid/appkey/appsecret等信息。

小米：https://dev.mi.com/console/?page=appservice&mod=push

魅族：http://push.meizu.com

华为：http://developer.huawei.com/consumer/cn/service/hms/pushservice.html

个推：http://www.getui.com/cn/getui.html

友盟：https://mobile.umeng.com/push

备注：申请过程中有疑问可联系技术中心（石瑶）进行协助。

#### 接入

1. 将其添加到存储库末尾的根build.gradle中：

   ```
   	allprojects {
   		repositories {
   			...
   			maven { url 'https://jitpack.io' }
   			maven {
               	url "http://mvn.gt.igexin.com/nexus/content/repositories/releases/"
           	}
   		}
   	}
   ```

2. 添加依赖项/配置，在app下面的build.gradle中：（manifestPlaceholders中的配置项的值需要更换成上文“准备”中申请的值）

   ```
   	compileSdkVersion 25
   	defaultConfig {
   		...
   		targetSdkVersion 25 //由于小米系统的离线推送对25以上版本有些bug，建议暂时使用25
   		...
            manifestPlaceholders = [
            	 //寅诺配置
            	 INNOTECH_APP_ID : 88888888,
                INNOTECH_APP_KEY : "88888888",
   			INNOTECH_PUSH_DEBUG : true,//是否开启调试模式，建议开发阶段打开，查看日志调错
   			//小米配置
                MI_APP_ID           : "2882303761517759216",
                MI_APP_KEY          : "5221775952216",
   			//魅族配置
                MEIZU_APP_ID        : "112950",
                MEIZU_APP_KEY       : "509a337735ba497ebba21d981924c9f4",
   			//华为配置
                HMS_APP_ID          : "100244129",
   			//个推配置
                GETUI_APP_ID        : "PH4qx2Le8w9Nz8pMyCPMZ7",
                GETUI_APP_KEY       : "qy4AYz75roAJiQjzkPLfc3",
                GETUI_APP_SECRET    : "tz6QPHBAOY8Scmcxo6y9P2"
   
           ]
       }
   	
   	dependencies {
   		...
   		//{version}版本号需修改成最新版本
   		implementation 'com.github.hgw900109:ITPushLibrary:{version}'
   	}
   ```

3. 在项目Application的onCreate方法中进行SDK初始化操作，如下：

   ```
   public class MyApplication extends PushApplication {

       @Override
       public void onCreate() {
           super.onCreate();
           //推送SDK初始化
           InnotechPushManager.getInstance().initPushSDK(this);
           //设置推送接收器
           InnotechPushManager.getInstance().setPushRevicer(new MyPushReceiver());
       }
   }
   ```

4. manifests中使用Application

   ```
   <application
       android:name=".MyApplication"
       ...
       >
       ...
   </application>
   ```

5. 创建消息推送接收器MyPushReceiver，可以根据自身情况重写个别方法

   ```
   public class MyPushReceiver extends PushReciver {
   	
   	/**
        * 透传信息处理回调方法
        * @param context
        * @param mPushMessage
        */
       @Override
       public void onReceivePassThroughMessage(Context context, InnotechMessage mPushMessage) {
           showMessageInfoforTest(context,"onReceivePassThroughMessage",mPushMessage);
       }

   	/**
        * 点击通知栏消息回调方法
        * @param context
        * @param mPushMessage
        */
       @Override
       public void onNotificationMessageClicked(Context context, InnotechMessage mPushMessage) 	{
           showMessageInfoforTest(context,"onNotificationMessageClicked",mPushMessage);
       }

   	/**
        * 通知栏详细到达时回调
        * @param context
        * @param mPushMessage
        */
       @Override
       public void onNotificationMessageArrived(Context context, InnotechMessage mPushMessage) 	{
           showMessageInfoforTest(context,"onNotificationMessageArrived",mPushMessage);
       }

       private void showMessageInfoforTest(Context context,String metodName,  InnotechMessage mPushMessage){
           String contentStr = mPushMessage.getContent();
           String titleStr = mPushMessage.getTitle();
           String data = mPushMessage.getData();
           String dataInfo = " ==app== contentStr:"+contentStr+" titleStr:"+titleStr+" contentStr:"+contentStr+" data:"+data;
           LogUtils.d(context,"metodName:"+metodName+dataInfo);
       }
   }
   ```

6. 在程序的入口Activity的onCreate方法中添加如下代码，

   ```
   public class MainActivity extends Activity {

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);
           //启动推送
           InnotechPushMethod.launcher(this);
       }
   }
   ```


#### 功能

1、设置别名：推送可通过别名进行推送。

```
InnotechPushMethod.setAlias(MainActivity.this, "test", new RequestCallback() {
    @Override
    public void onSuccess(String msg) {
        Log.i("Innotech_Push", ">>>>>>>>>>>>setAlias onSuccess msg:" + msg);
    }

    @Override
    public void onFail(String msg) {
        Log.i("Innotech_Push", ">>>>>>>>>>>setAlias onFail msg:" + msg);
    }
});
```

#### 测试

1. 调试模式打开情况下，运行程序后，查看Logcat，通过“innotech_push”进行过滤，可以看到推送初始化信息日志，通过信息可以判断出是否初始成功。如下：

       //个推成功的日志
       Innotech_Push: [GeTuiPush] onReceiveServicePid -> ServicePid = 2132
       Innotech_Push: [GeTuiPush] onReceiveClientId -> clientid = 997ea4305ce2d6d0a7a6c352c0f70e8b
       Innotech_Push: [GeTuiPush] onReceiveOnlineState() -> b = true

2. 进入各推送平台进行后台推送，小米/魅族/华为使用通知栏消息进行推送，个推和友盟使用透传消息进行推送。

#### 疑问

- 集团推送SDK为什么达到率比单独推送平台（如：个推）到达率高？

  集团推送集成了小米、魅族、华为、个推和集团推送五部分推送，并根据用户手机进行匹配开启相应推送机制；如：小米手机用户开启应用将开启小米推送。由于小米、魅族、华为三家的推送在自家品牌手机上的进程级别为系统级别，故，不管是否在线，到达率都接近100%，而除了这三家之外的手机将开启个推和集团推送双通道来保证到达率。

