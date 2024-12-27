package com.example.kh_studyprojects_weatherapp.type

enum class ViewType(val typeValue: Int) {
    YESTERDAY(0),
    TODAY(1),
    OTHER(2);

    companion object {
        fun fromOrdinal(ordinal: Int): ViewType {
            return values().getOrElse(ordinal) { OTHER }
        }
    }
}
