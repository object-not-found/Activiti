package com.yu.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yu.demo.base.BaseDao;
import com.yu.demo.domain.ApproveNotes;


public interface ApproveNotesMapper extends BaseDao<ApproveNotes, Long>{
	List<ApproveNotes> findNoteList(@Param("processInstanceId") String processInstanceId);
}
