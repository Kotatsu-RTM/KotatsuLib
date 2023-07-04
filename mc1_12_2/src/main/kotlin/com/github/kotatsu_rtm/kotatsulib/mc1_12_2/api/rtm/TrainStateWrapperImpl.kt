package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.rtm

import com.github.kotatsu_rtm.kotatsulib.api.rtm.TrainStateWrapper
import jp.ngt.rtm.entity.train.util.EnumNotch

class TrainStateWrapperImpl private constructor() {
    object DirectionImpl : TrainStateWrapper.Direction.IDirection<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> TrainStateWrapper.Direction.FRONT
                1 -> TrainStateWrapper.Direction.BACK
                else -> throw IllegalArgumentException("Argument must be 0 or 1")
            }

        override fun toNative(wrapperValue: TrainStateWrapper.Direction) =
            when (wrapperValue) {
                TrainStateWrapper.Direction.FRONT -> 0
                TrainStateWrapper.Direction.BACK -> 1
            }
    }

    object NotchImpl : TrainStateWrapper.Notch.INotch<EnumNotch> {
        fun fromInt(int: Int) = fromNative(EnumNotch.getNotch(int))

        override fun fromNative(native: EnumNotch) =
            when (native) {
                EnumNotch.emergency_brake -> TrainStateWrapper.Notch.EB
                EnumNotch.brake_7 -> TrainStateWrapper.Notch.B7
                EnumNotch.brake_6 -> TrainStateWrapper.Notch.B6
                EnumNotch.brake_5 -> TrainStateWrapper.Notch.B5
                EnumNotch.brake_4 -> TrainStateWrapper.Notch.B4
                EnumNotch.brake_3 -> TrainStateWrapper.Notch.B3
                EnumNotch.brake_2 -> TrainStateWrapper.Notch.B2
                EnumNotch.brake_1 -> TrainStateWrapper.Notch.B1
                EnumNotch.inertia -> TrainStateWrapper.Notch.N
                EnumNotch.accelerate_1 -> TrainStateWrapper.Notch.P1
                EnumNotch.accelerate_2 -> TrainStateWrapper.Notch.P2
                EnumNotch.accelerate_3 -> TrainStateWrapper.Notch.P3
                EnumNotch.accelerate_4 -> TrainStateWrapper.Notch.P4
                EnumNotch.accelerate_5 -> TrainStateWrapper.Notch.P5
            }

        fun toInt(wrapperValue: TrainStateWrapper.Notch) = toNative(wrapperValue).id

        override fun toNative(wrapperValue: TrainStateWrapper.Notch) =
            when (wrapperValue) {
                TrainStateWrapper.Notch.EB -> EnumNotch.emergency_brake
                TrainStateWrapper.Notch.B7 -> EnumNotch.brake_7
                TrainStateWrapper.Notch.B6 -> EnumNotch.brake_6
                TrainStateWrapper.Notch.B5 -> EnumNotch.brake_5
                TrainStateWrapper.Notch.B4 -> EnumNotch.brake_4
                TrainStateWrapper.Notch.B3 -> EnumNotch.brake_3
                TrainStateWrapper.Notch.B2 -> EnumNotch.brake_2
                TrainStateWrapper.Notch.B1 -> EnumNotch.brake_1
                TrainStateWrapper.Notch.N -> EnumNotch.inertia
                TrainStateWrapper.Notch.P1 -> EnumNotch.accelerate_1
                TrainStateWrapper.Notch.P2 -> EnumNotch.accelerate_2
                TrainStateWrapper.Notch.P3 -> EnumNotch.accelerate_3
                TrainStateWrapper.Notch.P4 -> EnumNotch.accelerate_4
                TrainStateWrapper.Notch.P5 -> EnumNotch.accelerate_5
            }

        override fun getMaxSpeed(notch: TrainStateWrapper.Notch) = toNative(notch).max_speed

        override fun getAcceleration(notch: TrainStateWrapper.Notch) = toNative(notch).acceleration
    }

    object DoorImpl : TrainStateWrapper.Door.IDoor<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> TrainStateWrapper.Door.CLOSED
                1 -> TrainStateWrapper.Door.RIGHT_OPEN
                2 -> TrainStateWrapper.Door.LEFT_OPEN
                3 -> TrainStateWrapper.Door.BOTH_OPEN
                else -> throw IllegalArgumentException("Argument must between 0 to 3")
            }

        override fun toNative(wrapperValue: TrainStateWrapper.Door) =
            when (wrapperValue) {
                TrainStateWrapper.Door.CLOSED -> 0
                TrainStateWrapper.Door.RIGHT_OPEN -> 1
                TrainStateWrapper.Door.LEFT_OPEN -> 2
                TrainStateWrapper.Door.BOTH_OPEN -> 3
            }
    }

    object LightImpl : TrainStateWrapper.Light.ILight<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> TrainStateWrapper.Light.OFF
                1 -> TrainStateWrapper.Light.ON
                2 -> TrainStateWrapper.Light.FULLY_ON
                else -> throw IllegalArgumentException("Argument must between 0 to 2")
            }

        override fun toNative(wrapperValue: TrainStateWrapper.Light) =
            when (wrapperValue) {
                TrainStateWrapper.Light.OFF -> 0
                TrainStateWrapper.Light.ON -> 1
                TrainStateWrapper.Light.FULLY_ON -> 2
            }
    }

    object PantographImpl : TrainStateWrapper.Pantograph.IPantograph<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> TrainStateWrapper.Pantograph.DOWN
                1 -> TrainStateWrapper.Pantograph.UP
                else -> throw IllegalArgumentException("Argument must be 0 or 1")
            }

        override fun toNative(wrapperValue: TrainStateWrapper.Pantograph) =
            when (wrapperValue) {
                TrainStateWrapper.Pantograph.DOWN -> 0
                TrainStateWrapper.Pantograph.UP -> 1
            }
    }

    object RoleImpl : TrainStateWrapper.Role.IRole<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> TrainStateWrapper.Role.FRONT
                1 -> TrainStateWrapper.Role.MIDDLE
                2 -> TrainStateWrapper.Role.BACK
                else -> throw IllegalArgumentException("Argument must between 0 to 2")
            }

        override fun toNative(wrapperValue: TrainStateWrapper.Role) =
            when (wrapperValue) {
                TrainStateWrapper.Role.FRONT -> 0
                TrainStateWrapper.Role.MIDDLE -> 1
                TrainStateWrapper.Role.BACK -> 2
            }
    }

    object InteriorLightImpl : TrainStateWrapper.InteriorLight.IInteriorLight<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> TrainStateWrapper.InteriorLight.OFF
                1 -> TrainStateWrapper.InteriorLight.ON
                2 -> TrainStateWrapper.InteriorLight.FULLY_ON
                else -> throw IllegalArgumentException("Argument must between 0 to 2")
            }

        override fun toNative(wrapperValue: TrainStateWrapper.InteriorLight) =
            when (wrapperValue) {
                TrainStateWrapper.InteriorLight.OFF -> 0
                TrainStateWrapper.InteriorLight.ON -> 1
                TrainStateWrapper.InteriorLight.FULLY_ON -> 2
            }
    }
}
