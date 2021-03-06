/*
 * Portions of this file Copyright 2004-2007 Shanghai Jiaotong University
 * 
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/legal/4.0/
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */

package org.teragrid.portal.filebrowser.applet.transfer;

import java.text.MessageFormat;

import org.globus.ftp.ByteRange;
import org.globus.ftp.FileInfo;
import org.teragrid.portal.filebrowser.applet.ui.DrawState;
import org.teragrid.portal.filebrowser.applet.ui.ListModel;
import org.teragrid.portal.filebrowser.applet.util.ResourceName;
import org.teragrid.portal.filebrowser.applet.util.SGGCResourceBundle;


@SuppressWarnings("unchecked")
public class FileTransferTask implements Task, Comparable {
    private int id = -1;
    private FileInfo file = null;//the file to transfer
    private FTPSettings srcSite = null;//source Site
    private FTPSettings destSite = null;//destination Site
    private String srcDir = null;//source directory
    private String destDir = null;//destination directory

    private ByteRange srcRange = null;//the source file range
    private long dstStartOffset = 0;//the destination file range
    private UrlCopy process = null;//UrlCopy process

    private int status = Task.WAITING;//the status of the transfering
    private int progress = 0;//progress:0~100
    private long totalTime = 0;//time elapsed(in millisecond)
    private long leftTime = 0;//time left(in millisecond)
    private long startTime = 0;// time transfer started
    private long speed = 0;//speed(in bps)
    
    private boolean resume = false;
    private String reName = null;
    private String displayName = "";
    private int paraID = 1;
    private int para = 1;
    private long created = 0;
    
    //directory separator
    @SuppressWarnings("unused")
	private String SEPARTOR = "/";
    private boolean cancelled = false; 

    public FileTransferTask(FileInfo file, FTPSettings srcSite, FTPSettings destSite, String srcDir, String destDir){
        this(file, srcSite, destSite,srcDir,destDir,new ByteRange(0, file.getSize()));
    }

    public FileTransferTask(FileInfo file, FTPSettings srcSite, FTPSettings destSite, String srcDir, String destDir, ByteRange srcRange){
    	this(file, srcSite, destSite,srcDir,destDir,srcRange, 0);
    }
    
    public FileTransferTask(FileInfo file, FTPSettings srcSite, FTPSettings destSite, String srcDir, String destDir, ByteRange srcRange, int dstStartOffset){
        this.file = file;
        this.srcSite = srcSite;
        this.destSite = destSite;
        this.srcDir = srcDir;
        this.destDir = destDir;
        this.displayName = file.getName();
        this.startTime = System.currentTimeMillis();
        this.srcRange = srcRange;
        this.dstStartOffset = dstStartOffset;
        this.created = System.currentTimeMillis();
        
    }

    public void cancel(){
        if(null!=process) {
        	process.cancel();
        }
        this.cancelled = true;
        this.setStatus(Task.FAILED);
    }
    
    public void kill(){
        if(null!=process) {
            process.cancel();
        }
        this.cancelled = true;
        this.setStatus(Task.STOPPED);
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Return the Fileinfo
     * @return FileInfo
     */
    public FileInfo getFile(){
        return this.file;
    }

    /**
     * Set the Fileinfo
     * @param file FileInfo
     */
    public void setFile(FileInfo file){
        this.file = file;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    
    public String getStartTimeString() {
        return ListModel.getDate(startTime);
    }
    
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Return true if the tranfer object is a file, not a directory.
     * @return boolean
     */
    public boolean isFileTask(){
        return file.isFile();
    }

    /**
     * Return the transfer status
     * @return int
     * @todo Implement this cgftp.Task method
     */
    public int getStatus() {
        return this.status;
    }
    /**
     * @return the created
     */
    public long getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * Return the transfer status string
     * @return String
     */
    public String getStatusString(){
        String statusString = "";

        switch(this.status){
        case Task.DONE:
            statusString = "Done";
            break;
            case Task.FAILED:
                statusString = "Failed";
            break;
            case Task.ONGOING:
                statusString = "Ongoing";
                break;
            case Task.RESTARTABLE:
                statusString = "Restartable";
                break;
            case Task.WAITING:
                statusString = "Waiting";
                break;
            case Task.STOPPED:
                statusString = "Stopped";
                break;
        }

        return statusString;
    }
    
    /**
     * Return the transfer status string
     * @return String
     */
    public static String getStatusString(int status){
        String statusString = "";

        switch(status){
        case Task.DONE:
            statusString = "Done";
            break;
            case Task.FAILED:
                statusString = "Failed";
            break;
            case Task.ONGOING:
                statusString = "Ongoing";
                break;
            case Task.RESTARTABLE:
                statusString = "Restartable";
                break;
            case Task.WAITING:
                statusString = "Waiting";
                break;
            case Task.STOPPED:
                statusString = "Stopped";
                break;
        }

        return statusString;
    }

    /**
     * Set the transfer status
     * @param status int
     * @throws IllegalArgumentException
     * @todo Implement this cgftp.Task method
     */
    public void setStatus(int status) throws IllegalArgumentException {
        if ((status != Task.WAITING) && (status != Task.ONGOING)
				&& (status != Task.DONE) && (status != Task.FAILED)
				&& (status != Task.RESTARTABLE)
				&& (status != Task.STOPPED)) {
        	String exceptionMsg = MessageFormat.format(SGGCResourceBundle.getResourceString(ResourceName.KEY_EXCEPTION_FILETRANSFERTASK_INVALIDSTATUS), 
        			new Object[] {new Integer(status)});
			throw new IllegalArgumentException(exceptionMsg);
		}

		this.status = status;
    }

    /**
	 * Return the source site
	 * 
	 * @return FTPSettings
	 */
    public FTPSettings getSrcSite(){
        return this.srcSite;
    }

    /**
     * Set the source site
     * @param srcSite FTPSettings
     */
    public void setSrcSite(FTPSettings srcSite){
        this.srcSite = srcSite;
    }

    /**
     * Return the destination site
     * @return FTPSettings
     */
    public FTPSettings getDestSite(){
        return this.destSite;
    }

    /**
     * Set the destination site
     * @param destSite FTPSettings
     */
    public void setDestSite(FTPSettings destSite){
        this.destSite = destSite;
    }

    /**
     * Return the source directory
     * @return String
     */
    public String getSrcDir(){
        return this.srcDir;
    }

    /**
     * Set the source directory
     * @param srcDir String
     */
    public void setSrcDir(String srcDir){
        this.srcDir = srcDir;
    }

    /**
     * Return the destination directory
     * @return String
     */
    public String getDestDir(){
        return this.destDir;
    }

    /**
     * Set the destination directory
     * @param destDir String
     */
    public void setDestDir(String destDir){
        this.destDir = destDir;
    }

    /**
     * Return the transfer range of the file
     * @return long
     */
    public ByteRange getSrcRange(){
        return this.srcRange;
    }

    /**
     * Set the transfer range of the file
     * @param startPoint long
     */
    public void setSrcRange(ByteRange range){
        this.srcRange = range;
    }

    /**
     * Return the transfer size of the file
     * @return long
     */
    public long getSize(){
        return srcRange.to - srcRange.from;
    }

    /**
     * Return the transfer progress of the file
     * @return int
     */
    public int getProgress(){
        return this.progress;
    }

    /**
     * Set the transfer progress of the file
     * @param progress int
     */
    public void setProgress(int progress){
        if(progress <=0 ){
            this.progress = 0;
        }else if(progress >= 100){
            this.progress = 100;
        }else{
            this.progress = progress;
        }
    }

    /**
     * Set the transfer process
     * @param process UrlCopy
     */
    public void setProcess(UrlCopy process){
        this.process=process;
    }

    /**
     * Get the time elapsed
     * @return String
     */
    public long getTotalTime(){
        return this.totalTime;
    }

    public String getTotalTimeString(){
        return ListModel.getTime(totalTime);
    }

    /**
     * Set the time elapsed
     * @param totalTime String
     */
    public void setTotalTime(long totalTime){
        this.totalTime = totalTime;
    }

    /**
     * Get the time remained
     * @return String
     */
    public long getLeftTime(){
        return this.leftTime;
    }

    public String getLeftTimeString(){
        return ListModel.getTime(leftTime);
    }

    /**
     * Set the time remained
     * @param leftTime String
     */
    public void setLeftTime(long leftTime){
        this.leftTime = leftTime;
    }

    /**
     * Get the transfer speed
     * @return long
     */
    public long getSpeed(){
        return this.speed;
    }

    public String getSpeedString(){
        return ListModel.getSpeed(speed);
    }

    /**
     * Set the transfer speed
     * @param speed double
     */
    public void setSpeed(long speed){
        this.speed = speed;
    }

    /**
     * Set the resume mode
     */
    public void setResume(){
        this.resume = true;
    }

    /**
     * Get the resume mode
     * @return boolean
     */
    public boolean isResume(){
        return resume;
    }

    /**
     * Rename the file
     * @param name String
     */
    public void setNewName(String name){
        this.reName = name;
    }

    /**
     * Get the rename
     * @return String
     */
    public String getNewName(){
        return this.reName;
    }

    /**
     * Return a DrawState object for map view
     * @param time long
     * @return String
     */
    public DrawState task2State() {
        DrawState state = new DrawState(this, DrawState.ARROW_LEN, DrawState.STEP_LEN);
        return state;
    }

    /**
     * Return a url string
     * @return String
     */
    public String toString() {
        return this.srcSite.getPrefix() + this.srcSite.host + this.srcDir + "/" + this.file.getName() + " (" + this.srcSite.name + ")";
    }

	public long getDstStartOffset() {
		return this.dstStartOffset;
	}

	public void setDstStartOffset(long dstStartOffset) {
		this.dstStartOffset = dstStartOffset;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getPara() {
		return this.para;
	}

	public void setPara(int para) {
		this.para = para;
	}

	public int getParaID() {
		return this.paraID;
	}

	public void setParaID(int paraID) {
		this.paraID = paraID;
	}
    
    public int compareTo(Object o) {
        if (o instanceof FileTransferTask) {
//            Long a = new Long(startTime);
//            Long b = new Long(((FileTransferTask)o).startTime);
//            LogManager.debug("Comparing tasks " + getId() + " with " + ((FileTransferTask)o).getId() + " - " + a + ":" + b + " = " + a.compareTo(b));
            if (created < ((FileTransferTask)o).created) return 1;
            if (created > ((FileTransferTask)o).created) return -1;
        }
        return 0;
    }

}
