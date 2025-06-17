/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Ev3 message opcodes
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ev3

/* Android import */
import android.os.Parcelable

/* Kotlinx import */
import kotlinx.parcelize.Parcelize

@Parcelize                /* makes it bundle-friendly for NavComponent */
enum class Ev3OpCode(val code: Int) : Parcelable {
    SOUND(0x94),
    SOUND_TEST(0x95),
    SOUND_READY(0x96),
    INPUT_DEVICE_LIST(0x98),
    INPUT_DEVICE(0x99),
    READ(0x9A),
    TEST(0x9B),
    READY(0x9C),
    READ_SI(0x9D),
    READ_EXT(0x9E),
    WRITE(0x9F),
    OUTPUT_SET_TYPE(0xA1),
    OUTPUT_RESET(0xA2),
    OUTPUT_STOP(0xA3),
    OUTPUT_POWER(0xA4),
    OUTPUT_SPEED(0xA5),
    OUTPUT_START(0xA6),
    OUTPUT_POLARITY(0xA7),
    OUTPUT_READ(0xA8),
    OUTPUT_TEST(0xA9),
    OUTPUT_READY(0xAA),
    OUTPUT_STEP_POWER(0xAC),
    OUTPUT_TIME_POWER(0xAD),
    OUTPUT_STEP_SPEED(0xAE),
    OUTPUT_TIME_SPEED(0xAF),
    OUTPUT_STEP_SYNC(0xB0),
    OUTPUT_TIME_SYNC(0xB1),
    OUTPUT_CLR_COUNT(0xB2),
    OUTPUT_GET_COUNT(0xB3),
    OUTPUT_PRG_STOP(0xB4);
}