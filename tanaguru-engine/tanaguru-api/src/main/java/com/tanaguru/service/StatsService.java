package com.tanaguru.service;

import java.util.Date;

import org.json.JSONObject;

public interface StatsService {

	JSONObject createStats();
	
	Integer getNbPageAuditedByPeriod(Date startDate, Date endDate);
	
	Integer getNbSiteAuditedByPeriod(Date startDate, Date endDate);

	Integer getNbScenarioAuditedByPeriod(Date startDate, Date endDate);

	Integer getNbFileAuditedByPeriod(Date startDate, Date endDate);

}
