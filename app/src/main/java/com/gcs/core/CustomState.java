package com.gcs.core;

import com.sharedlib.model.State;

public class CustomState extends State {

	private boolean isInConflict = false;
	private boolean isOnUniqueAltitude = false;
    private boolean hasCommConnection = false;

    private ConflictStatus conflictStatus = ConflictStatus.GRAY;
    private TaskStatus taskStatus = TaskStatus.NONE;

    public void updateConflictStatus() {
        if(isOnUniqueAltitude){
            conflictStatus = ConflictStatus.GRAY;
        } else {
            if (isInConflict){
                conflictStatus = ConflictStatus.RED;
            } else {
                conflictStatus = ConflictStatus.BLUE;
            }
        }
    }

    public void setConflictStatusNew(ConflictStatus NewStatus) {
        conflictStatus = NewStatus;
    }

    public ConflictStatus getConflictStatus() {
        return conflictStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setHasCommConnection(boolean hasCommConnection) { this.hasCommConnection = hasCommConnection; }

    public boolean hasCommConnection() { return hasCommConnection; }
}