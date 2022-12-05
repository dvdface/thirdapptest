package thirdapp.test.ctsd

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before



/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TiktokTest {

    private val tag = "TiktokTest"

    private lateinit var device:UiDevice
    private lateinit var context:Context

    private val timeout = 5000L
    private val packageName = "com.ss.android.ugc.aweme"


    @Before
    fun setUp() {

        // init test parameters
        Configurator.getInstance().apply {
            waitForIdleTimeout = 500
        }

        // init device
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // init context
        context = ApplicationProvider.getApplicationContext()
        assertNotNull(context)


    }

    @After
    fun tearDown() {

    }

    @Test
    @LargeTest
    fun fastFlingLiveVideos() {


        // launch
        launch(packageName)

        // click suggestion menu
        gotoSuggestionMenu()

        // fling video
        flingVideo(true, 5000L, 2*60*60*1000)
    }

    /**
     * watch video by fling gesture
     *  videos have many categories:
     *      short video
     *      fullscreen video
     *      live video
     *      VR video
     *      picture video
     *      ai video
     *      reminder video
     * Params:
     * enter - whether to enter play page by click full screen watch / landscape watch / VR watch
     * time - how long to play in single video, in milliseconds
     * duration - how long to test, in milliseconds
     * Returns:
     *  None
     */
    private fun flingVideo(enter:Boolean=false, time:Long=3000L, duration:Long=14400L) {

        val actionsForVideos = listOf<()->Unit>(
            {
                // fullscreen video
                Log.i(tag, "check if it's a fullscreen video")
                val byRule = By.clazz("android.widget.LinearLayout").descContains("全屏观看，按钮")
                if(device.hasObject(byRule)) {
                    Log.i(tag, "fullscreen video")
                    device.findObject(byRule)?.click()
                    SystemClock.sleep(time)
                    device.pressBack()
                } else {
                    Log.d(tag, "no")
                }
            },  {
                // live video
                Log.i(tag, "check if it's a live video")
                val byRule = By.clazz("android.widget.TextView").text("点击进入直播间")
                if(device.hasObject(byRule)){
                    Log.i(tag, "live video")
                    device.findObject(byRule)?.click()
                    SystemClock.sleep(time)
                    device.pressBack()
                } else {
                    Log.d(tag, "no")
                }
            },  {
                // vr video
                Log.i(tag, "check if it's a vr video")
                val byRule = By.clazz("android.view.View").descContains("点击体验VR直播，按钮")
                if(device.hasObject(byRule)){
                    Log.i(tag, "vr video")
                    device.findObject(byRule)?.click()
                    SystemClock.sleep(time)
                    device.pressBack()
                } else {
                    Log.d(tag, "no")
                }
            },  {
                // picture video
                Log.i(tag, "check if it's a picture video")
                val byRule = By.clazz("android.widget.LinearLayout").hasChild(By.clazz("android.widget.TextView").text("图文"))
                if(device.hasObject(byRule)) {
                        Log.i(tag, "picture video")
                        SystemClock.sleep(time)
                } else {
                    Log.d(tag, "no")
                }
            },  {
                // ai video
                Log.i(tag, "check if it's a ai video")
                val byRule = By.clazz("android.widget.TextView").textContains("特效")
                if(device.hasObject(byRule)){
                    Log.i(tag, "ai video")
                    SystemClock.sleep(time)
                } else {
                    Log.d(tag, "no")
                }
            }
        )

        val startTime = SystemClock.elapsedRealtime()
        var lastTime:Long?
        while((SystemClock.elapsedRealtime() - startTime) < duration) {

            lastTime = SystemClock.elapsedRealtime()
            // according to video type , do something
            if(enter) {
                actionsForVideos.forEach{
                    it()
                }
            }

            // next
            fling()

            Log.i(tag, "elapse: ${(SystemClock.elapsedRealtime() - lastTime)/1000.0}s, total: ${(SystemClock.elapsedRealtime() - startTime)/1000}s")
        }

    }

    /**
     * fling gesture
     * Params:
     *  step - steps to fling, more steps more slower, default 6
     * Returns:
     *  none
     */
    private fun fling(step:Int = 6) {

        Log.i(tag, "fling")
        when(device.displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> { Log.d(tag, "fling in portrait"); device.swipe(500, 1400, 500, 800, step) }
            Surface.ROTATION_90, Surface.ROTATION_270 ->  { Log.d(tag, "fling in landscape"); device.swipe(1200, 800, 1200, 300, step) }
            else -> Log.e(tag, "unknown direction")
        }
        Log.i(tag, "fling done")
    }

    /**
     * goto suggestion menu
     * Params:
     *  None
     * Returns:
     *  None
     */
    private fun gotoSuggestionMenu() {

        // click suggestion menu
        Log.i(tag, "enter suggestion")

        Log.d(tag, "find first page button")
        device.findObject(By.clazz("android.widget.TextView").textStartsWith("首页").descContains("首页，按钮"))?.let {
            Log.d(tag, "click first page button")
            it.click()
        }

        Log.d(tag, "find suggestion")
        device.findObject(By.clazz("android.widget.TextView").textStartsWith("推荐").descContains("推荐，按钮"))?.let {
            Log.d(tag, "click suggestion button")
            it.click()
            it.wait(Until.descContains("已选中"), timeout)
        }
    }



    /**
     * launch app by clear Intent.FLAG_ACTIVITY_CLEAR_TASK
     * Params:
     *  package - package to launch
     *  timeout - launching timeout, default 5000 ms
     * Returns:
     *  None
     */
    private fun launch(packageName:String, timeout:Long=5000) {

        Log.i(tag, "launch app")

        // get launch intent
        Log.d(tag, "get launch intent")
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)

        // intent can't be null
        assertNotNull(intent)

        intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        // start app
        Log.d(tag, "launch app by intent")
        context.startActivity(intent)

        // wait app
        Log.d(tag, "wait app to launch")
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), timeout)

    }
}