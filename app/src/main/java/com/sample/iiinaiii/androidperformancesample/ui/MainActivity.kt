package com.sample.iiinaiii.androidperformancesample.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FrameMetricsAggregator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.sample.iiinaiii.androidperformancesample.R

class MainActivity : AppCompatActivity() {

    private lateinit var trace: Trace
    private lateinit var frameMetricsAggregator: FrameMetricsAggregator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        registerFragmentCallback()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment1.newInstance())
                    .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment? = when (item.itemId) {
            R.id.add1 -> Fragment1.newInstance()
            R.id.add2 -> Fragment2.newInstance()
            R.id.add3 -> Fragment3.newInstance()
            R.id.add4 -> Fragment4.newInstance()
            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, it, it.javaClass.simpleName)
                    .commit()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun registerFragmentCallback() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                super.onFragmentCreated(fm, f, savedInstanceState)
                Log.d("aaa", "${f.javaClass.simpleName} : onFragmentCreated")
            }

            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                Log.d("aaa", "${f.javaClass.simpleName} : onFragmentStarted")
                super.onFragmentStarted(fm, f)
                frameMetricsAggregator = FrameMetricsAggregator().apply {
                    add(this@MainActivity)
                }
                trace = FirebasePerformance.startTrace(f.javaClass.simpleName)
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                Log.d("aaa", "$${f.javaClass.simpleName} : onFragmentResumed")
                super.onFragmentResumed(fm, f)
            }

            override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                Log.d("aaa", "$${f.javaClass.simpleName} : onFragmentPaused")
                super.onFragmentPaused(fm, f)
            }

            override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                Log.d("aaa", "${f.javaClass.simpleName} : onFragmentStopped")
                super.onFragmentStopped(fm, f)

                val performanceTag = f.javaClass.simpleName

                var totalSampleNum = 0
                var slowRenderingCount = 0
                var frozenFrameCount = 0

                val collectedMetrics = frameMetricsAggregator.remove(this@MainActivity)
                collectedMetrics?.get(0)?.let { totalDuration ->
                    for (i in 0 until totalDuration.size()) {
                        val frameDuration = totalDuration.keyAt(i)
                        val sampleNum = totalDuration.valueAt(i)
                        totalSampleNum += sampleNum
                        if (frameDuration > 700) {
                            frozenFrameCount += sampleNum
                        }

                        if (frameDuration > 16) {
                            slowRenderingCount += sampleNum
                        }
                    }
                }

                if (totalSampleNum > 0) {
                    trace.incrementCounter("_fr_tot", totalSampleNum.toLong())
                }

                if (slowRenderingCount > 0) {
                    trace.incrementCounter("_fr_slo", slowRenderingCount.toLong())
                }

                if (frozenFrameCount > 0) {
                    trace.incrementCounter("_fr_fzn", frozenFrameCount.toLong())
                }

                Log.d("FirebasePerformance",
                        (StringBuilder(81 + performanceTag.length))
                                .append("sendScreenTrace name:").append(performanceTag)
                                .append(" _fr_tot:").append(totalSampleNum)
                                .append(" _fr_slo:").append(slowRenderingCount)
                                .append(" _fr_fzn:").append(frozenFrameCount)
                                .toString()
                )

                trace.stop()
            }
        }, true)
    }
}
