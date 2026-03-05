package ywcheong.sofia.domain.systemphase.enums

enum class SystemPhaseType {
    INACTIVE,
    RECRUIT,
    TRANSLATE,
    SETTLE,
    ;

    val next: SystemPhaseType
        get() =
            when (this) {
                INACTIVE -> RECRUIT
                RECRUIT -> TRANSLATE
                TRANSLATE -> SETTLE
                SETTLE -> INACTIVE
            }
}
