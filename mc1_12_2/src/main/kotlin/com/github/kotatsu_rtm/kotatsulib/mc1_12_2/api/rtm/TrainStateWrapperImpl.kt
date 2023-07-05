package com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.rtm

import com.github.kotatsu_rtm.kotatsulib.api.rtm.TrainStateWrapper
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.EnumNotch
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType

class TrainStateWrapperImpl(entity: EntityTrainBase) : TrainStateWrapper<EntityTrainBase>(entity) {
    @Suppress("UNCHECKED_CAST")
    override fun <U : Any> getState(entity: EntityTrainBase, state: TrainState<U>) =
        when (state) {
            TrainState.Direction ->
                DirectionImpl.fromNative(entity.getVehicleState(TrainStateType.Direction).toInt())

            TrainState.Notch ->
                NotchImpl.fromInt(entity.getVehicleState(TrainStateType.Notch).toInt())

            TrainState.Signal ->
                entity.getVehicleState(TrainStateType.Signal).toInt()

            TrainState.Door ->
                DoorImpl.fromNative(entity.getVehicleState(TrainStateType.Door).toInt())

            TrainState.Light ->
                LightImpl.fromNative(entity.getVehicleState(TrainStateType.Light).toInt())

            TrainState.Pantograph ->
                PantographImpl.fromNative(entity.getVehicleState(TrainStateType.Pantograph).toInt())

            TrainState.Destination ->
                entity.getVehicleState(TrainStateType.Destination).toInt()

            TrainState.Announcement ->
                entity.getVehicleState(TrainStateType.Announcement).toInt()

            TrainState.Role ->
                RoleImpl.fromNative(entity.getVehicleState(TrainStateType.Role).toInt())

            TrainState.InteriorLight ->
                InteriorLightImpl.fromNative(entity.getVehicleState(TrainStateType.InteriorLight).toInt())
        } as U

    override fun <U : Any> setState(entity: EntityTrainBase, state: TrainState<U>, value: U) {
        when (state) {
            TrainState.Direction ->
                entity.setVehicleState(TrainStateType.Direction, DirectionImpl.toNative(value as Direction).toByte())

            TrainState.Notch ->
                entity.setVehicleState(TrainStateType.Notch, NotchImpl.toNative(value as Notch).id.toByte())

            TrainState.Signal ->
                entity.setVehicleState(TrainStateType.Signal, (value as Int).toByte())

            TrainState.Door ->
                entity.setVehicleState(TrainStateType.Door, DoorImpl.toNative(value as Door).toByte())

            TrainState.Light ->
                entity.setVehicleState(TrainStateType.Light, LightImpl.toNative(value as Light).toByte())

            TrainState.Pantograph ->
                entity.setVehicleState(TrainStateType.Pantograph, PantographImpl.toNative(value as Pantograph).toByte())

            TrainState.Destination ->
                entity.setVehicleState(TrainStateType.Destination, (value as Int).toByte())

            TrainState.Announcement ->
                entity.setVehicleState(TrainStateType.Announcement, (value as Int).toByte())

            TrainState.Role ->
                entity.setVehicleState(TrainStateType.Role, RoleImpl.toNative(value as Role).toByte())

            TrainState.InteriorLight ->
                entity.setVehicleState(
                    TrainStateType.InteriorLight,
                    InteriorLightImpl.toNative(value as InteriorLight).toByte()
                )
        }
    }

    object TrainStateImpl : TrainState.ITrainState<TrainStateType> {
        override fun fromNative(native: TrainStateType) =
            when (native) {
                TrainStateType.Direction -> TrainState.Direction
                TrainStateType.Notch -> TrainState.Notch
                TrainStateType.Signal -> TrainState.Signal
                TrainStateType.Door -> TrainState.Door
                TrainStateType.Light -> TrainState.Light
                TrainStateType.Pantograph -> TrainState.Pantograph
                TrainStateType.Destination -> TrainState.Destination
                TrainStateType.Announcement -> TrainState.Announcement
                TrainStateType.Role -> TrainState.Role
                TrainStateType.InteriorLight -> TrainState.InteriorLight
                else -> throw IllegalArgumentException("Unsupported type $native")
            }

        override fun toNative(wrapperValue: TrainState<*>) =
            when (wrapperValue) {
                TrainState.Direction -> TrainStateType.Direction
                TrainState.Notch -> TrainStateType.Notch
                TrainState.Signal -> TrainStateType.Signal
                TrainState.Door -> TrainStateType.Door
                TrainState.Light -> TrainStateType.Light
                TrainState.Pantograph -> TrainStateType.Pantograph
                TrainState.Destination -> TrainStateType.Destination
                TrainState.Announcement -> TrainStateType.Announcement
                TrainState.Role -> TrainStateType.Role
                TrainState.InteriorLight -> TrainStateType.InteriorLight
            }
    }

    object DirectionImpl : Direction.IDirection<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> Direction.FRONT
                1 -> Direction.BACK
                else -> throw IllegalArgumentException("Argument must be 0 or 1")
            }

        override fun toNative(wrapperValue: Direction) =
            when (wrapperValue) {
                Direction.FRONT -> 0
                Direction.BACK -> 1
            }
    }

    object NotchImpl : Notch.INotch<EnumNotch> {
        fun fromInt(int: Int) = fromNative(EnumNotch.getNotch(int))

        override fun fromNative(native: EnumNotch) =
            when (native) {
                EnumNotch.emergency_brake -> Notch.EB
                EnumNotch.brake_7 -> Notch.B7
                EnumNotch.brake_6 -> Notch.B6
                EnumNotch.brake_5 -> Notch.B5
                EnumNotch.brake_4 -> Notch.B4
                EnumNotch.brake_3 -> Notch.B3
                EnumNotch.brake_2 -> Notch.B2
                EnumNotch.brake_1 -> Notch.B1
                EnumNotch.inertia -> Notch.N
                EnumNotch.accelerate_1 -> Notch.P1
                EnumNotch.accelerate_2 -> Notch.P2
                EnumNotch.accelerate_3 -> Notch.P3
                EnumNotch.accelerate_4 -> Notch.P4
                EnumNotch.accelerate_5 -> Notch.P5
            }

        fun toInt(wrapperValue: Notch) = toNative(wrapperValue).id

        override fun toNative(wrapperValue: Notch) =
            when (wrapperValue) {
                Notch.EB -> EnumNotch.emergency_brake
                Notch.B7 -> EnumNotch.brake_7
                Notch.B6 -> EnumNotch.brake_6
                Notch.B5 -> EnumNotch.brake_5
                Notch.B4 -> EnumNotch.brake_4
                Notch.B3 -> EnumNotch.brake_3
                Notch.B2 -> EnumNotch.brake_2
                Notch.B1 -> EnumNotch.brake_1
                Notch.N -> EnumNotch.inertia
                Notch.P1 -> EnumNotch.accelerate_1
                Notch.P2 -> EnumNotch.accelerate_2
                Notch.P3 -> EnumNotch.accelerate_3
                Notch.P4 -> EnumNotch.accelerate_4
                Notch.P5 -> EnumNotch.accelerate_5
            }

        override fun getMaxSpeed(notch: Notch) = toNative(notch).max_speed

        override fun getAcceleration(notch: Notch) = toNative(notch).acceleration
    }

    object DoorImpl : Door.IDoor<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> Door.CLOSED
                1 -> Door.RIGHT_OPEN
                2 -> Door.LEFT_OPEN
                3 -> Door.BOTH_OPEN
                else -> throw IllegalArgumentException("Argument must between 0 to 3")
            }

        override fun toNative(wrapperValue: Door) =
            when (wrapperValue) {
                Door.CLOSED -> 0
                Door.RIGHT_OPEN -> 1
                Door.LEFT_OPEN -> 2
                Door.BOTH_OPEN -> 3
            }
    }

    object LightImpl : Light.ILight<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> Light.OFF
                1 -> Light.ON
                2 -> Light.FULLY_ON
                else -> throw IllegalArgumentException("Argument must between 0 to 2")
            }

        override fun toNative(wrapperValue: Light) =
            when (wrapperValue) {
                Light.OFF -> 0
                Light.ON -> 1
                Light.FULLY_ON -> 2
            }
    }

    object PantographImpl : Pantograph.IPantograph<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> Pantograph.DOWN
                1 -> Pantograph.UP
                else -> throw IllegalArgumentException("Argument must be 0 or 1")
            }

        override fun toNative(wrapperValue: Pantograph) =
            when (wrapperValue) {
                Pantograph.DOWN -> 0
                Pantograph.UP -> 1
            }
    }

    object RoleImpl : Role.IRole<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> Role.FRONT
                1 -> Role.MIDDLE
                2 -> Role.BACK
                else -> throw IllegalArgumentException("Argument must between 0 to 2")
            }

        override fun toNative(wrapperValue: Role) =
            when (wrapperValue) {
                Role.FRONT -> 0
                Role.MIDDLE -> 1
                Role.BACK -> 2
            }
    }

    object InteriorLightImpl : InteriorLight.IInteriorLight<Int> {
        override fun fromNative(native: Int) =
            when (native) {
                0 -> InteriorLight.OFF
                1 -> InteriorLight.ON
                2 -> InteriorLight.FULLY_ON
                else -> throw IllegalArgumentException("Argument must between 0 to 2")
            }

        override fun toNative(wrapperValue: InteriorLight) =
            when (wrapperValue) {
                InteriorLight.OFF -> 0
                InteriorLight.ON -> 1
                InteriorLight.FULLY_ON -> 2
            }
    }
}
