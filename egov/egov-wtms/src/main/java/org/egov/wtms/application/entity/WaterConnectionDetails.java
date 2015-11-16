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
package org.egov.wtms.application.entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.commons.EgwStatus;
import org.egov.commons.entity.ChairPerson;
import org.egov.demand.model.EgDemand;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.wtms.masters.entity.ApplicationType;
import org.egov.wtms.masters.entity.ConnectionCategory;
import org.egov.wtms.masters.entity.PipeSize;
import org.egov.wtms.masters.entity.PropertyType;
import org.egov.wtms.masters.entity.UsageType;
import org.egov.wtms.masters.entity.WaterSource;
import org.egov.wtms.masters.entity.enums.ConnectionStatus;
import org.egov.wtms.masters.entity.enums.ConnectionType;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

@Entity
@Table(name = "egwtr_connectiondetails")
@SequenceGenerator(name = WaterConnectionDetails.SEQ_CONNECTIONDETAILS, sequenceName = WaterConnectionDetails.SEQ_CONNECTIONDETAILS, allocationSize = 1)
public class WaterConnectionDetails extends StateAware {

    private static final long serialVersionUID = -4667948558401042849L;
    public static final String SEQ_CONNECTIONDETAILS = "SEQ_EGWTR_CONNECTIONDETAILS";

    public enum WorkFlowState {
        CREATED, CHECKED, APPROVED, REJECTED, CANCELLED;
    }

    @Id
    @GeneratedValue(generator = SEQ_CONNECTIONDETAILS, strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "applicationtype", nullable = false)
    private ApplicationType applicationType;

    @ManyToOne
    @JoinColumn(name = "statusid", nullable = false)
    private EgwStatus status;

    @ManyToOne(cascade = CascadeType.ALL)
    @Valid
    @NotNull
    @JoinColumn(name = "connection", nullable = false)
    private WaterConnection connection;

    @Valid
    @OneToOne(mappedBy = "waterConnectionDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ExistingConnectionDetails existingConnection;

    // @Column(name = "applicationNumber", unique = true)
    @SafeHtml
    private String applicationNumber;

    @Temporal(value = TemporalType.DATE)
    private Date applicationDate;

    @Temporal(value = TemporalType.DATE)
    private Date disposalDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ConnectionType connectionType;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "category", nullable = false)
    private ConnectionCategory category;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "usageType", nullable = false)
    private UsageType usageType;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "propertyType", nullable = false)
    private PropertyType propertyType;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "waterSource", nullable = false)
    private WaterSource waterSource;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "pipeSize", nullable = false)
    private PipeSize pipeSize;

    private Long sumpCapacity;

    private Integer numberOfPerson;

    @Length(max = 1024)
    private String connectionReason;

    private Integer numberOfRooms;

    @Length(max = 150)
    private String bplCardHolderName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;

    @SafeHtml
    @Length(min = 3, max = 50)
    private String approvalNumber;

    @Temporal(value = TemporalType.DATE)
    private Date approvalDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "demand")
    private EgDemand demand;

    @Temporal(value = TemporalType.DATE)
    private Date workOrderDate;

    private String workOrderNumber;

    private double donationCharges;
    private Boolean legacy = false;

    @Temporal(value = TemporalType.DATE)
    private Date executionDate;

    @Temporal(value = TemporalType.DATE)
    private Date closeApprovalDate;

    @Temporal(value = TemporalType.DATE)
    private Date reconnectionApprovalDate;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "filestoreid")
    private FileStoreMapper fileStore;

    @ManyToOne
    @JoinColumn(name = "chairPerson")
    private ChairPerson chairPerson;
    private Boolean isHistory = false;
    @Valid
    @OneToOne(mappedBy = "waterConnectionDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FieldInspectionDetails fieldInspectionDetails;

    @OrderBy("id")
    @OneToMany(mappedBy = "waterConnectionDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApplicationDocuments> applicationDocs = new ArrayList<ApplicationDocuments>(0);

    @OrderBy("id")
    @OneToMany(mappedBy = "waterConnectionDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConnectionEstimationDetails> estimationDetails = new ArrayList<ConnectionEstimationDetails>(0);

    @OrderBy("id desc")
    @OneToMany(mappedBy = "waterConnectionDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MeterReadingConnectionDetails> meterConnection = new ArrayList<MeterReadingConnectionDetails>(0);

    @OrderBy("ID DESC")
    @OneToMany(mappedBy = "waterConnectionDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NonMeteredConnBillDetails> nonmeteredBillDetails = new HashSet<NonMeteredConnBillDetails>(0);

    @Transient
    private List<DemandDetail> demandDetailBeanList = new ArrayList<DemandDetail>(0);

    private String closeConnectionType;

    private String previousApplicationType;

    @Length(max = 1024)
    private String closeconnectionreason;

    @Length(max = 1024)
    private String reConnectionReason;

    @Transient
    private String meesevaApplicationNumber;

    public List<MeterReadingConnectionDetails> getMeterConnection() {
        return meterConnection;
    }

    public void setMeterConnection(final List<MeterReadingConnectionDetails> meterConnection) {
        this.meterConnection = meterConnection;
    }

    public List<DemandDetail> getDemandDetailBeanList() {
        return demandDetailBeanList;
    }

    public void setDemandDetailBeanList(final List<DemandDetail> demandDetailBeanList) {
        this.demandDetailBeanList = demandDetailBeanList;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String myLinkId() {
        return applicationNumber;

    }

    public String getPreviousApplicationType() {
        return previousApplicationType;
    }

    public void setPreviousApplicationType(final String previousApplicationType) {
        this.previousApplicationType = previousApplicationType;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(final ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public WaterConnection getConnection() {
        return connection;
    }

    public void setConnection(final WaterConnection connection) {
        this.connection = connection;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(final Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Date getDisposalDate() {
        return disposalDate;
    }

    public void setDisposalDate(final Date disposalDate) {
        this.disposalDate = disposalDate;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(final ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionCategory getCategory() {
        return category;
    }

    public void setCategory(final ConnectionCategory category) {
        this.category = category;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(final UsageType usageType) {
        this.usageType = usageType;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(final PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public WaterSource getWaterSource() {
        return waterSource;
    }

    public void setWaterSource(final WaterSource waterSource) {
        this.waterSource = waterSource;
    }

    public PipeSize getPipeSize() {
        return pipeSize;
    }

    public void setPipeSize(final PipeSize pipeSize) {
        this.pipeSize = pipeSize;
    }

    public Long getSumpCapacity() {
        return sumpCapacity;
    }

    public void setSumpCapacity(final Long sumpCapacity) {
        this.sumpCapacity = sumpCapacity;
    }

    public Integer getNumberOfPerson() {
        return numberOfPerson;
    }

    public void setNumberOfPerson(final Integer numberOfPerson) {
        this.numberOfPerson = numberOfPerson;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(final ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getApprovalNumber() {
        return approvalNumber;
    }

    public void setApprovalNumber(final String approvalNumber) {
        this.approvalNumber = approvalNumber;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(final Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getConnectionReason() {
        return connectionReason;
    }

    public void setConnectionReason(final String connectionReason) {
        this.connectionReason = connectionReason;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(final Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public EgDemand getDemand() {
        return demand;
    }

    public void setDemand(final EgDemand demand) {
        this.demand = demand;
    }

    public FieldInspectionDetails getFieldInspectionDetails() {
        return fieldInspectionDetails;
    }

    public void setFieldInspectionDetails(final FieldInspectionDetails fieldInspectionDetails) {
        this.fieldInspectionDetails = fieldInspectionDetails;
    }

    public List<ApplicationDocuments> getApplicationDocs() {
        return applicationDocs;
    }

    public void setApplicationDocs(final List<ApplicationDocuments> applicationDocs) {
        this.applicationDocs = applicationDocs;
    }

    public List<ConnectionEstimationDetails> getEstimationDetails() {
        return estimationDetails;
    }

    public void setEstimationDetails(final List<ConnectionEstimationDetails> estimationDetails) {
        this.estimationDetails = estimationDetails;
    }

    public EgwStatus getStatus() {
        return status;
    }

    public void setStatus(final EgwStatus status) {
        this.status = status;
    }

    @Override
    public String getStateDetails() {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return String.format("Application Number %s for %s with application date %s.", applicationNumber,
                applicationType.getName(), formatter.format(applicationDate));
    }

    public String getBplCardHolderName() {
        return bplCardHolderName;
    }

    public Date getWorkOrderDate() {
        return workOrderDate;
    }

    public void setWorkOrderDate(final Date workOrderDate) {
        this.workOrderDate = workOrderDate;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(final String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public void setBplCardHolderName(final String bplCardHolderName) {
        this.bplCardHolderName = bplCardHolderName;
    }

    public ExistingConnectionDetails getExistingConnection() {
        return existingConnection;
    }

    public void setExistingConnection(final ExistingConnectionDetails existingConnection) {
        this.existingConnection = existingConnection;
    }

    public FileStoreMapper getFileStore() {
        return fileStore;
    }

    public void setFileStore(final FileStoreMapper fileStore) {
        this.fileStore = fileStore;
    }

    public ChairPerson getChairPerson() {
        return chairPerson;
    }

    public void setChairPerson(final ChairPerson chairPerson) {
        this.chairPerson = chairPerson;
    }

    public double getDonationCharges() {
        return donationCharges;
    }

    public void setDonationCharges(final double donationCharges) {
        this.donationCharges = donationCharges;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(final Date executionDate) {
        this.executionDate = executionDate;
    }

    public Boolean getLegacy() {
        return legacy;
    }

    public void setLegacy(final Boolean legacy) {
        this.legacy = legacy;
    }

    public Boolean getIsHistory() {
        return isHistory;
    }

    public void setIsHistory(final Boolean isHistory) {
        this.isHistory = isHistory;
    }

    public Set<NonMeteredConnBillDetails> getNonmeteredBillDetails() {
        return nonmeteredBillDetails;
    }

    public void setNonmeteredBillDetails(final Set<NonMeteredConnBillDetails> nonmeteredBillDetails) {
        this.nonmeteredBillDetails = nonmeteredBillDetails;
    }

    public String getCloseConnectionType() {
        return closeConnectionType;
    }

    public void setCloseConnectionType(final String closeConnectionType) {
        this.closeConnectionType = closeConnectionType;
    }

    public String getCloseconnectionreason() {
        return closeconnectionreason;
    }

    public void setCloseconnectionreason(final String closeconnectionreason) {
        this.closeconnectionreason = closeconnectionreason;
    }

    public String getReConnectionReason() {
        return reConnectionReason;
    }

    public void setReConnectionReason(final String reConnectionReason) {
        this.reConnectionReason = reConnectionReason;
    }

    public Date getCloseApprovalDate() {
        return closeApprovalDate;
    }

    public void setCloseApprovalDate(final Date closeApprovalDate) {
        this.closeApprovalDate = closeApprovalDate;
    }

    public Date getReconnectionApprovalDate() {
        return reconnectionApprovalDate;
    }

    public void setReconnectionApprovalDate(final Date reconnectionApprovalDate) {
        this.reconnectionApprovalDate = reconnectionApprovalDate;
    }

    public String getMeesevaApplicationNumber() {
        return meesevaApplicationNumber;
    }

    public void setMeesevaApplicationNumber(final String meesevaApplicationNumber) {
        this.meesevaApplicationNumber = meesevaApplicationNumber;
    }

}