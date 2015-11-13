/**
 * eGov suite of products aim to improve the internal efficiency,transparency, accountability and the service delivery of the
 * government organizations.
 *
 * Copyright (C) <2015> eGovernments Foundation
 *
 * The updated version of eGov suite of products as by eGovernments Foundation is available at http://www.egovernments.org
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/ or http://www.gnu.org/licenses/gpl.html .
 *
 * In addition to the terms of the GPL license to be adhered to in using this program, the following additional terms are to be
 * complied with:
 *
 * 1) All versions of this program, verbatim or modified must carry this Legal Notice.
 *
 * 2) Any misrepresentation of the origin of the material is prohibited. It is required that all modified versions of this
 * material be marked in reasonable ways as different from the original version.
 *
 * 3) This license does not grant any rights to any user of the program with regards to rights under trademark law for use of the
 * trade names or trademarks of eGovernments Foundation.
 *
 * In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.wtms.application.service;

import static org.egov.wtms.utils.constants.WaterTaxConstants.WFLOW_ACTION_STEP_REJECT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;

import org.egov.commons.EgModules;
import org.egov.commons.Installment;
import org.egov.demand.model.EgDemand;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.EisCommonService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.search.elastic.entity.ApplicationIndex;
import org.egov.infra.search.elastic.entity.ApplicationIndexBuilder;
import org.egov.infra.search.elastic.service.ApplicationIndexService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.infra.utils.EgovThreadLocals;
import org.egov.infra.workflow.entity.State;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.infstr.workflow.WorkFlowMatrix;
import org.egov.pims.commons.Position;
import org.egov.ptis.domain.model.AssessmentDetails;
import org.egov.ptis.domain.model.OwnerName;
import org.egov.ptis.domain.service.property.PropertyExternalService;
import org.egov.wtms.application.entity.ApplicationDocuments;
import org.egov.wtms.application.entity.WaterConnection;
import org.egov.wtms.application.entity.WaterConnectionDetails;
import org.egov.wtms.application.repository.WaterConnectionDetailsRepository;
import org.egov.wtms.application.workflow.ApplicationWorkflowCustomDefaultImpl;
import org.egov.wtms.elasticSearch.service.ConsumerIndexService;
import org.egov.wtms.masters.entity.ApplicationType;
import org.egov.wtms.masters.entity.DocumentNames;
import org.egov.wtms.masters.entity.enums.ConnectionStatus;
import org.egov.wtms.masters.entity.enums.ConnectionType;
import org.egov.wtms.masters.service.ApplicationProcessTimeService;
import org.egov.wtms.masters.service.ApplicationTypeService;
import org.egov.wtms.masters.service.DocumentNamesService;
import org.egov.wtms.utils.PropertyExtnUtils;
import org.egov.wtms.utils.WaterTaxNumberGenerator;
import org.egov.wtms.utils.WaterTaxUtils;
import org.egov.wtms.utils.constants.WaterTaxConstants;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WaterConnectionDetailsService {

    protected WaterConnectionDetailsRepository waterConnectionDetailsRepository;
    private static final Logger LOG = LoggerFactory.getLogger(WaterConnectionDetailsService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;

    @Autowired
    private ApplicationTypeService applicationTypeService;

    @Autowired
    private SimpleWorkflowService<WaterConnectionDetails> waterConnectionWorkflowService;

    @Autowired
    private ApplicationProcessTimeService applicationProcessTimeService;

    @Autowired
    private ApplicationIndexService applicationIndexService;

    @Autowired
    private DocumentNamesService documentNamesService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private PropertyExtnUtils propertyExtnUtils;

    @Autowired
    private EisCommonService eisCommonService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private PositionMasterService positionMasterService;

    @Autowired
    private WaterTaxNumberGenerator waterTaxNumberGenerator;

    @Autowired
    private ConsumerIndexService consumerIndexService;

    @Autowired
    private WaterTaxUtils waterTaxUtils;

    @Autowired
    private ConnectionDemandService connectionDemandService;

    @Autowired
    private WaterConnectionSmsAndEmailService waterConnectionSmsAndEmailService;

    @Autowired
    public WaterConnectionDetailsService(final WaterConnectionDetailsRepository waterConnectionDetailsRepository) {
        this.waterConnectionDetailsRepository = waterConnectionDetailsRepository;
    }

    public WaterConnectionDetails findBy(final Long waterConnectionId) {
        return waterConnectionDetailsRepository.findOne(waterConnectionId);
    }

    public List<WaterConnectionDetails> findAll() {
        return waterConnectionDetailsRepository.findAll(new Sort(Sort.Direction.ASC,
                WaterTaxConstants.APPLICATION_NUMBER));
    }

    public WaterConnectionDetails findByApplicationNumber(final String applicationNumber) {
        return waterConnectionDetailsRepository.findByApplicationNumber(applicationNumber);
    }

    public WaterConnectionDetails load(final Long id) {
        return waterConnectionDetailsRepository.getOne(id);
    }

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public Page<WaterConnectionDetails> getListWaterConnectionDetails(final Integer pageNumber, final Integer pageSize) {
        final Pageable pageable = new PageRequest(pageNumber - 1, pageSize, Sort.Direction.ASC,
                WaterTaxConstants.APPLICATION_NUMBER);
        return waterConnectionDetailsRepository.findAll(pageable);
    }

    @Transactional
    public WaterConnectionDetails createNewWaterConnection(final WaterConnectionDetails waterConnectionDetails,
            final Long approvalPosition, final String approvalComent, final String additionalRule,
            final String workFlowAction) {
        if (waterConnectionDetails.getApplicationNumber() == null)
            waterConnectionDetails.setApplicationNumber(applicationNumberGenerator.generate());
        waterConnectionDetails.setApplicationDate(new Date());
        final Integer appProcessTime = applicationProcessTimeService.getApplicationProcessTime(
                waterConnectionDetails.getApplicationType(), waterConnectionDetails.getCategory());
        if (appProcessTime != null)
            waterConnectionDetails.setDisposalDate(getDisposalDate(waterConnectionDetails, appProcessTime));
        final WaterConnectionDetails savedWaterConnectionDetails = waterConnectionDetailsRepository
                .save(waterConnectionDetails);
        // TODO: as off now using anonymous created for MeeSeva after we need to craete role for this and add in appconfig for
        // recordCreatedBYNonEmployee API
        // so it will work as CSC Operator record
        if (userService.getUserById(waterConnectionDetails.getCreatedBy().getId()).getUsername().equals("anonymous")) {
            EgovThreadLocals.setUserId(Long.valueOf("40"));
            savedWaterConnectionDetails.setCreatedBy(userService.getUserById(EgovThreadLocals.getUserId()));
        }
        if (LOG.isDebugEnabled())
            LOG.debug(" persisting WaterConnectionDetail object is completed and WorkFlow API Stared ");
        final ApplicationWorkflowCustomDefaultImpl applicationWorkflowCustomDefaultImpl = getInitialisedWorkFlowBean();
        if (LOG.isDebugEnabled())
            LOG.debug("applicationWorkflowCustomDefaultImpl initialization is done");
        applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(savedWaterConnectionDetails,
                approvalPosition, approvalComent, additionalRule, workFlowAction);

        updateIndexes(savedWaterConnectionDetails);
        waterConnectionSmsAndEmailService.sendSmsAndEmail(waterConnectionDetails, workFlowAction);
        if (LOG.isDebugEnabled())
            LOG.debug("updating water Connection Deatail is complted");

        return savedWaterConnectionDetails;
    }

    @Transactional
    public WaterConnectionDetails createExisting(final WaterConnectionDetails waterConnectionDetails) {
        waterConnectionDetails.getExistingConnection().setWaterConnectionDetails(waterConnectionDetails);
        waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                WaterTaxConstants.APPLICATION_STATUS_SANCTIONED, WaterTaxConstants.MODULETYPE));
        if (waterConnectionDetails.getApplicationType().getCode().equalsIgnoreCase(WaterTaxConstants.ADDNLCONNECTION)) {
            final WaterConnectionDetails primaryConnectionDetails = getPrimaryConnectionDetailsByPropertyIdentifier(waterConnectionDetails
                    .getConnection().getPropertyIdentifier());
            waterConnectionDetails.getConnection().setParentConnection(primaryConnectionDetails.getConnection());
        }
        final WaterConnectionDetails savedWaterConnectionDetails = waterConnectionDetailsRepository
                .save(waterConnectionDetails);
        updateConsmerIndex(savedWaterConnectionDetails);
        // TODO Updation of Demand should be done here also fixupdate indexes
        return savedWaterConnectionDetails;
    }

    public List<ConnectionType> getAllConnectionTypes() {
        return Arrays.asList(ConnectionType.values());
    }

    public Map<String, String> getConnectionTypesMap() {
        final Map<String, String> connectionTypeMap = new LinkedHashMap<String, String>();
        connectionTypeMap.put(ConnectionType.METERED.toString(), WaterTaxConstants.METERED);
        connectionTypeMap.put(ConnectionType.NON_METERED.toString(), WaterTaxConstants.NON_METERED);
        return connectionTypeMap;
    }

    public List<DocumentNames> getAllActiveDocumentNames(final ApplicationType applicationType) {
        return documentNamesService.getAllActiveDocumentNamesByApplicationType(applicationType);
    }

    public WaterConnectionDetails findByApplicationNumberOrConsumerCodeAndStatus(final String number,
            final ConnectionStatus connectionStatus) {
        return waterConnectionDetailsRepository.findByApplicationNumberOrConnection_ConsumerCodeAndConnectionStatus(
                number, number, connectionStatus);
    }

    public WaterConnectionDetails findByApplicationNumberOrConsumerCode(final String number) {
        return waterConnectionDetailsRepository.findByApplicationNumberOrConnection_ConsumerCode(number, number);
    }

    public WaterConnectionDetails findByConnection(final WaterConnection waterConnection) {
        return waterConnectionDetailsRepository.findByConnection(waterConnection);
    }

    public WaterConnectionDetails findByConsumerCodeAndConnectionStatus(final String comsumerCode,
            final ConnectionStatus connectionStatus) {
        return waterConnectionDetailsRepository.findByConnection_ConsumerCodeAndConnectionStatus(comsumerCode,
                connectionStatus);
    }

    public WaterConnectionDetails getActiveConnectionDetailsByConnection(final WaterConnection waterConnection) {
        return waterConnectionDetailsRepository.findByConnectionAndConnectionStatus(waterConnection,
                ConnectionStatus.ACTIVE);
    }

    public WaterConnectionDetails getPrimaryConnectionDetailsByPropertyIdentifier(final String propertyIdentifier) {
        return waterConnectionDetailsRepository.getPrimaryConnectionDetailsByPropertyID(propertyIdentifier);
    }

    public List<WaterConnectionDetails> getAllConnectionDetailsByParentConnection(final Long parentId) {
        return waterConnectionDetailsRepository.getAllConnectionDetailsByParentConnection(parentId);
    }

    public List<Hashtable<String, Object>> getHistory(final WaterConnectionDetails waterConnectionDetails) {
        User user = null;
        final List<Hashtable<String, Object>> historyTable = new ArrayList<Hashtable<String, Object>>();
        final State state = waterConnectionDetails.getState();
        final Hashtable<String, Object> map = new Hashtable<String, Object>(0);
        if (null != state) {
            map.put("date", state.getDateInfo());
            map.put("comments", state.getComments());
            map.put("updatedBy", state.getLastModifiedBy().getUsername() + "::" + state.getLastModifiedBy().getName());
            map.put("status", state.getValue());
            final Position ownerPosition = state.getOwnerPosition();
            user = state.getOwnerUser();
            if (null != user) {
                map.put("user", user.getUsername() + "::" + user.getName());
                map.put("department", null != eisCommonService.getDepartmentForUser(user.getId()) ? eisCommonService
                        .getDepartmentForUser(user.getId()).getName() : "");
            } else if (null != ownerPosition && null != ownerPosition.getDeptDesig()) {
                user = eisCommonService.getUserForPosition(ownerPosition.getId(), new Date());
                map.put("user", null != user.getUsername() ? user.getUsername() + "::" + user.getName(): "");
                map.put("department", null != ownerPosition.getDeptDesig().getDepartment() ? ownerPosition
                        .getDeptDesig().getDepartment().getName() : "");
            }
            historyTable.add(map);
            if (!waterConnectionDetails.getStateHistory().isEmpty() && waterConnectionDetails.getStateHistory() != null)
                Collections.reverse(waterConnectionDetails.getStateHistory());
            for (final StateHistory stateHistory : waterConnectionDetails.getStateHistory()) {
                final Hashtable<String, Object> HistoryMap = new Hashtable<String, Object>(0);
                HistoryMap.put("date", stateHistory.getDateInfo());
                HistoryMap.put("comments", stateHistory.getComments());
                HistoryMap.put("updatedBy",stateHistory.getLastModifiedBy().getUsername() + "::" + stateHistory.getLastModifiedBy().getName());
                HistoryMap.put("status", stateHistory.getValue());
                final Position owner = stateHistory.getOwnerPosition();
                user = stateHistory.getOwnerUser();
                if (null != user) {
                    HistoryMap.put("user", user.getUsername() + "::" + user.getName());
                    HistoryMap.put("department",
                            null != eisCommonService.getDepartmentForUser(user.getId()) ? eisCommonService
                                    .getDepartmentForUser(user.getId()).getName() : "");
                } else if (null != owner && null != owner.getDeptDesig()) {
                    user = eisCommonService.getUserForPosition(owner.getId(), new Date());
                    HistoryMap.put("user", null != user.getUsername() ? user.getUsername() + "::" + user.getName(): "");
                    HistoryMap.put("department", null != owner.getDeptDesig().getDepartment() ? owner.getDeptDesig()
                            .getDepartment().getName() : "");
                }
                historyTable.add(HistoryMap);
            }
        }
        return historyTable;
    }

    @Transactional
    public WaterConnectionDetails updateWaterConnection(final WaterConnectionDetails waterConnectionDetails,
            final Long approvalPosition, final String approvalComent, String additionalRule,
            final String workFlowAction, final String mode) throws ValidationException {
        applicationStatusChange(waterConnectionDetails, workFlowAction, mode);
        if (WaterTaxConstants.APPLICATION_STATUS_CLOSERAPRROVED.equals(waterConnectionDetails.getStatus().getCode())
                && waterConnectionDetails.getCloseConnectionType() != null
                && workFlowAction.equals(WaterTaxConstants.APPROVEWORKFLOWACTION)) {
            waterConnectionDetails.setApplicationType(applicationTypeService
                    .findByCode(WaterTaxConstants.CLOSINGCONNECTION));
            waterConnectionDetails.setCloseApprovalDate(new Date());
        }
        if (WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONAPPROVED.equals(waterConnectionDetails.getStatus()
                .getCode())
                && waterConnectionDetails.getCloseConnectionType().equals(WaterTaxConstants.TEMPERARYCLOSECODE)
                && waterConnectionDetails.getReConnectionReason() != null
                && workFlowAction.equals(WaterTaxConstants.APPROVEWORKFLOWACTION)
                && ConnectionType.NON_METERED.equals(waterConnectionDetails.getConnectionType())) {
            waterConnectionDetails.setApplicationType(applicationTypeService
                    .findByCode(WaterTaxConstants.RECONNECTIONCONNECTION));
            waterConnectionDetails.setConnectionStatus(ConnectionStatus.ACTIVE);
            waterConnectionDetails.setReconnectionApprovalDate(new Date());
            Installment nonMeterReconnInstallment = null;
            Boolean reconnInSameInstallment = null;
            if (checkTwoDatesAreInSameInstallment(waterConnectionDetails)) {
                final Installment nonMeterCurrentInstallment = connectionDemandService.getCurrentInstallment(
                        WaterTaxConstants.WATER_RATES_NONMETERED_PTMODULE, null,
                        waterConnectionDetails.getReconnectionApprovalDate());
                final Calendar cal = Calendar.getInstance();
                cal.setTime(nonMeterCurrentInstallment.getToDate());
                cal.add(Calendar.DATE, 1);
                final Date newDateForNextInstall = cal.getTime();
                nonMeterReconnInstallment = connectionDemandService.getCurrentInstallment(
                        WaterTaxConstants.WATER_RATES_NONMETERED_PTMODULE, null, newDateForNextInstall);
                reconnInSameInstallment = Boolean.TRUE;
            } else {
                nonMeterReconnInstallment = connectionDemandService.getCurrentInstallment(
                        WaterTaxConstants.WATER_RATES_NONMETERED_PTMODULE, null,
                        waterConnectionDetails.getReconnectionApprovalDate());
                reconnInSameInstallment = Boolean.FALSE;
            }
            connectionDemandService.updateDemandForNonmeteredConnection(waterConnectionDetails,
                    nonMeterReconnInstallment, reconnInSameInstallment);
            updateIndexes(waterConnectionDetails);
        }

        if (ConnectionType.NON_METERED.equals(waterConnectionDetails.getConnectionType())
                && WaterTaxConstants.APPLICATION_STATUS_SANCTIONED.equalsIgnoreCase(waterConnectionDetails.getStatus()
                        .getCode())) {
            connectionDemandService.updateDemandForNonmeteredConnection(waterConnectionDetails, null, null);
            updateIndexes(waterConnectionDetails);
        }

        WaterConnectionDetails updatedWaterConnectionDetails = waterConnectionDetailsRepository
                .save(waterConnectionDetails);
        final ApplicationWorkflowCustomDefaultImpl applicationWorkflowCustomDefaultImpl = getInitialisedWorkFlowBean();
        if (waterConnectionDetails.getCloseConnectionType() != null)
            additionalRule = WaterTaxConstants.WORKFLOW_CLOSUREADDITIONALRULE;

        if (waterConnectionDetails.getReConnectionReason() != null)
            additionalRule = WaterTaxConstants.RECONNECTIONCONNECTION;
        applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(updatedWaterConnectionDetails,
                approvalPosition, approvalComent, additionalRule, workFlowAction);

        // To backUpdate waterConnectiondetails after ClosureConnection is
        // cancelled
        if (waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getReConnectionReason() == null
                && waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_CANCELLED)
                && waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.INACTIVE)) {
            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                    WaterTaxConstants.APPLICATION_STATUS_SANCTIONED, WaterTaxConstants.MODULETYPE));
            waterConnectionDetails.setConnectionStatus(ConnectionStatus.ACTIVE);
            waterConnectionDetails.setCloseConnectionType(null);
            waterConnectionDetails.setCloseconnectionreason(null);
            waterConnectionDetails.setApplicationType(applicationTypeService.findByCode(waterConnectionDetails
                    .getPreviousApplicationType()));
            updateIndexes(waterConnectionDetails);
            updatedWaterConnectionDetails = waterConnectionDetailsRepository.save(waterConnectionDetails);
        }
        // back to CLoserSanctioned Status if Reconnection is Rejected 2 times
        if (waterConnectionDetails.getReConnectionReason() != null
                && waterConnectionDetails.getCloseConnectionType() == WaterTaxConstants.TEMPERARYCLOSECODE
                && waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_CANCELLED)
                && waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.INACTIVE)) {
            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                    WaterTaxConstants.APPLICATION_STATUS_CLOSERSANCTIONED, WaterTaxConstants.MODULETYPE));
            waterConnectionDetails.setConnectionStatus(ConnectionStatus.CLOSED);
            waterConnectionDetails.setReConnectionReason(null);
            waterConnectionDetails.setApplicationType(applicationTypeService
                    .findByCode(WaterTaxConstants.CLOSINGCONNECTION));
            updateIndexes(waterConnectionDetails);
            updatedWaterConnectionDetails = waterConnectionDetailsRepository.save(waterConnectionDetails);
        }
        if (!workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT))
            waterConnectionSmsAndEmailService.sendSmsAndEmail(waterConnectionDetails, workFlowAction);
        return updatedWaterConnectionDetails;
    }

    /**
     * @return Initialise Bean ApplicationWorkflowCustomDefaultImpl
     */
    public ApplicationWorkflowCustomDefaultImpl getInitialisedWorkFlowBean() {
        ApplicationWorkflowCustomDefaultImpl applicationWorkflowCustomDefaultImpl = null;
        if (null != context)
            applicationWorkflowCustomDefaultImpl = (ApplicationWorkflowCustomDefaultImpl) context
            .getBean("applicationWorkflowCustomDefaultImpl");
        return applicationWorkflowCustomDefaultImpl;
    }

    public Boolean checkTwoDatesAreInSameInstallment(final WaterConnectionDetails waterConnectionDetails) {
        Boolean DateInSameInstallment = Boolean.FALSE;

        final Installment nonMeterClosedInstallment = connectionDemandService.getCurrentInstallment(
                WaterTaxConstants.WATER_RATES_NONMETERED_PTMODULE, null, waterConnectionDetails.getCloseApprovalDate());
        final Installment nonMeterReconnInstallment = connectionDemandService.getCurrentInstallment(
                WaterTaxConstants.WATER_RATES_NONMETERED_PTMODULE, null,
                waterConnectionDetails.getReconnectionApprovalDate());
        if (nonMeterClosedInstallment.getInstallmentNumber().equals(nonMeterReconnInstallment.getInstallmentNumber()))
            DateInSameInstallment = Boolean.TRUE;

        return DateInSameInstallment;
    }

    public void applicationStatusChange(final WaterConnectionDetails waterConnectionDetails,
            final String workFlowAction, final String mode) {
        if (null != waterConnectionDetails && null != waterConnectionDetails.getStatus()
                && null != waterConnectionDetails.getStatus().getCode())
            if (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_CREATED)
                    && waterConnectionDetails.getState() != null && workFlowAction.equals("Submit")) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_VERIFIED, WaterTaxConstants.MODULETYPE));
                updateIndexes(waterConnectionDetails);
            } else if (waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_VERIFIED)) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_ESTIMATENOTICEGEN, WaterTaxConstants.MODULETYPE));
                updateIndexes(waterConnectionDetails);
            } else if (waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_ESTIMATENOTICEGEN)) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_FEEPAID, WaterTaxConstants.MODULETYPE));
                updateIndexes(waterConnectionDetails);
            } else if (waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_FEEPAID)
                    && workFlowAction.equalsIgnoreCase(WaterTaxConstants.APPROVEWORKFLOWACTION)) {

                if (waterConnectionDetails.getConnection().getConsumerCode() == null)
                    waterConnectionDetails.getConnection().setConsumerCode(
                            waterTaxNumberGenerator.generateConsumerNumber());

                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_APPROVED, WaterTaxConstants.MODULETYPE));
                updateIndexes(waterConnectionDetails);
            } else if (waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_APPROVED)) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_DIGITALSIGNUPDATED, WaterTaxConstants.MODULETYPE));

                updateIndexes(waterConnectionDetails);
            }
            else if (waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_DIGITALSIGNUPDATED)) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_WOGENERATED, WaterTaxConstants.MODULETYPE));

                updateIndexes(waterConnectionDetails);
            }
            else if (WaterTaxConstants.APPLICATION_STATUS_WOGENERATED.equalsIgnoreCase(waterConnectionDetails
                    .getStatus().getCode())) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_SANCTIONED, WaterTaxConstants.MODULETYPE));
                updateIndexes(waterConnectionDetails);
            } else if (WaterTaxConstants.APPLICATION_STATUS_SANCTIONED.equalsIgnoreCase(waterConnectionDetails
                    .getStatus().getCode()) && waterConnectionDetails.getCloseConnectionType() != null) {
                waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                        WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED, WaterTaxConstants.MODULETYPE));
                updateIndexes(waterConnectionDetails);
            } else if (!workFlowAction.equals("Reject"))
                if (!mode.equals("closeredit")
                        && WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED
                        .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())
                        && waterConnectionDetails.getCloseConnectionType() != null) {
                    waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                            WaterTaxConstants.APPLICATION_STATUS_CLOSERINPROGRESS, WaterTaxConstants.MODULETYPE));
                    updateIndexes(waterConnectionDetails);
                } else if (workFlowAction.equals("Approve")
                        && WaterTaxConstants.APPLICATION_STATUS_CLOSERINPROGRESS
                        .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())
                        && waterConnectionDetails.getCloseConnectionType() != null) {
                    waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                            WaterTaxConstants.APPLICATION_STATUS_CLOSERAPRROVED, WaterTaxConstants.MODULETYPE));
                    updateIndexes(waterConnectionDetails);
                } 
                else if (WaterTaxConstants.APPLICATION_STATUS_CLOSERAPRROVED.equalsIgnoreCase(waterConnectionDetails
                        .getStatus().getCode()) && waterConnectionDetails.getCloseConnectionType() != null) {
                    waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                            WaterTaxConstants.APPLICATION_STATUS_CLOSERDIGSIGNUPDATED, WaterTaxConstants.MODULETYPE));

                    updateIndexes(waterConnectionDetails);
                }else if (WaterTaxConstants.APPLICATION_STATUS_CLOSERDIGSIGNUPDATED.equalsIgnoreCase(waterConnectionDetails
                        .getStatus().getCode()) && waterConnectionDetails.getCloseConnectionType() != null) {
                    waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                            WaterTaxConstants.APPLICATION_STATUS_CLOSERSANCTIONED, WaterTaxConstants.MODULETYPE));
                    updateIndexes(waterConnectionDetails);
                } else if (WaterTaxConstants.APPLICATION_STATUS_CLOSERSANCTIONED
                        .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())
                        && waterConnectionDetails.getCloseConnectionType().equals(WaterTaxConstants.TEMPERARYCLOSECODE)) {
                    waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                            WaterTaxConstants.WORKFLOW_RECONNCTIONINITIATED, WaterTaxConstants.MODULETYPE));
                    updateIndexes(waterConnectionDetails);
                } else if (!workFlowAction.equals("Reject"))
                    if (!mode.equals("reconnectioneredit"))
                        if (WaterTaxConstants.WORKFLOW_RECONNCTIONINITIATED.equalsIgnoreCase(waterConnectionDetails
                                .getStatus().getCode())
                                && waterConnectionDetails.getCloseConnectionType().equals(
                                        WaterTaxConstants.TEMPERARYCLOSECODE)) {
                            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                                    WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONINPROGRESS,
                                    WaterTaxConstants.MODULETYPE));
                            updateIndexes(waterConnectionDetails);
                        }

                        else if (workFlowAction.equals("Approve")
                                && WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONINPROGRESS
                                .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())
                                && waterConnectionDetails.getCloseConnectionType().equals(
                                        WaterTaxConstants.TEMPERARYCLOSECODE)) {
                            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                                    WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONAPPROVED,
                                    WaterTaxConstants.MODULETYPE));
                            updateIndexes(waterConnectionDetails);
                        }

                        else if (WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONAPPROVED
                                .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())
                                && waterConnectionDetails.getCloseConnectionType().equals(
                                        WaterTaxConstants.TEMPERARYCLOSECODE)) {
                            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                                    WaterTaxConstants.APPLICATION_STATUS_RECONNDIGSIGNUPDATED,
                                    WaterTaxConstants.MODULETYPE));
                            updateIndexes(waterConnectionDetails);
                        }
                        else if (WaterTaxConstants.APPLICATION_STATUS_RECONNDIGSIGNUPDATED
                                .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())
                                && waterConnectionDetails.getCloseConnectionType().equals(
                                        WaterTaxConstants.TEMPERARYCLOSECODE)) {
                            waterConnectionDetails.setStatus(waterTaxUtils.getStatusByCodeAndModuleType(
                                    WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONSANCTIONED,
                                    WaterTaxConstants.MODULETYPE));
                            updateIndexes(waterConnectionDetails);
                        }

    }

    public Long getApprovalPositionByMatrixDesignation(final WaterConnectionDetails waterConnectionDetails,
            Long approvalPosition, final String additionalRule, final String mode, final String workFlowAction) {
        final WorkFlowMatrix wfmatrix = waterConnectionWorkflowService.getWfMatrix(waterConnectionDetails
                .getStateType(), null, null, additionalRule, waterConnectionDetails.getCurrentState().getValue(), null);
        if (waterConnectionDetails.getStatus() != null && waterConnectionDetails.getStatus().getCode() != null)
            if (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_CREATED)
                    && waterConnectionDetails.getState() != null)
                if (mode.equals("edit"))
                    approvalPosition = waterConnectionDetails.getState().getOwnerPosition().getId();
                else
                    approvalPosition = waterTaxUtils.getApproverPosition(wfmatrix.getNextDesignation(),
                            waterConnectionDetails);
            else if (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_APPROVED)
                    || WaterTaxConstants.APPLICATION_STATUS_ESTIMATENOTICEGEN.equalsIgnoreCase(waterConnectionDetails
                            .getStatus().getCode()) ||(!"".equals(workFlowAction) && workFlowAction.equals(WFLOW_ACTION_STEP_REJECT)
                                    && waterConnectionDetails.getStatus().getCode()
                                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED)
                                    && waterConnectionDetails.getState().getValue().equals("Rejected")))
                approvalPosition = waterTaxUtils.getApproverPosition(wfmatrix.getNextDesignation(),
                        waterConnectionDetails);
            else if (wfmatrix.getNextDesignation()!=null && (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_FEEPAID)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERINPROGRESS)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERAPRROVED)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERDIGSIGNUPDATED)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_RECONNDIGSIGNUPDATED)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_DIGITALSIGNUPDATED)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONAPPROVED)
                    || workFlowAction.equals(WFLOW_ACTION_STEP_REJECT) && waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.WORKFLOW_RECONNCTIONINITIATED)
                    ||(workFlowAction.equals(WFLOW_ACTION_STEP_REJECT)
                            && waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED)) ))
                approvalPosition = waterTaxUtils.getApproverPosition(wfmatrix.getNextDesignation(),
                        waterConnectionDetails);
            else if (wfmatrix.getNextDesignation()!=null && (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_VERIFIED)
                    || (!workFlowAction.equals(WFLOW_ACTION_STEP_REJECT)
                    && waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED))
                    || !waterConnectionDetails.getState().getValue().equals("Rejected")
                    && !workFlowAction.equals(WFLOW_ACTION_STEP_REJECT)
                    && (waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.WORKFLOW_RECONNCTIONINITIATED) || waterConnectionDetails
                            .getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONINPROGRESS)) )) {
                final Position posobj = waterTaxUtils.getCityLevelCommissionerPosition(wfmatrix.getNextDesignation());
                if (posobj != null)
                    approvalPosition = posobj.getId();
            }

        return approvalPosition;
    }

    public void updateConsmerIndex(final WaterConnectionDetails waterConnectionDetails) {
        final AssessmentDetails assessmentDetails = propertyExtnUtils.getAssessmentDetailsForFlag(
                waterConnectionDetails.getConnection().getPropertyIdentifier(),
                PropertyExternalService.FLAG_FULL_DETAILS);
        final BigDecimal amountTodisplayInIndex = getTotalAmount(waterConnectionDetails);
        if (waterConnectionDetails.getLegacy())
            consumerIndexService.createConsumerIndex(waterConnectionDetails, assessmentDetails, amountTodisplayInIndex);
    }

    public void updateIndexes(final WaterConnectionDetails waterConnectionDetails) {
        final AssessmentDetails assessmentDetails = propertyExtnUtils.getAssessmentDetailsForFlag(
                waterConnectionDetails.getConnection().getPropertyIdentifier(),
                PropertyExternalService.FLAG_FULL_DETAILS);
        if (LOG.isDebugEnabled())
            LOG.debug(" updating Indexes Started... ");
        BigDecimal amountTodisplayInIndex = BigDecimal.ZERO;
        if (waterConnectionDetails.getConnection().getConsumerCode() != null)
            amountTodisplayInIndex = getTotalAmount(waterConnectionDetails);
        if (waterConnectionDetails.getLegacy()) {
            consumerIndexService.createConsumerIndex(waterConnectionDetails, assessmentDetails, amountTodisplayInIndex);
            return;
        }
        Iterator<OwnerName> ownerNameItr = assessmentDetails.getOwnerNames().iterator();
        final StringBuilder consumerName = new StringBuilder();
        if (ownerNameItr.hasNext()) {
            consumerName.append(ownerNameItr.next().getOwnerName());
            while (ownerNameItr.hasNext())
                consumerName.append(", ".concat(ownerNameItr.next().getOwnerName()));
        }
        if (waterConnectionDetails.getStatus() != null
                && !waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_CREATED)) {
            if (waterConnectionDetails.getStatus() != null
                    && (waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_APPROVED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_VERIFIED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_ESTIMATENOTICEGEN)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_FEEPAID)
                             || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_DIGITALSIGNUPDATED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CANCELLED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERINPROGRESS)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERAPRROVED)
                            ||waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERDIGSIGNUPDATED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERSANCTIONED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.WORKFLOW_RECONNCTIONINITIATED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONINPROGRESS)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONSANCTIONED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_RECONNDIGSIGNUPDATED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_WOGENERATED)
                            || waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS_SANCTIONED) || waterConnectionDetails
                            .getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERSANCTIONED))) {
                final ApplicationIndex applicationIndex = applicationIndexService
                        .findByApplicationNumber(waterConnectionDetails.getApplicationNumber());
                applicationIndex.setStatus(waterConnectionDetails.getStatus().getDescription());
                applicationIndex.setApplicantAddress(assessmentDetails.getPropertyAddress());
                if (waterConnectionDetails.getConnection().getConsumerCode() != null)
                    applicationIndex.setConsumerCode(waterConnectionDetails.getConnection().getConsumerCode());
                applicationIndexService.updateApplicationIndex(applicationIndex);
            }
            // Creating Consumer Index only on Sanction
            if (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_SANCTIONED))
                if (waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.INPROGRESS)
                        && !waterConnectionDetails.getApplicationType().getCode()
                        .equalsIgnoreCase(WaterTaxConstants.CHANGEOFUSE)) {
                    waterConnectionDetails.setConnectionStatus(ConnectionStatus.ACTIVE);
                    if (LOG.isDebugEnabled())
                        LOG.debug(" updating Consumer Index Started... ");
                    if (!waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.INACTIVE)
                            || !waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.INPROGRESS))
                        consumerIndexService.createConsumerIndex(waterConnectionDetails, assessmentDetails,
                                amountTodisplayInIndex);
                    if (LOG.isDebugEnabled())
                        LOG.debug(" updating Consumer Index completed... ");
                }
            // To Update After ClosureConnection is rejected
            if (waterConnectionDetails.getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS_SANCTIONED)
                    && waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.ACTIVE))
                consumerIndexService.createConsumerIndex(waterConnectionDetails, assessmentDetails,
                        amountTodisplayInIndex);
            if (waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERSANCTIONED)
                    || waterConnectionDetails.getStatus().getCode()
                    .equals(WaterTaxConstants.APPLICATION_STATUS_CLOSERAPRROVED)
                    && waterConnectionDetails.getConnectionStatus().equals(ConnectionStatus.CLOSED))
                consumerIndexService.createConsumerIndex(waterConnectionDetails, assessmentDetails,
                        amountTodisplayInIndex);

            if (waterConnectionDetails.getCloseConnectionType() != null
                    && waterConnectionDetails.getCloseConnectionType().equals(WaterTaxConstants.TEMPERARYCLOSECODE)
                    && (waterConnectionDetails.getStatus().getCode()
                            .equals(WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONAPPROVED) || waterConnectionDetails
                            .getStatus().getCode().equals(WaterTaxConstants.APPLICATION_STATUS__RECONNCTIONSANCTIONED))) {
                waterConnectionDetails.setConnectionStatus(ConnectionStatus.ACTIVE);
                consumerIndexService.createConsumerIndex(waterConnectionDetails, assessmentDetails,
                        amountTodisplayInIndex);
            }
        } else {
            final String strQuery = "select md from EgModules md where md.name=:name";
            final Query hql = getCurrentSession().createQuery(strQuery);
            hql.setParameter("name", WaterTaxConstants.EGMODULES_NAME);
            if (LOG.isDebugEnabled())
                LOG.debug(" updating Application Index creation Started... ");
            final ApplicationIndexBuilder applicationIndexBuilder = new ApplicationIndexBuilder(
                    ((EgModules) hql.uniqueResult()).getName(), waterConnectionDetails.getApplicationNumber(),
                    waterConnectionDetails.getApplicationDate(), waterConnectionDetails.getApplicationType().getName(),
                    consumerName.toString(), waterConnectionDetails.getStatus().getDescription().toString(),
                    "/wtms/application/view/" + waterConnectionDetails.getApplicationNumber(),
                    assessmentDetails.getPropertyAddress());

            if (waterConnectionDetails.getDisposalDate() != null)
                applicationIndexBuilder.disposalDate(waterConnectionDetails.getDisposalDate());
            ownerNameItr = assessmentDetails.getOwnerNames().iterator();
            if (ownerNameItr != null && ownerNameItr.hasNext())
                applicationIndexBuilder.mobileNumber(ownerNameItr.next().getMobileNumber());

            final ApplicationIndex applicationIndex = applicationIndexBuilder.build();
            applicationIndexService.createApplicationIndex(applicationIndex);
            if (LOG.isDebugEnabled())
                LOG.debug(" updating Application Index creation complted... ");
        }
    }

    public Date getDisposalDate(final WaterConnectionDetails waterConnectionDetails, final Integer appProcessTime) {
        final Calendar c = Calendar.getInstance();
        c.setTime(waterConnectionDetails.getApplicationDate());
        c.add(Calendar.DATE, appProcessTime);
        return c.getTime();
    }

    public WaterConnectionDetails getParentConnectionDetails(final String propertyIdentifier,
            final ConnectionStatus connectionStatus) {
        return waterConnectionDetailsRepository
                .findByConnection_PropertyIdentifierAndConnectionStatusAndConnection_ParentConnectionIsNull(
                        propertyIdentifier, connectionStatus);
    }

    public WaterConnectionDetails getParentConnectionDetailsForParentConnectionNotNull(final String consumercode,
            final ConnectionStatus connectionStatus) {
        return waterConnectionDetailsRepository
                .findByConnection_ConsumerCodeAndConnectionStatusAndAndConnection_ParentConnectionIsNotNull(
                        consumercode, connectionStatus);
    }

    public WaterConnectionDetails getWaterConnectionDetailsByDemand(final EgDemand demand) {
        return waterConnectionDetailsRepository.findByDemand(demand);
    }

    @Transactional
    public void save(final WaterConnectionDetails detail) {
        waterConnectionDetailsRepository.save(detail);
    }

    public WaterConnectionDetails getActiveNonHistoryConnectionDetailsByConnection(final WaterConnection waterConnection) {
        return waterConnectionDetailsRepository.findByConnectionAndConnectionStatusAndIsHistory(waterConnection,
                ConnectionStatus.ACTIVE, Boolean.FALSE);
    }

    public BigDecimal getTotalAmount(final WaterConnectionDetails waterConnectionDetails) {
        final EgDemand currentDemand = waterConnectionDetails.getDemand();
        BigDecimal balance = BigDecimal.ZERO;
        if (currentDemand != null) {
            final List<Object> instVsAmt = connectionDemandService.getDmdCollAmtInstallmentWise(currentDemand);
            for (final Object object : instVsAmt) {
                final Object[] ddObject = (Object[]) object;
                final BigDecimal dmdAmt = (BigDecimal) ddObject[2];
                BigDecimal collAmt = BigDecimal.ZERO;
                if (ddObject[2] != null)
                    collAmt = new BigDecimal((Double) ddObject[3]);
                balance = balance.add(dmdAmt.subtract(collAmt));
            }
        }
        return balance;
    }

    public List<ApplicationDocuments> getApplicationDocForExceptClosureAndReConnection(
            final WaterConnectionDetails waterConnectionDetails) {
        final List<ApplicationDocuments> tempDocList = new ArrayList<ApplicationDocuments>(0);
        if (waterConnectionDetails != null)
            for (final ApplicationDocuments appDoc : waterConnectionDetails.getApplicationDocs())
                if (appDoc.getDocumentNames() != null
                && (appDoc.getDocumentNames().getApplicationType().getCode()
                        .equals(WaterTaxConstants.NEWCONNECTION)
                        || appDoc.getDocumentNames().getApplicationType().getCode()
                        .equals(WaterTaxConstants.ADDNLCONNECTION) || appDoc.getDocumentNames()
                        .getApplicationType().getCode()
                        .equals(WaterTaxConstants.CHANGEOFUSE)))
                    tempDocList.add(appDoc);
        return tempDocList;
    }
}
