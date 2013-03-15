package ece1779.ec2;

public class WorkerRecord
{
	private String instanceID = "";
	private double cpuLoad = 0.0d;
	private boolean isActive = false;
	private boolean isStopped = false;
	private long lastInactivated = 0l;

	public String getInstanceID() {
		return instanceID;
	}

	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	public double getCpuLoad() {
		return cpuLoad;
	}

	public void setCpuLoad(double cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isStopped() {
		return isStopped;
	}

	public void setStopped(boolean isStopping) {
		this.isStopped = isStopping;
	}

	public long getLastInactivated() {
		return lastInactivated;
	}

	public void setLastInactivated(long lastInactivated) {
		this.lastInactivated = lastInactivated;
	}
}