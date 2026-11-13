package com.kitewind.weaver.domain.usecase

import com.kitewind.weaver.domain.model.FlowerPlacement
import com.kitewind.weaver.domain.model.HillSpec
import com.kitewind.weaver.domain.model.MeadowProgress

class MeadowRules(val hills: List<HillSpec>) {
    fun unlocked(hill: Int, progress: MeadowProgress): Boolean =
        hill in hills.indices && (hill == 0 || progress.flowers[hill - 1] >= hills[hill - 1].target)
    fun completed(progress: MeadowProgress): Int = hills.count { progress.flowers[it.index] >= it.target }
    fun destination(preferred: Int, progress: MeadowProgress): Int =
        if (unlocked(preferred, progress) && progress.flowers[preferred] < hills[preferred].target) preferred
        else hills.firstOrNull { unlocked(it.index, progress) && progress.flowers[it.index] < it.target }?.index ?: preferred
    fun flowers(hill: Int, count: Int): List<FlowerPlacement> = (0 until count.coerceIn(0, hills[hill].target)).map { slot ->
        val cell = (13 * slot + 5 * hill) % 32
        FlowerPlacement(((cell % 8) + 0.5f) / 8f, ((cell / 8) + 0.5f) / 4f, hill % 5)
    }
}
