package com.yu.demo.domain;

import java.io.Serializable;

public class ApproveNotes implements Serializable {

	private static final long serialVersionUID = 1061553451928728197L;
	
	private int id;
	private String processInstanceId;  //流程实例ID
	private String taskId;  //任务ID
	private String userId;  //审核人
	private String note;  //审核意见
	private long status;  //1--审核中  2--同意  3--拒绝  4--撤回   5--完成
	private long ctime; //审核时间
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public long getStatus() {
		return status;
	}
	public void setStatus(long status) {
		this.status = status;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	
}
