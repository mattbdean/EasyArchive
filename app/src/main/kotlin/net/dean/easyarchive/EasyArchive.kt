package net.dean.easyarchive

import android.app.Application
import net.dean.easyarchive.library.InflaterAggregation

public class EasyArchive : Application() {
    companion object {
        public val inflaters = InflaterAggregation()
    }
}
