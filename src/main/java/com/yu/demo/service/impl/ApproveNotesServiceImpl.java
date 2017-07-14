package com.yu.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yu.demo.base.AbstractBaseService;
import com.yu.demo.dao.ApproveNotesMapper;
import com.yu.demo.domain.ApproveNotes;
import com.yu.demo.service.ApproveNotesService;


@Service
public class ApproveNotesServiceImpl extends AbstractBaseService<ApproveNotes, Long> implements ApproveNotesService {

	@Autowired
	private ApproveNotesMapper baseDao;
	
	@Override
	public void setBaseDao() {
		super.baseDao = baseDao;
	}

	@Override
	public List<ApproveNotes> findNoteList(String processInstanceId) {
		// TODO Auto-generated method stub
		return baseDao.findNoteList(processInstanceId);
	}

}
