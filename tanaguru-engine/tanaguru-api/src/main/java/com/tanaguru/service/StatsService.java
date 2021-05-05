package com.tanaguru.service;

import java.util.Date;

import org.json.JSONObject;

public interface StatsService {

	JSONObject createStats();
	
	Integer getNbPageByPeriod(Date startDate, Date endDate);
	
	Integer getNbSiteByPeriod(Date startDate, Date endDate);

	Integer getNbScenarioByPeriod(Date startDate, Date endDate);

	Integer getNbFileByPeriod(Date startDate, Date endDate);

}
