package com.yu.demo.manager;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yu.demo.domain.ApproveNotes;
import com.yu.demo.service.ApproveNotesService;
import com.yu.demo.util.ActivitiUtil;

@Service
public class ActivitiManager {
	@Autowired
	private ApproveNotesService approveNotesService;
	@Autowired
	private ActivitiUtil activitiUtil;
	
	//授权
	public void transfer(String taskId, String userId, String nextUserId, String note){
		//为任务设置审核人
		activitiUtil.setAssignee(taskId, nextUserId);
		
		//记录信息
		ApproveNotes notes = new ApproveNotes();
		String processInstanceId = activitiUtil.viewTask(taskId).getProcessInstanceId();
		long time = System.currentTimeMillis();
		notes.setProcessInstanceId(processInstanceId);
		notes.setTaskId(taskId);
		notes.setUserId(userId);
		notes.setNote(note);
		notes.setStatus(1);
		notes.setCtime(time);
		approveNotesService.insert(notes);
	}
	
	//完成任务
	public void complete(String taskId,String nextTaskId, String userId, Boolean result, String note, Map<String, Object> map){
		ApproveNotes notes = new ApproveNotes();
		long time = System.currentTimeMillis();
		String processInstanceId = activitiUtil.viewTask(taskId).getProcessInstanceId();
		notes.setProcessInstanceId(processInstanceId);
		notes.setTaskId(taskId);
		notes.setUserId(userId);
		notes.setCtime(time);
		notes.setNote(note);
		
		//result:  true--同意   false--拒绝
		if(result){
			activitiUtil.complete(taskId, map);
			if(activitiUtil.isEnd(processInstanceId)){
				notes.setStatus(5);
			}else{
				notes.setStatus(2);
			}
		}else{
			notes.setStatus(3);
			activitiUtil.endProcess(taskId, nextTaskId);
		}
		
		approveNotesService.insert(notes);
	}
	
	//撤回任务
	public void recall(String taskId, String userId, String note){
		//先判断是否能撤回
		
		
		ApproveNotes notes = new ApproveNotes();
		String processInstanceId = activitiUtil.viewTask(taskId).getProcessInstanceId();
		long time = System.currentTimeMillis();
		notes.setProcessInstanceId(processInstanceId);
		notes.setTaskId(taskId);
		notes.setUserId(userId);
		notes.setNote(note);
		notes.setStatus(4);
		notes.setCtime(time);

		activitiUtil.endProcess(taskId, null);
		approveNotesService.insert(notes);
	}
	
	public List<ApproveNotes> findApproveNotesList(String processInstanceId){
		List<ApproveNotes> noteList = approveNotesService.findNoteList(processInstanceId);
		return noteList;
	}
}
