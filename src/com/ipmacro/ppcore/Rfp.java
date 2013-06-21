package com.ipmacro;

public class Rfp {

	public static boolean start(String FileID) {
		synchronized (PPCore.mMutex) {
			return nativeStart(FileID);
		}
	}

	public static boolean stop(String FileID) {
		synchronized (PPCore.mMutex) {
			return nativeStop(FileID);
		}
	}

	public static boolean addPeer(String FileID, int PeerID, String Agent,
			String Ip, short Port, boolean Nat) {
		synchronized (PPCore.mMutex) {
			return nativeAddPeer(FileID, PeerID, Agent, Ip, Port, Nat);
		}
	}

	public static int getProgress(String FileID) {
		synchronized (PPCore.mMutex) {
			return nativeGetProgress(FileID);
		}
	}

	public static int getRate(String FileID) {
		synchronized (PPCore.mMutex) {
			return nativeGetRate(FileID);
		}
	}

	private Rfp() {
	}

	private static native boolean nativeStart(String FileID);

	private static native boolean nativeStop(String FileID);

	private static native boolean nativeAddPeer(String FileID, int PeerID,
			String Agent, String Ip, short Port, boolean Nat);

	private static native int nativeGetProgress(String FileID);

	private static native int nativeGetRate(String FileID);

}
