package cn.jingedawang.bluetoothdemo;

import android.bluetooth.BluetoothSocket;

public class BluetoothUtils {

	private static BluetoothSocket mmSocket = null;
	
	public static void setBluetoothSocket(BluetoothSocket socket) {
		mmSocket = socket;
	}
	
	public static BluetoothSocket getBluetoothSocket() {
		if(mmSocket != null) {
			return mmSocket;
		}
		return null;
	}
	
}
