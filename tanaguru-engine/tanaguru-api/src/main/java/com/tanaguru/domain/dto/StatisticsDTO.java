package com.tanaguru.domain.dto;

public class StatisticsDTO {
	
	private int nbProjects;
	
	private int nbUsers;
	
	private int nbAudits;
	
	private int nbContracts;
	
	private Double meanNbErrorsPage;
	
	private Double meanNbErrorsAudit;
	
	private Double meanNbErrorsProject;
	
	private int nbSiteAudit;
	
	private int nbUploadAudit;

	private int nbPageAudit;
	
	private int nbScenarioAudit;
	
	private Double meanNbUsersByProject;
	
	private Double meanNbAuditsByProject;

	public int getNbProjects() {
		return nbProjects;
	}

	public void setNbProjects(int nbProjects) {
		this.nbProjects = nbProjects;
	}

	public int getNbUsers() {
		return nbUsers;
	}

	public void setNbUsers(int nbUsers) {
		this.nbUsers = nbUsers;
	}

	public int getNbAudits() {
		return nbAudits;
	}

	public void setNbAudits(int nbAudits) {
		this.nbAudits = nbAudits;
	}

	public int getNbContracts() {
		return nbContracts;
	}

	public void setNbContracts(int nbContracts) {
		this.nbContracts = nbContracts;
	}

	public Double getMeanNbErrorsPage() {
		return meanNbErrorsPage;
	}

	public void setMeanNbErrorsPage(Double meanNbErrorsPage) {
		this.meanNbErrorsPage = meanNbErrorsPage;
	}

	public Double getMeanNbErrorsAudit() {
		return meanNbErrorsAudit;
	}

	public void setMeanNbErrorsAudit(Double meanNbErrorsAudit) {
		this.meanNbErrorsAudit = meanNbErrorsAudit;
	}

	public Double getMeanNbErrorsProject() {
		return meanNbErrorsProject;
	}

	public void setMeanNbErrorsProject(Double meanNbErrorsProject) {
		this.meanNbErrorsProject = meanNbErrorsProject;
	}

	public int getNbSiteAudit() {
		return nbSiteAudit;
	}

	public void setNbSiteAudit(int nbSiteAudit) {
		this.nbSiteAudit = nbSiteAudit;
	}

	public int getNbUploadAudit() {
		return nbUploadAudit;
	}

	public void setNbUploadAudit(int nbUploadAudit) {
		this.nbUploadAudit = nbUploadAudit;
	}

	public int getNbPageAudit() {
		return nbPageAudit;
	}

	public void setNbPageAudit(int nbPageAudit) {
		this.nbPageAudit = nbPageAudit;
	}

	public int getNbScenarioAudit() {
		return nbScenarioAudit;
	}

	public void setNbScenarioAudit(int nbScenarioAudit) {
		this.nbScenarioAudit = nbScenarioAudit;
	}

	public Double getMeanNbUsersByProject() {
		return meanNbUsersByProject;
	}

	public void setMeanNbUsersByProject(Double meanNbUsersByProject) {
		this.meanNbUsersByProject = meanNbUsersByProject;
	}

	public Double getMeanNbAuditsByProject() {
		return meanNbAuditsByProject;
	}

	public void setMeanNbAuditsByProject(Double meanNbAuditsByProject) {
		this.meanNbAuditsByProject = meanNbAuditsByProject;
	}

}
