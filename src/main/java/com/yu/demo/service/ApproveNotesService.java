package com.yu.demo.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yu.demo.base.BaseService;
import com.yu.demo.domain.ApproveNotes;


public interface ApproveNotesService extends BaseService<ApproveNotes, Long>{

	public List<ApproveNotes> findNoteList(@Param("processInstanceId") String processInstanceId);
}
