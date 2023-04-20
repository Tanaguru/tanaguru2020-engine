package com.tanaguru.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.dto.StatisticsDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.StatusResultRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.StatsService;

@Service
@Transactional
public class StatsServiceImpl implements StatsService {
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final StatusResultRepository statusResultRepository;
	private final AuditRepository auditRepository;
	private final AuditService auditService;
	private final ContractRepository contractRepository;
	private final PageRepository pageRepository;
	private StatisticsDTO stats = new StatisticsDTO();

	@Autowired
	public StatsServiceImpl(ProjectRepository projectRepository, UserRepository userRepository,
			StatusResultRepository statusResultRepository, AuditRepository auditRepository, AuditService auditService,
			ContractRepository contractRepository, PageRepository pageRepository) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.statusResultRepository = statusResultRepository;
		this.auditRepository = auditRepository;
		this.auditService = auditService;
		this.contractRepository = contractRepository;
		this.pageRepository = pageRepository;
	}

	@Override
	public StatisticsDTO createStats() {
		return this.stats;
	}

	@Scheduled(fixedDelayString = "${statistics.fixedDelay}")
	public void createStatsScheduled() {
		this.stats.setNbProjects((int) this.projectRepository.count());
		this.stats.setNbUsers((int) this.userRepository.count());
		this.stats.setNbAudits((int) this.auditRepository.count());
		this.stats.setNbContracts((int) this.contractRepository.count());
		this.stats.setMeanNbErrorsPage(this.statusResultRepository.getAverageNumberOfErrorsByPage());
		this.stats.setMeanNbErrorsAudit(this.getAverageNbErrorsByAudit());
		this.stats.setMeanNbErrorsProject(this.getAverageNbErrorsByProject());

		this.stats.setNbPageAudit(this.auditRepository.numberOfAuditByType(EAuditType.PAGE));
		this.stats.setNbSiteAudit(this.auditRepository.numberOfAuditByType(EAuditType.SITE));
		this.stats.setNbUploadAudit(this.auditRepository.numberOfAuditByType(EAuditType.UPLOAD));
		this.stats.setNbScenarioAudit(this.auditRepository.numberOfAuditByType(EAuditType.SCENARIO));

		Double avgNbAuditsByProject = this.getAverageNbAuditsByProject();
		this.stats.setMeanNbAuditsByProject(Double.isFinite(avgNbAuditsByProject) ? avgNbAuditsByProject : 0.0);
		Double avgNbUsersByProject = this.getAverageNbUsersByProject();
		this.stats.setMeanNbUsersByProject(Double.isFinite(avgNbUsersByProject) ? avgNbUsersByProject : 0.0);
	}

	/**
	 * Return the average of number of users per project
	 *
	 * @return the average of number of users per project
	 */
	private double getAverageNbUsersByProject() {
		Stream<Project> projectStream = this.projectRepository.getAll();
		return projectStream.map(project -> project.getProjectAppUsers())
				.mapToDouble(projectAppUsers -> projectAppUsers.size()).average().orElse(0.0);
	}

	/**
	 * Return the average of number of audits per project
	 *
	 * @return the average of number of audits per project
	 */
	private double getAverageNbAuditsByProject() {
		Stream<Project> projectStream = this.projectRepository.getAll();
		return projectStream.map(project -> project.getActs()).mapToDouble(act -> act.size()).average().orElse(0.0);
	}

	/***
	 * Returns the average number of errors per audit
	 *
	 * @return the average number of errors per audit
	 */
	@Transactional
	private double getAverageNbErrorsByAudit() {
		Stream<Audit> auditStream = this.auditRepository.getAll();
		List<Long> pagesId = auditStream.flatMap(audit -> audit.getPages().stream()).map(page -> page.getId())
				.collect(Collectors.toList());
		Integer error = this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId);
		double nbAudits = this.auditRepository.count();
		double average = 0.0;

		if(nbAudits != 0 && error != null) {
			average = (double) error / nbAudits;
		}

		return average;
	}

	/**
	 * Return the average number of errors per project
	 *
	 * @return the average number of errors per project
	 */
	private double getAverageNbErrorsByProject() {
		Stream<Project> projectStream = this.projectRepository.getAll();
		List<Long> pagesId = projectStream.flatMap(project -> this.auditService.findAllByProject(project).stream())
				.flatMap(audit -> audit.getPages().stream()).map(page -> page.getId()).collect(Collectors.toList());

		Integer error = this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId);
		double nbProjects = this.projectRepository.count();
		double average = 0.0;

		if(error != null && nbProjects != 0) {
			average = (double) error / nbProjects;
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
		Collection<Page> pages = pageRepository
				.findAllByAuditDateStartLessThanEqualAndAuditDateEndGreaterThanEqual(endDate, startDate);
		List<Long> pagesId = pages.stream().map(page -> page.getId()).collect(Collectors.toList());

		Integer errorSum = this.statusResultRepository.getSumNumberOfErrorsForPages(pagesId);
		double nbPages = pagesId.size();
		double avg = 0.0;

		if(errorSum != null && nbPages != 0) {
			avg = (double) errorSum / nbPages;
		}

		return avg;
	}
}
