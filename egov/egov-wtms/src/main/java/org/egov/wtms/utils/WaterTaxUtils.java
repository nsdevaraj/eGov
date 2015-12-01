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
package org.egov.wtms.utils;

import static org.egov.ptis.constants.PropertyTaxConstants.MEESEVA_OPERATOR_ROLE;
import static org.egov.ptis.constants.PropertyTaxConstants.PTMODULENAME;
import static org.egov.ptis.constants.PropertyTaxConstants.QUERY_INSTALLMENTLISTBY_MODULE_AND_STARTYEAR;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.egov.commons.EgwStatus;
import org.egov.commons.Installment;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.DesignationService;
import org.egov.eis.service.EmployeeService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.BoundaryType;
import org.egov.infra.admin.master.entity.Department;
import org.egov.infra.admin.master.entity.HierarchyType;
import org.egov.infra.admin.master.entity.Role;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.admin.master.service.BoundaryTypeService;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.admin.master.service.DepartmentService;
import org.egov.infra.admin.master.service.HierarchyTypeService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.messaging.MessagingService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.EgovThreadLocals;
import org.egov.infra.workflow.entity.State;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.infstr.services.PersistenceService;
import org.egov.pims.commons.Designation;
import org.egov.pims.commons.Position;
import org.egov.ptis.domain.model.AssessmentDetails;
import org.egov.ptis.domain.model.OwnerName;
import org.egov.ptis.domain.service.property.PropertyExternalService;
import org.egov.wtms.application.entity.WaterConnectionDetails;
import org.egov.wtms.application.service.WaterConnectionDetailsService;
import org.egov.wtms.utils.constants.WaterTaxConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

@Service
public class WaterTaxUtils {

    @Qualifier("entityQueryService")
    private @Autowired PersistenceService entityQueryService;

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    private CityService cityService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DesignationService designationService;

    @Autowired
    private BoundaryTypeService boundaryTypeService;

    @Autowired
    private HierarchyTypeService hierarchyTypeService;
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private BoundaryService boundaryService;

    @Autowired
    private PropertyExtnUtils propertyExtnUtils;

    @Autowired
    private PositionMasterService positionMasterService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private WaterConnectionDetailsService waterConnectionDetailsService;
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    @Qualifier("fileStoreService")
    protected FileStoreService fileStoreService;

    public Boolean isSmsEnabled() {
        final AppConfigValues appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.SENDSMSFORWATERTAX).get(0);
        return "YES".equalsIgnoreCase(appConfigValue.getValue());
    }

    public String getDepartmentForWorkFlow() {
        String department = "";
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.WATERTAXWORKFLOWDEPARTEMENT);
        if (null != appConfigValue && !appConfigValue.isEmpty())
            department = appConfigValue.get(0).getValue();
        return department;
    }

    public String getDesignationForThirdPartyUser() {
        String designation = "";
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.CLERKDESIGNATIONFORCSCOPERATOR);
        if (null != appConfigValue && !appConfigValue.isEmpty())
            designation = appConfigValue.get(0).getValue();
        return designation;
    }

    public List<AppConfigValues> getThirdPartyUserRoles() {

        final List<AppConfigValues> appConfigValueList = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.ROLEFORNONEMPLOYEEINWATERTAX);

        return !appConfigValueList.isEmpty() ? appConfigValueList : null;

    }

    /**
     * @return appconfigValues List for Keyname='ROLESFORLOGGEDINUSER'
     */
    public List<AppConfigValues> getUserRolesForLoggedInUser() {
        final List<AppConfigValues> appConfigValueList = appConfigValuesService
                .getConfigValuesByModuleAndKeyByValueAsc(WaterTaxConstants.MODULE_NAME,
                        WaterTaxConstants.ROLESFORLOGGEDINUSER);
        // TODO: this method getting Values by Order By value Asc and based on
        // that returning LoggedInRoles
        return !appConfigValueList.isEmpty() ? appConfigValueList : null;
    }

    public Boolean getCurrentUserRole(final User currentUser) {
        Boolean applicationByOthers = false;
        for (final Role userrole : currentUser.getRoles())
            for (final AppConfigValues appconfig : getThirdPartyUserRoles()) {
                if (userrole != null && userrole.getName().equals(appconfig.getValue())) {
                    applicationByOthers = true;
                    break;
                }
                break;
            }
        return applicationByOthers;
    }

    /**
     * Checks whether user is an meeseva operator or not
     *
     * @param user
     * @return
     */
    public Boolean isMeesevaUser(final User user) {
        for (final Role role : user.getRoles())
            if (role != null && role.getName().equalsIgnoreCase(MEESEVA_OPERATOR_ROLE))
                return true;
        return false;
    }

    public Boolean isEmailEnabled() {
        final AppConfigValues appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.SENDEMAILFORWATERTAX).get(0);
        return "YES".equalsIgnoreCase(appConfigValue.getValue());
    }

    public Boolean isNewConnectionAllowedIfPTDuePresent() {
        final AppConfigValues appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.NEWCONNECTIONALLOWEDIFPTDUE).get(0);
        return "YES".equalsIgnoreCase(appConfigValue.getValue());
    }

    public Boolean isMultipleNewConnectionAllowedForPID() {
        final AppConfigValues appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.MULTIPLENEWCONNECTIONFORPID).get(0);
        return "YES".equalsIgnoreCase(appConfigValue.getValue());
    }

    public Boolean isConnectionAllowedIfWTDuePresent(final String connectionType) {
        final Boolean isAllowed = false;
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, connectionType);
        if (null != appConfigValue && !appConfigValue.isEmpty())
            return "YES".equalsIgnoreCase(appConfigValue.get(0).getValue());

        return isAllowed;
    }

    public String documentRequiredForBPLCategory() {
        String documentName = null;
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(
                WaterTaxConstants.MODULE_NAME, WaterTaxConstants.DOCUMENTREQUIREDFORBPL);
        if (appConfigValue != null && !appConfigValue.isEmpty())
            documentName = appConfigValue.get(0).getValue();
        return documentName;
    }

    public String getCityName() {
        return null != cityService.getCityByURL(EgovThreadLocals.getDomainName()).getPreferences() ? cityService
                .getCityByURL(EgovThreadLocals.getDomainName()).getPreferences().getMunicipalityName() : cityService
                .getCityByURL(EgovThreadLocals.getDomainName()).getName();
    }

    public String getCityCode() {
        return cityService.getCityByURL(EgovThreadLocals.getDomainName()).getCode();
    }

    public String smsAndEmailBodyByCodeAndArgsForRejection(final String code, final String approvalComment,
            final String applicantName) {
        final Locale locale = LocaleContextHolder.getLocale();
        final String smsMsg = messageSource.getMessage(code, new String[] { applicantName, approvalComment,
                getCityName() }, locale);
        return smsMsg;
    }

    public String emailBodyforApprovalEmailByCodeAndArgs(final String code,
            final WaterConnectionDetails waterConnectionDetails, final String applicantName) {
        final Locale locale = LocaleContextHolder.getLocale();
        final String smsMsg = messageSource.getMessage(code,
                new String[] { applicantName, waterConnectionDetails.getApplicationNumber(),
                        waterConnectionDetails.getConnection().getConsumerCode(), getCityName() }, locale);
        return smsMsg;
    }

    public String emailSubjectforEmailByCodeAndArgs(final String code, final String applicationNumber) {
        final Locale locale = LocaleContextHolder.getLocale();
        final String emailSubject = messageSource.getMessage(code, new String[] { applicationNumber }, locale);
        return emailSubject;
    }

    public void sendSMSOnWaterConnection(final String mobileNumber, final String smsBody) {
        messagingService.sendSMS(mobileNumber, smsBody);
    }

    public void sendEmailOnWaterConnection(final String email, final String emailBody, final String emailSubject) {
        messagingService.sendEmail(email, emailSubject, emailBody);
    }

    public Position getCityLevelCommissionerPosition(final String commissionerDesgn) {
        String commdesgnname = "";
        final String[] degnName = commissionerDesgn.split(",");
        if (degnName.length > 1)
            commdesgnname = degnName[0];
        else
            commdesgnname = commissionerDesgn;
        final Designation desgnObj = designationService.getDesignationByName(commissionerDesgn);
        if (commissionerDesgn.equals("Commissioner")) {
            final Department deptObj = departmentService.getDepartmentByName(WaterTaxConstants.ROLE_COMMISSIONERDEPARTEMNT);
            return assignmentService
                    .getPositionsByDepartmentAndDesignationForGivenRange(deptObj.getId(), desgnObj.getId(), new Date())
                    .get(0).getPosition();
        } else
            return assignmentService.findPrimaryAssignmentForDesignationName(commdesgnname).get(0).getPosition();
    }

    public String getApproverUserName(final Long approvalPosition) {
        Assignment assignment = null;
        if (approvalPosition != null)
            assignment = assignmentService.getPrimaryAssignmentForPositionAndDate(approvalPosition, new Date());
        return assignment != null ? assignment.getEmployee().getUsername() : "";
    }

    public EgwStatus getStatusByCodeAndModuleType(final String code, final String moduleName) {
        return (EgwStatus) persistenceService.find("from EgwStatus where moduleType=? and code=?", moduleName, code);
    }

    /*
     * public List<EgwStatus> getStatusByModuleType(final String moduleName) { return (List<EgwStatus> )
     * persistenceService.findAllBy( "from EgwStatus where moduleType=? order by code", moduleName); }
     */

    public Long getApproverPosition(final String designationName, final WaterConnectionDetails waterConnectionDetails) {

        final List<StateHistory> stateHistoryList = waterConnectionDetails.getState().getHistory();
        Long approverPosition = 0l;
        final String[] desgnArray = designationName.split(",");

        if (stateHistoryList != null && !stateHistoryList.isEmpty()) {
            for (final StateHistory stateHistory : stateHistoryList)
                if (stateHistory.getOwnerPosition() != null) {
                    final List<Assignment> assignmentList = assignmentService.getAssignmentsForPosition(stateHistory
                            .getOwnerPosition().getId(), new Date());
                    for (final Assignment assgn : assignmentList)
                        for (final String str : desgnArray)
                            if (assgn.getDesignation().getName().equals(str)) {
                                approverPosition = stateHistory.getOwnerPosition().getId();
                                break;
                            }

                }
            if (approverPosition == 0) {
                final State stateObj = waterConnectionDetails.getState();
                final List<Assignment> assignmentList = assignmentService.getAssignmentsForPosition(stateObj
                        .getOwnerPosition().getId(), new Date());
                for (final Assignment assgn : assignmentList)
                    if (assgn.getDesignation().getName().equals(designationName)) {
                        approverPosition = stateObj.getOwnerPosition().getId();
                        break;
                    }
            }
        } else {
            final Position posObjToClerk = positionMasterService.getCurrentPositionForUser(waterConnectionDetails
                    .getCreatedBy().getId());
            approverPosition = posObjToClerk.getId();
        }

        return approverPosition;

    }

    public Position getZonalLevelClerkForLoggedInUser(final String asessmentNumber) {
        final AssessmentDetails assessmentDetails = propertyExtnUtils.getAssessmentDetailsForFlag(asessmentNumber,
                PropertyExternalService.FLAG_FULL_DETAILS);
        Assignment assignmentObj = null;
        final HierarchyType hierarchy = hierarchyTypeService
                .getHierarchyTypeByName(WaterTaxConstants.HIERARCHYNAME_ADMIN);
        final BoundaryType boundaryTypeObj = boundaryTypeService.getBoundaryTypeByNameAndHierarchyType(
                assessmentDetails.getBoundaryDetails().getWardBoundaryType(), hierarchy);
        final Boundary boundaryObj = boundaryService.getBoundaryByTypeAndNo(boundaryTypeObj, assessmentDetails
                .getBoundaryDetails().getZoneNumber());
        assignmentObj = getUserPositionByZone(asessmentNumber, assessmentDetails, boundaryObj);

        return assignmentObj != null ? assignmentObj.getPosition() : null;
    }

    /**
     * Getting User assignment based on designation ,department and zone boundary Reading Designation and Department from
     * appconfig values and Values should be 'Senior Assistant,Junior Assistant' for designation and
     * 'Revenue,Accounts,Administration' for department
     *
     * @param asessmentNumber ,
     * @Param assessmentDetails
     * @param boundaryObj
     * @return Assignment
     */
    public Assignment getUserPositionByZone(final String asessmentNumber, final AssessmentDetails assessmentDetails,
            final Boundary boundaryObj) {
        final String designationStr = getDesignationForThirdPartyUser();
        final String departmentStr = getDepartmentForWorkFlow();
        final String[] department = departmentStr.split(",");
        final String[] designation = designationStr.split(",");
        List<Assignment> assignment = new ArrayList<Assignment>();
        if (department.length > 0 && designation.length > 0)
            if (StringUtils.isNotBlank(department[0]) && StringUtils.isNotBlank(designation[0])) {
                assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                        .getDepartmentByName(department[0]).getId(),
                        designationService.getDesignationByName(designation[0]).getId(), boundaryObj.getId());
                if (assignment.isEmpty() && StringUtils.isNotBlank(designation[1]))
                    assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                            .getDepartmentByName(department[0]).getId(),
                            designationService.getDesignationByName(designation[1]).getId(), boundaryObj.getId());
            } else if (StringUtils.isNotBlank(department[1]) && StringUtils.isNotBlank(designation[0])) {
                assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                        .getDepartmentByName(department[1]).getId(),
                        designationService.getDesignationByName(designation[0]).getId(), boundaryObj.getId());
                if (assignment.isEmpty() && StringUtils.isNotBlank(designation[1]))
                    assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                            .getDepartmentByName(department[1]).getId(),
                            designationService.getDesignationByName(designation[1]).getId(), boundaryObj.getId());
            } else if (StringUtils.isNotBlank(department[2]) && StringUtils.isNotBlank(designation[0])) {
                assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                        .getDepartmentByName(department[2]).getId(),
                        designationService.getDesignationByName(designation[0]).getId(), boundaryObj.getId());
                if (assignment.isEmpty() && StringUtils.isNotBlank(designation[1]))
                    assignment = assignmentService.findByDepartmentDesignationAndBoundary(departmentService
                            .getDepartmentByName(department[2]).getId(),
                            designationService.getDesignationByName(designation[1]).getId(), boundaryObj.getId());
            }
        return !assignment.isEmpty() ? assignment.get(0) : null;
    }

    @ModelAttribute(value = "checkOperator")
    public Boolean checkCollectionOperatorRole() {
        Boolean isCSCOperator = false;
        // as per Adoni allowing collection for ULB Operator
        if (EgovThreadLocals.getUserId() != null) {
            final User userObj = userService.getUserById(EgovThreadLocals.getUserId());
            if (userObj != null)
                for (final Role role : userObj.getRoles())
                    if (role != null && role.getName().contains(WaterTaxConstants.ROLE_CSCOPERTAOR) || role != null
                            && role.getName().contains(WaterTaxConstants.ROLE_ULBOPERATOR)) {
                        isCSCOperator = true;
                        break;
                    }
        }
        return isCSCOperator;
    }

    public List<Installment> getInstallmentListByStartDate(final Date startDate) {
        return entityQueryService.findAllByNamedQuery(QUERY_INSTALLMENTLISTBY_MODULE_AND_STARTYEAR, startDate,
                startDate, PTMODULENAME);
    }

    public Double waterConnectionDue(final long parentId) {
        Double finalDueAmount = (double) 0;
        final List<WaterConnectionDetails> waterConnectionDetails = waterConnectionDetailsService
                .getAllConnectionDetailsByParentConnection(parentId);
        for (final WaterConnectionDetails waterconnectiondetails : waterConnectionDetails)
            if (waterconnectiondetails.getDemand() != null)
                finalDueAmount = finalDueAmount
                        + (waterconnectiondetails.getDemand().getBaseDemand().doubleValue() - waterconnectiondetails
                                .getDemand().getAmtCollected().doubleValue());
        return finalDueAmount;
    }

    public Boolean getCitizenUserRole() {
        Boolean citizenrole = Boolean.FALSE;
        if (EgovThreadLocals.getUserId() != null) {
            final User currentUser = userService.getUserById(EgovThreadLocals.getUserId());
            if (currentUser.getRoles().isEmpty() && securityUtils.getCurrentUser().getUsername().equals("anonymous"))
                citizenrole = Boolean.TRUE;
            for (final Role userrole : currentUser.getRoles())
                if (userrole != null && userrole.getName().equals(WaterTaxConstants.ROLE_CITIZEN)) {
                    citizenrole = Boolean.TRUE;
                    break;
                }
        } else
            citizenrole = Boolean.TRUE;
        return citizenrole;
    }

    public ReportOutput getReportOutput(final WaterConnectionDetails connectionDetails, final String workFlowAction, final String cityMunicipalityName, final String districtName) {
        Map<String, Object> reportParams = new HashMap<String, Object>();
        ReportRequest reportInput = null;
        ReportOutput reportOutput = null;
        if (null != connectionDetails) {
            final AssessmentDetails assessmentDetails = propertyExtnUtils.getAssessmentDetailsForFlag(
                    connectionDetails.getConnection().getPropertyIdentifier(),
                    PropertyExternalService.FLAG_FULL_DETAILS);
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            final String doorno[] = assessmentDetails.getPropertyAddress().split(",");
            String ownerName = "";
            for (final OwnerName names : assessmentDetails.getOwnerNames()) {
                ownerName = names.getOwnerName();
                break;
            }
            if (WaterTaxConstants.NEWCONNECTION.equalsIgnoreCase(connectionDetails.getApplicationType().getCode())) {
                reportParams.put("conntitle", WordUtils.capitalize(connectionDetails.getApplicationType().getName()).toString());
                reportParams.put("applicationtype", messageSource.getMessage("msg.new.watertap.conn", null, null));
            } else if (WaterTaxConstants.ADDNLCONNECTION.equalsIgnoreCase(connectionDetails.getApplicationType().getCode())) {
                reportParams.put("conntitle", WordUtils.capitalize(connectionDetails.getApplicationType().getName()).toString());
                reportParams.put("applicationtype", messageSource.getMessage("msg.add.watertap.conn", null, null));
            } else {
                reportParams.put("conntitle", WordUtils.capitalize(connectionDetails.getApplicationType().getName()).toString());
                reportParams.put("applicationtype", messageSource.getMessage("msg.changeofuse.watertap.conn", null, null));
            }
            reportParams.put("municipality", cityMunicipalityName);
           // reportParams.put("district", districtName);
            reportParams.put("purpose", connectionDetails.getUsageType().getName());
            if(null != workFlowAction) {
                if(workFlowAction.equalsIgnoreCase(WaterTaxConstants.WF_WORKORDER_BUTTON)) {
                    reportParams.put("workorderdate", formatter.format(connectionDetails.getWorkOrderDate()));
                    reportParams.put("workorderno", connectionDetails.getWorkOrderNumber());
                }
                if(workFlowAction.equalsIgnoreCase(WaterTaxConstants.WF_PREVIEW_BUTTON)) {
                    reportParams.put("workorderdate", "");
                    reportParams.put("workorderno", "");
                }
                if(workFlowAction.equalsIgnoreCase(WaterTaxConstants.WF_SIGN_BUTTON)) {
                    reportParams.put("workorderdate", formatter.format(connectionDetails.getWorkOrderDate()));
                    reportParams.put("workorderno", connectionDetails.getWorkOrderNumber());
                }
            }
            reportParams.put("consumerNumber", connectionDetails.getConnection().getConsumerCode());
            reportParams.put("applicantname", WordUtils.capitalize(ownerName));
            reportParams.put("address", assessmentDetails.getPropertyAddress());
            reportParams.put("doorno", doorno[0]);
            reportParams.put("applicationDate",formatter.format(connectionDetails.getApplicationDate()));
            reportInput = new ReportRequest(WaterTaxConstants.CONNECTION_WORK_ORDER, connectionDetails, reportParams);
        }
        reportOutput = reportService.createReport(reportInput);
        return reportOutput;
    }
}
