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
package org.egov.ptis.domain.service.property;

import static org.egov.ptis.constants.PropertyTaxConstants.ARR_COLL_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.ARR_DMD_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.COMMISSIONER_DESGN;
import static org.egov.ptis.constants.PropertyTaxConstants.CURR_COLL_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.CURR_DMD_STR;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_STR_EDUCATIONAL_CESS;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_STR_GENERAL_TAX;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_STR_LIBRARY_CESS;
import static org.egov.ptis.constants.PropertyTaxConstants.DEMANDRSN_STR_UNAUTHORIZED_PENALTY;
import static org.egov.ptis.constants.PropertyTaxConstants.REVENUE_INSPECTOR_DESGN;
import static org.egov.ptis.constants.PropertyTaxConstants.REVENUE_OFFICER_DESGN;
import static org.egov.ptis.constants.PropertyTaxConstants.VR_STATUS_APPROVED;
import static org.egov.ptis.constants.PropertyTaxConstants.VR_STATUS_REJECTED;
import static org.egov.ptis.constants.PropertyTaxConstants.VR_STATUS_REJECTION_ACK_GENERATED;
import static org.egov.ptis.constants.PropertyTaxConstants.VR_STATUS_WORKFLOW;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_APPROVE;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_FORWARD;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_NOTICE_GENERATE;
import static org.egov.ptis.constants.PropertyTaxConstants.WFLOW_ACTION_STEP_REJECT;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REJECTED;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REVENUE_CLERK_APPROVAL_PENDING;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REVENUE_INSPECTOR_APPROVAL_PENDING;
import static org.egov.ptis.constants.PropertyTaxConstants.WF_STATE_REVENUE_INSPECTOR_REJECTED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.EisCommonService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.infstr.workflow.WorkFlowMatrix;
import org.egov.pims.commons.Designation;
import org.egov.pims.commons.Position;
import org.egov.ptis.client.util.PropertyTaxUtil;
import org.egov.ptis.domain.dao.demand.PtDemandDao;
import org.egov.ptis.domain.entity.demand.Ptdemand;
import org.egov.ptis.domain.entity.property.BasicProperty;
import org.egov.ptis.domain.entity.property.Property;
import org.egov.ptis.domain.entity.property.VacancyRemission;
import org.egov.ptis.domain.entity.property.VacancyRemissionApproval;
import org.egov.ptis.domain.entity.property.VacancyRemissionDetails;
import org.egov.ptis.domain.repository.vacancyremission.VacancyRemissionApprovalRepository;
import org.egov.ptis.domain.repository.vacancyremission.VacancyRemissionRepository;
import org.elasticsearch.common.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Service
@Transactional(readOnly = true)
public class VacancyRemissionService {

    private static final Logger LOG = LoggerFactory.getLogger(VacancyRemissionService.class);

    private final VacancyRemissionRepository vacancyRemissionRepository;

    private final VacancyRemissionApprovalRepository vacancyRemissionApprovalRepository;

    @Autowired
    private EisCommonService eisCommonService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private PositionMasterService positionMasterService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private SimpleWorkflowService<VacancyRemission> vacancyRemissionWorkflowService;

    @Autowired
    private PropertyTaxUtil propertyTaxUtil;

    @Autowired
    private PtDemandDao ptDemandDAO;
    
    @Autowired
    private PropertyService propertyService;
    
    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;

    @Autowired
    public VacancyRemissionService(final VacancyRemissionRepository vacancyRemissionRepository,
            final VacancyRemissionApprovalRepository vacancyRemissionApprovalRepository) {
        this.vacancyRemissionRepository = vacancyRemissionRepository;
        this.vacancyRemissionApprovalRepository = vacancyRemissionApprovalRepository;
    }

    public VacancyRemission getApprovedVacancyRemissionForProperty(final String upicNo) {
        return vacancyRemissionRepository.findByUpicNo(upicNo);
    }

    public VacancyRemission getRejectAckGeneratedVacancyRemissionForProperty(final String upicNo) {
        return vacancyRemissionRepository.findRejectionAckGeneratedForUpicNo(upicNo);
    }

    public VacancyRemission getVacancyRemissionById(Long id) {
        return vacancyRemissionRepository.findOne(id);
    }

    public List<VacancyRemission> getAllVacancyRemissionByUpicNo(final String upicNo) {
        return vacancyRemissionRepository.getAllVacancyRemissionByUpicNo(upicNo);
    }

    public VacancyRemission getRejectedVacancyRemissionForProperty(final String upicNo) {
        return vacancyRemissionRepository.findRejectedByUpicNo(upicNo);
    }

    @Transactional
    public void saveVacancyRemission(final VacancyRemission vacancyRemission, Long approvalPosition,
            final String approvalComent, final String additionalRule, final String workFlowAction, Boolean propertyByEmployee) {
        if (LOG.isDebugEnabled())
            LOG.debug(" Create WorkFlow Transition Started  ...");
        final User user = securityUtils.getCurrentUser();
        final DateTime currentDate = new DateTime();
        final Assignment userAssignment = assignmentService.getPrimaryAssignmentForUser(user.getId());
        Position pos = null;
        Assignment wfInitiator = null;
        String currentState = "";

        if (!propertyByEmployee) {
            currentState = "Created";
            final Assignment assignment = propertyService.getUserPositionByZone(vacancyRemission.getBasicProperty());
            if (null != assignment) 
            	approvalPosition = assignment.getPosition().getId();
        } else
            currentState = null;
        
        if (vacancyRemission.getId() != null
                && (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT) || workFlowAction
                        .equalsIgnoreCase(WFLOW_ACTION_STEP_NOTICE_GENERATE))) {
        	wfInitiator = getWorkflowInitiator(vacancyRemission);
        }

        if(StringUtils.isBlank(vacancyRemission.getApplicationNumber())){
        	vacancyRemission.setApplicationNumber(applicationNumberGenerator.generate());
        }
        if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_NOTICE_GENERATE)) {
            if (wfInitiator.equals(userAssignment)) {
                vacancyRemission.setStatus(VR_STATUS_REJECTION_ACK_GENERATED);
                vacancyRemission.transition(true).end().withSenderName(user.getName()).withComments(approvalComent)
                        .withDateInfo(currentDate.toDate());
            }
        } else if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT)) {
            final String stateValue = WF_STATE_REJECTED;
            vacancyRemission.setStatus(VR_STATUS_REJECTED);
            vacancyRemission.transition(true).withSenderName(user.getName()).withComments(approvalComent)
                    .withStateValue(stateValue).withDateInfo(currentDate.toDate()).withOwner(wfInitiator.getPosition())
                    .withNextAction("Application Rejected");
        } else {
            if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_FORWARD)) {
                vacancyRemission.setStatus(VR_STATUS_WORKFLOW);
            } else if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_APPROVE)) {
                vacancyRemission.setStatus(VR_STATUS_APPROVED);
            }

            if (null != approvalPosition && approvalPosition != -1 && !approvalPosition.equals(Long.valueOf(0)))
                pos = positionMasterService.getPositionById(approvalPosition);
            WorkFlowMatrix wfmatrix = null;
            if (null == vacancyRemission.getState()) {
                wfmatrix = vacancyRemissionWorkflowService.getWfMatrix(vacancyRemission.getStateType(), null, null,
                        additionalRule, currentState, null);
                vacancyRemission.transition().start().withSenderName(user.getName()).withComments(approvalComent)
                        .withStateValue(wfmatrix.getNextState()).withDateInfo(new Date()).withOwner(pos)
                        .withNextAction(wfmatrix.getNextAction());
            } else {
                wfmatrix = vacancyRemissionWorkflowService.getWfMatrix(vacancyRemission.getStateType(), null, null,
                        additionalRule, vacancyRemission.getCurrentState().getValue(), null);

                if (wfmatrix != null) {
                    if (wfmatrix.getNextAction().equalsIgnoreCase("END")) {
                        vacancyRemission.transition(true).end().withSenderName(user.getName()).withComments(approvalComent)
                                .withDateInfo(currentDate.toDate());
                    } else {
                        vacancyRemission.transition(false).withSenderName(user.getName()).withComments(approvalComent)
                                .withStateValue(wfmatrix.getNextState()).withDateInfo(currentDate.toDate())
                                .withOwner(pos).withNextAction(wfmatrix.getNextAction());
                    }
                }
            }

        }
        if (LOG.isDebugEnabled())
            LOG.debug(" WorkFlow Transition Completed  ...");
        vacancyRemissionRepository.save(vacancyRemission);
    }

    public void addModelAttributes(Model model, BasicProperty basicProperty) {
        Property property = basicProperty.getActiveProperty();
        model.addAttribute("property", property);
        Ptdemand ptDemand = ptDemandDAO.getNonHistoryCurrDmdForProperty(property);
        if (ptDemand != null && ptDemand.getDmdCalculations() != null && ptDemand.getDmdCalculations().getAlv() != null)
            model.addAttribute("ARV", ptDemand.getDmdCalculations().getAlv());
        else
            model.addAttribute("ARV", BigDecimal.ZERO);
        if (!basicProperty.getActiveProperty().getIsExemptedFromTax()) {
            final Map<String, BigDecimal> demandCollMap = propertyTaxUtil.prepareDemandDetForView(property,
                    PropertyTaxUtil.getCurrentInstallment());
            model.addAttribute("currTax", demandCollMap.get(CURR_DMD_STR));
            model.addAttribute("eduCess", demandCollMap.get(DEMANDRSN_STR_EDUCATIONAL_CESS));
            model.addAttribute("currTaxDue", demandCollMap.get(CURR_DMD_STR).subtract(demandCollMap.get(CURR_COLL_STR)));
            model.addAttribute("libraryCess", demandCollMap.get(DEMANDRSN_STR_LIBRARY_CESS));
            model.addAttribute("totalArrDue", demandCollMap.get(ARR_DMD_STR).subtract(demandCollMap.get(ARR_COLL_STR)));
            model.addAttribute("propertyTax", demandCollMap.get(DEMANDRSN_STR_GENERAL_TAX));
            BigDecimal totalTax = demandCollMap.get(DEMANDRSN_STR_EDUCATIONAL_CESS)
                    .add(demandCollMap.get(DEMANDRSN_STR_LIBRARY_CESS))
                    .add(demandCollMap.get(DEMANDRSN_STR_GENERAL_TAX));
            if(StringUtils.isNotBlank(property.getPropertyDetail().getDeviationPercentage()) 
            		&& !property.getPropertyDetail().getDeviationPercentage().equalsIgnoreCase("-1")){
            	model.addAttribute("unauthorisedPenalty", demandCollMap.get(DEMANDRSN_STR_UNAUTHORIZED_PENALTY));
            	model.addAttribute("totalTax",totalTax.add(demandCollMap.get(DEMANDRSN_STR_UNAUTHORIZED_PENALTY)));
            	model.addAttribute("showUnauthorisedPenalty", "yes");
            }else{
            	model.addAttribute("totalTax",totalTax);
            	model.addAttribute("showUnauthorisedPenalty", "no");
            }
            Boolean propertyByEmployee = Boolean.TRUE; 
        	propertyByEmployee = checkIfEmployee(getLoggedInUser());
        	model.addAttribute("propertyByEmployee", propertyByEmployee);
        }
    }
    
    public Boolean checkIfEmployee(User user){
    	return propertyService.isEmployee(user);
    }
    
    public String getInitiatorName(VacancyRemission vacancyRemission){
    	String initiatorName = "";
    	if (checkIfEmployee(vacancyRemission.getCreatedBy()))
    		initiatorName = vacancyRemission.getCreatedBy().getName();
        else
        	initiatorName = assignmentService
                    .getPrimaryAssignmentForPositon(vacancyRemission.getStateHistory().get(0).getOwnerPosition().getId())
                    .getEmployee().getUsername();
    	return initiatorName;
    }

    protected Assignment getWorkflowInitiator(final VacancyRemission vacancyRemission) {
        Assignment wfInitiator;
        if (checkIfEmployee(vacancyRemission.getCreatedBy()))
            wfInitiator = assignmentService.getPrimaryAssignmentForUser(vacancyRemission.getCreatedBy().getId());
        else if (!vacancyRemission.getStateHistory().isEmpty())
            wfInitiator = assignmentService.getPrimaryAssignmentForPositon(vacancyRemission.getStateHistory().get(0)
                    .getOwnerPosition().getId());
        else
            wfInitiator = assignmentService.getPrimaryAssignmentForPositon(vacancyRemission.getState().getOwnerPosition()
                    .getId());
        return wfInitiator;
    }
    
    @Transactional
    public void saveRemissionDetails(final VacancyRemission vacancyRemission) {
        vacancyRemissionRepository.save(vacancyRemission);
    }

    public List<VacancyRemissionDetails> getMonthlyDetailsHistory(VacancyRemission vacancyRemission) {
        List<VacancyRemissionDetails> historyList = new ArrayList<VacancyRemissionDetails>();
        if (!vacancyRemission.getVacancyRemissionDetails().isEmpty()) {
            historyList = vacancyRemission.getVacancyRemissionDetails();
            Collections.reverse(historyList);
        }
        return historyList;
    }

    public VacancyRemissionApproval getVacancyRemissionApprovalById(Long id) {
        return vacancyRemissionApprovalRepository.findOne(id);
    }

    @Transactional
    public void saveVacancyRemissionApproval(final VacancyRemissionApproval vacancyRemissionApproval,
            final Long approvalPosition, final String approvalComent, final String additionalRule,
            final String workFlowAction) {

        if (LOG.isDebugEnabled())
            LOG.debug(" Create WorkFlow Transition Started  ...");
        final User user = securityUtils.getCurrentUser();
        final DateTime currentDate = new DateTime();
        final Assignment userAssignment = assignmentService.getPrimaryAssignmentForUser(user.getId());
        final Designation designation = propertyTaxUtil.getDesignationForUser(user.getId());
        Position pos = null;
        Assignment wfInitiator = null;

        if (vacancyRemissionApproval.getId() != null
                && (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT) || workFlowAction
                        .equalsIgnoreCase(WFLOW_ACTION_STEP_NOTICE_GENERATE))) {
            if (designation.getName().equalsIgnoreCase(REVENUE_INSPECTOR_DESGN)) {
                wfInitiator = assignmentService.getPrimaryAssignmentForUser(vacancyRemissionApproval
                        .getVacancyRemission().getCreatedBy().getId());
            } else {
                wfInitiator = assignmentService.getPrimaryAssignmentForUser(vacancyRemissionApproval.getCreatedBy()
                        .getId());
            }
        }

        if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_NOTICE_GENERATE)) {
            if (wfInitiator.equals(userAssignment)) {
                vacancyRemissionApproval.setStatus(VR_STATUS_REJECTION_ACK_GENERATED);
                vacancyRemissionApproval.transition().end().withSenderName(user.getName()).withComments(approvalComent)
                        .withDateInfo(currentDate.toDate());
            }
        } else if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT)) {
            String stateValue = "";
            String nextAction = "";
            if (designation.getName().equalsIgnoreCase(REVENUE_OFFICER_DESGN)
                    || designation.getName().equalsIgnoreCase(COMMISSIONER_DESGN)) {
                stateValue = WF_STATE_REJECTED;
                nextAction = WF_STATE_REVENUE_INSPECTOR_APPROVAL_PENDING;
            } else {
                stateValue = WF_STATE_REVENUE_INSPECTOR_REJECTED;
                nextAction = WF_STATE_REVENUE_CLERK_APPROVAL_PENDING;
            }
            vacancyRemissionApproval.setStatus(VR_STATUS_REJECTED);
            vacancyRemissionApproval.transition().withSenderName(user.getName()).withComments(approvalComent)
                    .withStateValue(stateValue).withDateInfo(currentDate.toDate()).withOwner(wfInitiator.getPosition())
                    .withNextAction(nextAction);
        } else {
            if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_FORWARD)) {
                vacancyRemissionApproval.setStatus(VR_STATUS_WORKFLOW);
            } else if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_APPROVE)) {
                vacancyRemissionApproval.setStatus(VR_STATUS_APPROVED);
            }

            if (null != approvalPosition && approvalPosition != -1 && !approvalPosition.equals(Long.valueOf(0)))
                pos = positionMasterService.getPositionById(approvalPosition);
            else if (WFLOW_ACTION_STEP_APPROVE.equalsIgnoreCase(workFlowAction))
                pos = assignmentService.getPrimaryAssignmentForUser(
                        vacancyRemissionApproval.getVacancyRemission().getCreatedBy().getId()).getPosition();
            WorkFlowMatrix wfmatrix = null;
            if (null == vacancyRemissionApproval.getState()) {
                wfmatrix = vacancyRemissionWorkflowService.getWfMatrix(vacancyRemissionApproval.getStateType(), null,
                        null, additionalRule, null, null);
                vacancyRemissionApproval.transition().start().withSenderName(user.getName())
                        .withComments(approvalComent).withStateValue(wfmatrix.getNextState()).withDateInfo(new Date())
                        .withOwner(pos).withNextAction(wfmatrix.getNextAction());
            } else {
                wfmatrix = vacancyRemissionWorkflowService.getWfMatrix(vacancyRemissionApproval.getStateType(), null,
                        null, additionalRule, vacancyRemissionApproval.getCurrentState().getValue(), null);
                if (wfmatrix != null) {
                    if (wfmatrix.getNextAction().equalsIgnoreCase("END")) {
                        vacancyRemissionApproval.transition().end().withSenderName(user.getName())
                                .withComments(approvalComent).withDateInfo(currentDate.toDate());
                    } else {
                        vacancyRemissionApproval.transition(false).withSenderName(user.getName())
                                .withComments(approvalComent).withStateValue(wfmatrix.getNextState())
                                .withDateInfo(currentDate.toDate()).withOwner(pos)
                                .withNextAction(wfmatrix.getNextAction());
                    }
                }
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug(" WorkFlow Transition Completed  ...");
        vacancyRemissionApprovalRepository.save(vacancyRemissionApproval);
    }

    public User getLoggedInUser() {
        return securityUtils.getCurrentUser();
    }
}
