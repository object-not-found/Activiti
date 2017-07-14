package com.yu.demo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.yu.demo.manager.ActivitiManager;
import com.yu.demo.util.ActivitiUtil;

@RestController
public class ActivitiController {
	@Autowired
	private ActivitiUtil activitiUtil;
	@Autowired
	private ActivitiManager activitiManager;
	
	//部署流程定义
	//文件存放位置可以自定义，如果使用bpmn文件和png文件部署的话名字最好一致
	//使用ZIP部署的话要把两个文件一起打包
	@RequestMapping(value = "/deploy", method = RequestMethod.GET)
	public String deployProcess(@RequestParam("deployName") String deployName){
		String sourceName="diagrams/"+deployName;
		Deployment deployment = activitiUtil.deployProcessByResources(deployName, sourceName);
		return JSON.toJSONString(deployment);
	}
	
	//查看流程定义列表
	//相当于查看都有什么类型的合同，可以从具体的字段拿到想要的东西返回到前端
	@RequestMapping(value = "/findProcessList", method = RequestMethod.GET)
	public String findProcessList(){
		List<ProcessDefinition> list = activitiUtil.findProcessDefList();
		Map<String, String> map = null;
		List<Map<String, String>> listMap = new ArrayList<>();
		for(ProcessDefinition pro: list){
			map = new HashMap<>();
			map.put("id", pro.getId());
			map.put("key", pro.getKey());
			map.put("name", pro.getName());
			listMap.add(map);
			
		}
		return JSON.toJSONString(listMap);
	}
	
	//启动流程定义
	//这个map可以是从前端拿到的表单数据，存放在数据库中作为流程变量，
	//这个流程变量在整个流程运转过程中都可以使用
	//表单数据也可以单独存放在数据库中不作为流程变量
	@RequestMapping(value = "/start", method = RequestMethod.GET)
	public String start(@RequestParam("processKey") String processKey){
		Map<String, Object> map = new HashMap<>();
		if("myProcess2".equals(processKey)){
			map.put("users", Arrays.asList("xiaoliu,xiaoqiang,xiaowang".split(",")));
			map.put("assignee", "xxx");
		}
		ProcessInstance ins = activitiUtil.startProcessInstanceByKey(processKey, map);
		return ins.getProcessInstanceId();
		
	}
	
	
	//查看任务列表,包括待办和未认领
	//相当于查询登录人的任务列表
	@RequestMapping(value = "/findTaskList", method = RequestMethod.GET)
	public String findTaskList(@RequestParam("userId")String userId, @RequestParam("role")String role){
		List<Task> taskList = activitiUtil.findTaskList(userId, role);
		Map<String, String> map = null;
		List<Map<String, String>> listMap = new ArrayList<>();
		for(Task task: taskList){
			map = new HashMap<>();
			map.put("id", task.getId());
			map.put("name", task.getName());
			map.put("assignee", task.getAssignee() == null ? "" : task.getAssignee());
			listMap.add(map);
		}
		return JSON.toJSONString(listMap);
	}
	
	//待办任务
	@RequestMapping(value = "/myTaskList", method = RequestMethod.GET)
	public String myTaskList(@RequestParam("userId")String userId){
		List<Task> taskList = activitiUtil.findTaskList(userId, null);
		Map<String, String> map = null;
		List<Map<String, String>> listMap = new ArrayList<>();
		for(Task task: taskList){
			map = new HashMap<>();
			map.put("id", task.getId());
			map.put("name", task.getName());
			map.put("assignee", task.getAssignee() == null ? "" : task.getAssignee());
			listMap.add(map);
		}
		return JSON.toJSONString(listMap);
	}
	
	//任务认领
	//如果不是指定到人的任务，需要先认领任务，认领后该任务其他人就看不到了
	@RequestMapping(value = "/claimTask", method = RequestMethod.GET)
	public String claimTask(@RequestParam("userId")String userId, @RequestParam("taskId")String taskId){
		activitiUtil.claimTask(taskId, userId);
		return "success";
	}
	
	//查看任务
	@RequestMapping(value = "/viewTask", method = RequestMethod.GET)
	public String viewTask(@RequestParam("taskId")String taskId){
		Task task = activitiUtil.viewTask(taskId);
		return JSON.toJSONString(task.getId());
	}
	
	//授权审批
	@RequestMapping(value = "/transfer", method = RequestMethod.GET)
	public String transfer(@RequestParam("userId")String userId, @RequestParam("nextUserId") String nextUserId, @RequestParam("taskId")String taskId, @RequestParam("note")String note){
		activitiManager.transfer(taskId, userId,nextUserId, note);
		return "success";
	}
	
	//完成任务
	//完成任务可以同意，也可以拒绝
	@RequestMapping(value = "/completeTask", method = RequestMethod.GET)
	public String completeTask(@RequestParam("taskId")String taskId, @RequestParam("userId")String userId, @RequestParam("result")Boolean result, @RequestParam("note")String note, @RequestParam("nextTaskId") String nextTaskId){
		Map<String, Object> map = new HashMap<>();
		activitiManager.complete(taskId, nextTaskId, userId, result, note, map);
		return "success";
	}

	//查询审批记录
	@RequestMapping(value = "/findApproveNotesList", method = RequestMethod.GET)
	public String findApproveNotesList(@RequestParam("processInstanceId")String processInstanceId){
		
		return JSON.toJSONString(activitiManager.findApproveNotesList(processInstanceId));
	}
	
	//读取流程图
	@RequestMapping(value = "/viewPicture", method = RequestMethod.GET)
	public void viewPicture(@RequestParam("taskId")String taskId){
		activitiUtil.showProcessPic(taskId);
	}
}
