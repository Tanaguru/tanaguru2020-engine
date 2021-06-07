package com.tanaguru.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.dto.StatisticsDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.ContractRepository;
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
	private final ContractRepository contractRepository;

	@Autowired
	public StatsServiceImpl(ProjectRepository projectRepository,
			UserRepository userRepository,
			StatusResultRepository statusResultRepository,
			AuditRepository auditRepository,
			AuditService auditService,
			ContractRepository contractRepository) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.statusResultRepository = statusResultRepository;
		this.auditRepository = auditRepository;
		this.auditService = auditService;
		this.contractRepository = contractRepository;
	}
	
	@Override
	public StatisticsDTO createStats() {
		StatisticsDTO stats = new StatisticsDTO();
		stats.setNbProjects((int) this.projectRepository.count());
		stats.setNbUsers((int) this.userRepository.count());
		stats.setNbAudits((int) this.auditRepository.count());
		stats.setNbContracts((int) this.contractRepository.count());
		stats.setMeanNbErrorsPage(this.statusResultRepository.getAverageNumberOfErrorsByPage());
		stats.setMeanNbErrorsAudit(this.getAverageNbErrorsByAudit());
		stats.setMeanNbErrorsProject(this.getAverageNbErrorsByProject());
		
		stats.setNbPageAudit(this.auditRepository.numberOfAuditByType(EAuditType.PAGE));
		stats.setNbSiteAudit(this.auditRepository.numberOfAuditByType(EAuditType.SITE));
		stats.setNbUploadAudit(this.auditRepository.numberOfAuditByType(EAuditType.UPLOAD));
		stats.setNbScenarioAudit(this.auditRepository.numberOfAuditByType(EAuditType.SCENARIO));
		
		Double avgNbAuditsByProject = this.getAverageNbAuditsByProject();
		stats.setMeanNbAuditsByProject( Double.isFinite(avgNbAuditsByProject) ? avgNbAuditsByProject : 0.0);
		Double avgNbUsersByProject = this.getAverageNbUsersByProject();
		stats.setMeanNbUsersByProject(Double.isFinite(avgNbUsersByProject) ? avgNbUsersByProject : 0.0);
		return stats;
	}
	
	/**
	 * Return the average of number of users per project
	 * @return the average of number of users per project
	 */
	private double getAverageNbUsersByProject() {
		Stream<Project> projectStream = this.projectRepository.getAll();
		return projectStream.map(project -> project.getProjectAppUsers())
				.mapToDouble(projectAppUsers -> projectAppUsers.size())
				.average()
				.orElse(0.0);
	}
	
	/**
	 * Return the average of number of audits per project
	 * @return the average of number of audits per project
	 */
	private double getAverageNbAuditsByProject() {
		Stream<Project> projectStream = this.projectRepository.getAll();
		return projectStream.map(project -> project.getActs())
				.mapToDouble(act -> act.size())
				.average()
				.orElse(0.0);
	}
	
	/***
	 * Returns the average number of errors per audit
	 * @return the average number of errors per audit
	 */
	@Transactional
	private double getAverageNbErrorsByAudit() {
		Stream<Audit> auditStream = this.auditRepository.getAll();
		List<Long> pagesId = auditStream.flatMap(audit -> audit.getPages().stream())
				.map(page -> page.getId())
				.collect(Collectors.toList());
		Integer error = this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId);
		double nbAudits = (double) this.auditRepository.count();
		double average = 0.0;
		if(nbAudits != 0 && error != 0) {
			average = error / nbAudits;
		}
	    return average;
	}
	
	/**
	 * Return the average number of errors per project
	 * @return the average number of errors per project
	 */
	private double getAverageNbErrorsByProject() {
		Stream<Project> projectStream = this.projectRepository.getAll();
		List<Long> pagesId = projectStream.flatMap(project -> this.auditService.findAllByProject(project).stream())
				.flatMap(audit -> audit.getPages().stream())
				.map(page -> page.getId())
				.collect(Collectors.toList());
			
		int error = this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId);
		double nbProjects = (double) this.projectRepository.count();
		double average = 0.0;
		if(error != 0 && nbProjects !=0) {
			average = error/nbProjects;
		}
	    return average;
	}

	@Override
	public int getNbPageAuditedByPeriod(Date startDate, Date endDate) {
		return this.auditRepository.numberOfAuditByTypeAndPeriod(EAuditType.PAGE, startDate, endDate);
	}
	
	@Override
	public int getNbSiteAuditedByPeriod(Date startDate, Date endDate) {
		return this.auditRepository.numberOfAuditByTypeAndPeriod(EAuditType.SITE, startDate, endDate);
	}

	@Override
	public int getNbScenarioAuditedByPeriod(Date startDate, Date endDate) {
		return this.auditRepository.numberOfAuditByTypeAndPeriod(EAuditType.SCENARIO, startDate, endDate);
	}
	
	@Override
	public int getNbFileAuditedByPeriod(Date startDate, Date endDate) {
		return this.auditRepository.numberOfAuditByTypeAndPeriod(EAuditType.UPLOAD, startDate, endDate);
	}

	@Override
	public double getAverageNbErrorsForPageByPeriod(Date startDate, Date endDate) {
		Stream<Audit> auditStream = this.auditRepository.getAll();
		List<Long> pagesId = auditStream.filter(audit -> audit.getDateStart().after(startDate) && audit.getDateStart().before(endDate))
				.flatMap(audit -> audit.getPages().stream())
				.map(page -> page.getId())
				.collect(Collectors.toList());
		
		Double avg = 0.0;
		Integer errorSum = this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId);
		if(errorSum != null && errorSum != 0) {
			avg = (double) errorSum/pagesId.size();
		}
		return avg;
	}
}
