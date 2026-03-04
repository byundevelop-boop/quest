package com.kurly.android.quest.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SectionTypeTest {

    // 정상 값 테스트
    // 서버에서 내려오는 정확한 타입 문자열이
    // 각각 예상되는 enum(VERTICAL/HORIZONTAL/GRID)로 바뀌는지 확인
    @Test
    fun `maps known raw values`() {
        assertEquals(SectionType.VERTICAL, SectionType.from("vertical"))
        assertEquals(SectionType.HORIZONTAL, SectionType.from("horizontal"))
        assertEquals(SectionType.GRID, SectionType.from("grid"))
    }

    // 입력 보정 테스트
    // 대소문자/공백이 섞인 문자열도 trim + lowercase 처리로
    // 올바른 enum으로 매핑되는지 확인
    @Test
    fun `normalizes casing and spaces`() {
        assertEquals(SectionType.VERTICAL, SectionType.from(" VERTICAL "))
        assertEquals(SectionType.HORIZONTAL, SectionType.from("HoRiZoNtAl"))
    }

    // 실패/예외 입력 방어 테스트
    // 알 수 없는 문자열이 들어와도 앱이 크래시하지 않도록
    // 기본값(VERTICAL)으로 안전하게 fallback 되는지 확인
    @Test
    fun `maps unknown raw value to vertical as safe fallback`() {
        assertEquals(SectionType.VERTICAL, SectionType.from("unexpected"))
    }
}
