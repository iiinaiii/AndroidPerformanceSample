package com.sample.iiinaiii.androidperformancesample.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.sample.iiinaiii.androidperformancesample.R
import com.sample.iiinaiii.androidperformancesample.util.FragmentPerformanceTracer

class MainActivity : AppCompatActivity() {

    private val performanceTracer = FragmentPerformanceTracer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        performanceTracer.start(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment1.newInstance())
                    .commitNow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        performanceTracer.stop(this)
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
}
