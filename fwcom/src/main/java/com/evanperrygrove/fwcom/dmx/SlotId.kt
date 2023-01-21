package com.evanperrygrove.fwcom.dmx

object SlotId {
    const val INTENSITY = 0x0001
    const val INTENSITY_MASTER = 0x0002

    const val PAN = 0x0101
    const val TILT = 0x0101

    const val COLOR_WHEEL = 0x0201
    const val COLOR_SUB_CYAN = 0x0202
    const val COLOR_SUB_YELLOW = 0x0203
    const val COLOR_SUB_MAGENTA = 0x0204
    const val COLOR_ADD_RED = 0x0205
    const val COLOR_ADD_GREEN = 0x0206
    const val COLOR_ADD_BLUE = 0x0207
    const val COLOR_CORRECTION = 0x0208
    const val COLOR_SCROLL = 0x0209
    const val COLOR_SEMAPHORE = 0x0210
    const val COLOR_ADD_AMBER = 0x0211
    const val COLOR_ADD_WHITE = 0x0212
    const val COLOR_ADD_WARM_WHITE = 0x0213
    const val COLOR_ADD_COOL_WHITE = 0x0214
    const val COLOR_SUB_UV = 0x0215
    const val COLOR_HUE = 0x0216
    const val COLOR_SATURATION = 0x0217

    const val STATIC_GOBO_WHEEL = 0x0301
    const val ROTO_GOBO_WHEEL = 0x0302
    const val PRISM_WHEEL = 0x0303
    const val EFFECTS_WHEEL = 0x0304

    const val BEAM_SIZE_IRIS = 0x0401
    const val EDGE = 0x0402
    const val FROST = 0x0403
    const val STROBE = 0x0404
    const val ZOOM = 0x0405
    const val FRAMING_SHUTTER = 0x0406
    const val SHUTTER_ROTATE = 0x0407
    const val DOUSER = 0x0408
    const val BARN_DOOR = 0x0409

    const val LAMP_CONTROL = 0x0501
    const val FIXTURE_CONTROL = 0x0502
    const val FIXTURE_SPEED = 0x0503
    const val MACRO = 0x0504

    const val UNDEFINED = 0xFFFF
}