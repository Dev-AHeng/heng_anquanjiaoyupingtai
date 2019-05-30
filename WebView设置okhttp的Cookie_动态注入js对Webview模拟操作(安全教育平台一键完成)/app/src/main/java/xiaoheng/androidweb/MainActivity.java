package xiaoheng.androidweb;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.text.*;
import android.util.*;
import android.webkit.*;
import android.widget.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import okhttp3.*;

/**
 * 这是安全教育平台一键完成作业的一个例子，这个例子是针对《2019年全国中小学生防灾减灾安全教育》这课做的，如果要写其它作业的一键完成可以按照这样的套路写。
 * 思路就是先用okhttp进行post键值模拟登录，然后获取到用户的cookie再将获取到的cookie设置到webview中（目的是快速对两个不同的模块进行快速登录），然后对webview进行动态注入js代码并执行（用于对作业的单选题和提交按钮的模拟点击），主要思路就是这样
 * 2019.5.26
 * By_小亨
 */

public class MainActivity extends AppCompatActivity 
{
	// private WebView wv[] = new WebView[2];
	private WebView wv1, wv2;
	private WebSettings ws1, ws2;
	private String workjs1, workjs2, jsonContent, newCookie;
	private TextView txv1;

	private FileUtil fu = new FileUtil();

	private Gson gson;
	private JsonParser jsonParser;
	private JsonArray jsonElements;
	private JsonBean bean1;

	private final int MIN1 = 10000000;
	private final int MAX1 = 99999999;
	private final int MIN2 = 10000000;
	private final int MAX2 = 99999999;
	private int randNumber1,randNumber2;
	private Random rand;

	private OkHttpClient client;
	private FormBody requestBody;
	private Request request1;
	private ArrayList<JsonBean> beans, content;

	private ArrayList<Cookie> cookies;
	private Cookie cookie;
	private CookieManager cookieManager;

	private boolean work1 = false;
	private boolean work2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		/*
		 // 将okhttp中的cookie设置到webview
		 syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", "Unique=582ad57ba37543188c3f3262e2d5e656; domain=.xueanquan.com; expires=Mon, 24-Jun-2019 06:30:16 GMT; path=/");
		 syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", "RecordLoginInput_-1=zhangjianheng666; domain=.xueanquan.com; expires=Sun, 24-May-2020 06:30:16 GMT; path=/");
		 syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", "UserID=371BEFF5B89ACBF98FEB66B9983F30C3; domain=.xueanquan.com; expires=Sun, 26-May-2019 06:30:16 GMT; path=/");
		 syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", "_UCodeStr={%0d%0a  \"Grade\": 11,%0d%0a  \"ClassRoom\": 533319468,%0d%0a  \"CityCode\": 120001%0d%0a}; domain=.xueanquan.com; expires=Sun, 26-May-2019 06:30:16 GMT; path=/");
		 syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", "ServerSide=https://yunfu.xueanquan.com; domain=.xueanquan.com; expires=Sun, 26-May-2019 06:30:16 GMT; path=/");
		 syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", "_UserID=Bmx7cUfCNHQv/cYq2vgJDk9nSoEJY62eOmnlCZmMy3s=; domain=.xueanquan.com; expires=Sun, 26-May-2019 06:30:16 GMT; path=/");
		 */
		/*
		 wv1 = new WebView(getApplicationContext());
		 wv1 = new WebView(getApplicationContext());
		 /*
		 LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout1);
		 for (int i = 0; i < 2; i++)
		 {
		 wv[i] = new WebView(this);
		 linearLayout.addView(wv[i]);
		 }*/

		txv1 = (TextView)findViewById(R.id.mainTextView1);
		wv1 = (WebView)findViewById(R.id.activitymainWebView1);
		wv2 = (WebView)findViewById(R.id.activitymainWebView2);

		/*
		获取本地json内容 一定要设置 格式如下然后以此类推
		 [{
		 	"admin" : "第一个账号",
		 	"password" : "第一个密码"
		 } , {
		 	"admin" : "第二个账号",
		 	"password" : "第二个密码"
		 }]
		 */
		jsonContent = fu.readTxt(Environment.getExternalStorageDirectory().getAbsolutePath() + "/htmlcode/usermessage.json");
		
		
		// 开始
		isTF(true, true);

    }

	// 同步cookie
	public boolean syncCookie(Context context, String url, String cookie)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		{   
			CookieSyncManager.createInstance(context);
		}  
		cookieManager = CookieManager.getInstance();
		// 键值形式设置cookie 以"key=value"形式作为cookie即可
		cookieManager.setCookie(url, cookie);
		newCookie = cookieManager.getCookie(url);
		return TextUtils.isEmpty(newCookie) ?false: true;
	}


	// json解析
	public ArrayList<JsonBean> jsonList(String jsonContentString) throws Exception
	{
		gson = new Gson();
		jsonParser = new JsonParser();
		// 获取JsonArray对象
		jsonElements = jsonParser.parse(jsonContentString).getAsJsonArray();
		beans = new ArrayList<>();
		for (JsonElement bean : jsonElements)
		{
			// 解析
			bean1 = gson.fromJson(bean, JsonBean.class);
			beans.add(bean1);
		}
		return beans;
	}


	// 生成17位双精度浮点型随机数
	public String suiJiShu()
	{


		rand = new Random();
		randNumber1 = rand.nextInt(MAX1 - MIN1 + 1) + MIN1;
		randNumber2 = rand.nextInt(MAX2 - MIN2 + 1) + MIN2;
		return "0." + randNumber1 + randNumber2;
	}

	// 时间截 从1970-01-01 00:00:00 000开始
	public String linuxTime()
	{
		long time = System.currentTimeMillis();
		return Long.toString(time);
	}

	// 判断完成 下个
	public void isTF(boolean work1tf, boolean work2tf)
	{
		// 判断两个作业都完成
		if (work1tf && work2tf)
		{
			try
			{
				// json内容
				content = jsonList(jsonContent);
				for (int i = 0; i < content.size();)
				{
					// Log.i("jsoncontent", "账号：" + content.get(i).getAdmin() + " 密码：" + content.get(i).getPassword());
					// 开始自动做作业
					startDoWork(content.get(i).getAdmin(), content.get(i).getPassword());
					i++;
				}
				Toast.makeText(MainActivity.this, "两个都完成", Toast.LENGTH_SHORT).show();
				// txv1.setText("两个都完成");
			}
			catch (Exception e)
			{}
		}
	}

	// 开始做作业
	public void startDoWork(String username, String password)
	{
		client = new OkHttpClient.Builder().cookieJar(new CookieJar()
			{
				// 这里可以做cookie传递，保存等操作
				@Override
				public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
				{
					// 可以做保存cookies操作
					for (Cookie cookie : cookies)
					{
						// Log.i("cookies", cookie.toString());
						syncCookie(MainActivity.this, "https://yunfu.xueanquan.com/", cookie.toString());
					}
				}
				@Override
				public List<Cookie> loadForRequest(HttpUrl url)
				{
					// 加载新的cookies
					cookies = new ArrayList<>();
					cookie = new Cookie.Builder()
						.hostOnlyDomain(url.host())
						.name("SESSION").value("zyao89")
						.build();
					cookies.add(cookie);
					return cookies;
				}
			})
			.build();

		// 开始键值
		requestBody = new FormBody.Builder()
			.add("userName", username)
			.add("password", password)
			.add("checkcode", " ")
			.add("type", "login")
			.add("loginType", "1")
			.add("r", suiJiShu())
			.add("_", linuxTime())
			.build();

		// 设置请求头
		request1 = new Request.Builder()
			.url("https://guangdonglogin.xueanquan.com/LoginHandler.ashx")
			.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
			.header("Connection", "keep-alive")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.header("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; MI 6 Build/OPR1.170623.027; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/68.0.3440.91 Mobile Safari/537.36 jdhttpmonitor/3.2.31")
			.post(requestBody)
			.build();

		// 请求监听
		client.newCall(request1).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					Log.i("失败", "失败");
				}
				@Override
				public void onResponse(Call call, Response response) throws IOException
				{
					if (response.isSuccessful())
					{
						// okhttp登陆成功返回的数据
						// txv1.setText(response.body().string());
						// Log.i("返回数据", response.body().string());


						// 模块一
						new Thread(new Runnable() 
							{
								public void run() 
								{
									runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												// ui操作
												wv1.loadUrl("https://huodong.xueanquan.com/2019fangzaijianzai/zhishi.html");
												// 声明WebSettings子类
												ws1 = wv1.getSettings();
												// webview活跃状态
												ws1.setJavaScriptEnabled(true);
												// // https页面加载不出来就用它  能登录成功的关键
												ws1.setDomStorageEnabled(true);
												// 支持自动加载图片
												ws1.setLoadsImagesAutomatically(false);

												// 设置WebViewClient类
												wv1.setWebViewClient(new WebViewClient()
													{
														@Override
														public void onPageFinished(final WebView view, String url)
														{
															// 标题和路径
															// txv2.setText(view.getTitle()+view.getUrl());

															new Handler().postDelayed(new Runnable()
																{
																	@Override
																	public void run()
																	{
																		// 倒计时完成
																		// 动态添加id 提交
																		workjs1 = "javascript:document.getElementsByClassName('btnbox')[0].setAttribute('id','xiaoheng');"
																			+ "document.getElementById('xiaoheng').click();";
																		view.loadUrl(workjs1);

																		// 完成
																		work1 = true;
																		// 判断是否两个都完成
																		isTF(work1, work2);
																	}
																}, 2500);
														}
													});
											}
										});
								}
							}).start();



						// 模块二
						new Thread(new Runnable() 
							{
								public void run() 
								{
									runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												// ui操作
												wv2.loadUrl("https://huodong.xueanquan.com/2019fangzaijianzai/question.html");
												ws2 = wv2.getSettings();
												ws2.setJavaScriptEnabled(true);
												ws2.setDomStorageEnabled(true);
												// 支持自动加载图片
												ws2.setLoadsImagesAutomatically(false);

												// 设置WebViewClient类
												wv2.setWebViewClient(new WebViewClient()
													{
														@Override
														public void onPageFinished(WebView view, String url)
														{
															// 标题和路径
															// txv2.setText(view.getTitle()+view.getUrl());
															// Toast.makeText(MainActivity.this, "完成：" + view.getTitle(), Toast.LENGTH_LONG).show();
															new Handler().postDelayed(new Runnable()
																{
																	@Override
																	public void run()
																	{
																		// 倒计时完成
																		// txv1.setText("完成");

																		// 作业一的模块二

																		// 选择 动态添加id 提交
																		workjs2 = "javascript:function floatselect(){"
																			+ "document.getElementById('radio_1_1').click();"
																			+ "document.getElementById('radio_0_2').click();"
																			+ "document.getElementById('radio_1_3').click();"
																			+ "document.getElementById('radio_1_4').click();"
																			+ "document.getElementById('radio_0_5').click();"
																			+ "document.getElementById('radio_0_6').click();"
																			+ "document.getElementById('radio_1_7').click();"
																			+ "document.getElementById('radio_2_8').click();"
																			+ "document.getElementById('radio_1_9').click();"
																			+ "document.getElementById('radio_0_10').click();}"
																			+ "floatselect();"
																			+ "document.getElementsByClassName('btn')[0].setAttribute('id','heng');"
																			+ "document.getElementById('heng').click();";
																		wv2.loadUrl(workjs2);

																		// 完成
																		work2 = true;
																		// 判断是否两个都完成
																		isTF(work1, work2);
																	}
																}, 2500);

														}
													});

											}
										});
								}
							}).start();

					}
				}
			});
	}



}
