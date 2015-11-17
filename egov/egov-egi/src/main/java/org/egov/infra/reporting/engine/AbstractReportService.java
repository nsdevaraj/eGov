/**
 * eGov suite of products aim to improve the internal efficiency,transparency, 
   accountability and the service delivery of the government  organizations.

    Copyright (C) <2015>  eGovernments Foundation

    The updated version of eGov suite of products as by eGovernments Foundation 
    is available at http://www.egovernments.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/ or 
    http://www.gnu.org/licenses/gpl.html .

    In addition to the terms of the GPL license to be adhered to in using this
    program, the following additional terms are to be complied with:

	1) All versions of this program, verbatim or modified must carry this 
	   Legal Notice.

	2) Any misrepresentation of the origin of the material is prohibited. It 
	   is required that all modified versions of this material be marked in 
	   reasonable ways as different from the original version.

	3) This license does not grant any rights to any user of the program 
	   with regards to rights under trademark law for use of the trade names 
	   or trademarks of eGovernments Foundation.

  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.infra.reporting.engine;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.infra.cache.impl.LRUCache;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.reporting.engine.jasper.JasperReportService;
import org.egov.infra.reporting.util.ReportUtil;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract report service providing common eGov reporting functionality. eGov infrastructure uses JasperReports for creating reports {@link JasperReportService}. Any other third party reporting framework can be supported by implementing a class that extends from {@link AbstractReportService} and
 * then configuring that class in the global bean definitions xml.
 */
public abstract class AbstractReportService<T> implements ReportService {
	/**
	 * The report template cache. Most frequently used report templates are cached in memory to improve performance of report generation.
	 */
	private LRUCache<String, T> templateCache;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportService.class);

	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * Creates a report using given report input where the report data source is java beans
	 * @param reportInput The report input
	 * @return The report output
	 */
	abstract protected ReportOutput createReportFromJavaBean(ReportRequest reportInput);

	/**
	 * Creates report for given report template, format, connection and parameters
	 * @param reportInput Report Input object
	 * @param dataSource Data source
	 * @return Report output for given report template, format, data source and parameters.
	 */
	abstract protected ReportOutput createReportFromSql(ReportRequest reportInput, Connection connection);

	/**
	 * Creates report using a template that uses HQL for fetching data
	 * @param reportInput The report input
	 * @return Report output created using given input
	 */
	abstract protected ReportOutput createReportFromHql(ReportRequest reportInput);

	/**
	 * @return Extension of the report templates supported by the report service
	 */
	abstract protected String getTemplateExtension();

	/**
	 * @param templateInputStream Input stream from which the report template is to be loaded
	 * @return The report template object
	 */
	abstract protected T loadTemplate(InputStream templateInputStream);

	/**
	 * Initializes the report service (sets up the report template cache)
	 * @param templateCacheMinSize Minimum size of template cache
	 * @param templateCacheMaxSize Maximum size of template cache
	 */
	private void initialize(final int templateCacheMinSize, final int templateCacheMaxSize) {
		this.templateCache = new LRUCache<String, T>(templateCacheMinSize, templateCacheMaxSize);
	}

	/**
	 * @param templateCacheMinSize Minimum size of template cache
	 * @param templateCacheMaxSize Maximum size of template cache
	 */
	public AbstractReportService(final int templateCachMinSize, final int templateCacheMaxSize) {
		initialize(templateCachMinSize, templateCacheMaxSize);
	}

	/*
	 * (non-Javadoc)
	 * @see org.egov.infra.reporting.engine.ReportService#createReport(org.egov. infstr.reporting.engine.ReportInput)
	 */
	@Override
	public ReportOutput createReport(final ReportRequest reportInput) {
		switch (reportInput.getReportDataSourceType()) {
		case JAVABEAN:
			return createReportFromJavaBean(reportInput);
		case SQL:
			return createReportFromSql(reportInput);
		case HQL:
			return createReportFromHql(reportInput);
		default:
			throw new ApplicationRuntimeException("Invalid report data source type [" + reportInput.getReportDataSourceType() + "]");
		}
	}

	/**
	 * Creates report using a JDBC connection
	 * @param reportInput The report input
	 * @return The report output
	 */
	protected ReportOutput createReportFromSql(final ReportRequest reportInput) {
		// Hibernate Session.connection() is deprecated. Hence using the Work
		// contract for performing discrete JDBC operation.
		final JdbcReportWork reportWork = new JdbcReportWork(reportInput);
		entityManager.unwrap(Session.class).doWork(reportWork);
		return reportWork.getReportOutput();
	}

	/**
	 * Returns the Report Template object for given template path. Fetches it from the template cache is available; else loads the template from disk.
	 * @param templateName Name of the Report template (without extension)
	 * @return Report object for given template path.
	 */
	protected T getTemplate(final String templateName) {
		String errMsg = null;
		// Check if the report template is available in the cache
		T reportTemplate = this.templateCache.get(templateName);

		if (reportTemplate == null) {
			// not found in cache. Try to load the template
			try {
				final InputStream templateInputStream = ReportUtil.getTemplateAsStream(templateName + getTemplateExtension());
				reportTemplate = loadTemplate(templateInputStream);

				// Loaded successfully. Add to cache.
				this.templateCache.put(templateName, reportTemplate);

				if (reportTemplate == null) {
					errMsg = "Report template [" + templateName + "] could not be loaded";
					LOGGER.error(errMsg);
					throw new ApplicationRuntimeException(errMsg);
				}
			} catch (final Exception e) {
				errMsg = "Exception in getting report template [" + templateName + "]";
				LOGGER.error(errMsg, e);
				throw new ApplicationRuntimeException(errMsg, e);
			}
		}
		return reportTemplate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.egov.infra.reporting.engine.ReportService#isValidTemplate(java.lang .String)
	 */
	@Override
	public boolean isValidTemplate(final String templateName) {
		T report = null;

		try {
			report = getTemplate(templateName);
		} catch (final Exception e) {
			// Template could not be loaded, which means it is not valid.
			LOGGER.error(templateName + " is not a valid template name.", e);
		}

		return (report != null);
	}

	/**
	 * Inner class used to generate report using SQL connection
	 */
	private class JdbcReportWork implements Work {
		private final ReportRequest reportInput;
		private ReportOutput reportOutput;

		/**
		 * Constructor
		 * @param reportInput The report input
		 */
		public JdbcReportWork(final ReportRequest reportInput) {
			this.reportInput = reportInput;
		}

		/**
		 * @return the Report Output
		 */
		public ReportOutput getReportOutput() {
			return this.reportOutput;
		}

		@Override
		public void execute(final Connection connection) throws SQLException {
			this.reportOutput = createReportFromSql(this.reportInput, connection);
		}
	}
}
