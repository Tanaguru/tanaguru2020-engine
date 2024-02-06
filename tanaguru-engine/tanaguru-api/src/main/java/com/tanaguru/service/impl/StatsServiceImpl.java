package com.tanaguru.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.dto.StatisticsDTO;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.StatusResultRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.StatsService;

@Service
@Transactional
public class StatsServiceImpl implements StatsService {
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final StatusResultRepository statusResultRepository;
	private final AuditRepository auditRepository;
	private final ContractRepository contractRepository;
	private final PageRepository pageRepository;
	private StatisticsDTO stats = new StatisticsDTO();

	@Autowired
	public StatsServiceImpl(ProjectRepository projectRepository, UserRepository userRepository,
			StatusResultRepository statusResultRepository, AuditRepository auditRepository,
			ContractRepository contractRepository, PageRepository pageRepository) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.statusResultRepository = statusResultRepository;
		this.auditRepository = auditRepository;
		this.contractRepository = contractRepository;
		this.pageRepository = pageRepository;
	}

	@Override
	public StatisticsDTO createStats() {
		return this.stats;
	}

	@Scheduled(fixedDelayString = "${statistics.fixedDelay}")
	public void createStatsScheduled() {
		double projectCount = this.projectRepository.count();
		double auditCount = this.auditRepository.count();
		Integer sumNumberOfErrorsForPages = this.statusResultRepository.getSumNumberOfErrorsForPages();
		
		this.stats.setNbProjects((int) projectCount);
		this.stats.setNbUsers((int) this.userRepository.count());
		this.stats.setNbAudits((int) auditCount);
		this.stats.setNbContracts((int) this.contractRepository.count());
		
		this.stats.setMeanNbErrorsPage(this.statusResultRepository.getAverageNumberOfErrorsByPage());
		this.stats.setMeanNbErrorsAudit(this.getAverageNbErrorsBy(auditCount, sumNumberOfErrorsForPages));
		this.stats.setMeanNbErrorsProject(this.getAverageNbErrorsBy(projectCount, sumNumberOfErrorsForPages));

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

	/**
	 * Return the average number of errors per count
	 *
	 * @return the average number of errors per count
	 */
	private double getAverageNbErrorsBy(double count, Integer error) {
		double average = 0.0;

		if(error != null && count != 0) {
			average = (double) error / count;
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

		Integer errorSum = this.statusResultRepository.getSumNumberOfErrorsForPagesByDatesInterval(startDate, endDate);
		
		return getAverageNbErrorsBy(pages.size(), errorSum);
	}
}
