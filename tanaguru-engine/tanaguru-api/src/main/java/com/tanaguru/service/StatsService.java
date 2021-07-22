package com.tanaguru.service;

import java.util.Date;

import org.json.JSONObject;

import com.tanaguru.domain.dto.StatisticsDTO;

public interface StatsService {

	StatisticsDTO createStats();
	
	int getNbPageAuditedByPeriod(Date startDate, Date endDate);
	
	int getNbSiteAuditedByPeriod(Date startDate, Date endDate);

	int getNbScenarioAuditedByPeriod(Date startDate, Date endDate);

	int getNbFileAuditedByPeriod(Date startDate, Date endDate);

	double getAverageNbErrorsForPageByPeriod(Date startDate, Date endDate);
}
