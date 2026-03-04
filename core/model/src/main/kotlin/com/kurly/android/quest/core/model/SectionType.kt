package com.kurly.android.quest.core.model

import java.util.Locale

/** 어떻게 그릴지에 대한 타입 */
enum class SectionType {
    VERTICAL,
    HORIZONTAL,
    GRID;

    companion object {
        /** 서버에서 받은 문자열을 SectionType 으로 변환 */
        fun from(raw: String): SectionType {
            return when (raw.trim().lowercase(Locale.US)) {
                "vertical" -> VERTICAL
                "horizontal" -> HORIZONTAL
                "grid" -> GRID
                else -> VERTICAL
            }
        }
    }
}
