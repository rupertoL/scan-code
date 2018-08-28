# scan-code
使用zxing集成的二维码和一维码扫描识别功能，可以根据配置调整识别框大小，切换识别算法

#添加依赖

  添加：
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  依赖：
  implementation 'com.github.rupertoL:scan-code:1.0.2'

#使用：
  ##初始化：
  使用前初始化或者直接在Application的onCreate中,参数为Application对象或者其子类 
   ScanCodeMangerUtils.newInstance().init(this);
  ##代码中调用

        使用前需要根据android系统版本判断添加动态申请权限
      Manifest.permission.WRITE_EXTERNAL_STORAGE
      Manifest.permission.READ_EXTERNAL_STORAGE
      Manifest.permission.CAMERA
 /**
     * @param activity        需要启动的页面Activity
     * @param scanCodeMode    扫描模式（条形码, 二维码）
     * @param code            请求响应码（请求响应码相同）
     * @param needCahangPager 切换算法
     */
 ScanCodeMangerUtils.newInstance().startIntent(final Activity activity, final ScanCodeMode scanCodeMode, final int code, final boolean needCahangPager);
