package com.tanaguru.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.StatusResultRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.StatsService;

@Service
@Transactional
public class StatsServiceImpl implements StatsService{

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final StatusResultRepository statusResultRepository;
	private final AuditRepository auditRepository;
	private final AuditService auditService;

	@Autowired
	public StatsServiceImpl(ProjectRepository projectRepository,
			UserRepository userRepository,
			StatusResultRepository statusResultRepository,
			AuditRepository auditRepository,
			AuditService auditService
			) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.statusResultRepository = statusResultRepository;
		this.auditRepository = auditRepository;
		this.auditService = auditService;
	}
	
	@Override
	public JSONObject createStats() {
		long nbProjects = projectRepository.count();
		long nbUsers = userRepository.count();
		JSONObject jsonStatsObject = new JSONObject();
		jsonStatsObject.put("nbProjects", nbProjects);
		jsonStatsObject.put("nbUsers", nbUsers);
		jsonStatsObject.put("meanNbErrorsPage", this.statusResultRepository.getAverageNumberOfErrorsByPage());
		jsonStatsObject.put("meanNbErrorsAudit", this.getAverageNbErrorsByAudit());
		jsonStatsObject.put("meanNbErrorsProject", this.getAverageNbErrorsByProject());
		for(EAuditType type : EAuditType.values()) {
			jsonStatsObject.put("nb"+type.toString()+"Audit", this.auditRepository.numberOfAuditByType(type));
		}

		return jsonStatsObject;
	}
	
	/***
	 * Returns the average number of errors per audit
	 * @return the average number of errors per audit
	 */
	private double getAverageNbErrorsByAudit() {
		List<Audit> audits = this.auditRepository.findAll();
		List<Long> pagesId = new ArrayList<>();
		List<Integer> auditErrors = new ArrayList<>();
		for(Audit audit : audits) {
			Collection<Page> pages = audit.getPages();
			for(Page page : pages) {
				pagesId.add(page.getId());
			}
			auditErrors.add(this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId));
			pagesId.clear();
		}
	    Double average = auditErrors.stream().mapToInt(val -> val).average().orElse(0.0);
	    return average;
	}
	
	/**
	 * Return the average number of errors per project
	 * @return the average number of errors per project
	 */
	private double getAverageNbErrorsByProject() {
		List<Project> projects = this.projectRepository.findAll();
		List<Integer> projectErrors = new ArrayList<>();
		for(Project project : projects) {
			Collection<Audit> audits = this.auditService.findAllByProject(project);
			List<Long> pagesId = new ArrayList<>();
			List<Integer> auditErrors = new ArrayList<>();
			for(Audit audit : audits) {
				Collection<Page> pages = audit.getPages();
				for(Page page : pages) {
					pagesId.add(page.getId());
				}
				auditErrors.add(this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId));
				pagesId.clear();
			}
			projectErrors.add(auditErrors.stream().reduce(0, Integer::sum));
		}
	    Double average = projectErrors.stream().mapToInt(val -> val).average().orElse(0.0);
	    return average;
	}

}
