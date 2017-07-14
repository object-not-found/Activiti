package com.yu.demo.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivitiUtil {
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	
	
	//部署流程定义  通过资源名称部署
	public Deployment deployProcessByResources(String deployName, String sourceName){
		Deployment deployment = repositoryService.createDeployment()
				.name(deployName)
				.addClasspathResource(sourceName+".bpmn")
				.addClasspathResource(sourceName+".png")
				.deploy();
				
		return deployment;
	}
	
	//部署流程定义  通过ZIP方式部署
	public Deployment deployProcessByZIP(String name, ZipInputStream zipInputStream){
		Deployment deployment = repositoryService.createDeployment()
				.name(name)
				.addZipInputStream(zipInputStream)
				.deploy();
		
		return deployment;
	}
	
	//查看流程定义列表
	public List<ProcessDefinition> findProcessDefList(){
		
		return repositoryService.createProcessDefinitionQuery().list();
	}
	
	//根据流程定义key启动流程实例
	public ProcessInstance startProcessInstanceByKey(String key, Map<String, Object> varMap){
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, varMap);
		return processInstance;
	}
	
	//查询任务
	//从三中情况中查询后合并
	public List<Task> findTaskList(String userId, String role){
		//直接指定到人
		List<Task> tasks = taskService.createTaskQuery()
				.taskAssignee(String.valueOf(userId))
				.list();
		
		//候选人列表
		List<Task> taskList = taskService.createTaskQuery()
				.taskCandidateUser(userId)
				.list();
		
		//候选组
		if(null != role){
			List<Task> roleTasks = taskService.createTaskQuery()
					.taskCandidateGroup(role)
					.list();
			tasks.addAll(roleTasks);
		}
		tasks.addAll(taskList);
		return tasks;
	}
	
	//认领任务
	public void claimTask(String taskId, String userId){
		taskService.claim(taskId, userId);
	}
	
	//查询任务
	public Task viewTask(String taskId){
		return taskService.createTaskQuery().taskId(taskId).singleResult();
	}
	
	//为任务设置审核人
	public void setAssignee(String taskId, String userId){
		taskService.setAssignee(taskId, userId);
	}
	
	//完成任务
	public void complete(String taskId, Map<String, Object> varMap){
		taskService.complete(taskId, varMap);
	}
	
	//审批拒绝，默认直接跳转到结束节点
	public void endProcess(String taskId, String nextTaskId){
		if(null == nextTaskId || nextTaskId.length() == 0){
			nextTaskId = "end";
		}
		
		ActivityImpl endActivity = findActivitiImpl(taskId, nextTaskId);
		
		commitProcess(taskId, null, endActivity.getId());
	}

	//流程转向执行任务节点ID
	private void commitProcess(String taskId, Map<String, Object> variables, String activityId) {
		if (variables == null) {
			variables = new HashMap<String, Object>();
		}
		// 跳转节点为空，默认提交操作
		if (null == activityId || activityId.length() == 0) {
			taskService.complete(taskId, variables);
		} else {// 流程转向操作
			turnTransition(taskId, activityId, variables);
		}
	}

	//流程转向操作
	private void turnTransition(String taskId, String activityId, Map<String, Object> variables) {
		// 当前节点
		ActivityImpl currActivity = findActivitiImpl(taskId, null);
		// 清空当前流向
		List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);

		// 创建新流向
		TransitionImpl newTransition = currActivity.createOutgoingTransition();
		// 目标节点
		ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);
		// 设置新流向的目标节点
		newTransition.setDestination(pointActivity);

		// 执行转向任务
		taskService.complete(taskId, variables);
		// 删除目标节点新流入
		pointActivity.getIncomingTransitions().remove(newTransition);

		// 还原以前流向
		restoreTransition(currActivity, oriPvmTransitionList);
	}

	//还原指定活动节点流向
	private void restoreTransition(ActivityImpl activityImpl, List<PvmTransition> oriPvmTransitionList) {
		// 清空现有流向
		List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
		pvmTransitionList.clear();
		// 还原以前流向
		for (PvmTransition pvmTransition : oriPvmTransitionList) {
			pvmTransitionList.add(pvmTransition);
		}
	}

	//清空指定活动节点流向
	private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
		// 存储当前节点所有流向临时变量
		List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
		// 获取当前节点所有流向，存储到临时变量，然后清空
		List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
		for (PvmTransition pvmTransition : pvmTransitionList) {
			oriPvmTransitionList.add(pvmTransition);
		}
		pvmTransitionList.clear();

		return oriPvmTransitionList;
	}

	//根据任务ID和节点ID获取活动节点
	private ActivityImpl findActivitiImpl(String taskId, String activityId) {
		//获取流程定义
		ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);

		// 获取当前活动节点ID
		if (null == activityId || activityId.length() == 0) {
			activityId = viewTask(taskId).getTaskDefinitionKey();
		}

		// 根据流程定义，获取该流程实例的结束节点
		if (activityId.toUpperCase().equals("END")) {
			for (ActivityImpl activityImpl : processDefinition.getActivities()) {
				List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
				if (pvmTransitionList.isEmpty()) {
					return activityImpl;
				}
			}
		}

		// 根据节点ID，获取对应的活动节点
		ActivityImpl activityImpl = ((ProcessDefinitionImpl) processDefinition).findActivity(activityId);

		return activityImpl;
	}

	//根据任务ID获取流程定义
	private ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) {
		ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
				.getDeployedProcessDefinition(viewTask(taskId).getProcessDefinitionId());
		
		return processDefinition;
	}

	//流程是否结束
	public Boolean isEnd(String processInstanceId){
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();

		return processInstance == null;
	}
	
	public InputStream showProcessPic(String taskId) {
		String deploymentId = findProcessDefinitionEntityByTaskId(taskId).getDeploymentId();
		List<String> list = repositoryService.getDeploymentResourceNames(deploymentId);

		String resourceName = "";
		if (list != null && list.size() > 0) {
			for (String name : list) {
				if (name.indexOf(".png") > 0)
					resourceName = name;
			}
		}

		// 获取图片的输入流
		InputStream is = repositoryService.getResourceAsStream(deploymentId, resourceName);

		return is;
	}
}
