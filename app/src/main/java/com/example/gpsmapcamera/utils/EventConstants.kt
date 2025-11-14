package com.example.gpsmapcamera.utils

object EventConstants {
    // Activities / Screens
    const val SPLASH_SCREEN = "Splash"
    const val LANGUAGE_SCREEN = "Language"
    const val ONBOARDING_SCREEN = "Onboarding"
    const val PERMISSION_SCREEN = "Permission Screen"
    const val LANDING_SCREEN = "Landing Screen"
    const val CAMERA_SCREEN = "Camera Screen"
    const val TEMPLATE_SCREEN = "Template Screen"

    // Events
    const val EVENT_SPLASH = "splash"
    const val EVENT_SPLASH_TIME = "splash_time"
    const val EVENT_LANGUAGE = "language"
    const val EVENT_ONBOARDING = "onboarding"
    const val EVENT_PERMISSION = "permission"
    const val EVENT_LANDING = "landing"
    const val EVENT_CAMERA = "camera"
    const val EVENT_TEMPLATE = "template"

    // Parameters
    const val PARAM_SCREEN = "screen"
    const val PARAM_START = "start"
    const val PARAM_COLD = "cold"
    const val PARAM_WARM = "warm"
    const val PARAM_HOT = "hot"
    const val PARAM_SELECT = "select"
    const val PARAM_OTHER = "other"
    const val PARAM_NEXT = "next"
    const val PARAM_SKIP = "skip"
    const val PARAM_MODE = "mode"
    const val PARAM_ACTION = "action"
    const val PARAM_VIEW = "view"
    const val PARAM_FEATURE_NAME = "feature_name"
    const val PARAM_ZOOM = "zoom"
    const val PARAM_ACTION_TYPE = "action_type"
    const val PARAM_CLICKED = "clicked"
    const val PARAM_ALLOW = "allow"
    const val PARAM_SELECTED = "selected"
    const val PARAM_ALLOW_VALUE = "all / camera / location / gallery"
    const val PARAM_VALUE_SHOWN = "shown"
    const val PARAM_VALUE_NOT_SHOWN = "not_shown"

    const val PARAM_VALUE_PHOTO_CAPTURED = "photo_captured"
    const val PARAM_VALUE_VIDEO_CAPTURED = "video_captured"
    const val PARAM_VALUE_QUICK_SHARE_CAPTURED = "quick_share_captured"
    const val PARAM_VALUE_FRONT = "front"
    const val PARAM_VALUE_BACK = "back"
    const val PARAM_VALUE_1X = "1x"
    const val PARAM_VALUE_2X = "2x"

    // Ratios
    const val PARAM_VALUE_RATIO_4_3 = "ratio_4:3"
    const val PARAM_VALUE_RATIO_16_9 = "ratio_16:9"

    // Grid layouts
    const val PARAM_VALUE_GRID_2 = "grid2"
    const val PARAM_VALUE_GRID_3 = "grid3"
    const val PARAM_VALUE_GRID_4 = "grid4"

    // Timer
    const val PARAM_VALUE_TIMER_3_SEC = "timer3sec"
    const val PARAM_VALUE_TIMER_5_SEC = "timer5sec"

    // Mirror
    const val PARAM_VALUE_MIRROR_ON = "mirror_on"
    const val PARAM_VALUE_MIRROR_OFF = "mirror_off"

    // Volume
    const val PARAM_VALUE_VOLUME = "volume"

    // White balance
    const val PARAM_VALUE_WHITE_BALANCE = "white_balance"

    // Camera level
    const val PARAM_VALUE_CAMERA_LEVEL = "camera_level"

    // Focus
    const val PARAM_VALUE_FOCUS = "focus"

    // Setting
    const val PARAM_VALUE_SETTING = "setting"

    // File name
    const val PARAM_VALUE_FILE_NAME = "file_name"

    // Saved folder
    const val PARAM_VALUE_SAVED_FOLDER = "saved_folder"
}
