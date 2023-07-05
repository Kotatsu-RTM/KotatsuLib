package com.github.kotatsu_rtm.kotatsulib.api.rtm

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class TrainStateWrapper<T : Any>(private val entity: T) {
    protected abstract fun <U : Any> getState(entity: T, state: TrainState<U>): U

    protected abstract fun <U : Any> setState(entity: T, state: TrainState<U>, value: U)

    var direction: Direction
        get() = getState(entity, TrainState.Direction)
        set(value) = setState(entity, TrainState.Direction, value)

    var notch: Notch
        get() = getState(entity, TrainState.Notch)
        set(value) = setState(entity, TrainState.Notch, value)

    var door: Door
        get() = getState(entity, TrainState.Door)
        set(value) = setState(entity, TrainState.Door, value)

    var light: Light
        get() = getState(entity, TrainState.Light)
        set(value) = setState(entity, TrainState.Light, value)

    var pantograph: Pantograph
        get() = getState(entity, TrainState.Pantograph)
        set(value) = setState(entity, TrainState.Pantograph, value)

    var destination: Int
        get() = getState(entity, TrainState.Destination)
        set(value) = setState(entity, TrainState.Destination, value)

    var announcement: Int
        get() = getState(entity, TrainState.Announcement)
        set(value) = setState(entity, TrainState.Announcement, value)

    var role: Role
        get() = getState(entity, TrainState.Role)
        set(value) = setState(entity, TrainState.Role, value)

    var interiorLight: InteriorLight
        get() = getState(entity, TrainState.InteriorLight)
        set(value) = setState(entity, TrainState.InteriorLight, value)

    sealed interface TrainState<T : Any> {
        object Direction : TrainState<TrainStateWrapper.Direction>
        object Notch : TrainState<TrainStateWrapper.Notch>
        object Signal : TrainState<Int>
        object Door : TrainState<TrainStateWrapper.Door>
        object Light : TrainState<TrainStateWrapper.Light>
        object Pantograph : TrainState<TrainStateWrapper.Pantograph>
        object Destination : TrainState<Int>
        object Announcement : TrainState<Int>
        object Role : TrainState<TrainStateWrapper.Role>
        object InteriorLight : TrainState<TrainStateWrapper.InteriorLight>

        interface ITrainState<T : Any> : NativeBridge<T, TrainState<*>>
    }

    enum class Direction {
        FRONT,
        BACK;

        interface IDirection<T : Any> : NativeBridge<T, Direction>
    }

    enum class Notch(private val numerical: Int) {
        EB(-8),
        B7(-7),
        B6(-6),
        B5(-5),
        B4(-4),
        B3(-3),
        B2(-2),
        B1(-1),
        N(0),
        P1(1),
        P2(2),
        P3(3),
        P4(4),
        P5(5);

        val isBrake = numerical < 0

        val isAccelerator = numerical > 0

        fun getBreakingDistance(impl: INotch<*>, speed: Float): Float {
            if (numerical >= 0) return 0.0F

            val deceleration = abs(impl.getAcceleration(this)) * 1443.0F
            return speed / 3.6F * (speed / deceleration) / 2.0F
        }

        @Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
        class Range(val start: Notch, val endInclusion: Notch) {
            private val _range =
                IntRange(min(start.numerical, endInclusion.numerical), max(start.numerical, endInclusion.numerical))

            operator fun contains(notch: Notch) = _range.contains(notch.numerical)
        }

        interface INotch<T : Any> : NativeBridge<T, Notch> {
            fun getMaxSpeed(notch: Notch): Float

            fun getAcceleration(notch: Notch): Float
        }
    }

    enum class Door {
        CLOSED,
        LEFT_OPEN,
        RIGHT_OPEN,
        BOTH_OPEN;

        interface IDoor<T : Any> : NativeBridge<T, Door>
    }

    enum class Light {
        OFF,
        ON,
        FULLY_ON;

        interface ILight<T : Any> : NativeBridge<T, Light>
    }

    enum class Pantograph {
        DOWN,
        UP;

        interface IPantograph<T : Any> : NativeBridge<T, Pantograph>
    }

    enum class Role {
        FRONT,
        MIDDLE,
        BACK;

        interface IRole<T : Any> : NativeBridge<T, Role>
    }

    enum class InteriorLight {
        OFF,
        ON,
        FULLY_ON;

        interface IInteriorLight<T : Any> : NativeBridge<T, InteriorLight>
    }

    interface NativeBridge<T : Any, U : Any> {
        fun fromNative(native: T): U

        fun toNative(wrapperValue: U): T
    }
}
