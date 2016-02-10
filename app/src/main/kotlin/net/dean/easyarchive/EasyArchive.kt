package net.dean.easyarchive

import android.app.Application
import net.dean.easyarchive.library.InflaterAggregation

class EasyArchive : Application() {
    companion object {
        val inflaters = InflaterAggregation()
    }
}
