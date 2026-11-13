package com.kitewind.weaver.data.sample

import com.kitewind.weaver.domain.model.HillSpec
import com.kitewind.weaver.domain.model.KiteSpec

object SampleData {
    val hills = listOf(
        HillSpec("clover_rise", 0, 12),
        HillSpec("daisy_drift", 1, 16),
        HillSpec("poppy_fold", 2, 20),
        HillSpec("iris_crest", 3, 24),
        HillSpec("buttercup_hill", 4, 28),
        HillSpec("wildflower_crown", 5, 32)
    )
    val kites = listOf(
        KiteSpec("blossom_diamond", 0, 0),
        KiteSpec("leaf_dancer", 1, 1),
        KiteSpec("ribbon_swallow", 2, 3),
        KiteSpec("meadow_star", 3, 6)
    )
}
