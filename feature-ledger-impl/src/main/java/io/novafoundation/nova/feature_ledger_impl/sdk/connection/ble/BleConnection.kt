package io.novafoundation.nova.feature_ledger_impl.sdk.connection.ble

import android.bluetooth.BluetoothDevice
import io.novafoundation.nova.feature_ledger_api.sdk.connection.LedgerConnection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend

class BleConnection(
    private val bleManager: LedgerBleManager,
    private val bluetoothDevice: BluetoothDevice,
) : LedgerConnection, DataReceivedCallback {

    suspend fun connect() {
        bleManager.connect(bluetoothDevice).suspend()

        bleManager.readCallback = this
    }

    override val type: LedgerConnection.Type = LedgerConnection.Type.BLE

    override val isActive: Flow<Boolean>
        get() = bleManager.stateAsFlow()
            .map { it == ConnectionState.Ready }

    override suspend fun mtu(): Int {
        ensureCorrectDevice()

        return bleManager.deviceMtu
    }

    override suspend fun send(chunks: List<ByteArray>) {
        ensureCorrectDevice()

        bleManager.send(chunks)
    }

    override val receiveChannel = Channel<ByteArray>(Channel.BUFFERED)

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        ensureCorrectDevice()

        data.value?.let(receiveChannel::trySend)
    }

    private fun ensureCorrectDevice() = require(bleManager.bluetoothDevice?.address == bluetoothDevice.address) {
        "Wrong device connected"
    }
}
